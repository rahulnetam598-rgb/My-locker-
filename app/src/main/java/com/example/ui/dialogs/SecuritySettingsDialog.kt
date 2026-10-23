package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LockerBlueLight
import com.example.ui.theme.LockerBluePrimary

@Composable
fun SecuritySettingsDialog(
    isLockEnabled: Boolean,
    currentPin: String,
    onDismiss: () -> Unit,
    onToggleLock: (Boolean) -> Unit,
    onChangePin: (String) -> Unit
) {
    var lockActive by remember { mutableStateOf(isLockEnabled) }
    var pinValue by remember { mutableStateOf(currentPin) }
    var pinError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(LockerBlueLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = LockerBluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "App Lock Settings",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Toggle row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable 4-Digit Passcode Lock",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Requires PIN entry to view stored documents",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = lockActive,
                        onCheckedChange = { lockActive = it },
                        modifier = Modifier.testTag("app_lock_switch")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (lockActive) {
                    OutlinedTextField(
                        value = pinValue,
                        onValueChange = { input ->
                            if (input.length <= 4 && input.all { it.isDigit() }) {
                                pinValue = input
                                pinError = false
                            }
                        },
                        label = { Text("Set 4-Digit PIN") },
                        placeholder = { Text("1234") },
                        singleLine = true,
                        isError = pinError,
                        supportingText = if (pinError) {
                            { Text("PIN must be exactly 4 digits", color = MaterialTheme.colorScheme.error) }
                        } else {
                            { Text("Default testing PIN is 1234") }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("security_pin_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Required Disclosure Box (Requirement 7)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFEF3C7))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Security Status Notice",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "This lock screen serves as an interface access gate. Full cryptographic Android Keystore encryption and biometric authentication are under development and not yet finalized or claimed.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = Color(0xFF78350F)
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (lockActive && pinValue.length != 4) {
                        pinError = true
                    } else {
                        onToggleLock(lockActive)
                        if (lockActive) {
                            onChangePin(pinValue)
                        }
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LockerBluePrimary),
                modifier = Modifier.testTag("save_security_settings_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
