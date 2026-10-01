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

def test_family_create_bill_query_and_hospital_reply():
    family_token = get_token("family@medsure.demo")
    hosp_token = get_token("hospital@medsure.demo")

    # 1. Family creates query on bill item bill_005 (Administrative fee)
    q_res = client.post(
        "/cases/MS-001/queries",
        json={
            "bill_item_id": "bill_005",
            "message": "Can you please clarify what is included under this Administrative Fee?"
        },
        headers={"Authorization": f"Bearer {family_token}"}
    )
    assert q_res.status_code == 201
    q_data = q_res.json()
    query_id = q_data["id"]
    assert q_data["status"] == "OPEN"
    assert q_data["bill_item_id"] == "bill_005"

    # 2. Hospital Desk sees the query
    list_res = client.get("/cases/MS-001/queries", headers={"Authorization": f"Bearer {hosp_token}"})
    assert list_res.status_code == 200
    queries = list_res.json()
    matching = [q for q in queries if q["id"] == query_id]
    assert len(matching) == 1

    # 3. Hospital Desk replies
    reply_res = client.post(
        f"/queries/{query_id}/reply",
        json={"reply": "This fee covers the digital medical records processing and insurance liaison paperwork."},
        headers={"Authorization": f"Bearer {hosp_token}"}
    )
    assert reply_res.status_code == 200
    reply_data = reply_res.json()
    assert reply_data["status"] == "RESOLVED"
    assert "digital medical records" in reply_data["reply"]

    # 4. Patient/Family can see the reply
    f_check_res = client.get("/cases/MS-001/queries", headers={"Authorization": f"Bearer {family_token}"})
    assert f_check_res.status_code == 200
    f_queries = f_check_res.json()
    f_match = [q for q in f_queries if q["id"] == query_id][0]
    assert f_match["status"] == "RESOLVED"
    assert f_match["reply"] is not None

def test_patient_cannot_reply_to_query():
    patient_token = get_token("patient@medsure.demo")
    res = client.post(
        "/queries/nonexistent/reply",
        json={"reply": "Invalid reply"},
        headers={"Authorization": f"Bearer {patient_token}"}
    )
    assert res.status_code == 403
