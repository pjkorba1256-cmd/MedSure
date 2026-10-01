import pytest
from fastapi.testclient import TestClient
from app.main import app
from app.config import settings

client = TestClient(app)

def test_login_patient_success():
    res = client.post("/auth/login", json={
        "email": "patient@medsure.demo",
        "password": settings.DEMO_PASSWORD
    })
    assert res.status_code == 200
    data = res.json()
    assert "access_token" in data
    assert data["user"]["role"] == "PATIENT"
    assert data["user"]["email"] == "patient@medsure.demo"

def test_login_family_success():
    res = client.post("/auth/login", json={
        "email": "family@medsure.demo",
        "password": settings.DEMO_PASSWORD
    })
    assert res.status_code == 200
    data = res.json()
    assert data["user"]["role"] == "FAMILY"

def test_login_hospital_success():
    res = client.post("/auth/login", json={
        "email": "hospital@medsure.demo",
        "password": settings.DEMO_PASSWORD
    })
    assert res.status_code == 200
    data = res.json()
    assert data["user"]["role"] == "HOSPITAL_DESK"

def test_login_invalid_password():
    res = client.post("/auth/login", json={
        "email": "patient@medsure.demo",
        "password": "WrongPassword123!"
    })
    assert res.status_code == 401
    assert "Invalid email or password" in res.json()["detail"]

def test_auth_me_with_valid_token():
    # Login as patient
    login_res = client.post("/auth/login", json={
        "email": "patient@medsure.demo",
        "password": settings.DEMO_PASSWORD
    })
    token = login_res.json()["access_token"]
    
    me_res = client.get("/auth/me", headers={"Authorization": f"Bearer {token}"})
    assert me_res.status_code == 200
    assert me_res.json()["email"] == "patient@medsure.demo"

def test_auth_me_unauthorized():
    me_res = client.get("/auth/me")
    assert me_res.status_code == 401
