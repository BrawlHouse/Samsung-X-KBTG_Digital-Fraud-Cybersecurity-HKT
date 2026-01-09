package com.brawlhouse.familyguard.ui.screens

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brawlhouse.familyguard.ui.theme.Danger
import com.brawlhouse.familyguard.ui.theme.FamilyGuardTheme
import com.brawlhouse.familyguard.ui.theme.Primary
import com.brawlhouse.familyguard.ui.theme.Warning
import com.brawlhouse.familyguard.viewmodel.MainViewModel

enum class RiskType {
        LowRisk,
        HighRisk,
        ApprovalRequired
}

@Composable
fun RiskResultScreen(
        type: RiskType,
        onCloseClick: () -> Unit = {},
        viewModel: MainViewModel = viewModel()
) {
        if (type == RiskType.ApprovalRequired) {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                        while (true) {
                                kotlinx.coroutines.delay(3000)
                                viewModel.checkUserStatus()
                        }
                }
        }
        Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

                        // Background Gradient for HighRisk
                        if (type == RiskType.HighRisk) {
                                Box(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .height(300.dp)
                                                        .background(
                                                                brush =
                                                                        Brush.verticalGradient(
                                                                                colors =
                                                                                        listOf(
                                                                                                Danger.copy(
                                                                                                        alpha =
                                                                                                                0.1f
                                                                                                ),
                                                                                                Color.Transparent
                                                                                        )
                                                                        )
                                                        )
                                )
                        }

                        Column(
                                modifier = Modifier.fillMaxSize().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                                // Top Title (varying)
                                val title =
                                        when (type) {
                                                RiskType.LowRisk -> "Risk Assessment"
                                                RiskType.HighRisk ->
                                                        "" // No top title in HTML, gradient covers
                                                // it
                                                RiskType.ApprovalRequired -> "Family Approval"
                                        }

                                if (title.isNotEmpty()) {
                                        Text(
                                                title,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onBackground
                                        )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                // Center Content
                                when (type) {
                                        RiskType.LowRisk -> LowRiskContent()
                                        RiskType.HighRisk -> HighRiskContent()
                                        RiskType.ApprovalRequired -> ApprovalRequiredContent()
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                // Footer Actions
                                if (type == RiskType.ApprovalRequired) {
                                        Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(bottom = 24.dp)
                                        ) {
                                                // Ping animation
                                                val infiniteTransition =
                                                        rememberInfiniteTransition(label = "ping")
                                                val scale by
                                                        infiniteTransition.animateFloat(
                                                                initialValue = 1f,
                                                                targetValue = 1.5f,
                                                                animationSpec =
                                                                        infiniteRepeatable(
                                                                                tween(1000)
                                                                        ),
                                                                label = "scale"
                                                        )
                                                val alpha by
                                                        infiniteTransition.animateFloat(
                                                                initialValue = 0.75f,
                                                                targetValue = 0f,
                                                                animationSpec =
                                                                        infiniteRepeatable(
                                                                                tween(1000)
                                                                        ),
                                                                label = "alpha"
                                                        )

                                                Box(contentAlignment = Alignment.Center) {
                                                        Box(
                                                                modifier =
                                                                        Modifier.size(12.dp)
                                                                                .scale(scale)
                                                                                .background(
                                                                                        Warning.copy(
                                                                                                alpha =
                                                                                                        alpha
                                                                                        ),
                                                                                        CircleShape
                                                                                )
                                                        )
                                                        Box(
                                                                modifier =
                                                                        Modifier.size(12.dp)
                                                                                .background(
                                                                                        Warning,
                                                                                        CircleShape
                                                                                )
                                                        )
                                                }

                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                        "Waiting for response...",
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onBackground.copy(
                                                                        alpha = 0.6f
                                                                ),
                                                        fontWeight = FontWeight.Medium
                                                )
                                        }
                                }

                                Button(
                                        onClick = onCloseClick,
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        containerColor =
                                                                if (type == RiskType.HighRisk ||
                                                                                type ==
                                                                                        RiskType.ApprovalRequired
                                                                )
                                                                        MaterialTheme.colorScheme
                                                                                .surfaceVariant
                                                                else
                                                                        Color(
                                                                                0xFFF0F2F4
                                                                        ), // Light gray from HTML
                                                        contentColor =
                                                                MaterialTheme.colorScheme.onSurface
                                                )
                                ) {
                                        Text(
                                                if (type == RiskType.ApprovalRequired)
                                                        "Exit Application"
                                                else "Close",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                        )
                                }
                        }
                }
        }
}

@Composable
fun LowRiskContent() {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.BottomCenter) {
                        Box(
                                modifier =
                                        Modifier.size(192.dp)
                                                .clip(CircleShape)
                                                .border(
                                                        4.dp,
                                                        Color(0xFFF0F2F4),
                                                        CircleShape
                                                ), // Outer ring
                                contentAlignment = Alignment.Center
                        ) {
                                Box(
                                        modifier =
                                                Modifier.size(160.dp)
                                                        .background(
                                                                Primary.copy(alpha = 0.1f),
                                                                CircleShape
                                                        ),
                                        contentAlignment = Alignment.Center
                                ) {
                                        Icon(
                                                Icons.Outlined.Shield,
                                                contentDescription = null,
                                                modifier = Modifier.size(72.dp),
                                                tint = Primary
                                        )
                                }
                        }
                        // Badge
                        Surface(
                                color = Primary,
                                shape = RoundedCornerShape(16.dp),
                                border =
                                        androidx.compose.foundation.BorderStroke(
                                                4.dp,
                                                MaterialTheme.colorScheme.background
                                        ),
                                modifier = Modifier.offset(y = 12.dp)
                        ) {
                                Text(
                                        "SAFE",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier =
                                                Modifier.padding(
                                                        horizontal = 16.dp,
                                                        vertical = 4.dp
                                                )
                                )
                        }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                        "Low Risk",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                        "This situation appears safe based on your provided information.",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(300.dp)
                )
        }
}

@Composable
fun HighRiskContent() {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                        modifier =
                                Modifier.size(120.dp)
                                        .background(Danger.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                ) {
                        Icon(
                                Icons.Filled.Warning, // report_problem
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Danger
                        )
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text(
                        "This situation appears risky.",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                        "We recommend contacting your family for confirmation.",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(280.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))
                Surface(
                        shape = RoundedCornerShape(24.dp),
                        border =
                                androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        Color.LightGray.copy(alpha = 0.5f)
                                ),
                        color = Color.Transparent
                ) {
                        Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Icon(
                                        Icons.Outlined.Shield,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                        "Safety Check Active",
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                )
                        }
                }
        }
}

@Composable
fun ApprovalRequiredContent() {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.BottomCenter) {
                        Box(
                                modifier =
                                        Modifier.size(192.dp)
                                                .clip(CircleShape)
                                                .border(4.dp, Color(0xFFF0F2F4), CircleShape),
                                contentAlignment = Alignment.Center
                        ) {
                                Box(
                                        modifier =
                                                Modifier.size(160.dp)
                                                        .background(
                                                                Warning.copy(alpha = 0.1f),
                                                                CircleShape
                                                        ),
                                        contentAlignment = Alignment.Center
                                ) {
                                        Icon(
                                                Icons.Filled.HourglassTop,
                                                contentDescription = null,
                                                modifier = Modifier.size(72.dp),
                                                tint = Warning
                                        )
                                }
                        }
                        Surface(
                                color = Warning,
                                shape = RoundedCornerShape(16.dp),
                                border =
                                        androidx.compose.foundation.BorderStroke(
                                                4.dp,
                                                MaterialTheme.colorScheme.background
                                        ),
                                modifier = Modifier.offset(y = 12.dp)
                        ) {
                                Text(
                                        "WAITING",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier =
                                                Modifier.padding(
                                                        horizontal = 16.dp,
                                                        vertical = 4.dp
                                                )
                                )
                        }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text(
                        "Approval Required",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                        "A potential risk was detected. This action is paused and cannot proceed until a family member reviews and approves it.",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(300.dp)
                )
        }
}

@Preview(showBackground = true)
@Composable
fun LowRiskPreview() {
        FamilyGuardTheme { RiskResultScreen(RiskType.LowRisk) }
}

@Preview(showBackground = true)
@Composable
fun HighRiskPreview() {
        FamilyGuardTheme { RiskResultScreen(RiskType.HighRisk) }
}

@Preview(showBackground = true)
@Composable
fun ApprovalPreview() {
        FamilyGuardTheme { RiskResultScreen(RiskType.ApprovalRequired) }
}
