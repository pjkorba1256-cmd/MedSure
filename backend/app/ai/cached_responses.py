from typing import Dict, Any

CACHED_DISCHARGE_PACK: Dict[str, Any] = {
    "summary": "Patient Rajesh Sharma was treated for Acute Myocardial Infarction with successful percutaneous coronary intervention (PCI). Discharged in stable condition with lifestyle modifications and cardioprotective medication regimen.",
    "what_happened": {
        "text": "You were admitted for acute chest pain and underwent an angioplasty with stent placement in the Left Anterior Descending (LAD) artery. Your heart rhythm, blood pressure, and recovery were monitored closely, and you are recovering well.",
        "source": {
            "document_name": "Discharge Summary MS-001",
            "page": 1,
            "excerpt": "Diagnosis: Acute Coronary Syndrome (STEMI). Procedure: Primary PCI to LAD with DES stent."
        }
    },
    "medications": [
        {
            "name": "Metformin",
            "dosage": "500 mg",
            "timing": "Twice daily after meals",
            "purpose": "Controls blood sugar levels to assist cardiovascular healing and metabolic health.",
            "source": {
                "document_name": "Discharge Summary MS-001",
                "page": 2,
                "excerpt": "Tab Metformin 500mg PO BD PC"
            }
        },
        {
            "name": "Atorvastatin",
            "dosage": "40 mg",
            "timing": "Once daily at bedtime",
            "purpose": "Lowers cholesterol and stabilizes vascular plaques to prevent future blockages.",
            "source": {
                "document_name": "Discharge Summary MS-001",
                "page": 2,
                "excerpt": "Tab Atorvastatin 40mg PO OD HS"
            }
        },
        {
            "name": "Aspirin + Clopidogrel (Dual Antiplatelet)",
            "dosage": "75 mg / 75 mg",
            "timing": "Once daily morning after food",
            "purpose": "Prevents blood clots inside the newly placed cardiac stent.",
            "source": {
                "document_name": "Discharge Summary MS-001",
                "page": 2,
                "excerpt": "Tab Ecosprin-AV 75/75 PO OD"
            }
        }
    ],
    "follow_up": {
        "instructions": "Follow-up consultation with Dr. Arvind Menon in Cardiology OPD for ECG and stent site check. Bring medication chart.",
        "date_or_days": "In 7 days (or immediately if symptoms arise)",
        "department": "Cardiology OPD, 2nd Floor",
        "source": {
            "document_name": "Discharge Summary MS-001",
            "page": 2,
            "excerpt": "Follow up in Cardiology OPD after 7 days with fresh ECG."
        }
    },
    "what_to_watch": [
        {
            "warning": "Shortness of breath or rapid breathing while resting",
            "action": "Stop activity immediately, sit upright, and contact hospital emergency if not resolved in 5 minutes.",
            "severity": "RED",
            "source": {
                "document_name": "Discharge Summary MS-001",
                "page": 2,
                "excerpt": "Warning signs: Dyspnea at rest, chest heaviness, cold clammy sweats."
            }
        },
        {
            "warning": "Chest pain, pressure, or heaviness radiating to arm or jaw",
            "action": "Immediate emergency call (108/hospital hotline) - do not drive yourself.",
            "severity": "RED",
            "source": {
                "document_name": "Discharge Summary MS-001",
                "page": 2,
                "excerpt": "Warning signs: Recurrent angina, syncope."
            }
        },
        {
            "warning": "Persistent dizziness, extreme fatigue, or fever over 100 F",
            "action": "Report in the daily app check-in and notify the nursing helpline.",
            "severity": "AMBER",
            "source": {
                "document_name": "Discharge Summary MS-001",
                "page": 2,
                "excerpt": "Notify clinic for pyrexia or persistent lightheadedness."
            }
        }
    ]
}

CACHED_CLAIM_DRAFT: Dict[str, Any] = {
    "summary": "Health insurance reimbursement claim draft for post-discharge hospitalization expenses at MedSure Trust Hospital.",
    "claim_reason": "Inpatient treatment for Acute Coronary Syndrome with Primary PCI stenting.",
    "treatment_summary": "Patient Rajesh Sharma was admitted for emergency cardiac stenting. All clinical investigations and post-procedural recovery were completed successfully according to standard medical guidelines.",
    "claim_amount": 185000.0,
    "supporting_documents": [
        "Discharge Summary (Complete)",
        "Final Consolidated Hospital Bill",
        "Government ID Proof (Aadhaar)",
        "Catheterization Lab & ECG Investigation Reports"
    ],
    "explanation_request": "This claim draft addresses the missing investigation report requirement and explains the emergency PCI procedure. All requisite diagnostic attachments have now been compiled for formal insurer re-evaluation.",
    "draft_text": """Subject: Formal Reimbursement Submission & Re-examination Request - Claim #CLM-2026-001

To: Claims Adjudication Department, Star Health Insurance
Policyholder: Rajesh Sharma | Case ID: MS-001 | Claim Ref: CLM-2026-001

Dear Claims Team,

We hereby submit the comprehensive documentation pack for hospitalization reimbursement regarding the inpatient admission of Rajesh Sharma at MedSure Trust Hospital.

1. Clinical Summary:
Patient was admitted in emergency condition presenting with acute chest pain and underwent timely Primary PCI to the LAD with Drug Eluting Stent. Total length of stay was 4 days.

2. Document Rectification:
In response to the initial claim status query regarding missing investigation reports, we have attached:
- Full Catheterization Procedure Report with Stent serial verification.
- Pre and post-operative 12-lead ECG reports.
- Consolidated itemized bill and payment receipts.

Total Claimable Amount: INR 1,85,000.00

Please review the attached certified documents and process the reimbursement to the designated bank account on file.

Sincerely,
Anita Sharma (Caregiver / Primary Contact for Rajesh Sharma)
Phone: +91 98765 43210
"""
}
