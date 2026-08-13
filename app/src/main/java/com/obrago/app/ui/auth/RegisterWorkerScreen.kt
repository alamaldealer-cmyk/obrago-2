package com.obrago.app.ui.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.obrago.app.R
import com.obrago.app.data.model.Role
import com.obrago.app.ui.common.DataPrivacyCard
import com.obrago.app.ui.common.ErrorBanner
import com.obrago.app.ui.common.ObragoButton
import com.obrago.app.ui.common.ObragoInput
import com.obrago.app.ui.common.ObragoTopBar
import com.obrago.app.ui.common.RoleToggleSignup
import com.obrago.app.ui.common.SectionHeaderTitle
import com.obrago.app.util.ImageUtils

private val DarkCanvasBg = Color(0xFF030B12)
private val NeonGreen = Color(0xFF00E676)
private val DarkGreenBtnBg = Color(0xFF00C853)

@Composable
fun RegisterWorkerScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onGoLogin: () -> Unit = onBack
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

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
            ObragoTopBar(
                title = if (state.step == 2) "ID Verification (${state.step}/2)" else "",
                onBack = { if (state.step == 2) viewModel.goToWorkerStep1() else onBack() }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Green Pin Badge Logo
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(NeonGreen, DarkGreenBtnBg)
                            )
                        )
                        .shadow(16.dp, CircleShape, spotColor = NeonGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Engineering,
                        contentDescription = "Obrago Logo",
                        tint = Color(0xFF021B11),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Headline Title
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                            append("Create Worker ")
                        }
                        withStyle(SpanStyle(color = NeonGreen, fontWeight = FontWeight.ExtraBold)) {
                            append("Account")
                        }
                    },
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color(0xFF94A3B8))) {
                            append("Join ")
                        }
                        withStyle(SpanStyle(color = NeonGreen, fontWeight = FontWeight.Bold)) {
                            append("Obrago")
                        }
                        withStyle(SpanStyle(color = Color(0xFF94A3B8))) {
                            append(" and start earning today")
                        }
                    },
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Role Toggle
                RoleToggleSignup(
                    isWorkerSelected = state.role == Role.WORKER,
                    onCustomerSelected = { viewModel.setRole(Role.CUSTOMER) },
                    onWorkerSelected = { viewModel.setRole(Role.WORKER) }
                )

                ErrorBanner(state.errorMsg)

                if (state.step == 1) {
                    WorkerStep1(state, viewModel)
                } else {
                    WorkerStep2(state, viewModel)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Already have an account? ",
                        color = Color(0xFF94A3B8),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Sign In",
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable(onClick = onGoLogin)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

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
private fun WorkerStep1(state: AuthUiState, viewModel: AuthViewModel) {
    val context = LocalContext.current
    val profilePicLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.setProfilePic(ImageUtils.uriToBase64DataUrl(context, uri))
    }

    SectionHeaderTitle("Personal Information", modifier = Modifier.fillMaxWidth())

    // Full Name
    ObragoInput(
        label = "Full Name",
        value = state.name,
        onValueChange = viewModel::setName,
        icon = Icons.Default.Person,
        placeholder = "Full Name",
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Phone Number
    Column(modifier = Modifier.fillMaxWidth()) {
        ObragoInput(
            label = "Phone Number",
            value = state.phoneRaw,
            onValueChange = viewModel::setPhoneRaw,
            icon = Icons.Default.Phone,
            placeholder = "03001234567",
            keyboardType = KeyboardType.Phone,
            enabled = !state.isPhoneVerified,
            modifier = Modifier.fillMaxWidth()
        )

        // Phone OTP row status
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (state.isPhoneVerified) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Phone Verified ✓", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else if (state.phoneRaw.isNotBlank()) {
                Text(
                    text = if (state.loadingOtp) "Sending OTP..." else "Send OTP Code",
                    color = NeonGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        val activity = context as? android.app.Activity
                        if (activity != null) viewModel.sendOtp(activity)
                    }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Email Address
    ObragoInput(
        label = "Email Address",
        value = state.email,
        onValueChange = viewModel::setEmail,
        icon = Icons.Default.Email,
        placeholder = "Email Address",
        keyboardType = KeyboardType.Email,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Home Address
    ObragoInput(
        label = "Home Address",
        value = state.address,
        onValueChange = viewModel::setAddress,
        icon = Icons.Default.Home,
        placeholder = "House / Street / Area",
        modifier = Modifier.fillMaxWidth()
    )

    SectionHeaderTitle("Security", modifier = Modifier.fillMaxWidth())

    ObragoInput(
        label = "Password",
        value = state.password,
        onValueChange = viewModel::setPassword,
        icon = Icons.Default.Lock,
        isPassword = true,
        placeholder = "Password",
        keyboardType = KeyboardType.Password,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Profile photo upload button
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Profile Picture *", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedButton(
            onClick = { profilePicLauncher.launch("image/*") },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFF061823),
                contentColor = NeonGreen
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = NeonGreen)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (state.profilePicBase64 != null) "Profile Photo Selected ✓" else "Upload Profile Photo")
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    DataPrivacyCard()

    Spacer(modifier = Modifier.height(20.dp))

    ObragoButton(text = "Continue to Step 2") {
        viewModel.submitWorkerStep1()
    }
}

@Composable
private fun WorkerStep2(state: AuthUiState, viewModel: AuthViewModel) {
    val context = LocalContext.current
    val frontPicLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.setIdFrontPic(ImageUtils.uriToBase64DataUrl(context, uri))
    }
    val backPicLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.setIdBackPic(ImageUtils.uriToBase64DataUrl(context, uri))
    }

    SectionHeaderTitle("CNIC / ID Card Verification", modifier = Modifier.fillMaxWidth())

    ObragoInput(
        label = "CNIC / ID Card Number",
        value = state.idCard,
        onValueChange = viewModel::setIdCard,
        icon = Icons.Default.CreditCard,
        placeholder = "42101-1234567-1",
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(14.dp))

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("CNIC Front Picture *", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedButton(
            onClick = { frontPicLauncher.launch("image/*") },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFF061823),
                contentColor = NeonGreen
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Badge, contentDescription = null, tint = NeonGreen)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (state.idFrontPicBase64 != null) "Front Photo Selected ✓" else "Upload Front Side")
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("CNIC Back Picture *", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedButton(
            onClick = { backPicLauncher.launch("image/*") },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFF061823),
                contentColor = NeonGreen
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Badge, contentDescription = null, tint = NeonGreen)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (state.idBackPicBase64 != null) "Back Photo Selected ✓" else "Upload Back Side")
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    TermsCheckbox(checked = state.termsAgreed, onCheckedChange = viewModel::setTermsAgreed)

    Spacer(modifier = Modifier.height(16.dp))

    DataPrivacyCard()

    Spacer(modifier = Modifier.height(20.dp))

    ObragoButton(text = "Submit Registration", loading = state.isSubmitting) {
        viewModel.submitWorkerRegister()
    }
}
