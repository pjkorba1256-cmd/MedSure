import uuid
from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.entities import BillItem, User
from app.schemas import BillItemResponse, BillProcessRequest
from app.auth.rbac import get_current_user, check_case_access
from app.ai import ai_service

router = APIRouter(prefix="/cases/{case_id}/bills", tags=["Bills & Fees"])

@router.get("", response_model=List[BillItemResponse])
def get_bills(case_id: str, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    check_case_access(case_id, current_user, db)
    items = db.query(BillItem).filter(BillItem.case_id == case_id).all()
    return items

@router.post("/process", response_model=List[BillItemResponse])
def process_bills(
    case_id: str,
    payload: BillProcessRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    check_case_access(case_id, current_user, db)
    created_items = []
    seen_descriptions = set()

    unusual_keywords = [
        "administrative", "admin", "miscellaneous", "handling fee",
        "documentation fee", "non-medical consumable", "duplicate consultation"
    ]

    for item in payload.items:
        desc = item.get("description", "").strip()
        amt = float(item.get("amount", 0.0))
        cat = item.get("category", "General")
        
        explanation_data = ai_service.explain_bill_item(desc, amt, cat)
        
        flagged = explanation_data["flagged"]
        flag_reason = explanation_data["flag_reason"]
        
        # Rule 1: Duplicate bill line detection
        if desc.lower() in seen_descriptions:
            flagged = True
            flag_reason = "Worth asking about: Duplicate bill line"
        seen_descriptions.add(desc.lower())

        # Rule 2: Non-medical / unusual item detection using fixed list
        for kw in unusual_keywords:
            if kw in desc.lower():
                flagged = True
                if not flag_reason or "Duplicate" not in flag_reason:
                    flag_reason = f"Worth asking about: Non-medical/unusual item ({kw.capitalize()})"
                break

        b_item = BillItem(
            id=f"bill_{case_id}_{uuid.uuid4().hex[:8]}",
            case_id=case_id,
            description=desc,
            amount=amt,
            category=cat,
            explanation=explanation_data["explanation"],
            flagged=flagged,
            flag_reason=flag_reason
        )
        db.add(b_item)
        created_items.append(b_item)

    db.commit()
    for item in created_items:
        db.refresh(item)
    return created_items
