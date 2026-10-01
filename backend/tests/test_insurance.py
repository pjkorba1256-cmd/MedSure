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

def test_claim_status_and_missing_document_detection():
    token = get_token("family@medsure.demo")
    res = client.get("/cases/MS-001/claim", headers={"Authorization": f"Bearer {token}"})
    assert res.status_code == 200
    data = res.json()
    
    assert data["claim_number"] == "CLM-2026-001"
    assert data["status"] == "DENIED"
    
    # Missing document detection
    assert "Catheterization & ECG Investigation Reports" in data["missing_documents"]
    
    # Missing page count detection
    assert any("Possible missing page detected" in warning for warning in data["page_discrepancies"])

def test_claim_checklist():
    token = get_token("family@medsure.demo")
    res = client.get("/cases/MS-001/claim/checklist", headers={"Authorization": f"Bearer {token}"})
    assert res.status_code == 200
    items = res.json()
    assert len(items) == 4

def test_ai_claim_draft_requires_approval():
    token = get_token("family@medsure.demo")
    res = client.post("/cases/MS-001/claim/draft", headers={"Authorization": f"Bearer {token}"})
    assert res.status_code == 200
    draft = res.json()
    
    assert draft["requires_approval"] is True
    assert draft["approval_status"] == "PENDING"
    assert "CLM-2026-001" in draft["draft_text"]
    assert draft["claim_amount"] > 0
