package com.brawlhouse.familyguard.ui // ต้อง package นี้
import com.brawlhouse.familyguard.ui.screens.RiskType

sealed class Screen {
    object Login : Screen()
    object Register : Screen()
    object RoleSelection : Screen()
    object Invite : Screen()
    object Dashboard : Screen()
    object Survey : Screen()
    data class RiskResult(val type: RiskType) : Screen()
    data class Approval(val transactionId: Int, val score: String, val reason: String) : Screen()
}