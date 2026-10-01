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

def test_checkin_questions():
    token = get_token("patient@medsure.demo")
    res = client.get("/cases/MS-001/checkin/questions", headers={"Authorization": f"Bearer {token}"})
    assert res.status_code == 200
    questions = res.json()
    assert len(questions) == 6
    ids = [q["id"] for q in questions]
    assert "pain" in ids
    assert "breathing_difficulty" in ids
    assert "fever" in ids

def test_patient_submits_breathing_difficulty_triggers_red_alert():
    patient_token = get_token("patient@medsure.demo")
    hosp_token = get_token("hospital@medsure.demo")

    payload = {
        "pain": 3,
        "fever": False,
        "breathing_difficulty": True,
        "dizziness": False,
        "vomiting": False,
        "free_text": "Shortness of breath started an hour ago"
    }

    chk_res = client.post(
        "/cases/MS-001/checkin",
        json=payload,
        headers={"Authorization": f"Bearer {patient_token}"}
    )
    assert chk_res.status_code == 200
    chk_data = chk_res.json()
    assert "Difficulty Breathing" in chk_data["alerts_triggered"]

    # Verify hospital desk sees the red alert
    hosp_alerts = client.get("/hospital/alerts", headers={"Authorization": f"Bearer {hosp_token}"})
    assert hosp_alerts.status_code == 200
    alerts = hosp_alerts.json()
    red_alerts = [a for a in alerts if a["case_id"] == "MS-001" and a["severity"] == "RED"]
    assert len(red_alerts) >= 1
    assert "Difficulty Breathing" in red_alerts[0]["title"]
