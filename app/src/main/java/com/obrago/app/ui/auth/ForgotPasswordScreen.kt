package com.obrago.app.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.obrago.app.R
import com.obrago.app.ui.common.ErrorBanner
import com.obrago.app.ui.common.ObragoButton
import com.obrago.app.ui.common.ObragoInput
import com.obrago.app.ui.common.ObragoTopBar

private val DarkCanvasBg = Color(0xFF030B12)
private val NeonGreen = Color(0xFF00E676)

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.resetPasswordDone) {
        if (state.resetPasswordDone) {
            viewModel.consumeResetPasswordDone()
            onDone()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvasBg)
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_selection_screen),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(modifier = Modifier.fillMaxSize()) {
            ObragoTopBar(title = "Reset Password", onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = "Enter your registered phone number to reset your password.",
                    color = Color(0xFF8BA2B2),
                    fontSize = 13.sp
                )
                ErrorBanner(state.errorMsg)

                PhoneOtpRow(state = state, viewModel = viewModel)

                if (state.isPhoneVerified) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ObragoInput(
                        label = "New Password *", value = state.newPassword, onValueChange = viewModel::setNewPassword,
                        icon = Icons.Default.Lock, isPassword = true, placeholder = "••••••••"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ObragoInput(
                        label = "Confirm New Password *", value = state.confirmPassword, onValueChange = viewModel::setConfirmPassword,
                        icon = Icons.Default.Lock, isPassword = true, placeholder = "••••••••"
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    ObragoButton(text = "Change Password", loading = state.isSubmitting) {
                        viewModel.submitResetPassword()
                    }
                }
            }
        }
    }

    val context = LocalContext.current

    OtpDialog(
        state = state,
        onOtpChange = viewModel::setUserOtpInput,
        onVerify = viewModel::verifyOtp,
        onDismiss = viewModel::dismissOtpModal,
        onResend = {
            val activity = context as? android.app.Activity
            if (activity != null) viewModel.sendOtp(activity)
        }
    )
}

@Composable
private fun PhoneOtpRow(state: AuthUiState, viewModel: AuthViewModel) {
    val context = LocalContext.current
    Column(modifier = Modifier.padding(top = 12.dp)) {
        ObragoInput(
            label = "Mobile Phone Number *",
            value = state.phoneRaw,
            onValueChange = viewModel::setPhoneRaw,
            icon = Icons.Default.Phone,
            placeholder = "03001234567",
            keyboardType = KeyboardType.Phone,
            enabled = !state.isPhoneVerified
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (state.isPhoneVerified) {
            AssistChip(
                onClick = { viewModel.setPhoneRaw(state.phoneRaw) },
                label = { Text("Verified", color = NeonGreen) },
                leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen) },
                colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF061823))
            )
        } else {
            ObragoButton(
                text = if (state.loadingOtp) "Sending..." else "Send OTP Code",
                fullWidth = false,
                enabled = state.phoneRaw.isNotBlank() && !state.loadingOtp,
                onClick = {
                    val activity = context as? android.app.Activity
                    if (activity != null) viewModel.sendOtp(activity)
                }
            )
        }
    }
}
