from datetime import datetime, timedelta
from sqlalchemy.orm import Session
from app.models.entities import (
    User, Case, CaseMember, Document, BillItem, Claim, ClaimDocument, RoleEnum, ClaimStatusEnum
)
from app.auth.password import hash_password
from app.config import settings

def seed_database(db: Session):
    if db.query(User).filter(User.email == "patient@medsure.demo").first():
        return

    pwd_hash = hash_password(settings.DEMO_PASSWORD)

    patient = User(
        id="usr_patient_001",
        name="Rajesh Sharma",
        email="patient@medsure.demo",
        phone="+91 98765 43210",
        password_hash=pwd_hash,
        role=RoleEnum.PATIENT.value
    )
    family = User(
        id="usr_family_001",
        name="Anita Sharma",
        email="family@medsure.demo",
        phone="+91 98765 43211",
        password_hash=pwd_hash,
        role=RoleEnum.FAMILY.value
    )
    hospital = User(
        id="usr_hospital_001",
        name="Dr. Arvind Menon (Desk)",
        email="hospital@medsure.demo",
        phone="+91 98765 43212",
        password_hash=pwd_hash,
        role=RoleEnum.HOSPITAL_DESK.value
    )
    db.add_all([patient, family, hospital])
    db.commit()

    admission_dt = datetime.utcnow() - timedelta(days=5)
    discharge_dt = datetime.utcnow() - timedelta(days=1)
    case = Case(
        id="MS-001",
        patient_id=patient.id,
        hospital_name="MedSure Trust Hospital",
        admission_date=admission_dt,
        discharge_date=discharge_dt,
        status="ACTIVE"
    )
    db.add(case)
    db.commit()

    cm_patient = CaseMember(id="cm_001", case_id=case.id, user_id=patient.id, role="PATIENT")
    cm_family = CaseMember(id="cm_002", case_id=case.id, user_id=family.id, role="FAMILY")
    db.add_all([cm_patient, cm_family])
    db.commit()

    items = [
        BillItem(
            id="bill_001",
            case_id=case.id,
            description="Cardiac ICU Deluxe Room Charges (3 Days)",
            amount=45000.0,
            category="Room & Board",
            explanation="Inpatient ICU monitoring, oxygen support, and sanitization.",
            flagged=False
        ),
        BillItem(
            id="bill_002",
            case_id=case.id,
            description="Cardiology Senior Consultant Visit",
            amount=2500.0,
            category="Professional Fee",
            explanation="Daily post-operative evaluation and medication titration.",
            flagged=False
        ),
        BillItem(
            id="bill_003",
            case_id=case.id,
            description="Cardiology Senior Consultant Visit",
            amount=2500.0,
            category="Professional Fee",
            explanation="Duplicate professional consultation charge detected on same date.",
            flagged=True,
            flag_reason="Worth asking about: Duplicate consultation line"
        ),
        BillItem(
            id="bill_004",
            case_id=case.id,
            description="Drug Eluting Stent (DES - Promus Premier)",
            amount=65000.0,
            category="Implant & Devices",
            explanation="Approved coronary stent implant per NPPA capped guidelines.",
            flagged=False
        ),
        BillItem(
            id="bill_005",
            case_id=case.id,
            description="Administrative & Documentation Handling Fee",
            amount=8500.0,
            category="Administrative",
            explanation="Non-clinical handling surcharge.",
            flagged=True,
            flag_reason="Worth asking about: Non-medical administrative surcharge"
        ),
        BillItem(
            id="bill_006",
            case_id=case.id,
            description="Pharmacy & Post-Op Infusion Medications",
            amount=14500.0,
            category="Pharmacy",
            explanation="Prescribed antiplatelets, statins, and saline infusions.",
            flagged=False
        )
    ]
    db.add_all(items)
    db.commit()

    claim = Claim(
        id="claim_001",
        case_id=case.id,
        claim_number="CLM-2026-001",
        status=ClaimStatusEnum.DENIED.value,
        claim_type="REIMBURSEMENT",
        draft_text=None
    )
    db.add(claim)
    db.commit()

    claim_docs = [
        ClaimDocument(
            id="cd_001",
            claim_id=claim.id,
            document_type="Discharge Summary",
            required=True,
            present=True,
            page_count=3,
            expected_page_count=4
        ),
        ClaimDocument(
            id="cd_002",
            claim_id=claim.id,
            document_type="Final Itemized Bill",
            required=True,
            present=True,
            page_count=2,
            expected_page_count=2
        ),
        ClaimDocument(
            id="cd_003",
            claim_id=claim.id,
            document_type="Government ID Proof",
            required=True,
            present=True,
            page_count=1,
            expected_page_count=1
        ),
        ClaimDocument(
            id="cd_004",
            claim_id=claim.id,
            document_type="Catheterization & ECG Investigation Reports",
            required=True,
            present=False,
            page_count=0,
            expected_page_count=3
        )
    ]
    db.add_all(claim_docs)
    db.commit()

    print("Seed data inserted successfully for MS-001!")
