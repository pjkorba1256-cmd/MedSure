from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.entities import Alert, User, Case
from app.schemas import AlertResponse
from app.auth.rbac import get_current_user, check_case_access

router = APIRouter(tags=["Alerts"])

@router.get("/cases/{case_id}/alerts", response_model=List[AlertResponse])
def get_case_alerts(case_id: str, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    case = check_case_access(case_id, current_user, db)
    alerts = db.query(Alert).filter(Alert.case_id == case_id).order_by(Alert.created_at.desc()).all()
    p_name = case.patient.name if case.patient else "Patient"
    res = []
    for a in alerts:
        res.append({
            "id": a.id,
            "case_id": a.case_id,
            "checkin_id": a.checkin_id,
            "patient_name": p_name,
            "severity": a.severity,
            "title": a.title,
            "summary": a.summary,
            "rule_id": a.rule_id,
            "status": a.status,
            "created_at": a.created_at
        })
    return res

@router.get("/hospital/alerts", response_model=List[AlertResponse])
def get_hospital_alerts(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    if current_user.role != "HOSPITAL_DESK":
        raise HTTPException(status_code=403, detail="Only Hospital Desk can view desk-wide alerts")
    alerts = db.query(Alert).order_by(Alert.created_at.desc()).all()
    res = []
    for a in alerts:
        case = db.query(Case).filter(Case.id == a.case_id).first()
        p_name = case.patient.name if case and case.patient else "Rajesh Sharma"
        res.append({
            "id": a.id,
            "case_id": a.case_id,
            "checkin_id": a.checkin_id,
            "patient_name": p_name,
            "severity": a.severity,
            "title": a.title,
            "summary": a.summary,
            "rule_id": a.rule_id,
            "status": a.status,
            "created_at": a.created_at
        })
    return res
