package com.powerbank.medsure.data.model

import com.google.gson.annotations.SerializedName

// User & Auth
data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String? = null,
    val role: String,
    @SerializedName("created_at") val createdAt: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    val user: User
)

// Case
data class CaseMember(
    val id: String,
    @SerializedName("user_id") val userId: String,
    val role: String,
    @SerializedName("user_name") val userName: String? = null
)

data class Case(
    val id: String,
    @SerializedName("patient_id") val patientId: String,
    @SerializedName("patient_name") val patientName: String? = null,
    @SerializedName("hospital_name") val hospitalName: String,
    @SerializedName("admission_date") val admissionDate: String,
    @SerializedName("discharge_date") val dischargeDate: String? = null,
    val status: String,
    @SerializedName("created_at") val createdAt: String? = null,
    val members: List<CaseMember> = emptyList()
)

// Discharge Pack
data class SourceRef(
    @SerializedName("document_name") val documentName: String = "Discharge Summary",
    val page: Int = 1,
    val excerpt: String? = null
)

data class WhatHappenedCard(
    val text: String,
    val source: SourceRef
)

data class MedicationCard(
    val name: String,
    val dosage: String,
    val timing: String,
    val purpose: String,
    val source: SourceRef
)

data class FollowUpCard(
    val instructions: String,
    @SerializedName("date_or_days") val dateOrDays: String,
    val department: String,
    val source: SourceRef
)

data class WhatToWatchCard(
    val warning: String,
    val action: String,
    val severity: String = "AMBER",
    val source: SourceRef
)

data class DischargePack(
    val id: String,
    @SerializedName("case_id") val caseId: String,
    @SerializedName("source_document_id") val sourceDocumentId: String? = null,
    val summary: String,
    @SerializedName("what_happened") val whatHappened: WhatHappenedCard,
    val medications: List<MedicationCard> = emptyList(),
    @SerializedName("follow_up") val followUp: FollowUpCard,
    @SerializedName("what_to_watch") val whatToWatch: List<WhatToWatchCard> = emptyList(),
    @SerializedName("created_at") val createdAt: String? = null
)

// Bills & Fees
data class BillItem(
    val id: String,
    @SerializedName("case_id") val caseId: String,
    val description: String,
    val amount: Double,
    val category: String,
    val explanation: String? = null,
    val flagged: Boolean = false,
    @SerializedName("flag_reason") val flagReason: String? = null,
    @SerializedName("source_reference") val sourceReference: String? = "Hospital Bill Page 1"
)

// Queries
data class Query(
    val id: String,
    @SerializedName("case_id") val caseId: String,
    @SerializedName("bill_item_id") val billItemId: String? = null,
    @SerializedName("bill_item_description") val billItemDescription: String? = null,
    @SerializedName("bill_item_amount") val billItemAmount: Double? = null,
    @SerializedName("created_by") val createdBy: String,
    @SerializedName("created_by_name") val createdByName: String? = null,
    val message: String,
    val reply: String? = null,
    val status: String = "OPEN",
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("replied_at") val repliedAt: String? = null
)

data class QueryCreate(
    @SerializedName("bill_item_id") val billItemId: String? = null,
    val message: String
)

data class QueryReply(
    val reply: String
)

// Insurance & Claims
data class ClaimDocument(
    val id: String,
    @SerializedName("claim_id") val claimId: String,
    @SerializedName("document_id") val documentId: String? = null,
    @SerializedName("document_type") val documentType: String,
    val required: Boolean,
    val present: Boolean,
    @SerializedName("page_count") val pageCount: Int,
    @SerializedName("expected_page_count") val expectedPageCount: Int,
    @SerializedName("page_count_warning") val pageCountWarning: String? = null
)

data class Claim(
    val id: String,
    @SerializedName("case_id") val caseId: String,
    @SerializedName("claim_number") val claimNumber: String,
    val status: String,
    @SerializedName("claim_type") val claimType: String,
    @SerializedName("draft_text") val draftText: String? = null,
    @SerializedName("approved_at") val approvedAt: String? = null,
    @SerializedName("submitted_at") val submittedAt: String? = null,
    val documents: List<ClaimDocument> = emptyList(),
    @SerializedName("missing_documents") val missingDocuments: List<String> = emptyList(),
    @SerializedName("page_discrepancies") val pageDiscrepancies: List<String> = emptyList()
)

data class ClaimDraftResponse(
    @SerializedName("claim_id") val claimId: String,
    @SerializedName("patient_name") val patientName: String,
    @SerializedName("hospital_name") val hospitalName: String,
    @SerializedName("claim_number") val claimNumber: String,
    @SerializedName("treatment_summary") val treatmentSummary: String,
    @SerializedName("claim_amount") val claimAmount: Double,
    @SerializedName("supporting_documents") val supportingDocuments: List<String> = emptyList(),
    @SerializedName("explanation_request") val explanationRequest: String,
    @SerializedName("draft_text") val draftText: String,
    @SerializedName("requires_approval") val requiresApproval: Boolean = true,
    @SerializedName("approval_status") val approvalStatus: String = "PENDING"
)

// Approvals
data class Approval(
    val id: String,
    @SerializedName("case_id") val caseId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("action_type") val actionType: String,
    @SerializedName("target_id") val targetId: String,
    val approved: Boolean,
    val status: String,
    val timestamp: String? = null
)

// Check-In
data class CheckInQuestion(
    val id: String,
    val label: String,
    val type: String,
    val options: List<Any>? = null
)

data class CheckInRequest(
    val pain: Int,
    val fever: Boolean,
    @SerializedName("breathing_difficulty") val breathingDifficulty: Boolean,
    val dizziness: Boolean,
    val vomiting: Boolean,
    @SerializedName("free_text") val freeText: String? = null
)

data class CheckInResponse(
    val id: String,
    @SerializedName("case_id") val caseId: String,
    @SerializedName("patient_id") val patientId: String,
    @SerializedName("answers_json") val answersJson: Map<String, Any>,
    @SerializedName("free_text") val freeText: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("alerts_triggered") val alertsTriggered: List<String> = emptyList()
)

// Alerts
data class Alert(
    val id: String,
    @SerializedName("case_id") val caseId: String,
    @SerializedName("checkin_id") val checkinId: String? = null,
    @SerializedName("patient_name") val patientName: String? = null,
    val severity: String, // RED, AMBER
    val title: String,
    val summary: String,
    @SerializedName("rule_id") val ruleId: String,
    val status: String = "NEW",
    @SerializedName("created_at") val createdAt: String? = null
)

// Document
data class Document(
    val id: String,
    @SerializedName("case_id") val caseId: String,
    @SerializedName("uploaded_by") val uploadedBy: String,
    @SerializedName("document_type") val documentType: String,
    val filename: String,
    @SerializedName("storage_path") val storagePath: String,
    @SerializedName("ocr_confidence") val ocrConfidence: Double,
    @SerializedName("page_count") val pageCount: Int,
    @SerializedName("created_at") val createdAt: String? = null
)

// Notifications
data class NotificationItem(
    val id: String,
    @SerializedName("target_user_id") val targetUserId: String,
    val title: String,
    val body: String,
    @SerializedName("event_type") val eventType: String,
    val data: Map<String, Any> = emptyMap(),
    val timestamp: String,
    val read: Boolean = false
)
