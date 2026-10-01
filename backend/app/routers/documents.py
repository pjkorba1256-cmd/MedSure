import os
import shutil
import time
from fastapi import APIRouter, Depends, HTTPException, UploadFile, File, Form, status
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.entities import Document, User
from app.schemas import DocumentResponse
from app.auth.rbac import get_current_user, check_case_access
from app.ocr import ocr_service
from app.config import settings

router = APIRouter(prefix='/documents', tags=['Documents'])

os.makedirs(settings.UPLOAD_DIR, exist_ok=True)

@router.post('/upload', response_model=DocumentResponse)
def upload_document(
    case_id: str = Form(...),
    document_type: str = Form(...),
    file: UploadFile = File(...),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    check_case_access(case_id, current_user, db)
    
    file_id = f'doc_{int(time.time()*1000)}_{file.filename}'
    saved_path = os.path.join(settings.UPLOAD_DIR, file_id)
    with open(saved_path, 'wb') as buffer:
        shutil.copyfileobj(file.file, buffer)
        
    ocr_result = ocr_service.extract_document(saved_path)
    
    doc = Document(
        id=f'doc_{int(time.time()*1000)}',
        case_id=case_id,
        uploaded_by=current_user.id,
        document_type=document_type,
        filename=file.filename,
        storage_path=saved_path,
        ocr_text=ocr_result.text,
        ocr_confidence=ocr_result.overall_confidence,
        page_count=ocr_result.page_count
    )
    db.add(doc)
    db.commit()
    db.refresh(doc)
    return doc

@router.get('/{document_id}', response_model=DocumentResponse)
def get_document(document_id: str, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    doc = db.query(Document).filter(Document.id == document_id).first()
    if not doc:
        raise HTTPException(status_code=404, detail='Document not found')
    check_case_access(doc.case_id, current_user, db)
    return doc
