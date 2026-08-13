package com.obrago.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.obrago.app.ui.common.ErrorBanner
import com.obrago.app.ui.common.ObragoButton
import com.obrago.app.ui.common.ObragoInput

@Composable
fun OtpDialog(
    state: AuthUiState,
    onOtpChange: (String) -> Unit,
    onVerify: () -> Unit,
    onDismiss: () -> Unit,
    onResend: (() -> Unit)? = null
) {
    if (!state.showOtpModal) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Verify Phone Number", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = "A 6-digit OTP code has been sent via SMS to:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.fullPhoneNumber,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                ObragoInput(
                    label = "6-Digit OTP Code",
                    value = state.userOtpInput,
                    onValueChange = { input ->
                        if (input.length <= 6 && input.all { it.isDigit() }) {
                            onOtpChange(input)
                        }
                    },
                    icon = Icons.Default.Key,
                    placeholder = "123456",
                    keyboardType = KeyboardType.Number
                )

                ErrorBanner(state.otpError)

                // Fallback / Test helper notice if Firebase SMS didn't send or is in fallback mode
                if (state.verificationId == null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "🔑 Test / Local OTP Code: ${state.fallbackCode}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Firebase SMS is in test mode. Tap below to auto-fill the test code.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            TextButton(
                                onClick = { onOtpChange(state.fallbackCode) },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Auto-Fill Code")
                            }
                        }
                    }
                }

                if (onResend != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onResend,
                        enabled = !state.loadingOtp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Resend SMS OTP")
                    }
                }
            }
        },
        confirmButton = {
            ObragoButton(
                text = if (state.loadingOtp) "Verifying..." else "Verify OTP",
                fullWidth = false,
                loading = state.loadingOtp,
                enabled = state.userOtpInput.length == 6 && !state.loadingOtp,
                onClick = onVerify
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

