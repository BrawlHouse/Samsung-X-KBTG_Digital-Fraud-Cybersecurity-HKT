package com.brawlhouse.familyguard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import android.provider.Settings
import android.net.Uri
// import android.content.Intent // ลบ import ซ้ำออก
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brawlhouse.familyguard.data.MemberStatus
import com.brawlhouse.familyguard.ui.Screen
import com.brawlhouse.familyguard.ui.screens.*
import com.brawlhouse.familyguard.ui.screens.RiskType
import com.brawlhouse.familyguard.ui.theme.FamilyGuardTheme
import com.brawlhouse.familyguard.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. เช็คและขอ Notification Permission
        askNotificationPermission()
        
        // 2. เช็คและขอสิทธิ์ Draw Overlays (Popup)
        checkOverlayPermission() 

        enableEdgeToEdge()

        setContent {
            // ... (Content เดิมของคุณ) ...
            FamilyGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: MainViewModel = viewModel()
                    val currentScreen by viewModel.currentScreen.collectAsState()
                    val showRiskDialog by viewModel.showRiskDialog.collectAsState()

                    LaunchedEffect(intent) {
                        val navToSurvey = intent.getStringExtra("NAVIGATE_TO") == "SURVEY"
                        val isRiskAlert = intent.getStringExtra("action") == "risk_alert"

                        when {
                            navToSurvey -> {
                                viewModel.navigateTo(Screen.Survey)
                                intent.removeExtra("NAVIGATE_TO")
                            }
                            isRiskAlert -> {
                                val txId = intent.getStringExtra("transaction_id")?.toIntOrNull() ?: 0
                                val score = intent.getStringExtra("risk_score") ?: "0"
                                val reason = intent.getStringExtra("reasons") ?: "ไม่ระบุ"

                                viewModel.navigateTo(Screen.Approval(txId, score, reason))

                                intent.removeExtra("action")
                                intent.removeExtra("transaction_id")
                                intent.removeExtra("risk_score")
                                intent.removeExtra("reasons")
                            }
                        }
                    }

                    when (val screen = currentScreen) {
                        is Screen.Login -> LoginScreen(
                            viewModel = viewModel,
                            onCreateAccountClick = { viewModel.navigateTo(Screen.Register) }
                        )
                        is Screen.Register -> RegisterScreen(
                            viewModel = viewModel,
                            onBackClick = { viewModel.navigateTo(Screen.Login) }
                        )
                        is Screen.RoleSelection -> RoleSelectionScreen(
                            onBackClick = { viewModel.navigateTo(Screen.Login) },
                            onContinueClick = { viewModel.navigateTo(Screen.Invite) }
                        )
                        is Screen.Invite -> InviteScreen(
                            viewModel = viewModel,
                            onBackClick = { viewModel.navigateTo(Screen.Login) }
                        )
                        is Screen.Dashboard -> {
                            DashboardScreen(
                                viewModel = viewModel,
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
                        is Screen.Survey -> SafetySurveyScreen(
                            onCloseClick = { viewModel.navigateTo(Screen.Dashboard) },
                            onSubmit = { answersMap -> viewModel.submitSurvey(answersMap) }
                        )
                        is Screen.RiskResult -> RiskResultScreen(
                            type = screen.type,
                            onCloseClick = { viewModel.navigateTo(Screen.Dashboard) }
                        )
                        is Screen.Approval -> RiskApprovalScreen(
                            viewModel = viewModel,
                            transactionId = screen.transactionId,
                            riskScore = screen.score,
                            reason = screen.reason
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) { // แก้ไข onNewIntent ให้ถูกต้อง (ลบเครื่องหมาย ? ออกถ้า override ผิด)
        super.onNewIntent(intent)
        setIntent(intent)
    }

    // ฟังก์ชันตรวจสอบสิทธิ์ Overlay
    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }
    }
}