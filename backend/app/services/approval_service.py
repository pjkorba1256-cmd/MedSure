from datetime import datetime
from typing import Optional
from sqlalchemy.orm import Session
from fastapi import HTTPException, status
from app.models.entities import Approval, User, Claim, ClaimStatusEnum
from app.notifications import notification_service

class ApprovalService:
    @staticmethod
    def create_approval_request(
        db: Session,
        case_id: str,
        user_id: str,
        action_type: str,
        target_id: str
    ) -> Approval:
        approval = Approval(
            id=f"appr_{case_id}_{target_id}_{int(datetime.utcnow().timestamp())}",
            case_id=case_id,
            user_id=user_id,
            action_type=action_type,
            target_id=target_id,
            approved=False,
            status="PENDING",
            timestamp=datetime.utcnow()
        )
        db.add(approval)
        db.commit()
        db.refresh(approval)
        return approval

    @staticmethod
    def process_approval(
        db: Session,
        approval_id: str,
        user: User,
        approve: bool
    ) -> Approval:
        approval = db.query(Approval).filter(Approval.id == approval_id).first()
        if not approval:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Approval request not found")

        if user.role not in ["FAMILY", "PATIENT"]:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Only Family or Patient can approve this action"
            )

        approval.approved = approve
        approval.status = "APPROVED" if approve else "REJECTED"
        approval.timestamp = datetime.utcnow()
        approval.user_id = user.id

        if approval.action_type == "CLAIM_SUBMISSION":
            claim = db.query(Claim).filter(Claim.id == approval.target_id).first()
            if claim:
                if approve:
                    claim.approved_at = datetime.utcnow()
                    claim.submitted_at = datetime.utcnow()
                    claim.status = ClaimStatusEnum.REIMBURSEMENT.value
                    notification_service.broadcast_to_role(
                        role="HOSPITAL_DESK",
                        title="Claim Approved & Dispatched",
                        body=f"Claim #{claim.claim_number} was approved by {user.name} and submitted to insurer.",
                        event_type="CLAIM_APPROVED",
                        data={"claim_id": claim.id, "case_id": claim.case_id}
                    )
                else:
                    claim.status = ClaimStatusEnum.DENIED.value

        db.commit()
        db.refresh(approval)
        return approval

approval_service = ApprovalService()
