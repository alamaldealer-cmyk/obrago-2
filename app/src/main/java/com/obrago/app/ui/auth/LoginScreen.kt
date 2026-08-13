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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Lock
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
import com.obrago.app.ui.common.ErrorBanner
import com.obrago.app.ui.common.ObragoButton
import com.obrago.app.ui.common.ObragoInput
import com.obrago.app.ui.common.ObragoTopBar
import com.obrago.app.ui.common.RoleToggle

private val DarkCanvasBg = Color(0xFF030B12)
private val NeonGreen = Color(0xFF00E676)
private val DarkGreenBtnBg = Color(0xFF00C853)

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onGoRegister: () -> Unit,
    onGoForgotPassword: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvasBg)
    ) {
        // Base Dark Teal Wallpaper Background
        Image(
            painter = painterResource(id = R.drawable.bg_selection_screen),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Navigation Bar
            ObragoTopBar(title = "", onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
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

                // Headline Title "Welcome Back!"
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                            append("Welcome ")
                        }
                        withStyle(SpanStyle(color = NeonGreen, fontWeight = FontWeight.ExtraBold)) {
                            append("Back!")
                        }
                    },
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Subtitle
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color(0xFF94A3B8))) {
                            append("Start earning today with ")
                        }
                        withStyle(SpanStyle(color = NeonGreen, fontWeight = FontWeight.Bold)) {
                            append("Obrago")
                        }
                    },
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Two-way Role Toggle Tab
                RoleToggle(
                    isWorkerSelected = state.role == Role.WORKER,
                    onCustomerSelected = { viewModel.setRole(Role.CUSTOMER) },
                    onWorkerSelected = { viewModel.setRole(Role.WORKER) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Error Banner
                ErrorBanner(state.errorMsg)

                // Input 1: Email or Phone Number
                ObragoInput(
                    label = "Email or Phone Number",
                    value = state.email.ifBlank { state.phoneRaw },
                    onValueChange = { input ->
                        viewModel.setEmail(input)
                        viewModel.setPhoneRaw(input)
                    },
                    icon = Icons.Default.Email,
                    placeholder = "email@example.com or 03001234567",
                    keyboardType = KeyboardType.Email,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Input 2: Password
                ObragoInput(
                    label = "Password",
                    value = state.password,
                    onValueChange = viewModel::setPassword,
                    icon = Icons.Default.Lock,
                    isPassword = true,
                    placeholder = "••••••••",
                    keyboardType = KeyboardType.Password
                )

                // Forgot Password Right-aligned Link
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Forgot Password?",
                        color = NeonGreen,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable(onClick = onGoForgotPassword)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Login Primary Button
                ObragoButton(
                    text = "Login",
                    loading = state.isSubmitting
                ) {
                    viewModel.submitLogin()
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Register Link at Bottom
                Row(
                    modifier = Modifier.padding(bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Don't have an account? ",
                        color = Color(0xFF94A3B8),
                        fontSize = 14.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onGoRegister)
                    ) {
                        Text(
                            text = "Register",
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
