package com.brawlhouse.familyguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brawlhouse.familyguard.viewmodel.MainViewModel

@Composable
fun RiskApprovalScreen(
        viewModel: MainViewModel,
        transactionId: Int, // รับ ID รายการที่จะอนุมัติ
        riskScore: String,
        reason: String
) {
    Column(
            modifier =
                    Modifier.fillMaxSize()
                            .background(Color(0xFFFFF0F0)) // พื้นหลังสีแดงอ่อน
                            .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
    ) {
        Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
                text = "🚨 เตือนภัยครอบครัว!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("คะแนนความเสี่ยง: $riskScore%", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("สาเหตุความเสี่ยง:", fontWeight = FontWeight.Bold)
                Text(reason, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ปุ่มระงับ (เด่นๆ)
        Button(
                onClick = { viewModel.respondToRisk(transactionId, isAllowed = false) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
        ) { Text("⛔ ระงับรายการ (BLOCK)", fontSize = 18.sp, fontWeight = FontWeight.Bold) }

        Spacer(modifier = Modifier.height(16.dp))

        // ปุ่มอนุญาต (รองลงมา)
        OutlinedButton(
                onClick = { viewModel.respondToRisk(transactionId, isAllowed = true) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
        ) { Text("อนุญาตให้ทำต่อ (ปลอดภัย)", color = Color.Gray) }
    }
}
