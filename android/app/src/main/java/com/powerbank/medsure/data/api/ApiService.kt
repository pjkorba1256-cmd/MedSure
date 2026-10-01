package com.powerbank.medsure.data.api

import com.powerbank.medsure.data.model.*
import com.powerbank.medsure.data.model.Query
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.Query as QueryParam

interface ApiService {

    // Auth
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getMe(): Response<User>

    // Cases
    @GET("cases")
    suspend fun getCases(): Response<List<Case>>

    @GET("cases/{case_id}")
    suspend fun getCase(@Path("case_id") caseId: String): Response<Case>

    // Discharge Pack
    @GET("cases/{case_id}/discharge")
    suspend fun getDischargePack(@Path("case_id") caseId: String): Response<DischargePack>

    @POST("cases/{case_id}/discharge/process")
    suspend fun processDischarge(
        @Path("case_id") caseId: String,
        @QueryParam("document_id") documentId: String? = null
    ): Response<DischargePack>

    // Bills & Fees
    @GET("cases/{case_id}/bills")
    suspend fun getBills(@Path("case_id") caseId: String): Response<List<BillItem>>

    // Queries
    @GET("cases/{case_id}/queries")
    suspend fun getQueries(@Path("case_id") caseId: String): Response<List<Query>>

    @POST("cases/{case_id}/queries")
    suspend fun createQuery(
        @Path("case_id") caseId: String,
        @Body request: QueryCreate
    ): Response<Query>

    @POST("queries/{query_id}/reply")
    suspend fun replyToQuery(
        @Path("query_id") queryId: String,
        @Body request: QueryReply
    ): Response<Query>

    // Insurance & Claims
    @GET("cases/{case_id}/claim")
    suspend fun getClaim(@Path("case_id") caseId: String): Response<Claim>

    @GET("cases/{case_id}/claim/checklist")
    suspend fun getClaimChecklist(@Path("case_id") caseId: String): Response<List<ClaimDocument>>

    @POST("cases/{case_id}/claim/draft")
    suspend fun generateClaimDraft(@Path("case_id") caseId: String): Response<ClaimDraftResponse>

    @POST("cases/{case_id}/claim/approve")
    suspend fun approveClaimDraft(@Path("case_id") caseId: String): Response<Map<String, Any>>

    // Check-In
    @GET("cases/{case_id}/checkin/questions")
    suspend fun getCheckInQuestions(@Path("case_id") caseId: String): Response<List<CheckInQuestion>>

    @POST("cases/{case_id}/checkin")
    suspend fun submitCheckIn(
        @Path("case_id") caseId: String,
        @Body request: CheckInRequest
    ): Response<CheckInResponse>

    // Alerts
    @GET("cases/{case_id}/alerts")
    suspend fun getCaseAlerts(@Path("case_id") caseId: String): Response<List<Alert>>

    @GET("hospital/alerts")
    suspend fun getHospitalAlerts(): Response<List<Alert>>

    // Approvals
    @GET("approvals")
    suspend fun getApprovals(): Response<List<Approval>>

    @POST("approvals/{approval_id}/approve")
    suspend fun approveAction(@Path("approval_id") approvalId: String): Response<Approval>

    // Documents
    @Multipart
    @POST("documents/upload")
    suspend fun uploadDocument(
        @Part("case_id") caseId: RequestBody,
        @Part("document_type") documentType: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<Document>

    // Notifications
    @GET("notifications/poll")
    suspend fun pollNotifications(): Response<List<NotificationItem>>
}
