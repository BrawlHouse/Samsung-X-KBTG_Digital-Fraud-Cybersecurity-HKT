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
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brawlhouse.familyguard.data.MemberStatus
import com.brawlhouse.familyguard.ui.Screen
import com.brawlhouse.familyguard.ui.screens.*
import com.brawlhouse.familyguard.ui.theme.FamilyGuardTheme
import com.brawlhouse.familyguard.viewmodel.MainViewModel

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
                    // เรียกใช้ ViewModel
                    val viewModel: MainViewModel = viewModel()

                    // Handle incoming intents for navigation (e.g. from AccessibilityService)
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        if (intent.getStringExtra("NAVIGATE_TO") == "SURVEY") {
                            viewModel.navigateTo(Screen.Survey)
                            intent.removeExtra("NAVIGATE_TO")
                        }
                    }

                    // สังเกตสถานะการเปลี่ยนหน้า
                    val currentScreen by viewModel.currentScreen.collectAsState()
                    val showRiskDialog by viewModel.showRiskDialog.collectAsState()

                    when (val screen = currentScreen) {
                        is Screen.Login -> {
                            LoginScreen(
                                    viewModel = viewModel,
                                    // แก้ให้ไปหน้า Register ก่อน (ถ้าต้องการ) หรือไป RoleSelection
                                    // ตาม flow ของคุณ
                                    onCreateAccountClick = { viewModel.navigateTo(Screen.Register) }
                            )
                        }
                        // เพิ่มเคส Register ที่เราเพิ่งทำไป
                        is Screen.Register -> {
                            RegisterScreen(
                                    viewModel = viewModel,
                                    onBackClick = { viewModel.navigateTo(Screen.Login) }
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
                                    viewModel = viewModel,
                                    onBackClick = {
                                        viewModel.navigateTo(Screen.Login)
                                    } // หรือหน้าไหนที่ต้องการ
                            )
                        }
                        is Screen.Dashboard -> {
                            DashboardScreen(
                                    viewModel = viewModel,
                                    onSettingsClick = { viewModel.navigateTo(Screen.Survey) },
                                    onAddMemberClick = { viewModel.navigateTo(Screen.Invite) },
                                    onMemberClick = { member ->
                                        if (member.status == MemberStatus.Check) {
                                            viewModel.showRiskDialog()
                                        } else {
                                            viewModel.navigateTo(
                                                    Screen.RiskResult(RiskType.LowRisk)
                                            )
                                        }
                                    }
                            )

                            // แสดง Dialog ถ้า showRiskDialog เป็น true
                            if (showRiskDialog) {
                                RiskAlertDialog(
                                        onDismissRequest = { viewModel.dismissRiskDialog() },
                                        onRejectClick = {
                                            viewModel.dismissRiskDialog()
                                            viewModel.navigateTo(
                                                    Screen.RiskResult(RiskType.HighRisk)
                                            )
                                        },
                                        onAllowClick = {
                                            viewModel.dismissRiskDialog()
                                            viewModel.navigateTo(
                                                    Screen.RiskResult(RiskType.LowRisk)
                                            )
                                        }
                                )
                            }
                        }
                        is Screen.Survey -> {
                            SafetySurveyScreen(
                                    onCloseClick = { viewModel.navigateTo(Screen.Dashboard) },
                                    onSubmit = {
                                        viewModel.navigateTo(
                                                Screen.RiskResult(RiskType.ApprovalRequired)
                                        )
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
