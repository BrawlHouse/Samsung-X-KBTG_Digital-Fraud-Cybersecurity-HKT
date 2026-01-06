package com.brawlhouse.familyguard.ui

import androidx.lifecycle.ViewModel
import com.brawlhouse.familyguard.ui.screens.RiskType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class Screen {
    object Login : Screen()
    object RoleSelection : Screen()
    object Dashboard : Screen()
    object Invite : Screen()
    object Survey : Screen()
    data class RiskResult(val type: RiskType) : Screen()
}

class MainViewModel : ViewModel() {
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Login)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _showRiskDialog = MutableStateFlow(false)
    val showRiskDialog: StateFlow<Boolean> = _showRiskDialog.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun showRiskDialog() {
        _showRiskDialog.value = true
    }

    fun dismissRiskDialog() {
        _showRiskDialog.value = false
    }
}
