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

def test_get_bills_seeded_flags():
    token = get_token("patient@medsure.demo")
    res = client.get("/cases/MS-001/bills", headers={"Authorization": f"Bearer {token}"})
    assert res.status_code == 200
    items = res.json()
    assert len(items) >= 5

    # Check for duplicate item flag
    duplicate_items = [it for it in items if "Duplicate" in (it.get("flag_reason") or "")]
    assert len(duplicate_items) >= 1
    assert duplicate_items[0]["flagged"] is True
    assert "Worth asking about" in duplicate_items[0]["flag_reason"]

    # Check for non-medical item flag
    admin_items = [it for it in items if "Administrative" in it["description"]]
    assert len(admin_items) >= 1
    assert admin_items[0]["flagged"] is True
    assert "Worth asking about" in admin_items[0]["flag_reason"]

def test_process_bills_duplicate_and_unusual_detection():
    token = get_token("hospital@medsure.demo")
    payload = {
        "items": [
            {"description": "General Ward Bed (Day 1)", "amount": 3000.0, "category": "Room"},
            {"description": "General Ward Bed (Day 1)", "amount": 3000.0, "category": "Room"}, # duplicate!
            {"description": "Special Handling Miscellaneous Fee", "amount": 1500.0, "category": "Admin"} # unusual
        ]
    }
    res = client.post("/cases/MS-001/bills/process", json=payload, headers={"Authorization": f"Bearer {token}"})
    assert res.status_code == 200
    processed = res.json()
    
    # 2nd item must be flagged duplicate
    assert processed[1]["flagged"] is True
    assert "Duplicate" in processed[1]["flag_reason"]
    
    # 3rd item must be flagged unusual
    assert processed[2]["flagged"] is True
    assert "Worth asking about" in processed[2]["flag_reason"]
