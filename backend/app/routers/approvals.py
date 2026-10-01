from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.entities import Approval, User
from app.schemas import ApprovalResponse
from app.auth.rbac import get_current_user
from app.services.approval_service import approval_service

router = APIRouter(prefix="/approvals", tags=["Approval Gate"])

@router.get("", response_model=List[ApprovalResponse])
def get_approvals(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    approvals = db.query(Approval).all()
    return approvals

@router.post("/{approval_id}/approve", response_model=ApprovalResponse)
def approve_action(approval_id: str, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    return approval_service.process_approval(db=db, approval_id=approval_id, user=current_user, approve=True)

@router.post("/{approval_id}/reject", response_model=ApprovalResponse)
def reject_action(approval_id: str, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    return approval_service.process_approval(db=db, approval_id=approval_id, user=current_user, approve=False)
