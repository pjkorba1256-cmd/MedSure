import pytest
from app.ai.ai_service import ai_service
from app.ai.cached_responses import CACHED_DISCHARGE_PACK, CACHED_CLAIM_DRAFT

def test_ai_discharge_pack_fallback_without_api_key():
    # Force API key None
    ai_service.api_key = None
    pack = ai_service.generate_discharge_pack("synthetic raw ocr text")
    
    assert pack is not None
    assert "what_happened" in pack
    assert "medications" in pack
    assert len(pack["medications"]) >= 3
    assert "follow_up" in pack
    assert "what_to_watch" in pack
    assert pack["what_happened"]["source"]["page"] >= 1

def test_ai_bill_explanation_fallback():
    exp = ai_service.explain_bill_item(
        description="Administrative Handling Fee",
        amount=5000.0,
        category="Admin"
    )
    assert exp is not None
    assert exp["flagged"] is True
    assert "Worth asking about" in exp["flag_reason"]

def test_ai_claim_draft_fallback():
    draft = ai_service.draft_claim(
        patient_name="Rajesh Sharma",
        hospital_name="MedSure Trust Hospital",
        claim_number="CLM-2026-001",
        claim_amount=185000.0
    )
    assert draft is not None
    assert draft["patient_name"] == "Rajesh Sharma"
    assert "CLM-2026-001" in draft["draft_text"]
    assert len(draft["supporting_documents"]) >= 3
