package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LockerBlueLight
import com.example.ui.theme.LockerBluePrimary

@Composable
fun LockScreen(
    pinInput: String,
    errorMessage: String?,
    onDigitEntered: (String) -> Unit,
    onBackspace: () -> Unit,
    onSkipLock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFD))
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .testTag("lock_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Lock Emblem
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(LockerBlueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock",
                    tint = LockerBluePrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "MyLocker Protected",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = LockerBluePrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Enter 4-digit PIN to access your documents",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4 PIN Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val isFilled = index < pinInput.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (isFilled) LockerBluePrimary else Color.Transparent)
                            .border(
                                width = 2.dp,
                                color = if (isFilled) LockerBluePrimary else Color(0xFFCBD5E1),
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Error Message
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            } else {
                Spacer(modifier = Modifier.height(18.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Numeric Keypad
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    KeypadButton("1", onClick = { onDigitEntered("1") })
                    KeypadButton("2", onClick = { onDigitEntered("2") })
                    KeypadButton("3", onClick = { onDigitEntered("3") })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    KeypadButton("4", onClick = { onDigitEntered("4") })
                    KeypadButton("5", onClick = { onDigitEntered("5") })
                    KeypadButton("6", onClick = { onDigitEntered("6") })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    KeypadButton("7", onClick = { onDigitEntered("7") })
                    KeypadButton("8", onClick = { onDigitEntered("8") })
                    KeypadButton("9", onClick = { onDigitEntered("9") })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    // Quick default fill button
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .clickable {
                                onDigitEntered("1")
                                onDigitEntered("2")
                                onDigitEntered("3")
                                onDigitEntered("4")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "1234",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = LockerBluePrimary
                        )
                    }

                    KeypadButton("0", onClick = { onDigitEntered("0") })

                    // Backspace
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onBackspace)
                            .testTag("keypad_backspace"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Backspace",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Testing / Quick Unlock affordance
            OutlinedButton(
                onClick = onSkipLock,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LockerBluePrimary),
                modifier = Modifier.testTag("skip_lock_button")
            ) {
                Text("Bypass / Skip Lock (Preview Mode)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Honest Security Disclosure (Requirement 7)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFEF3C7))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFB45309),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Local PIN gate. Cryptographic Keystore security undergoing testing.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = Color(0xFF92400E)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    digit: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), CircleShape)
            .clickable(onClick = onClick)
            .testTag("keypad_$digit"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = Color(0xFF0F172A)
        )
    }
}
