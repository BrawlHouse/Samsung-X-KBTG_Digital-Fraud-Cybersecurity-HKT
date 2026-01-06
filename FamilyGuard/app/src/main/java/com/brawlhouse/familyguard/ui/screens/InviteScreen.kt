package com.brawlhouse.familyguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brawlhouse.familyguard.ui.theme.FamilyGuardTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteScreen(
    onCreateFamilyClick: () -> Unit = {},
    onJoinFamilyClick: (String) -> Unit = {}
) {
    var inviteCode by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Family Setup",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            // Hero Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text("Illustration Placeholder", color = Color.Gray)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Let's Get Connected",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "To start protecting your loved ones, create a new family circle or join an existing one.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.width(320.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onCreateFamilyClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Outlined.AddCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Create a New Family", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    "OR JOIN WITH CODE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(32.dp))
            
            // Join Family Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Have an invite code?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inviteCode,
                        onValueChange = { inviteCode = it.uppercase() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { 
                            Text(
                                "EX: A38-9Z2", 
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) 
                        },
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center, 
                            fontSize = 18.sp, 
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        trailingIcon = {
                            Icon(Icons.Outlined.Key, contentDescription = null, tint = Color.Gray)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = MaterialTheme.colorScheme.background
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onJoinFamilyClick(inviteCode) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.background, // Grayish per design?
                            // Actually design has gray bg button
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Transparent) // Adjust if need border
                    ) {
                         // Design says bg-gray-100 text-gray-900.
                         // My theme background is gray-ish (F6F7F8).
                         // Let's force a slightly darker gray for button
                         Text("Join Family", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Your family is protected with end-to-end encryption",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview
@Composable
fun InviteScreenPreview() {
    FamilyGuardTheme {
        InviteScreen()
    }
}
