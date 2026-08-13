package com.obrago.app.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PrimaryGreen = Color(0xFF00E676)
private val PrimaryGreenDark = Color(0xFF00C853)
private val InputBgColor = Color(0xFF061823)
private val InputBorderColor = Color(0xFF0E2D3E)
private val DarkTextGreen = Color(0xFF022B1C)

/** Custom Obrago Top Bar with circular translucent back button */
@Composable
fun ObragoTopBar(title: String = "", onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            onClick = onBack,
            shape = CircleShape,
            color = Color(0xFF081C26).copy(alpha = 0.85f),
            border = BorderStroke(1.dp, Color(0xFF14303F)),
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (title.isNotBlank()) {
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

/** Custom Obrago Primary Button with Glowing Green Gradient & Right Arrow Circle */
@Composable
fun ObragoButton(
    text: String,
    modifier: Modifier = Modifier,
    fullWidth: Boolean = true,
    enabled: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = { if (enabled && !loading) onClick() },
        shape = CircleShape,
        color = Color.Unspecified,
        enabled = enabled && !loading,
        modifier = (if (fullWidth) modifier.fillMaxWidth() else modifier)
            .shadow(12.dp, CircleShape, spotColor = PrimaryGreen)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(PrimaryGreen, PrimaryGreenDark)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = DarkTextGreen
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = text,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = DarkTextGreen
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = DarkTextGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/** Dark-themed Custom Input Field matching screenshot aesthetics */
@Composable
fun ObragoInput(
    label: String = "",
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPassword: Boolean = false,
    placeholder: String = "",
    enabled: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            placeholder = {
                Text(
                    placeholder.ifBlank { label },
                    color = Color(0xFF5B7385),
                    fontSize = 14.sp
                )
            },
            leadingIcon = icon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = Color(0xFF8BA2B2),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else trailingIcon,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = InputBgColor,
                unfocusedContainerColor = InputBgColor,
                disabledContainerColor = InputBgColor.copy(alpha = 0.5f),
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = InputBorderColor,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = PrimaryGreen
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

/** Section Header Title */
@Composable
fun SectionHeaderTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        modifier = modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

/** Data Privacy Card ("Your data is safe with us") */
@Composable
fun DataPrivacyCard(modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFF041C18),
        border = BorderStroke(1.dp, Color(0xFF0B3A32)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PrimaryGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Your data is safe with us",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "We never share your personal information",
                    color = Color(0xFF8BA2B2),
                    fontSize = 11.sp
                )
            }
        }
    }
}

/** Two-way Role Toggle Pill ("I'm a Worker" vs "I'm Hiring") */
@Composable
fun RoleToggleSignup(
    isWorkerSelected: Boolean,
    onCustomerSelected: () -> Unit,
    onWorkerSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = InputBgColor,
        border = BorderStroke(1.dp, InputBorderColor),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth()
        ) {
            // Worker Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isWorkerSelected) {
                            Brush.horizontalGradient(listOf(PrimaryGreen, PrimaryGreenDark))
                        } else {
                            Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        }
                    )
                    .clickable(onClick = onWorkerSelected)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isWorkerSelected) DarkTextGreen else Color(0xFF8BA2B2),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "I'm a Worker",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isWorkerSelected) DarkTextGreen else Color.White
                    )
                }
            }

            // Customer Tab ("I'm Hiring")
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (!isWorkerSelected) {
                            Brush.horizontalGradient(listOf(PrimaryGreen, PrimaryGreenDark))
                        } else {
                            Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        }
                    )
                    .clickable(onClick = onCustomerSelected)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = null,
                        tint = if (!isWorkerSelected) DarkTextGreen else Color(0xFF8BA2B2),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "I'm Hiring",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (!isWorkerSelected) DarkTextGreen else Color.White
                    )
                }
            }
        }
    }
}

/** Legacy RoleToggle for Login Screen */
@Composable
fun RoleToggle(
    isWorkerSelected: Boolean,
    onCustomerSelected: () -> Unit,
    onWorkerSelected: () -> Unit
) {
    RoleToggleSignup(
        isWorkerSelected = isWorkerSelected,
        onCustomerSelected = onCustomerSelected,
        onWorkerSelected = onWorkerSelected
    )
}

/** Red/Warning error banner */
@Composable
fun ErrorBanner(message: String?) {
    if (!message.isNullOrBlank()) {
        Surface(
            color = Color(0xFF330D12),
            border = BorderStroke(1.dp, Color(0xFFFF5252)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⚠️ ", color = Color(0xFFFF5252))
                Text(
                    text = message,
                    color = Color(0xFFFF8A80),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/** Confirmation Dialog before exiting/quitting the app */
@Composable
fun QuitConfirmationDialog(
    onConfirmQuit: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Quit Obrago App?",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 18.sp
            )
        },
        text = {
            Text(
                text = "Are you sure you want to exit Obrago? Your session will remain active.",
                color = Color(0xFFB0BEC5),
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmQuit,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Exit App", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, Color(0xFF00E676)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel", color = Color(0xFF00E676), fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF081C26),
        shape = RoundedCornerShape(20.dp)
    )
}

