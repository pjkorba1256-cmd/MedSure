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

def test_get_discharge_pack():
    token = get_token("patient@medsure.demo")
    res = client.get("/cases/MS-001/discharge", headers={"Authorization": f"Bearer {token}"})
    assert res.status_code == 200
    data = res.json()
    
    # Check 4 required cards
    assert "what_happened" in data
    assert "medications" in data
    assert "follow_up" in data
    assert "what_to_watch" in data
    
    # Check source references are present
    assert "source" in data["what_happened"]
    assert data["what_happened"]["source"]["page"] >= 1
    assert len(data["medications"]) > 0
    assert "source" in data["medications"][0]

def test_hospital_trigger_discharge_processing():
    token = get_token("hospital@medsure.demo")
    res = client.post("/cases/MS-001/discharge/process", headers={"Authorization": f"Bearer {token}"})
    assert res.status_code == 200
    data = res.json()
    assert data["case_id"] == "MS-001"
    assert "summary" in data

def test_patient_cannot_trigger_discharge_processing():
    token = get_token("patient@medsure.demo")
    res = client.post("/cases/MS-001/discharge/process", headers={"Authorization": f"Bearer {token}"})
    assert res.status_code == 403
