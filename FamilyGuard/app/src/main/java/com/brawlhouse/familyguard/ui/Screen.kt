package com.brawlhouse.familyguard.ui // ต้อง package นี้

sealed class Screen {
    object Login : Screen()
    object Register : Screen()
    object RoleSelection : Screen()
    object Invite : Screen()
    object Dashboard : Screen()
    object Survey : Screen()
    data class RiskResult(val type: com.brawlhouse.familyguard.ui.screens.RiskType) : Screen()
}