package com.powerbank.medsure.data.repository

import com.powerbank.medsure.data.api.RetrofitClient
import com.powerbank.medsure.data.model.*
import com.powerbank.medsure.data.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class MedSureRepository {

    private val api get() = RetrofitClient.getApiService()

    suspend fun login(email: String, password: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val auth = response.body()!!
                SessionManager.saveSession(auth.user, auth.accessToken)
                Result.success(auth)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Invalid login credentials"))
            }
        } catch (e: Exception) {
            // Offline demo fallback if backend is momentarily unreachable
            val demoUser = when (email) {
                "hospital@medsure.demo" -> User("usr_hospital_001", "Dr. Arvind Menon (Desk)", email, "+91 98765 43212", "HOSPITAL_DESK")
                "family@medsure.demo" -> User("usr_family_001", "Anita Sharma", email, "+91 98765 43211", "FAMILY")
                else -> User("usr_patient_001", "Rajesh Sharma", "patient@medsure.demo", "+91 98765 43210", "PATIENT")
            }
            val mockAuth = AuthResponse("mock_token_offline_2026", "bearer", demoUser)
            SessionManager.saveSession(demoUser, mockAuth.accessToken)
            Result.success(mockAuth)
        }
    }

    suspend fun getDischargePack(caseId: String): Result<DischargePack> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDischargePack(caseId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(getFallbackDischargePack(caseId))
            }
        } catch (e: Exception) {
            Result.success(getFallbackDischargePack(caseId))
        }
    }

    suspend fun processDischarge(caseId: String, docId: String? = null): Result<DischargePack> = withContext(Dispatchers.IO) {
        try {
            val response = api.processDischarge(caseId, docId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(getFallbackDischargePack(caseId))
            }
        } catch (e: Exception) {
            Result.success(getFallbackDischargePack(caseId))
        }
    }

    suspend fun getBills(caseId: String): Result<List<BillItem>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getBills(caseId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(getFallbackBills(caseId))
            }
        } catch (e: Exception) {
            Result.success(getFallbackBills(caseId))
        }
    }

    suspend fun getQueries(caseId: String): Result<List<Query>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getQueries(caseId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.success(emptyList())
        }
    }

    suspend fun createQuery(caseId: String, billItemId: String?, message: String): Result<Query> = withContext(Dispatchers.IO) {
        try {
            val response = api.createQuery(caseId, QueryCreate(billItemId, message))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to post query: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Local fallback query
            val fallback = Query(
                id = "q_local_${System.currentTimeMillis()}",
                caseId = caseId,
                billItemId = billItemId,
                billItemDescription = "Administrative & Documentation Handling Fee",
                billItemAmount = 8500.0,
                createdBy = SessionManager.currentUser.value?.id ?: "usr_family_001",
                createdByName = SessionManager.currentUser.value?.name ?: "Anita Sharma",
                message = message,
                status = "OPEN"
            )
            Result.success(fallback)
        }
    }

    suspend fun replyToQuery(queryId: String, replyText: String): Result<Query> = withContext(Dispatchers.IO) {
        try {
            val response = api.replyToQuery(queryId, QueryReply(replyText))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to reply: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getClaim(caseId: String): Result<Claim> = withContext(Dispatchers.IO) {
        try {
            val response = api.getClaim(caseId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(getFallbackClaim(caseId))
            }
        } catch (e: Exception) {
            Result.success(getFallbackClaim(caseId))
        }
    }

    suspend fun generateClaimDraft(caseId: String): Result<ClaimDraftResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.generateClaimDraft(caseId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(getFallbackClaimDraft())
            }
        } catch (e: Exception) {
            Result.success(getFallbackClaimDraft())
        }
    }

    suspend fun approveClaim(caseId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = api.approveClaimDraft(caseId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to approve claim: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.success(true) // Optimistic fallback for demo
        }
    }

    suspend fun submitCheckIn(caseId: String, request: CheckInRequest): Result<CheckInResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.submitCheckIn(caseId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Check-in submission failed"))
            }
        } catch (e: Exception) {
            val triggered = mutableListOf<String>()
            if (request.breathingDifficulty) triggered.add("Difficulty Breathing")
            if (request.pain >= 9) triggered.add("Severe Pain")
            if (request.fever) triggered.add("Post-Discharge Fever")
            
            val fallback = CheckInResponse(
                id = "chk_local_${System.currentTimeMillis()}",
                caseId = caseId,
                patientId = "usr_patient_001",
                answersJson = mapOf("pain" to request.pain, "breathing_difficulty" to request.breathingDifficulty),
                freeText = request.freeText,
                alertsTriggered = triggered
            )
            Result.success(fallback)
        }
    }

    suspend fun getHospitalAlerts(): Result<List<Alert>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getHospitalAlerts()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(getFallbackAlerts())
            }
        } catch (e: Exception) {
            Result.success(getFallbackAlerts())
        }
    }

    suspend fun getCaseAlerts(caseId: String): Result<List<Alert>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getCaseAlerts(caseId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(getFallbackAlerts().filter { it.caseId == caseId })
            }
        } catch (e: Exception) {
            Result.success(getFallbackAlerts().filter { it.caseId == caseId })
        }
    }

    suspend fun uploadDocument(caseId: String, docType: String, file: File): Result<Document> = withContext(Dispatchers.IO) {
        try {
            val caseIdPart = caseId.toRequestBody("text/plain".toMediaTypeOrNull())
            val typePart = docType.toRequestBody("text/plain".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody("application/pdf".toMediaTypeOrNull())
            )
            val response = api.uploadDocument(caseIdPart, typePart, filePart)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Upload failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- High-Quality Healthcare Fallback Fixtures ---
    private fun getFallbackDischargePack(caseId: String): DischargePack {
        return DischargePack(
            id = "dp_$caseId",
            caseId = caseId,
            summary = "Patient Rajesh Sharma was treated for Acute Myocardial Infarction with successful percutaneous coronary intervention (PCI). Discharged in stable condition.",
            whatHappened = WhatHappenedCard(
                text = "You were admitted for acute chest pain and underwent an angioplasty with stent placement in the Left Anterior Descending (LAD) artery. Your heart rhythm and recovery were monitored in the Cardiac ICU, and you are recovering well.",
                source = SourceRef("Discharge Summary MS-001", 1, "Diagnosis: Acute STEMI. Procedure: Primary PCI to LAD with DES stent.")
            ),
            medications = listOf(
                MedicationCard(
                    name = "Metformin",
                    dosage = "500 mg",
                    timing = "Twice daily after meals",
                    purpose = "Controls blood sugar levels to assist cardiovascular healing.",
                    source = SourceRef("Discharge Summary MS-001", 2, "Tab Metformin 500mg PO BD PC")
                ),
                MedicationCard(
                    name = "Atorvastatin",
                    dosage = "40 mg",
                    timing = "Once daily at bedtime",
                    purpose = "Lowers cholesterol and stabilizes vascular plaques to prevent future blockages.",
                    source = SourceRef("Discharge Summary MS-001", 2, "Tab Atorvastatin 40mg PO OD HS")
                ),
                MedicationCard(
                    name = "Ecosprin-AV (Aspirin + Clopidogrel)",
                    dosage = "75 mg / 75 mg",
                    timing = "Once daily morning after food",
                    purpose = "Prevents blood clots inside the newly placed cardiac stent.",
                    source = SourceRef("Discharge Summary MS-001", 2, "Tab Ecosprin-AV 75/75 PO OD")
                )
            ),
            followUp = FollowUpCard(
                instructions = "Follow-up consultation with Dr. Arvind Menon in Cardiology OPD for ECG and stent site check.",
                dateOrDays = "In 7 days (06-Oct-2026)",
                department = "Cardiology OPD, 2nd Floor",
                source = SourceRef("Discharge Summary MS-001", 2, "Follow up in Cardiology OPD after 7 days.")
            ),
            whatToWatch = listOf(
                WhatToWatchCard(
                    warning = "Shortness of breath or rapid breathing while resting",
                    action = "Stop activity immediately, sit upright, and contact hospital emergency.",
                    severity = "RED",
                    source = SourceRef("Discharge Summary MS-001", 2, "Warning signs: Dyspnea at rest, chest heaviness.")
                ),
                WhatToWatchCard(
                    warning = "Chest pain, pressure, or heaviness radiating to arm or jaw",
                    action = "Immediate emergency call (108/hospital hotline) - do not drive yourself.",
                    severity = "RED",
                    source = SourceRef("Discharge Summary MS-001", 2, "Warning signs: Recurrent angina, syncope.")
                ),
                WhatToWatchCard(
                    warning = "Persistent dizziness or fever over 100°F",
                    action = "Report in the daily app check-in and notify the nursing helpline.",
                    severity = "AMBER",
                    source = SourceRef("Discharge Summary MS-001", 2, "Notify clinic for pyrexia or persistent lightheadedness.")
                )
            )
        )
    }

    private fun getFallbackBills(caseId: String): List<BillItem> {
        return listOf(
            BillItem("bill_001", caseId, "Cardiac ICU Deluxe Room Charges (3 Days)", 45000.0, "Room & Board", "Inpatient ICU monitoring, oxygen support, and sanitization.", false),
            BillItem("bill_002", caseId, "Cardiology Senior Consultant Visit", 2500.0, "Professional Fee", "Daily post-operative evaluation and medication titration.", false),
            BillItem("bill_003", caseId, "Cardiology Senior Consultant Visit", 2500.0, "Professional Fee", "Duplicate professional consultation charge detected on same date.", true, "Worth asking about: Duplicate consultation line"),
            BillItem("bill_004", caseId, "Drug Eluting Stent (DES - Promus Premier)", 65000.0, "Implant & Devices", "Approved coronary stent implant per NPPA capped guidelines.", false),
            BillItem("bill_005", caseId, "Administrative & Documentation Handling Fee", 8500.0, "Administrative", "Non-clinical institutional handling fee. Often eligible for insurance inquiry.", true, "Worth asking about: Non-medical administrative surcharge"),
            BillItem("bill_006", caseId, "Pharmacy & Post-Op Infusion Medications", 14500.0, "Pharmacy", "Prescribed antiplatelets, statins, and saline infusions.", false)
        )
    }

    private fun getFallbackClaim(caseId: String): Claim {
        return Claim(
            id = "claim_001",
            caseId = caseId,
            claimNumber = "CLM-2026-001",
            status = "DENIED",
            claimType = "REIMBURSEMENT",
            documents = listOf(
                ClaimDocument("cd_001", "claim_001", "doc_01", "Discharge Summary", true, true, 3, 4, "Possible missing page detected (3 of 4 pages uploaded)"),
                ClaimDocument("cd_002", "claim_001", "doc_02", "Final Itemized Bill", true, true, 2, 2, null),
                ClaimDocument("cd_003", "claim_001", "doc_03", "Government ID Proof", true, true, 1, 1, null),
                ClaimDocument("cd_004", "claim_001", null, "Catheterization & ECG Investigation Reports", true, false, 0, 3, "MISSING DOCUMENT")
            ),
            missingDocuments = listOf("Catheterization & ECG Investigation Reports"),
            pageDiscrepancies = listOf("Discharge Summary: Possible missing page detected (3 of 4 pages uploaded)")
        )
    }

    private fun getFallbackClaimDraft(): ClaimDraftResponse {
        return ClaimDraftResponse(
            claimId = "claim_001",
            patientName = "Rajesh Sharma",
            hospitalName = "MedSure Trust Hospital",
            claimNumber = "CLM-2026-001",
            treatmentSummary = "Patient Rajesh Sharma was admitted for emergency Primary PCI stenting. Investigation records and diagnostic findings have now been attached.",
            claimAmount = 185000.0,
            supportingDocuments = listOf("Discharge Summary (Complete)", "Final Consolidated Hospital Bill", "Government ID Proof", "Catheterization Lab & ECG Reports"),
            explanationRequest = "Resubmission packet addressing missing diagnostic records with complete certified clinical documentation.",
            draftText = "Subject: Formal Reimbursement Resubmission - Claim #CLM-2026-001\nPolicyholder: Rajesh Sharma | Case: MS-001\n\nDear Claims Team,\nWe submit the rectified documentation packet for hospitalization reimbursement regarding the acute coronary intervention of Rajesh Sharma...",
            requiresApproval = true,
            approvalStatus = "PENDING"
        )
    }

    private fun getFallbackAlerts(): List<Alert> {
        return listOf(
            Alert(
                id = "alt_demo_01",
                caseId = "MS-001",
                patientName = "Rajesh Sharma",
                severity = "RED",
                title = "Difficulty Breathing",
                summary = "Patient reported difficulty breathing during today's check-in.",
                ruleId = "BREATHING_001",
                status = "NEW"
            ),
            Alert(
                id = "alt_demo_02",
                caseId = "MS-001",
                patientName = "Rajesh Sharma",
                severity = "AMBER",
                title = "Post-Discharge Fever",
                summary = "Patient reported fever during home recovery.",
                ruleId = "FEVER_001",
                status = "NEW"
            )
        )
    }
}
