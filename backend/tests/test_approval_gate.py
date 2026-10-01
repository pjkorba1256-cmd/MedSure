import pytest
from fastapi.testclient import TestClient
from app.main import app
from app.config import settings

client = TestClient(app)

def get_token(email: str):
    res = client.post("/auth/login", json={
        "email": email,
        "password": settings.DEMO_PASSWORD
    })
    return res.json()["access_token"]

def test_claim_cannot_be_dispatched_without_approval():
    family_token = get_token("family@medsure.demo")
    
    # 1. Draft claim
    client.post("/cases/MS-001/claim/draft", headers={"Authorization": f"Bearer {family_token}"})
    
    # Claim must remain in DENIED or PENDING until approved
    claim_res = client.get("/cases/MS-001/claim", headers={"Authorization": f"Bearer {family_token}"})
    assert claim_res.json()["approved_at"] is None

def test_family_approval_updates_claim_and_creates_audit_log():
    family_token = get_token("family@medsure.demo")
    
    # Generate draft to ensure pending approval exists
    client.post("/cases/MS-001/claim/draft", headers={"Authorization": f"Bearer {family_token}"})
    
    # Family approves
    appr_res = client.post("/cases/MS-001/claim/approve", headers={"Authorization": f"Bearer {family_token}"})
    assert appr_res.status_code == 200
    data = appr_res.json()
    assert data["success"] is True
    assert data["claim_status"] == "REIMBURSEMENT"
    assert data["approved_at"] is not None

    # Verify audit log in approvals
    all_appr = client.get("/approvals", headers={"Authorization": f"Bearer {family_token}"})
    assert all_appr.status_code == 200
    approvals = all_appr.json()
    approved_records = [a for a in approvals if a["action_type"] == "CLAIM_SUBMISSION" and a["approved"] is True]
    assert len(approved_records) >= 1

def test_hospital_desk_cannot_bypass_family_approval():
    hosp_token = get_token("hospital@medsure.demo")
    res = client.post("/cases/MS-001/claim/approve", headers={"Authorization": f"Bearer {hosp_token}"})
    assert res.status_code == 403
    assert "Only family or patient can approve" in res.json()["detail"]
