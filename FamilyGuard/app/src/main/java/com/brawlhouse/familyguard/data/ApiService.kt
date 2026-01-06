package com.brawlhouse.familyguard.data

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

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

// --- Interface ---
interface ApiService {
    @POST("users/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // เพิ่มฟังก์ชัน Register
    @POST("users/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
}