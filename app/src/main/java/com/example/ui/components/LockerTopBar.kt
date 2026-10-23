package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LockerBlueLight
import com.example.ui.theme.LockerBluePrimary

@Composable
fun LockerTopBar(
    isLockActive: Boolean,
    hasSamples: Boolean,
    onSecurityClicked: () -> Unit,
    onDisclaimerClicked: () -> Unit,
    onRestoreSamplesClicked: () -> Unit,
    onClearSamplesClicked: () -> Unit,
    onLockNowClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Emblem
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(LockerBlueLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "MyLocker Shield",
                tint = LockerBluePrimary,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "MyLocker",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = LockerBluePrimary
                )
            }
            Text(
                text = "Personal Digital Document Wallet",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Lock Status Action Button
        IconButton(
            onClick = {
                if (isLockActive) {
                    onLockNowClicked()
                } else {
                    onSecurityClicked()
                }
            },
            modifier = Modifier.testTag("lock_action_button")
        ) {
            Icon(
                imageVector = if (isLockActive) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = if (isLockActive) "Lock App Now" else "App Lock Settings",
                tint = if (isLockActive) LockerBluePrimary else MaterialTheme.colorScheme.outline
            )
        }

        // Info / Disclaimer Button
        IconButton(
            onClick = onDisclaimerClicked,
            modifier = Modifier.testTag("info_action_button")
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "About MyLocker & Disclaimers",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Overflow Menu
        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.testTag("more_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("App Lock Settings") },
                    onClick = {
                        menuExpanded = false
                        onSecurityClicked()
                    }
                )
                if (hasSamples) {
                    DropdownMenuItem(
                        text = { Text("Remove Sample Documents") },
                        onClick = {
                            menuExpanded = false
                            onClearSamplesClicked()
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Restore Sample Documents") },
                    onClick = {
                        menuExpanded = false
                        onRestoreSamplesClicked()
                    }
                )
                DropdownMenuItem(
                    text = { Text("About & Non-Affiliation") },
                    onClick = {
                        menuExpanded = false
                        onDisclaimerClicked()
                    }
                )
            }
        }
    }
}
