package com.brawlhouse.familyguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.brawlhouse.familyguard.ui.theme.Danger
import com.brawlhouse.familyguard.ui.theme.FamilyGuardTheme

// Custom colors for this screen based on HTML design
private val DialogPrimary = Color(0xFFEE8C2B) // Orange
private val DialogBackground = Color(0xFFF8F7F6)

@Composable
fun RiskAlertDialog(
        nickname: String = "Unknown",
        messages: List<String> = emptyList(),
        transactionId: Int = 0,
        onDismissRequest: () -> Unit = {},
        onRespond: (Int, Boolean) -> Unit = { _, _ -> }
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
                shape = RoundedCornerShape(16.dp),
                colors =
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning Icon
                Box(
                        modifier =
                                Modifier.size(64.dp)
                                        .background(DialogPrimary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                ) {
                    Icon(
                            Icons.Filled.Warning,
                            contentDescription = "Warning",
                            tint = DialogPrimary,
                            modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                        "Potential Risk Detected",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                            color = DialogPrimary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp),
                            border =
                                    androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            DialogPrimary.copy(alpha = 0.2f)
                                    )
                    ) {
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = DialogPrimary,
                                    modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            // [Edit] Nickname
                            Text(
                                    nickname, // "FATHER" -> nickname
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DialogPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                            "is currently on a call",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                // Info Box
                Column(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .background(
                                                MaterialTheme.colorScheme.background,
                                                RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.onBackground.copy(
                                                        alpha = 0.1f
                                                ),
                                                RoundedCornerShape(12.dp)
                                        )
                                        .padding(16.dp)
                ) {
                    // Contact
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                                modifier =
                                        Modifier.size(32.dp)
                                                .background(
                                                        Color.LightGray.copy(alpha = 0.5f),
                                                        CircleShape
                                                ),
                                contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                    Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                    "Contact Name",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            // [Edit] messages[0] as Contact Name
                            val contactName =
                                    if (messages.isNotEmpty()) messages[0] else "Unknown Caller"
                            Text(
                                    contactName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                            )
                            // Removed Phone Number Text as requested
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Risk
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                                modifier =
                                        Modifier.size(32.dp)
                                                .background(Danger.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                    Icons.Filled.Security,
                                    contentDescription = null,
                                    tint = Danger,
                                    modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                    "Risk Assessment",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                    color = DialogPrimary.copy(alpha = 0.1f),
                                    border =
                                            androidx.compose.foundation.BorderStroke(
                                                    1.dp,
                                                    DialogPrimary.copy(alpha = 0.2f)
                                            ),
                                    // Left border only in HTML, but full border ok
                                    shape = RoundedCornerShape(4.dp)
                            ) {
                                // [Edit] Join all messages
                                val fullReason = messages.joinToString(" ")
                                Text(
                                        fullReason.ifBlank {
                                            "Caller claims to be a government officer demanding payment."
                                        },
                                        modifier = Modifier.padding(8.dp),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                        "Please confirm if this activity should be blocked to ensure safety.",
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                        onClick = { onRespond(transactionId, false) }, // REJECT = false
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Danger),
                        shape = RoundedCornerShape(8.dp)
                ) { Text("Reject", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                        "Allow",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier =
                                Modifier.clickable {
                                    onRespond(transactionId, true)
                                } // ALLOW = true
                )
            }
        }
    }
}

@Preview
@Composable
fun RiskAlertPreview() {
    FamilyGuardTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f))) {
            RiskAlertDialog()
        }
    }
}
