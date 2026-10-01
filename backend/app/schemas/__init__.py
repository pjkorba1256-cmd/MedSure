from datetime import datetime
from typing import Optional, List, Dict, Any
from pydantic import BaseModel, EmailStr, Field

# User & Auth
class UserBase(BaseModel):
    name: str
    email: EmailStr
    phone: Optional[str] = None
    role: str

class UserCreate(UserBase):
    password: str

class UserResponse(UserBase):
    id: str
    created_at: datetime
    class Config:
        from_attributes = True

class Token(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user: UserResponse

class LoginRequest(BaseModel):
    email: str
    password: str

# Case
class CaseMemberResponse(BaseModel):
    id: str
    user_id: str
    role: str
    user_name: Optional[str] = None
    class Config:
        from_attributes = True

class CaseResponse(BaseModel):
    id: str
    patient_id: str
    patient_name: Optional[str] = None
    hospital_name: str
    admission_date: datetime
    discharge_date: Optional[datetime] = None
    status: str
    created_at: datetime
    members: List[CaseMemberResponse] = []
    class Config:
        from_attributes = True

class CaseCreate(BaseModel):
    id: str
    patient_id: str
    hospital_name: str
    admission_date: datetime
    discharge_date: Optional[datetime] = None

# Document
class DocumentResponse(BaseModel):
    id: str
    case_id: str
    uploaded_by: str
    document_type: str
    filename: str
    storage_path: str
    ocr_confidence: float
    page_count: int
    created_at: datetime
    class Config:
        from_attributes = True

# Discharge Pack
class SourceRef(BaseModel):
    document_name: str = "Discharge Summary"
    page: int = 1
    excerpt: Optional[str] = None

class WhatHappenedCard(BaseModel):
    text: str
    source: SourceRef

class MedicationCard(BaseModel):
    name: str
    dosage: str
    timing: str
    purpose: str
    source: SourceRef

class FollowUpCard(BaseModel):
    instructions: str
    date_or_days: str
    department: str
    source: SourceRef

class WhatToWatchCard(BaseModel):
    warning: str
    action: str
    severity: str = "AMBER"
    source: SourceRef

class DischargePackResponse(BaseModel):
    id: str
    case_id: str
    source_document_id: Optional[str] = None
    summary: str
    what_happened: WhatHappenedCard
    medications: List[MedicationCard]
    follow_up: FollowUpCard
    what_to_watch: List[WhatToWatchCard]
    created_at: datetime
    class Config:
        from_attributes = True

# Bill Items
class BillItemResponse(BaseModel):
    id: str
    case_id: str
    description: str
    amount: float
    category: str
    explanation: Optional[str] = None
    flagged: bool = False
    flag_reason: Optional[str] = None
    source_reference: Optional[str] = "Hospital Bill Page 1"
    class Config:
        from_attributes = True

class BillProcessRequest(BaseModel):
    items: List[Dict[str, Any]]

# Queries
class QueryCreate(BaseModel):
    bill_item_id: Optional[str] = None
    message: str

class QueryReply(BaseModel):
    reply: str

class QueryResponse(BaseModel):
    id: str
    case_id: str
    bill_item_id: Optional[str] = None
    bill_item_description: Optional[str] = None
    bill_item_amount: Optional[float] = None
    created_by: str
    created_by_name: Optional[str] = None
    message: str
    reply: Optional[str] = None
    status: str
    created_at: datetime
    replied_at: Optional[datetime] = None
    class Config:
        from_attributes = True

# Claim & Insurance
class ClaimDocumentResponse(BaseModel):
    id: str
    claim_id: str
    document_id: Optional[str] = None
    document_type: str
    required: bool
    present: bool
    page_count: int
    expected_page_count: int
    page_count_warning: Optional[str] = None
    class Config:
        from_attributes = True

class ClaimResponse(BaseModel):
    id: str
    case_id: str
    claim_number: str
    status: str
    claim_type: str
    draft_text: Optional[str] = None
    approved_at: Optional[datetime] = None
    submitted_at: Optional[datetime] = None
    documents: List[ClaimDocumentResponse] = []
    missing_documents: List[str] = []
    page_discrepancies: List[str] = []
    class Config:
        from_attributes = True

class ClaimDraftResponse(BaseModel):
    claim_id: str
    patient_name: str
    hospital_name: str
    claim_number: str
    treatment_summary: str
    claim_amount: float
    supporting_documents: List[str]
    explanation_request: str
    draft_text: str
    requires_approval: bool = True
    approval_status: str = "PENDING"

# Approvals
class ApprovalResponse(BaseModel):
    id: str
    case_id: str
    user_id: str
    action_type: str
    target_id: str
    approved: bool
    status: str
    timestamp: datetime
    class Config:
        from_attributes = True

# Check-in & Alerts
class CheckInQuestion(BaseModel):
    id: str
    label: str
    type: str  # scale, boolean, text
    options: Optional[List[Any]] = None

class CheckInCreate(BaseModel):
    pain: int = Field(ge=0, le=10)
    fever: bool
    breathing_difficulty: bool
    dizziness: bool
    vomiting: bool
    free_text: Optional[str] = None

class CheckInResponse(BaseModel):
    id: str
    case_id: str
    patient_id: str
    answers_json: Dict[str, Any]
    free_text: Optional[str] = None
    created_at: datetime
    alerts_triggered: List[str] = []
    class Config:
        from_attributes = True

class AlertResponse(BaseModel):
    id: str
    case_id: str
    checkin_id: Optional[str] = None
    patient_name: Optional[str] = None
    severity: str
    title: str
    summary: str
    rule_id: str
    status: str
    created_at: datetime
    class Config:
        from_attributes = True
