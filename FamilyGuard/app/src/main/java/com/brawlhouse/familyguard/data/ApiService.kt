package com.brawlhouse.familyguard.data

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// --- Existing Login Models (ของเดิม) ---
data class LoginRequest(val email: String, val password: String)

data class LoginResponse(
        val message: String,
        val token: String?,
        val user: UserData?,
        val error: String?
)

data class UserData(
        @SerializedName("user_id") val userId: Int,
        val nickname: String,
        val role: String,
        val email: String
)

// --- NEW: Register Models (เพิ่มใหม่) ---
data class RegisterRequest(
        val email: String,
        val password: String,
        val nickname: String,
        val role: String, // "parent" or "child"
        @SerializedName("device_id") val deviceId: String,
        @SerializedName("bank_account_number") val bankAccountNumber: String
)

data class RegisterResponse(
        val message: String,
        val user: UserData?, // Reuse UserData
        val error: String?
)

data class UserStatusResponse(
        @SerializedName("user_id") val userId: Int?,
        val nickname: String?,
        val role: String?,
        @SerializedName("fcm_token") val fcmToken: String?,
        val email: String?,
        @SerializedName("device_id") val deviceId: String?,
        @SerializedName("bank_account_number") val bankAccountNumber: String?,
        @SerializedName("family_id") val familyId: Int?,
        val status: String?,
        @SerializedName("createdAt") val createdAt: String?,
        @SerializedName("updatedAt") val updatedAt: String?
)

// Request ส่งข้อความไป
data class AnalyzeTextRequest(
        // เปลี่ยนเป็น "text" ให้ตรงกับที่ Backend (Node.js) รอรับ
        @SerializedName("text") val text: String
)

// Response ที่รับจาก AI (ตรงกับ JSON ที่ Backend ส่งมา)
data class AnalyzeTextResponse(
        val percentage: Int, // เปลี่ยนจาก risk_score
        val level: String,
        val reason: String, // เปลี่ยนจาก reasons (singular)
        @SerializedName("is_risk") val isRisk: Boolean
)
// --- Interface ---
interface ApiService {
        @POST("users/login") suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

        // เพิ่มฟังก์ชัน Register
        @POST("users/register")
        suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

        @POST("family/create") suspend fun createFamily(): Response<CreateFamilyResponse>

        @POST("family/join")
        suspend fun joinFamily(@Body request: JoinFamilyRequest): Response<GeneralResponse>

        @GET("family/members") suspend fun getFamilyMembers(): Response<GetMembersResponse>

        @DELETE("family/members/{user_id}")
        suspend fun removeMember(@Path("user_id") userId: Int): Response<GeneralResponse>

        @POST("risk/analyze")
        suspend fun analyzeRisk(@Body request: AnalyzeRequest): Response<AnalyzeResponse>
        @POST("risk/respond")
        suspend fun respondToTransaction(
                @Body request: RespondTransactionRequest
        ): Response<GeneralResponse>
        // อัปเดต FCM Token เพื่อให้ Backend รู้ว่าเครื่องไหนคือใคร
        @POST("users/fcm-token")
        suspend fun updateFcmToken(@Body request: UpdateTokenRequest): Response<GeneralResponse>

        @GET("users/{user_id}")
        suspend fun getUserStatus(@Path("user_id") userId: Int): Response<UserStatusResponse>

        @POST("analyze/message") // เปลี่ยนจาก risk/message เป็น analyze/message
        suspend fun analyzeText(@Body request: AnalyzeTextRequest): Response<AnalyzeTextResponse>
}
