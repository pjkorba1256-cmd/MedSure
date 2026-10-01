import time
from datetime import datetime
from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.entities import Query, BillItem, User
from app.schemas import QueryResponse, QueryCreate, QueryReply
from app.auth.rbac import get_current_user, check_case_access
from app.notifications import notification_service

router = APIRouter(tags=["Bill Queries"])

@router.get("/cases/{case_id}/queries", response_model=List[QueryResponse])
def get_queries(case_id: str, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    check_case_access(case_id, current_user, db)
    queries = db.query(Query).filter(Query.case_id == case_id).order_by(Query.created_at.desc()).all()
    res = []
    for q in queries:
        res.append({
            "id": q.id,
            "case_id": q.case_id,
            "bill_item_id": q.bill_item_id,
            "bill_item_description": q.bill_item.description if q.bill_item else None,
            "bill_item_amount": q.bill_item.amount if q.bill_item else None,
            "created_by": q.created_by,
            "created_by_name": db.query(User).filter(User.id == q.created_by).first().name if q.created_by else "User",
            "message": q.message,
            "reply": q.reply,
            "status": q.status,
            "created_at": q.created_at,
            "replied_at": q.replied_at
        })
    return res

@router.post("/cases/{case_id}/queries", response_model=QueryResponse, status_code=status.HTTP_201_CREATED)
def create_query(case_id: str, payload: QueryCreate, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    check_case_access(case_id, current_user, db)
    
    b_desc = None
    b_amt = None
    if payload.bill_item_id:
        bill_item = db.query(BillItem).filter(BillItem.id == payload.bill_item_id).first()
        if bill_item:
            b_desc = bill_item.description
            b_amt = bill_item.amount

    q = Query(
        id=f"q_{int(time.time()*1000)}",
        case_id=case_id,
        bill_item_id=payload.bill_item_id,
        created_by=current_user.id,
        message=payload.message,
        status="OPEN"
    )
    db.add(q)
    db.commit()
    db.refresh(q)

    # Notify Hospital Desk
    notification_service.broadcast_to_role(
        role="HOSPITAL_DESK",
        title="New Bill Query from Family",
        body=f"Query regarding '{b_desc or 'Bill Item'}': {payload.message}",
        event_type="NEW_QUERY",
        data={"case_id": case_id, "query_id": q.id}
    )

    return {
        "id": q.id,
        "case_id": q.case_id,
        "bill_item_id": q.bill_item_id,
        "bill_item_description": b_desc,
        "bill_item_amount": b_amt,
        "created_by": q.created_by,
        "created_by_name": current_user.name,
        "message": q.message,
        "reply": None,
        "status": q.status,
        "created_at": q.created_at,
        "replied_at": None
    }

@router.post("/queries/{query_id}/reply", response_model=QueryResponse)
def reply_to_query(query_id: str, payload: QueryReply, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    if current_user.role != "HOSPITAL_DESK":
        raise HTTPException(status_code=403, detail="Only Hospital Desk can reply to billing queries")
        
    q = db.query(Query).filter(Query.id == query_id).first()
    if not q:
        raise HTTPException(status_code=404, detail="Query not found")
        
    q.reply = payload.reply
    q.status = "RESOLVED"
    q.replied_at = datetime.utcnow()
    db.commit()
    db.refresh(q)

    notification_service.send_notification(
        target_user_id=q.created_by,
        title="Hospital Desk Replied to Your Bill Query",
        body=f"Hospital reply: {payload.reply}",
        event_type="QUERY_REPLIED",
        data={"query_id": q.id, "case_id": q.case_id}
    )

    return {
        "id": q.id,
        "case_id": q.case_id,
        "bill_item_id": q.bill_item_id,
        "bill_item_description": q.bill_item.description if q.bill_item else None,
        "bill_item_amount": q.bill_item.amount if q.bill_item else None,
        "created_by": q.created_by,
        "created_by_name": db.query(User).filter(User.id == q.created_by).first().name if q.created_by else "User",
        "message": q.message,
        "reply": q.reply,
        "status": q.status,
        "created_at": q.created_at,
        "replied_at": q.replied_at
    }
