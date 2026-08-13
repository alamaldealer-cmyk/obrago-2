package com.obrago.app.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

private val DarkCanvasBg = Color(0xFF030B12)
private val NeonGreen = Color(0xFF00E676)
private val DarkGreenBtnBg = Color(0xFF00C853)

@Composable
fun RegisterCustomerScreen(
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
        // Wallpaper Background
        Image(
            painter = painterResource(id = R.drawable.bg_selection_screen),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar with translucent back button
            ObragoTopBar(title = "", onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Green Pin Badge with Hardhat Logo
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

                // Headline "Create Your Account"
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                            append("Create Your ")
                        }
                        withStyle(SpanStyle(color = NeonGreen, fontWeight = FontWeight.ExtraBold)) {
                            append("Account")
                        }
                    },
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Subtitle
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color(0xFF94A3B8))) {
                            append("Join ")
                        }
                        withStyle(SpanStyle(color = NeonGreen, fontWeight = FontWeight.Bold)) {
                            append("Obrago")
                        }
                        withStyle(SpanStyle(color = Color(0xFF94A3B8))) {
                            append(" and start earning or hiring today")
                        }
                    },
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Two-way Role Pill Toggle
                RoleToggleSignup(
                    isWorkerSelected = state.role == Role.WORKER,
                    onCustomerSelected = { viewModel.setRole(Role.CUSTOMER) },
                    onWorkerSelected = { viewModel.setRole(Role.WORKER) }
                )

                // Error Banner
                ErrorBanner(state.errorMsg)

                // --- SECTION 1: Personal Information ---
                SectionHeaderTitle("Personal Information", modifier = Modifier.align(Alignment.Start))

                // Full Name (Full Width)
                ObragoInput(
                    label = "Full Name",
                    value = state.name,
                    onValueChange = viewModel::setName,
                    icon = Icons.Default.Person,
                    placeholder = "Full Name",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Phone Number (Full Width)
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

                    // Phone OTP status row
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

                // Email Address (Full Width)
                ObragoInput(
                    label = "Email Address",
                    value = state.email,
                    onValueChange = viewModel::setEmail,
                    icon = Icons.Default.Email,
                    placeholder = "Email Address",
                    keyboardType = KeyboardType.Email,
                    modifier = Modifier.fillMaxWidth()
                )

                // --- SECTION 2: Security ---
                SectionHeaderTitle("Security", modifier = Modifier.align(Alignment.Start))

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

                Spacer(modifier = Modifier.height(12.dp))

                ObragoInput(
                    label = "Confirm Password",
                    value = state.confirmPassword,
                    onValueChange = viewModel::setConfirmPassword,
                    icon = Icons.Default.Lock,
                    isPassword = true,
                    placeholder = "Confirm Password",
                    keyboardType = KeyboardType.Password,
                    modifier = Modifier.fillMaxWidth()
                )

                // --- SECTION 3: Location (Optional) ---
                SectionHeaderTitle("Location (Optional)", modifier = Modifier.align(Alignment.Start))

                CityDropdownSelector(state = state, viewModel = viewModel)

                Spacer(modifier = Modifier.height(18.dp))

                // Data Privacy Badge
                DataPrivacyCard()

                Spacer(modifier = Modifier.height(14.dp))

                // Terms Checkbox
                TermsCheckbox(checked = state.termsAgreed, onCheckedChange = viewModel::setTermsAgreed)

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Button "Create Account"
                ObragoButton(
                    text = "Create Account",
                    loading = state.isSubmitting
                ) {
                    if (state.role == Role.WORKER) {
                        viewModel.submitWorkerStep1()
                    } else {
                        viewModel.submitCustomerRegister()
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Footer Link: "Already have an account? Sign In"
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
private fun CityDropdownSelector(state: AuthUiState, viewModel: AuthViewModel) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().clickable { expanded = true }) {
            ObragoInput(
                label = "",
                value = state.city,
                onValueChange = {},
                enabled = false,
                icon = Icons.Default.LocationOn,
                placeholder = "Select your city",
                trailingIcon = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = NeonGreen
                        )
                    }
                }
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color(0xFF061823))
                .fillMaxWidth(0.85f)
        ) {
            state.availableCities.forEach { city ->
                DropdownMenuItem(
                    text = { Text(city, color = Color.White) },
                    onClick = {
                        viewModel.setCity(city)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun TermsCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = NeonGreen,
                checkmarkColor = Color(0xFF022B1C),
                uncheckedColor = Color(0xFF5B7385)
            )
        )
        Text(
            text = "I agree to the Terms & Conditions and Privacy Policy",
            color = Color(0xFF8BA2B2),
            fontSize = 12.sp
        )
    }
}
