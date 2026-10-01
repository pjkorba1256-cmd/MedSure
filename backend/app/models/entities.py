import enum
from datetime import datetime
from sqlalchemy import (
    Column, String, Integer, Float, Boolean, DateTime, ForeignKey, Text, JSON, Enum
)
from sqlalchemy.orm import relationship
from app.database import Base

class RoleEnum(str, enum.Enum):
    PATIENT = "PATIENT"
    FAMILY = "FAMILY"
    HOSPITAL_DESK = "HOSPITAL_DESK"

class ClaimStatusEnum(str, enum.Enum):
    PENDING = "PENDING"
    APPROVED = "APPROVED"
    DENIED = "DENIED"
    REIMBURSEMENT = "REIMBURSEMENT"

class SeverityEnum(str, enum.Enum):
    AMBER = "AMBER"
    RED = "RED"

class User(Base):
    __tablename__ = "users"
    
    id = Column(String, primary_key=True, index=True)
    name = Column(String, nullable=False)
    email = Column(String, unique=True, index=True, nullable=False)
    phone = Column(String, nullable=True)
    password_hash = Column(String, nullable=False)
    role = Column(String, nullable=False)  # RoleEnum
    created_at = Column(DateTime, default=datetime.utcnow)
    
    cases = relationship("Case", back_populates="patient", foreign_keys="Case.patient_id")
    memberships = relationship("CaseMember", back_populates="user")
    approvals = relationship("Approval", back_populates="user")

class Case(Base):
    __tablename__ = "cases"
    
    id = Column(String, primary_key=True, index=True)
    patient_id = Column(String, ForeignKey("users.id"), nullable=False)
    hospital_name = Column(String, nullable=False)
    admission_date = Column(DateTime, nullable=False)
    discharge_date = Column(DateTime, nullable=True)
    status = Column(String, default="ACTIVE")
    created_at = Column(DateTime, default=datetime.utcnow)
    
    patient = relationship("User", back_populates="cases", foreign_keys=[patient_id])
    members = relationship("CaseMember", back_populates="case", cascade="all, delete-orphan")
    documents = relationship("Document", back_populates="case", cascade="all, delete-orphan")
    discharge_packs = relationship("DischargePack", back_populates="case", cascade="all, delete-orphan")
    bill_items = relationship("BillItem", back_populates="case", cascade="all, delete-orphan")
    queries = relationship("Query", back_populates="case", cascade="all, delete-orphan")
    claims = relationship("Claim", back_populates="case", cascade="all, delete-orphan")
    checkins = relationship("CheckIn", back_populates="case", cascade="all, delete-orphan")
    alerts = relationship("Alert", back_populates="case", cascade="all, delete-orphan")
    approvals = relationship("Approval", back_populates="case", cascade="all, delete-orphan")

class CaseMember(Base):
    __tablename__ = "case_members"
    
    id = Column(String, primary_key=True, index=True)
    case_id = Column(String, ForeignKey("cases.id"), nullable=False)
    user_id = Column(String, ForeignKey("users.id"), nullable=False)
    role = Column(String, nullable=False)
    
    case = relationship("Case", back_populates="members")
    user = relationship("User", back_populates="memberships")

class Document(Base):
    __tablename__ = "documents"
    
    id = Column(String, primary_key=True, index=True)
    case_id = Column(String, ForeignKey("cases.id"), nullable=False)
    uploaded_by = Column(String, ForeignKey("users.id"), nullable=False)
    document_type = Column(String, nullable=False)  # DISCHARGE_SUMMARY, BILL, CLAIM_DOC, SCANNED_PAGE
    filename = Column(String, nullable=False)
    storage_path = Column(String, nullable=False)
    ocr_text = Column(Text, nullable=True)
    ocr_confidence = Column(Float, default=1.0)
    page_count = Column(Integer, default=1)
    created_at = Column(DateTime, default=datetime.utcnow)
    
    case = relationship("Case", back_populates="documents")

class Approval(Base):
    __tablename__ = "approvals"
    
    id = Column(String, primary_key=True, index=True)
    case_id = Column(String, ForeignKey("cases.id"), nullable=False)
    user_id = Column(String, ForeignKey("users.id"), nullable=False)
    action_type = Column(String, nullable=False)  # CLAIM_SUBMISSION, APPEAL_SUBMISSION
    target_id = Column(String, nullable=False)   # claim_id or document_id
    approved = Column(Boolean, default=False)
    status = Column(String, default="PENDING")   # PENDING, APPROVED, REJECTED
    timestamp = Column(DateTime, default=datetime.utcnow)
    
    case = relationship("Case", back_populates="approvals")
    user = relationship("User", back_populates="approvals")

class DischargePack(Base):
    __tablename__ = "discharge_packs"
    
    id = Column(String, primary_key=True, index=True)
    case_id = Column(String, ForeignKey("cases.id"), nullable=False)
    source_document_id = Column(String, ForeignKey("documents.id"), nullable=True)
    summary = Column(Text, nullable=False)
    what_happened = Column(JSON, nullable=False)  # {text: str, source: str, page: int}
    medications = Column(JSON, nullable=False)    # list of {name, dosage, timing, purpose, source, page}
    follow_up = Column(JSON, nullable=False)      # {instructions: str, date_or_days: str, department: str, source, page}
    what_to_watch = Column(JSON, nullable=False)  # list of {warning: str, action: str, severity: str, source, page}
    created_at = Column(DateTime, default=datetime.utcnow)
    
    case = relationship("Case", back_populates="discharge_packs")

class BillItem(Base):
    __tablename__ = "bill_items"
    
    id = Column(String, primary_key=True, index=True)
    case_id = Column(String, ForeignKey("cases.id"), nullable=False)
    description = Column(String, nullable=False)
    amount = Column(Float, nullable=False)
    category = Column(String, default="General")
    explanation = Column(Text, nullable=True)
    flagged = Column(Boolean, default=False)
    flag_reason = Column(String, nullable=True)  # e.g. "Worth asking about: Duplicate line", "Worth asking about: Non-medical item"
    
    case = relationship("Case", back_populates="bill_items")
    queries = relationship("Query", back_populates="bill_item")

class Query(Base):
    __tablename__ = "queries"
    
    id = Column(String, primary_key=True, index=True)
    case_id = Column(String, ForeignKey("cases.id"), nullable=False)
    bill_item_id = Column(String, ForeignKey("bill_items.id"), nullable=True)
    created_by = Column(String, ForeignKey("users.id"), nullable=False)
    message = Column(Text, nullable=False)
    reply = Column(Text, nullable=True)
    status = Column(String, default="OPEN")  # OPEN, RESOLVED
    created_at = Column(DateTime, default=datetime.utcnow)
    replied_at = Column(DateTime, nullable=True)
    
    case = relationship("Case", back_populates="queries")
    bill_item = relationship("BillItem", back_populates="queries")

class Claim(Base):
    __tablename__ = "claims"
    
    id = Column(String, primary_key=True, index=True)
    case_id = Column(String, ForeignKey("cases.id"), nullable=False)
    claim_number = Column(String, unique=True, index=True, nullable=False)
    status = Column(String, default="PENDING")  # ClaimStatusEnum
    claim_type = Column(String, default="REIMBURSEMENT")
    draft_text = Column(Text, nullable=True)
    approved_at = Column(DateTime, nullable=True)
    submitted_at = Column(DateTime, nullable=True)
    
    case = relationship("Case", back_populates="claims")
    claim_documents = relationship("ClaimDocument", back_populates="claim", cascade="all, delete-orphan")

class ClaimDocument(Base):
    __tablename__ = "claim_documents"
    
    id = Column(String, primary_key=True, index=True)
    claim_id = Column(String, ForeignKey("claims.id"), nullable=False)
    document_id = Column(String, ForeignKey("documents.id"), nullable=True)
    document_type = Column(String, nullable=False)
    required = Column(Boolean, default=True)
    present = Column(Boolean, default=False)
    page_count = Column(Integer, default=0)
    expected_page_count = Column(Integer, default=1)
    
    claim = relationship("Claim", back_populates="claim_documents")

class CheckIn(Base):
    __tablename__ = "checkins"
    
    id = Column(String, primary_key=True, index=True)
    case_id = Column(String, ForeignKey("cases.id"), nullable=False)
    patient_id = Column(String, ForeignKey("users.id"), nullable=False)
    answers_json = Column(JSON, nullable=False)  # {pain: int, fever: bool, breathing_difficulty: bool, dizziness: bool, vomiting: bool}
    free_text = Column(Text, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    
    case = relationship("Case", back_populates="checkins")
    alerts = relationship("Alert", back_populates="checkin")

class Alert(Base):
    __tablename__ = "alerts"
    
    id = Column(String, primary_key=True, index=True)
    case_id = Column(String, ForeignKey("cases.id"), nullable=False)
    checkin_id = Column(String, ForeignKey("checkins.id"), nullable=True)
    severity = Column(String, nullable=False)  # SeverityEnum: AMBER, RED
    title = Column(String, nullable=False)
    summary = Column(Text, nullable=False)
    rule_id = Column(String, nullable=False)
    status = Column(String, default="NEW")  # NEW, ACKNOWLEDGED, RESOLVED
    created_at = Column(DateTime, default=datetime.utcnow)
    
    case = relationship("Case", back_populates="alerts")
    checkin = relationship("CheckIn", back_populates="alerts")
