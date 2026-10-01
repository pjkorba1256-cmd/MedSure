import pytest
from app.database import engine, Base, SessionLocal
from app.services.seed_data import seed_database
from app.models.entities import Claim, ClaimStatusEnum, Approval, BillItem

@pytest.fixture(autouse=True)
def reset_db():
    db = SessionLocal()
    try:
        # Reset claim to DENIED and unapproved
        claim = db.query(Claim).filter(Claim.id == "claim_001").first()
        if claim:
            claim.status = ClaimStatusEnum.DENIED.value
            claim.approved_at = None
            claim.submitted_at = None
            claim.draft_text = None
        
        # Remove any pending or approved claim approvals
        db.query(Approval).filter(Approval.target_id == "claim_001").delete()

        # Clean up any non-seed bill items
        seed_bill_ids = ["bill_001", "bill_002", "bill_003", "bill_004", "bill_005", "bill_006"]
        db.query(BillItem).filter(BillItem.id.notin_(seed_bill_ids)).delete()

        db.commit()
    finally:
        db.close()
    yield
