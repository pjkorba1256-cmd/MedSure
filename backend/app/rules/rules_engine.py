import os
import yaml
from pathlib import Path
from typing import Dict, Any, List, Optional
from pydantic import BaseModel

class AlertTrigger(BaseModel):
    rule_id: str
    severity: str  # RED, AMBER
    title: str
    summary: str

class RulesEngine:
    def __init__(self, rules_path: Optional[str] = None):
        if not rules_path:
            rules_path = str(Path(__file__).parent / "rules.yaml")
        self.rules_path = rules_path
        self._load_rules()

    def _load_rules(self):
        with open(self.rules_path, "r", encoding="utf-8") as f:
            data = yaml.safe_load(f)
        self.structured_rules = data.get("rules", [])
        self.keyword_rules = data.get("keyword_rules", [])

    def evaluate(self, answers: Dict[str, Any], free_text: Optional[str] = None) -> List[AlertTrigger]:
        triggers: List[AlertTrigger] = []
        pain = int(answers.get("pain", 0))
        fever = bool(answers.get("fever", False))
        breathing_difficulty = bool(answers.get("breathing_difficulty", False))
        dizziness = bool(answers.get("dizziness", False))
        vomiting = bool(answers.get("vomiting", False))

        # 1. Structured rules
        for rule in self.structured_rules:
            rule_id = rule["id"]
            severity = rule["severity"]
            title = rule["title"]
            summary = rule["summary"]
            field = rule["field"]
            op = rule["operator"]
            val = rule.get("value")

            triggered = False
            if field == "breathing_difficulty" and op == "eq":
                if breathing_difficulty == val:
                    triggered = True
            elif field == "pain":
                if op == "gte":
                    if rule_id == "PAIN_HIGH_001" and pain >= 9:
                        triggered = True
                    elif rule_id == "PAIN_MODERATE_002" and 7 <= pain < 9:
                        triggered = True
            elif field == "fever" and op == "eq":
                if fever == val:
                    triggered = True
            elif field == "compound_dizzy_vomit":
                if dizziness or vomiting:
                    triggered = True

            if triggered:
                triggers.append(AlertTrigger(
                    rule_id=rule_id,
                    severity=severity,
                    title=title,
                    summary=summary
                ))

        # 2. Keyword rules on free_text (Strictly deterministic matching)
        if free_text:
            normalized_text = free_text.lower()
            for kw_rule in self.keyword_rules:
                for kw in kw_rule["keywords"]:
                    if kw in normalized_text:
                        triggers.append(AlertTrigger(
                            rule_id=kw_rule["id"],
                            severity=kw_rule["severity"],
                            title=kw_rule["title"],
                            summary=kw_rule["summary"]
                        ))
                        break  # Match once per rule

        # De-duplicate by rule_id preserving highest severity first
        seen_rules = set()
        unique_triggers = []
        # Sort RED before AMBER
        sorted_triggers = sorted(triggers, key=lambda t: 0 if t.severity == "RED" else 1)
        for t in sorted_triggers:
            if t.rule_id not in seen_rules:
                seen_rules.add(t.rule_id)
                unique_triggers.append(t)

        return unique_triggers

# Singleton instance
rules_engine = RulesEngine()
