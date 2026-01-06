package com.brawlhouse.familyguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brawlhouse.familyguard.ui.MainViewModel
import com.brawlhouse.familyguard.ui.Screen
import com.brawlhouse.familyguard.ui.screens.DashboardScreen
import com.brawlhouse.familyguard.ui.screens.InviteScreen
import com.brawlhouse.familyguard.ui.screens.LoginScreen
import com.brawlhouse.familyguard.ui.screens.RiskAlertDialog
import com.brawlhouse.familyguard.ui.screens.RiskResultScreen
import com.brawlhouse.familyguard.ui.screens.RiskType
import com.brawlhouse.familyguard.ui.screens.RoleSelectionScreen
import com.brawlhouse.familyguard.ui.screens.SafetySurveyScreen
import com.brawlhouse.familyguard.ui.theme.FamilyGuardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FamilyGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: MainViewModel = viewModel()
                    val currentScreen by viewModel.currentScreen.collectAsState()
                    val showRiskDialog by viewModel.showRiskDialog.collectAsState()

                    when (val screen = currentScreen) {
                        is Screen.Login -> {
                            LoginScreen(
                                onLoginClick = { viewModel.navigateTo(Screen.Dashboard) },
                                onCreateAccountClick = { viewModel.navigateTo(Screen.RoleSelection) }
                            )
                        }
                        is Screen.RoleSelection -> {
                            RoleSelectionScreen(
                                onBackClick = { viewModel.navigateTo(Screen.Login) },
                                onContinueClick = { viewModel.navigateTo(Screen.Invite) }
                            )
                        }
                        is Screen.Invite -> {
                            InviteScreen(
                                onCreateFamilyClick = { viewModel.navigateTo(Screen.Dashboard) },
                                onJoinFamilyClick = { viewModel.navigateTo(Screen.Dashboard) }
                            )
                        }
                        is Screen.Dashboard -> {
                            DashboardScreen(
                                onSettingsClick = { viewModel.navigateTo(Screen.Survey) },
                                onAddMemberClick = { viewModel.navigateTo(Screen.Invite) },
                                onMemberClick = { member ->
                                    if (member.status == com.brawlhouse.familyguard.ui.screens.MemberStatus.Check) {
                                        viewModel.showRiskDialog()
                                    } else {
                                        viewModel.navigateTo(Screen.RiskResult(RiskType.LowRisk))
                                    }
                                }
                            )
                            // Risk Dialog Demo
                           // Triggering logic is not implemented, but if showRiskDialog is true:
                            if (showRiskDialog) {
                                RiskAlertDialog(
                                    onDismissRequest = { viewModel.dismissRiskDialog() },
                                    onRejectClick = { 
                                        viewModel.dismissRiskDialog() 
                                        viewModel.navigateTo(Screen.RiskResult(RiskType.HighRisk))
                                    },
                                    onAllowClick = {
                                        viewModel.dismissRiskDialog()
                                        viewModel.navigateTo(Screen.RiskResult(RiskType.LowRisk))
                                    }
                                )
                            }
                        }
                        is Screen.Survey -> {
                            SafetySurveyScreen(
                                onCloseClick = { viewModel.navigateTo(Screen.Dashboard) },
                                onOptionSelected = { 
                                    // Demo: Selecting an option shows risk dialog or result
                                    viewModel.navigateTo(Screen.RiskResult(RiskType.ApprovalRequired))
                                }
                            )
                        }
                        is Screen.RiskResult -> {
                            RiskResultScreen(
                                type = screen.type,
                                onCloseClick = { viewModel.navigateTo(Screen.Dashboard) }
                            )
                        }
                    }
                }
            }
        }
    }
}