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

// Import ให้ถูกต้อง
import com.brawlhouse.familyguard.viewmodel.MainViewModel
import com.brawlhouse.familyguard.ui.Screen
import com.brawlhouse.familyguard.ui.screens.* // Import หน้าจอทั้งหมด
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
                    // เรียกใช้ ViewModel
                    val viewModel: MainViewModel = viewModel()

                    // สังเกตสถานะการเปลี่ยนหน้า
                    val currentScreen by viewModel.currentScreen.collectAsState()
                    val showRiskDialog by viewModel.showRiskDialog.collectAsState()

                    when (val screen = currentScreen) {
                        is Screen.Login -> {
                            LoginScreen(
                                viewModel = viewModel,
                                // แก้ให้ไปหน้า Register ก่อน (ถ้าต้องการ) หรือไป RoleSelection ตาม flow ของคุณ
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
                                onCreateFamilyClick = { viewModel.navigateTo(Screen.Dashboard) },
                                onJoinFamilyClick = { viewModel.navigateTo(Screen.Dashboard) }
                            )
                        }
                        is Screen.Dashboard -> {
                            DashboardScreen(
                                onSettingsClick = { viewModel.navigateTo(Screen.Survey) },
                                onAddMemberClick = { viewModel.navigateTo(Screen.Invite) },
                                onMemberClick = { member ->
                                    if (member.status == MemberStatus.Check) {
                                        viewModel.showRiskDialog()
                                    } else {
                                        viewModel.navigateTo(Screen.RiskResult(RiskType.LowRisk))
                                    }
                                }
                            )

                            // แสดง Dialog ถ้า showRiskDialog เป็น true
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