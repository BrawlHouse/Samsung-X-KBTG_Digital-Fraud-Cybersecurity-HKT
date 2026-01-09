package com.brawlhouse.familyguard.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brawlhouse.familyguard.ui.theme.FamilyGuardTheme
import com.brawlhouse.familyguard.ui.theme.Primary

@Composable
fun SafetySurveyScreen(onCloseClick: () -> Unit = {}, onSubmit: (Map<String, Any>) -> Unit = {}) {
        var currentStep by remember { mutableIntStateOf(1) }
        val totalSteps = 5

        // State for answers
        var answer1 by remember { mutableStateOf("") } // Who
        var answer2 by remember { mutableStateOf("") } // Relationship
        var answer3 by remember { mutableStateOf("") } // Profession

        // Step 4: Radio Button
        var answer4 by remember { mutableStateOf("") } // Action requested
        var answer4OtherText by remember { mutableStateOf("") }

        var answer5 by remember { mutableStateOf("") } // Urgency

        // Validation Check
        val isNextEnabled =
                when (currentStep) {
                        1 -> answer1.isNotBlank()
                        2 -> answer2.isNotBlank()
                        3 -> answer3.isNotBlank()
                        4 -> {
                                if (answer4 == "อื่น ๆ") answer4OtherText.isNotBlank()
                                else answer4.isNotBlank()
                        }
                        5 -> answer5.isNotBlank()
                        else -> false
                }

        Scaffold(
                topBar = {
                        Column {
                                Row(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .padding(
                                                                horizontal = 16.dp,
                                                                vertical = 12.dp
                                                        ),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                        Icons.Outlined.Shield,
                                                        contentDescription = null,
                                                        tint = Color(0xFFEA580C), // Orange-600
                                                        modifier = Modifier.size(28.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                        "SAFETY CHECK",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onBackground
                                                )
                                        }
                                        IconButton(
                                                onClick = onCloseClick,
                                                modifier =
                                                        Modifier.size(32.dp)
                                                                .background(
                                                                        MaterialTheme.colorScheme
                                                                                .onBackground.copy(
                                                                                alpha = 0.05f
                                                                        ),
                                                                        CircleShape
                                                                )
                                        ) {
                                                Icon(
                                                        Icons.Default.Close,
                                                        contentDescription = "Close",
                                                        modifier = Modifier.size(20.dp),
                                                        tint =
                                                                MaterialTheme.colorScheme
                                                                        .onBackground.copy(
                                                                        alpha = 0.6f
                                                                )
                                                )
                                        }
                                }

                                // Progress Bar
                                Row(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .padding(
                                                                horizontal = 24.dp,
                                                                vertical = 8.dp
                                                        ),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                        repeat(totalSteps) { index ->
                                                val step = index + 1
                                                Box(
                                                        modifier =
                                                                Modifier.weight(1f)
                                                                        .height(6.dp)
                                                                        .clip(
                                                                                RoundedCornerShape(
                                                                                        3.dp
                                                                                )
                                                                        )
                                                                        .background(
                                                                                if (step <=
                                                                                                currentStep
                                                                                )
                                                                                        Primary
                                                                                else
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .onBackground
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                0.1f
                                                                                                )
                                                                        )
                                                )
                                        }
                                }

                                Row(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .padding(horizontal = 24.dp)
                                                        .padding(bottom = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                        Text(
                                                "PROGRESS",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color =
                                                        MaterialTheme.colorScheme.onBackground.copy(
                                                                alpha = 0.4f
                                                        ),
                                                letterSpacing = 1.sp
                                        )
                                        Text(
                                                "STEP $currentStep/$totalSteps",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color =
                                                        MaterialTheme.colorScheme.onBackground.copy(
                                                                alpha = 0.4f
                                                        ),
                                                letterSpacing = 1.sp
                                        )
                                }
                                HorizontalDivider(
                                        color =
                                                MaterialTheme.colorScheme.onBackground.copy(
                                                        alpha = 0.05f
                                                )
                                )
                        }
                },
                bottomBar = {
                        Column(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.background)
                                                .border(
                                                        width = 1.dp,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onBackground.copy(
                                                                        alpha = 0.05f
                                                                )
                                                )
                                                .padding(24.dp)
                        ) {
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                        if (currentStep > 1) {
                                                OutlinedButton(
                                                        onClick = { currentStep-- },
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier.weight(1f),
                                                        colors =
                                                                ButtonDefaults.outlinedButtonColors(
                                                                        contentColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onBackground
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.7f
                                                                                        )
                                                                )
                                                ) {
                                                        Icon(
                                                                Icons.AutoMirrored.Filled.ArrowBack,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(Modifier.width(8.dp))
                                                        Text("Back")
                                                }
                                                Spacer(Modifier.width(16.dp))
                                        }

                                        Button(
                                                onClick = {
                                                        if (currentStep < totalSteps) {
                                                                currentStep++
                                                        } else {
                                                                // Submit
                                                                val result =
                                                                        mapOf(
                                                                                "q1" to answer1,
                                                                                "q2" to answer2,
                                                                                "q3" to answer3,
                                                                                "q4" to
                                                                                        if (answer4 ==
                                                                                                        "อื่น ๆ"
                                                                                        )
                                                                                                answer4OtherText
                                                                                        else
                                                                                                answer4,
                                                                                "q5" to answer5
                                                                        )
                                                                android.util.Log.d(
                                                                        "SURVEY_SUBMIT",
                                                                        "Answers: $result"
                                                                )
                                                                onSubmit(result)
                                                        }
                                                },
                                                enabled = isNextEnabled, // Control here
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.weight(1f),
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                containerColor = Primary,
                                                                disabledContainerColor =
                                                                        Primary.copy(alpha = 0.5f)
                                                        )
                                        ) {
                                                Text(
                                                        if (currentStep == totalSteps) "Finish"
                                                        else "Next"
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Icon(
                                                        Icons.AutoMirrored.Filled.ArrowForward,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                )
                                        }
                                }
                        }
                },
                containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(paddingValues)
                                        .verticalScroll(rememberScrollState())
                                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                        when (currentStep) {
                                1 -> Step1(answer1) { answer1 = it }
                                2 -> Step2(answer2) { answer2 = it }
                                3 -> Step3(answer3) { answer3 = it }
                                4 ->
                                        Step4(
                                                answer4,
                                                answer4OtherText,
                                                { answer4 = it },
                                                { answer4OtherText = it }
                                        )
                                5 -> Step5(answer5) { answer5 = it }
                        }
                }
        }
}

// --- Step Components ---

@Composable
fun Step1(answer: String, onAnswerChange: (String) -> Unit) {
        StepHeader(
                title = "ผู้ที่ติดต่อมาหาคุณคือใคร",
                subtitle = "ระบุชื่อ หรือหมายเลขโทรศัพท์ที่ติดต่อเข้ามา"
        )
        OutlinedTextField(
                value = answer,
                onValueChange = onAnswerChange,
                label = { Text("ชื่อ หรือ เบอร์โทรศัพท์") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
        )
}

@Composable
fun Step2(answer: String, onAnswerChange: (String) -> Unit) {
        StepHeader(
                title = "ความสัมพันธ์ระหว่างคุณกับผู้ติดต่อ",
                subtitle = "คุณรู้จักคนที่ติดต่อมาหรือไม่"
        )
        val options = listOf("รู้จักเป็นการส่วนตัว", "ไม่แน่ใจ", "ไม่รู้จัก")
        RadioGroup(options, answer, onAnswerChange)
}

@Composable
fun Step3(answer: String, onAnswerChange: (String) -> Unit) {
        StepHeader(
                title = "ผู้ติดต่อแจ้งว่าประกอบอาชีพอะไร",
                subtitle = "เช่น ตำรวจ, เจ้าหน้าที่ธนาคาร, พนักงานส่งของ"
        )
        OutlinedTextField(
                value = answer,
                onValueChange = onAnswerChange,
                label = { Text("ระบุอาชีพหรือหน่วยงาน") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
        )
}

@Composable
fun Step4(
        selectedOption: String,
        otherText: String,
        onOptionChange: (String) -> Unit,
        onOtherChange: (String) -> Unit
) {
        StepHeader(
                title = "ผู้ติดต่อร้องขอให้คุณดำเนินการใด",
                subtitle = "เลือก 1 ข้อที่สำคัญที่สุด"
        )
        val options = listOf("โอนเงิน", "ให้รหัสหรือข้อมูลส่วนตัว", "ติดตั้งแอปพลิเคชัน", "อื่น ๆ")

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                options.forEach { option ->
                        val isSelected = option == selectedOption
                        val isOther = option == "อื่น ๆ"

                        Surface(
                                shape = RoundedCornerShape(12.dp),
                                border =
                                        androidx.compose.foundation.BorderStroke(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color =
                                                        if (isSelected) Primary
                                                        else
                                                                MaterialTheme.colorScheme
                                                                        .onBackground.copy(
                                                                        alpha = 0.2f
                                                                )
                                        ),
                                color =
                                        if (isSelected) Primary.copy(alpha = 0.05f)
                                        else MaterialTheme.colorScheme.surface,
                                modifier =
                                        Modifier.fillMaxWidth().clickable { onOptionChange(option) }
                        ) {
                                Column {
                                        Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                RadioButton(
                                                        selected = isSelected,
                                                        onClick = null,
                                                        colors =
                                                                RadioButtonDefaults.colors(
                                                                        selectedColor = Primary
                                                                )
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                        text = option,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight =
                                                                if (isSelected) FontWeight.SemiBold
                                                                else FontWeight.Normal
                                                )
                                        }
                                        if (isOther && isSelected) {
                                                OutlinedTextField(
                                                        value = otherText,
                                                        onValueChange = onOtherChange,
                                                        placeholder = { Text("โปรดระบุ...") },
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(horizontal = 16.dp)
                                                                        .padding(bottom = 16.dp),
                                                        shape = RoundedCornerShape(8.dp)
                                                )
                                        }
                                }
                        }
                }
        }
}

@Composable
fun Step5(answer: String, onAnswerChange: (String) -> Unit) {
        StepHeader(
                title = "ผู้ติดต่อมีการสร้างแรงกดดันหรือไม่",
                subtitle = "เช่น เร่งรีบ, ข่มขู่, หรือทำให้ตกใจ"
        )
        val options = listOf("มี", "ไม่มี")
        RadioGroup(options, answer, onAnswerChange)
}

// --- Helper Components ---

@Composable
fun StepHeader(title: String, subtitle: String) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        lineHeight = 24.sp
                )
        }
}

@Composable
fun RadioGroup(options: List<String>, selected: String, onSelect: (String) -> Unit) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                options.forEach { option ->
                        val isSelected = option == selected
                        Surface(
                                shape = RoundedCornerShape(12.dp),
                                border =
                                        androidx.compose.foundation.BorderStroke(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color =
                                                        if (isSelected) Primary
                                                        else
                                                                MaterialTheme.colorScheme
                                                                        .onBackground.copy(
                                                                        alpha = 0.2f
                                                                )
                                        ),
                                color =
                                        if (isSelected) Primary.copy(alpha = 0.05f)
                                        else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth().clickable { onSelect(option) }
                        ) {
                                Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        RadioButton(
                                                selected = isSelected,
                                                onClick = null,
                                                colors =
                                                        RadioButtonDefaults.colors(
                                                                selectedColor = Primary
                                                        )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                                text = option,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight =
                                                        if (isSelected) FontWeight.SemiBold
                                                        else FontWeight.Normal
                                        )
                                }
                        }
                }
        }
}

@Preview
@Composable
fun SafetySurveyPreview() {
        FamilyGuardTheme { SafetySurveyScreen() }
}
