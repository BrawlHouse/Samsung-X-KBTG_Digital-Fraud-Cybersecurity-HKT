package com.brawlhouse.familyguard.data

import com.google.gson.annotations.SerializedName

// --- Enum ---
enum class MemberStatus {
    @SerializedName("approved") Approved,
    @SerializedName("pending") Check,
    @SerializedName("rejected") Rejected
}

// --- Requests ---
data class JoinFamilyRequest(
    @SerializedName("invite_code") val inviteCode: String
)

// [เพิ่มตัวนี้ครับ] ที่ Error เพราะขาดตัวนี้
data class RespondTransactionRequest(
    @SerializedName("transaction_id") val transactionId: Int,
    val action: String
)

data class AnalyzeRequest(
    val answers: List<String>,
    val amount: Double,
    val destination: String
)

data class UpdateTokenRequest(
    val token: String
)

// --- Responses ---
data class CreateFamilyResponse(
    val message: String,
    @SerializedName("invite_code") val inviteCode: String
)

data class GetMembersResponse(
    @SerializedName("invite_code") val inviteCode: String,
    val members: List<FamilyMember>
)

data class GeneralResponse(
    val message: String
)

data class AnalyzeResponse(
    val message: String,
    @SerializedName("ai_result") val aiResult: AiResult?,
    val transaction: TransactionData?
)

// --- Data Models ---
data class FamilyMember(
    @SerializedName("user_id") val userId: Int,
    val nickname: String,
    val role: String,
    val email: String,
    @SerializedName("status") val status: MemberStatus? = null
)

data class AiResult(
    @SerializedName("risk_score") val riskScore: Int,
    val level: String,
    val reasons: List<String>
)

data class TransactionData(
    @SerializedName("transaction_id") val transactionId: Int,
    val status: String
)