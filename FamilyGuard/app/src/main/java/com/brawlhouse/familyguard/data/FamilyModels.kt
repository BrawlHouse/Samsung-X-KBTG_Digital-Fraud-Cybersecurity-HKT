package com.brawlhouse.familyguard.data

import com.google.gson.annotations.SerializedName

// <--- เพิ่ม enum class MemberStatus ตรงนี้ ---
enum class MemberStatus {
    @SerializedName("approved") Approved,
    @SerializedName("pending")   Check,     // ใช้ "pending" เพราะ backend มักส่งแบบนี้
    @SerializedName("rejected") Rejected
}

// Request
data class JoinFamilyRequest(
    @SerializedName("invite_code") val inviteCode: String
)

// Response
data class CreateFamilyResponse(
    val message: String,
    @SerializedName("invite_code") val inviteCode: String
)

data class GetMembersResponse(
    @SerializedName("invite_code") val inviteCode: String,
    val members: List<FamilyMember>
)

// Model สมาชิกที่มาจาก API
data class FamilyMember(
    @SerializedName("user_id") val userId: Int,
    val nickname: String,
    val role: String,
    val email: String,
    @SerializedName("status") val status: MemberStatus? = null
)

data class GeneralResponse(
    val message: String
)