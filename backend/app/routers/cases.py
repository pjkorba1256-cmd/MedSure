from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.entities import Case, User, CaseMember
from app.schemas import CaseResponse, CaseCreate
from app.auth.rbac import get_current_user, check_case_access

router = APIRouter(prefix='/cases', tags=['Cases'])

@router.get('', response_model=List[CaseResponse])
def get_cases(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    if current_user.role == 'HOSPITAL_DESK':
        cases = db.query(Case).all()
    elif current_user.role == 'PATIENT':
        cases = db.query(Case).filter(Case.patient_id == current_user.id).all()
    else:  # FAMILY
        member_case_ids = [m.case_id for m in db.query(CaseMember).filter(CaseMember.user_id == current_user.id).all()]
        cases = db.query(Case).filter(Case.id.in_(member_case_ids)).all()
    
    res = []
    for c in cases:
        p_name = c.patient.name if c.patient else 'Unknown'
        members_data = [{'id': m.id, 'user_id': m.user_id, 'role': m.role, 'user_name': m.user.name if m.user else None} for m in c.members]
        res.append({
            'id': c.id,
            'patient_id': c.patient_id,
            'patient_name': p_name,
            'hospital_name': c.hospital_name,
            'admission_date': c.admission_date,
            'discharge_date': c.discharge_date,
            'status': c.status,
            'created_at': c.created_at,
            'members': members_data
        })
    return res

@router.get('/{case_id}', response_model=CaseResponse)
def get_case(case_id: str, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    case = check_case_access(case_id, current_user, db)
    p_name = case.patient.name if case.patient else 'Unknown'
    members_data = [{'id': m.id, 'user_id': m.user_id, 'role': m.role, 'user_name': m.user.name if m.user else None} for m in case.members]
    return {
        'id': case.id,
        'patient_id': case.patient_id,
        'patient_name': p_name,
        'hospital_name': case.hospital_name,
        'admission_date': case.admission_date,
        'discharge_date': case.discharge_date,
        'status': case.status,
        'created_at': case.created_at,
        'members': members_data
    }

@router.post('', response_model=CaseResponse, status_code=status.HTTP_201_CREATED)
def create_case(payload: CaseCreate, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    if current_user.role != 'HOSPITAL_DESK':
        raise HTTPException(status_code=403, detail='Only Hospital Desk can create cases')
    new_case = Case(
        id=payload.id,
        patient_id=payload.patient_id,
        hospital_name=payload.hospital_name,
        admission_date=payload.admission_date,
        discharge_date=payload.discharge_date,
        status='ACTIVE'
    )
    db.add(new_case)
    db.commit()
    db.refresh(new_case)
    return new_case
