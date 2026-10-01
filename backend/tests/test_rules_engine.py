import pytest
from app.rules.rules_engine import rules_engine

def test_rule_breathing_difficulty_red():
    answers = {"pain": 2, "fever": False, "breathing_difficulty": True, "dizziness": False, "vomiting": False}
    triggers = rules_engine.evaluate(answers)
    
    assert len(triggers) == 1
    assert triggers[0].rule_id == "BREATHING_001"
    assert triggers[0].severity == "RED"
    assert "breathing" in triggers[0].title.lower()

def test_rule_pain_high_red():
    answers = {"pain": 9, "fever": False, "breathing_difficulty": False, "dizziness": False, "vomiting": False}
    triggers = rules_engine.evaluate(answers)
    
    assert len(triggers) == 1
    assert triggers[0].rule_id == "PAIN_HIGH_001"
    assert triggers[0].severity == "RED"

def test_rule_pain_moderate_amber():
    answers = {"pain": 7, "fever": False, "breathing_difficulty": False, "dizziness": False, "vomiting": False}
    triggers = rules_engine.evaluate(answers)
    
    assert len(triggers) == 1
    assert triggers[0].rule_id == "PAIN_MODERATE_002"
    assert triggers[0].severity == "AMBER"

def test_rule_fever_amber():
    answers = {"pain": 3, "fever": True, "breathing_difficulty": False, "dizziness": False, "vomiting": False}
    triggers = rules_engine.evaluate(answers)
    
    assert len(triggers) == 1
    assert triggers[0].rule_id == "FEVER_001"
    assert triggers[0].severity == "AMBER"

def test_rule_dizziness_or_vomiting_amber():
    # Dizziness only
    t1 = rules_engine.evaluate({"pain": 2, "fever": False, "breathing_difficulty": False, "dizziness": True, "vomiting": False})
    assert len(t1) == 1
    assert t1[0].rule_id == "DIZZINESS_VOMITING_001"
    assert t1[0].severity == "AMBER"

    # Vomiting only
    t2 = rules_engine.evaluate({"pain": 2, "fever": False, "breathing_difficulty": False, "dizziness": False, "vomiting": True})
    assert len(t2) == 1
    assert t2[0].rule_id == "DIZZINESS_VOMITING_001"

def test_keyword_chest_pain_red():
    answers = {"pain": 2, "fever": False, "breathing_difficulty": False, "dizziness": False, "vomiting": False}
    triggers = rules_engine.evaluate(answers, free_text="Feeling sudden tight chest pain radiating to left shoulder")
    
    assert len(triggers) == 1
    assert triggers[0].rule_id == "KEYWORD_CHEST_PAIN"
    assert triggers[0].severity == "RED"

def test_keyword_breathless_red():
    answers = {"pain": 0, "fever": False, "breathing_difficulty": False, "dizziness": False, "vomiting": False}
    triggers = rules_engine.evaluate(answers, free_text="I feel breathless while sitting on the bed")
    
    assert len(triggers) == 1
    assert triggers[0].rule_id == "KEYWORD_BREATHLESS"
    assert triggers[0].severity == "RED"

def test_keyword_bleeding_red():
    answers = {"pain": 2, "fever": False, "breathing_difficulty": False, "dizziness": False, "vomiting": False}
    triggers = rules_engine.evaluate(answers, free_text="There is active bleeding from the catheter site")
    
    assert len(triggers) == 1
    assert triggers[0].rule_id == "KEYWORD_BLEEDING"
    assert triggers[0].severity == "RED"

def test_keyword_unconscious_red():
    answers = {"pain": 1, "fever": False, "breathing_difficulty": False, "dizziness": False, "vomiting": False}
    triggers = rules_engine.evaluate(answers, free_text="Experienced a fainting episode this morning")
    
    assert len(triggers) == 1
    assert triggers[0].rule_id == "KEYWORD_UNCONSCIOUS"
    assert triggers[0].severity == "RED"

def test_normal_checkin_no_alerts():
    answers = {"pain": 1, "fever": False, "breathing_difficulty": False, "dizziness": False, "vomiting": False}
    triggers = rules_engine.evaluate(answers, free_text="Feeling peaceful, took all morning medicines with breakfast.")
    assert len(triggers) == 0
