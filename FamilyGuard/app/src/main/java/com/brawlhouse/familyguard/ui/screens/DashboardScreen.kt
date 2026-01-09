package com.brawlhouse.familyguard.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brawlhouse.familyguard.data.FamilyMember
import com.brawlhouse.familyguard.data.MemberStatus
import com.brawlhouse.familyguard.ui.theme.Success
import com.brawlhouse.familyguard.ui.theme.Warning
import com.brawlhouse.familyguard.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
        viewModel: MainViewModel,
        onSettingsClick: () -> Unit,
        onAddMemberClick: () -> Unit,
        onMemberClick: (FamilyMember) -> Unit
) {
    val members by viewModel.familyMembers.collectAsState()
    val myInviteCode = viewModel.myInviteCode
    val isLoading = viewModel.isLoading
    var errorMessage by remember { mutableStateOf(viewModel.errorMessage) }
    var selectedMember by remember { mutableStateOf<FamilyMember?>(null) }

    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.loadFamilyData() }

    // ใช้ Box ห่อทั้งหมดเพื่อให้ใช้ .align() ได้
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
                topBar = {
                    Row(
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .windowInsetsPadding(WindowInsets.statusBars)
                                            .background(MaterialTheme.colorScheme.background)
                                            .padding(horizontal = 24.dp, vertical = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Family Safety", fontSize = 24.sp, fontWeight = FontWeight.Bold)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                        "Invite Code: ${myInviteCode.ifBlank { "Loading..." }}",
                                        fontSize = 16.sp,
                                        color =
                                                MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.6f
                                                )
                                )
                                if (myInviteCode.isNotBlank()) {
                                    IconButton(
                                            onClick = {
                                                val clipboard =
                                                        context.getSystemService(
                                                                Context.CLIPBOARD_SERVICE
                                                        ) as
                                                                ClipboardManager
                                                val clip =
                                                        ClipData.newPlainText(
                                                                "Invite Code",
                                                                myInviteCode
                                                        )
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(
                                                                context,
                                                                "Copied!",
                                                                Toast.LENGTH_SHORT
                                                        )
                                                        .show()
                                            }
                                    ) {
                                        Icon(
                                                Icons.Filled.ContentCopy,
                                                "Copy code",
                                                tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                    Icons.Outlined.Settings,
                                    "Settings",
                                    tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // [เพิ่มใหม่] ปุ่ม Logout
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(
                                    imageVector =
                                            Icons.AutoMirrored.Filled.ExitToApp, // ไอคอนประตูทางออก
                                    contentDescription = "Logout",
                                    tint =
                                            MaterialTheme.colorScheme
                                                    .error // สีแดงให้รู้ว่าเป็นปุ่มออก
                            )
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
            ) {
                item {
                    Card(
                            colors =
                                    CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                    )
                    ) {
                        Row(
                                modifier = Modifier.padding(24.dp),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                    modifier =
                                            Modifier.size(64.dp)
                                                    .background(Success.copy(0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                        Icons.Outlined.Shield,
                                        null,
                                        tint = Success,
                                        modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(Modifier.width(20.dp))
                            Column {
                                Text(
                                        "System Active",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                )
                                Text(
                                        "Monitoring ${members.size} members",
                                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                            "Family Members",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                    )
                }

                items(members) { member ->
                    MemberCard(
                            member = member,
                            onClick = { selectedMember = member },
                            onDelete = { viewModel.removeMember(member.userId) }
                    )
                }

                item {
                    Box(
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .height(64.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(
                                                    2.dp,
                                                    MaterialTheme.colorScheme.outline.copy(
                                                            alpha = 0.2f
                                                    ),
                                                    RoundedCornerShape(12.dp)
                                            )
                                            .clickable { onAddMemberClick() },
                            contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                    Icons.Outlined.AddCircle,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(12.dp))
                            Text("Add Family Member", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Loading Indicator
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        // Error Message
        errorMessage?.let { msg ->
            Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    action = {
                        TextButton(
                                onClick = {
                                    errorMessage = null
                                    viewModel.errorMessage = null // clear ใน ViewModel ด้วย
                                }
                        ) { Text("Dismiss") }
                    }
            ) { Text(msg) }
        }

        // Status Popup
        selectedMember?.let { member ->
            ChangeFamilyMemberStatusPopup(
                    userId = member.userId,
                    memberName = member.nickname,
                    currentStatus =
                            when (member.status) {
                                MemberStatus.Approved -> MemberStatusOption.Allow
                                MemberStatus.Check -> MemberStatusOption.Normal
                                MemberStatus.Rejected -> MemberStatusOption.Reject
                                else -> MemberStatusOption.Normal
                            },
                    onDismiss = { selectedMember = null },
                    onStatusSelected = { userId, status ->
                        // TODO: Call viewModel to update status
                        selectedMember = null
                    }
            )
        }
    }
}

@Composable
fun MemberCard(member: FamilyMember, onClick: () -> Unit, onDelete: () -> Unit) {
    val isPending = member.status == MemberStatus.Check
    val statusColor = if (isPending) Warning else Success
    val statusText = if (isPending) "Pending" else "Safe"
    val statusIcon = if (isPending) Icons.Filled.Warning else Icons.Filled.CheckCircle

    Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border =
                    if (isPending) androidx.compose.foundation.BorderStroke(2.dp, Warning) else null
    ) {
        Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                        shape = CircleShape,
                        color = Color.LightGray,
                        modifier = Modifier.size(60.dp)
                ) {
                    Icon(
                            Icons.Filled.Person,
                            null,
                            tint = Color.White,
                            modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(member.nickname, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                            member.role.replaceFirstChar { it.uppercase() },
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(statusText, color = statusColor, fontSize = 13.sp)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
