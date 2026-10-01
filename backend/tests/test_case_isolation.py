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

def test_patient_can_access_own_case():
    token = get_token("patient@medsure.demo")
    res = client.get("/cases/MS-001", headers={"Authorization": f"Bearer {token}"})
    assert res.status_code == 200
    assert res.json()["id"] == "MS-001"

def test_family_can_access_member_case():
    token = get_token("family@medsure.demo")
    res = client.get("/cases/MS-001", headers={"Authorization": f"Bearer {token}"})
    assert res.status_code == 200
    assert res.json()["id"] == "MS-001"

def test_hospital_can_access_all_cases():
    token = get_token("hospital@medsure.demo")
    res = client.get("/cases/MS-001", headers={"Authorization": f"Bearer {token}"})
    assert res.status_code == 200
    assert res.json()["id"] == "MS-001"

def test_access_nonexistent_case():
    token = get_token("patient@medsure.demo")
    res = client.get("/cases/NON-EXISTENT", headers={"Authorization": f"Bearer {token}"})
    assert res.status_code == 404
