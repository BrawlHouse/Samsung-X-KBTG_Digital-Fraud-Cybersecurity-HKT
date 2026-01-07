package com.brawlhouse.familyguard.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brawlhouse.familyguard.ui.theme.FamilyGuardTheme
import com.brawlhouse.familyguard.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit = {}
) {
    val isLoading by remember { derivedStateOf { viewModel.isLoading } }
    val errorMessage by remember { derivedStateOf { viewModel.errorMessage } }
    val inviteCode by remember { derivedStateOf { viewModel.myInviteCode } }
    val joinCodeInput = viewModel.joinCodeInput
    LaunchedEffect(Unit) {
        viewModel.loadFamilyData()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Family Setup", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Placeholder Illustration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text("Family Illustration", color = Color.Gray, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Let's Get Connected", fontSize = 28.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "To start protecting your loved ones, create a new family circle or join an existing one.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.9f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ปุ่มสร้าง Family
            Button(
                onClick = { viewModel.createFamily() },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(12.dp))
                }
                Icon(Icons.Outlined.AddCircle, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text("Create a New Family", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Divider(modifier = Modifier.weight(1f))
                Text(
                    " OR JOIN WITH CODE ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Divider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Card แสดง Invite Code + Join
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    
                    // 1. ถ้ามี Invite Code (เป็นคนสร้างบ้าน) ให้โชว์
                    // แต่ถ้าไม่มี (เป็นคนจะขอเข้า) ให้โชว์ช่องกรอก
                    
                    if (inviteCode.isNotBlank()) {
                        Text("Your Invite Code", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = inviteCode,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("Join an existing family", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // [เพิ่มช่องกรอก]
                        OutlinedTextField(
                            value = joinCodeInput,
                            onValueChange = { viewModel.onJoinCodeChange(it) },
                            label = { Text("Enter Invite Code") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ปุ่ม Join
                    Button(
                        // เปลี่ยนไปเรียกฟังก์ชัน joinFamily แบบไม่ต้องส่ง param (เพราะใช้ตัวแปรใน ViewModel)
                        onClick = { viewModel.joinFamily() }, 
                        // ปุ่มจะกดได้ก็ต่อเมื่อ "User พิมพ์รหัสแล้ว" (joinCodeInput ไม่ว่าง)
                        enabled = !isLoading && joinCodeInput.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary // เปลี่ยนสีให้เด่นหน่อย
                        )
                    ) {
                        if (isLoading) {
                             CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                             Text("Join Family", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // แสดง error message
            errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Your family is protected with end-to-end encryption",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview
@Composable
fun InviteScreenPreview() {
    FamilyGuardTheme {
        InviteScreen(viewModel = MainViewModel())
    }
}
