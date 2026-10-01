import time
from datetime import datetime
from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.entities import CheckIn, Alert, User, Case
from app.schemas import CheckInCreate, CheckInResponse, CheckInQuestion
from app.auth.rbac import get_current_user, check_case_access
from app.rules import rules_engine
from app.notifications import notification_service

router = APIRouter(prefix="/cases/{case_id}/checkin", tags=["Home Recovery"])

@router.get("/questions", response_model=List[CheckInQuestion])
def get_checkin_questions(case_id: str, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    check_case_access(case_id, current_user, db)
    return [
        CheckInQuestion(id="pain", label="Pain level (0 - 10)", type="scale", options=[0, 10]),
        CheckInQuestion(id="fever", label="Do you have a fever (temperature > 100°F)?", type="boolean"),
        CheckInQuestion(id="breathing_difficulty", label="Are you experiencing difficulty breathing or shortness of breath?", type="boolean"),
        CheckInQuestion(id="dizziness", label="Do you feel lightheaded or dizzy?", type="boolean"),
        CheckInQuestion(id="vomiting", label="Have you experienced nausea or vomiting?", type="boolean"),
        CheckInQuestion(id="free_text", label="Anything else you'd like to report?", type="text")
    ]

@router.post("", response_model=CheckInResponse)
def submit_checkin(case_id: str, payload: CheckInCreate, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    case = check_case_access(case_id, current_user, db)
    
    answers = {
        "pain": payload.pain,
        "fever": payload.fever,
        "breathing_difficulty": payload.breathing_difficulty,
        "dizziness": payload.dizziness,
        "vomiting": payload.vomiting
    }
    
    checkin = CheckIn(
        id=f"chk_{int(time.time()*1000)}",
        case_id=case_id,
        patient_id=case.patient_id,
        answers_json=answers,
        free_text=payload.free_text
    )
    db.add(checkin)
    db.commit()
    db.refresh(checkin)

    # Strictly evaluate using deterministic rules engine (NO LLM diagnosis)
    triggers = rules_engine.evaluate(answers=answers, free_text=payload.free_text)
    alert_ids = []

    for trig in triggers:
        alert = Alert(
            id=f"alt_{int(time.time()*1000)}_{trig.rule_id}",
            case_id=case_id,
            checkin_id=checkin.id,
            severity=trig.severity,
            title=trig.title,
            summary=trig.summary,
            rule_id=trig.rule_id,
            status="NEW"
        )
        db.add(alert)
        alert_ids.append(alert.id)

        # Broadcast immediately to Hospital Desk
        p_name = case.patient.name if case.patient else "Patient"
        notification_service.broadcast_to_role(
            role="HOSPITAL_DESK",
            title=f"[{trig.severity} ALERT] {p_name}: {trig.title}",
            body=f"{trig.summary} (Case: {case_id})",
            event_type=f"ALERT_{trig.severity}",
            data={"case_id": case_id, "alert_id": alert.id, "severity": trig.severity}
        )

    db.commit()

    return {
        "id": checkin.id,
        "case_id": checkin.case_id,
        "patient_id": checkin.patient_id,
        "answers_json": checkin.answers_json,
        "free_text": checkin.free_text,
        "created_at": checkin.created_at,
        "alerts_triggered": [t.title for t in triggers]
    }
