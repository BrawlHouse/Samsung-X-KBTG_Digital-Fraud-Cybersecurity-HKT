package com.brawlhouse.familyguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brawlhouse.familyguard.ui.theme.Danger
import com.brawlhouse.familyguard.ui.theme.Primary
import com.brawlhouse.familyguard.ui.theme.Success

enum class MemberStatusOption(
        val title: String,
        val description: String,
        val color: Color,
        val icon: ImageVector
) {
    Allow("Allow", "Safe & trusted", Success, Icons.Filled.CheckCircle),
    Normal("Normal", "Standard monitoring", Primary, Icons.Filled.Shield),
    Reject("Reject", "Block & report", Danger, Icons.Filled.Block)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeFamilyMemberStatusPopup(
        userId: Int,
        memberName: String,
        currentStatus: MemberStatusOption = MemberStatusOption.Normal,
        onDismiss: () -> Unit,
        onStatusSelected: (Int, MemberStatusOption) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = {
                Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                            modifier =
                                    Modifier.width(48.dp)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                            alpha = 0.2f
                                                    )
                                            )
                    )
                }
            }
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            // Header
            Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                            text = "Change Status",
                            style =
                                    MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold
                                    ),
                            color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(
                                text = "Select new status for ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                                text = memberName,
                                style =
                                        MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                        ),
                                color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                IconButton(
                        onClick = onDismiss,
                        modifier =
                                Modifier.background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        CircleShape
                                )
                ) {
                    Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Options
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                MemberStatusOption.values().forEach { option ->
                    StatusOptionItem(
                            option = option,
                            isSelected = option == currentStatus,
                            onClick = { onStatusSelected(userId, option) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cancel Button
            TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                            ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
            ) { Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun StatusOptionItem(option: MemberStatusOption, isSelected: Boolean, onClick: () -> Unit) {
    val containerColor =
            if (isSelected) {
                option.color.copy(alpha = 0.05f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            }

    val borderColor = if (isSelected) option.color.copy(alpha = 0.3f) else Color.Transparent

    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(containerColor)
                            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
                            .clickable(onClick = onClick)
                            .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icon Container
            Box(
                    modifier =
                            Modifier.size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.background)
                                    .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant.copy(
                                                    alpha = 0.2f
                                            ),
                                            CircleShape
                                    ),
                    contentAlignment = Alignment.Center
            ) {
                Icon(
                        imageVector = option.icon,
                        contentDescription = null,
                        tint = option.color,
                        modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                        text = option.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                        text = option.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Radio Button
        if (isSelected) {
            Box(
                    modifier =
                            Modifier.size(32.dp)
                                    .background(option.color.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
            ) {
                Icon(
                        Icons.Filled.CheckCircle,
                        null,
                        tint = option.color,
                        modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Icon(
                    Icons.Filled.RadioButtonUnchecked,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(24.dp)
            )
        }
    }
}
