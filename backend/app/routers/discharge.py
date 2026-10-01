from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.entities import DischargePack, Document, User
from app.schemas import DischargePackResponse
from app.auth.rbac import get_current_user, check_case_access
from app.ai import ai_service

router = APIRouter(prefix="/cases/{case_id}/discharge", tags=["Discharge Pack"])

@router.get("", response_model=DischargePackResponse)
def get_discharge_pack(case_id: str, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    check_case_access(case_id, current_user, db)
    pack = db.query(DischargePack).filter(DischargePack.case_id == case_id).order_by(DischargePack.created_at.desc()).first()
    if not pack:
        pack_data = ai_service.generate_discharge_pack(ocr_text="")
        pack = DischargePack(
            id=f"dp_{case_id}",
            case_id=case_id,
            source_document_id=None,
            summary=pack_data["summary"],
            what_happened=pack_data["what_happened"],
            medications=pack_data["medications"],
            follow_up=pack_data["follow_up"],
            what_to_watch=pack_data["what_to_watch"]
        )
        db.add(pack)
        db.commit()
        db.refresh(pack)
    return pack

@router.post("/process", response_model=DischargePackResponse)
def process_discharge(
    case_id: str,
    document_id: str = None,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    check_case_access(case_id, current_user, db)
    if current_user.role != "HOSPITAL_DESK":
        raise HTTPException(status_code=403, detail="Only Hospital Desk can trigger discharge processing")
    
    ocr_text = ""
    source_name = "Discharge Summary"
    if document_id:
        doc = db.query(Document).filter(Document.id == document_id, Document.case_id == case_id).first()
        if doc and doc.ocr_text:
            ocr_text = doc.ocr_text
            source_name = doc.filename
    
    pack_data = ai_service.generate_discharge_pack(ocr_text=ocr_text, source_doc_name=source_name)
    
    existing = db.query(DischargePack).filter(DischargePack.case_id == case_id).first()
    if existing:
        existing.summary = pack_data["summary"]
        existing.what_happened = pack_data["what_happened"]
        existing.medications = pack_data["medications"]
        existing.follow_up = pack_data["follow_up"]
        existing.what_to_watch = pack_data["what_to_watch"]
        existing.source_document_id = document_id
        db.commit()
        db.refresh(existing)
        return existing
    else:
        pack = DischargePack(
            id=f"dp_{case_id}",
            case_id=case_id,
            source_document_id=document_id,
            summary=pack_data["summary"],
            what_happened=pack_data["what_happened"],
            medications=pack_data["medications"],
            follow_up=pack_data["follow_up"],
            what_to_watch=pack_data["what_to_watch"]
        )
        db.add(pack)
        db.commit()
        db.refresh(pack)
        return pack
