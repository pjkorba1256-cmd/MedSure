from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.entities import Claim, ClaimDocument, Case, User, Approval
from app.schemas import ClaimResponse, ClaimDraftResponse, ClaimDocumentResponse
from app.auth.rbac import get_current_user, check_case_access
from app.ai import ai_service
from app.services.approval_service import approval_service

router = APIRouter(prefix="/cases/{case_id}/claim", tags=["Insurance Tracker"])

@router.get("", response_model=ClaimResponse)
def get_claim(case_id: str, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    check_case_access(case_id, current_user, db)
    claim = db.query(Claim).filter(Claim.case_id == case_id).first()
    if not claim:
        raise HTTPException(status_code=404, detail="No claim found for this case")
    
    docs_data = []
    missing_docs = []
    page_discrepancies = []
    
    for cd in claim.claim_documents:
        warning = None
        if cd.present and cd.page_count < cd.expected_page_count:
            warning = f"Possible missing page detected ({cd.page_count} of {cd.expected_page_count} pages uploaded)"
            page_discrepancies.append(f"{cd.document_type}: {warning}")
        
        if cd.required and not cd.present:
            missing_docs.append(cd.document_type)

        docs_data.append({
            "id": cd.id,
            "claim_id": cd.claim_id,
            "document_id": cd.document_id,
            "document_type": cd.document_type,
            "required": cd.required,
            "present": cd.present,
            "page_count": cd.page_count,
            "expected_page_count": cd.expected_page_count,
            "page_count_warning": warning
        })
        
    return {
        "id": claim.id,
        "case_id": claim.case_id,
        "claim_number": claim.claim_number,
        "status": claim.status,
        "claim_type": claim.claim_type,
        "draft_text": claim.draft_text,
        "approved_at": claim.approved_at,
        "submitted_at": claim.submitted_at,
        "documents": docs_data,
        "missing_documents": missing_docs,
        "page_discrepancies": page_discrepancies
    }

@router.get("/checklist", response_model=List[ClaimDocumentResponse])
def get_claim_checklist(case_id: str, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    check_case_access(case_id, current_user, db)
    claim = db.query(Claim).filter(Claim.case_id == case_id).first()
    if not claim:
        raise HTTPException(status_code=404, detail="Claim not found")
        
    res = []
    for cd in claim.claim_documents:
        warning = None
        if cd.present and cd.page_count < cd.expected_page_count:
            warning = f"Possible missing page detected ({cd.page_count} of {cd.expected_page_count} uploaded)"
        res.append({
            "id": cd.id,
            "claim_id": cd.claim_id,
            "document_id": cd.document_id,
            "document_type": cd.document_type,
            "required": cd.required,
            "present": cd.present,
            "page_count": cd.page_count,
            "expected_page_count": cd.expected_page_count,
            "page_count_warning": warning
        })
    return res

@router.post("/draft", response_model=ClaimDraftResponse)
def generate_claim_draft(case_id: str, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    case = check_case_access(case_id, current_user, db)
    claim = db.query(Claim).filter(Claim.case_id == case_id).first()
    if not claim:
        raise HTTPException(status_code=404, detail="Claim not found")

    p_name = case.patient.name if case.patient else "Rajesh Sharma"
    draft_data = ai_service.draft_claim(
        patient_name=p_name,
        hospital_name=case.hospital_name,
        claim_number=claim.claim_number,
        claim_amount=185000.0,
        rejection_reason="Missing investigation report"
    )
    
    claim.draft_text = draft_data["draft_text"]
    
    approval = db.query(Approval).filter(
        Approval.case_id == case_id,
        Approval.target_id == claim.id,
        Approval.action_type == "CLAIM_SUBMISSION",
        Approval.status == "PENDING"
    ).first()
    if not approval:
        approval = approval_service.create_approval_request(
            db=db,
            case_id=case_id,
            user_id=current_user.id,
            action_type="CLAIM_SUBMISSION",
            target_id=claim.id
        )

    db.commit()

    return {
        "claim_id": claim.id,
        "patient_name": draft_data["patient_name"],
        "hospital_name": draft_data["hospital_name"],
        "claim_number": draft_data["claim_number"],
        "treatment_summary": draft_data["treatment_summary"],
        "claim_amount": draft_data["claim_amount"],
        "supporting_documents": draft_data["supporting_documents"],
        "explanation_request": draft_data["explanation_request"],
        "draft_text": draft_data["draft_text"],
        "requires_approval": True,
        "approval_status": approval.status
    }

@router.post("/approve")
def approve_claim_draft(case_id: str, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    check_case_access(case_id, current_user, db)
    if current_user.role not in ["FAMILY", "PATIENT"]:
        raise HTTPException(status_code=403, detail="Only family or patient can approve claim submissions")
        
    claim = db.query(Claim).filter(Claim.case_id == case_id).first()
    if not claim:
        raise HTTPException(status_code=404, detail="Claim not found")

    approval = db.query(Approval).filter(
        Approval.case_id == case_id,
        Approval.target_id == claim.id,
        Approval.action_type == "CLAIM_SUBMISSION",
        Approval.status == "PENDING"
    ).first()
    
    if not approval:
        approval = approval_service.create_approval_request(
            db=db,
            case_id=case_id,
            user_id=current_user.id,
            action_type="CLAIM_SUBMISSION",
            target_id=claim.id
        )

    processed = approval_service.process_approval(
        db=db,
        approval_id=approval.id,
        user=current_user,
        approve=True
    )

    return {
        "success": True,
        "message": "Claim draft approved and transmitted to mock insurer",
        "claim_status": claim.status,
        "approval_id": processed.id,
        "approved_at": claim.approved_at
    }
