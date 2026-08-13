package com.obrago.app.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.obrago.app.R
import com.obrago.app.data.model.Role

private val DarkCanvasBg = Color(0xFF030B12)
private val NeonGreen = Color(0xFF00E676)
private val DarkGreenBtnBg = Color(0xFF00C853)
private val DarkCardBg = Color(0xFF061823)
private val DarkBorderColor = Color(0xFF0E2836)

@Composable
fun RoleSelectionScreen(
    onRoleChosen: (Role) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showQuitDialog by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler {
        showQuitDialog = true
    }

    if (showQuitDialog) {
        com.obrago.app.ui.common.QuitConfirmationDialog(
            onConfirmQuit = {
                showQuitDialog = false
                (context as? android.app.Activity)?.finish()
            },
            onDismiss = { showQuitDialog = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvasBg)
    ) {
        // Base Abstract Gradient & Glow Background Wallpaper
        Image(
            painter = painterResource(id = R.drawable.bg_selection_screen),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Top Header Logo: Green Pin Badge + Obrago Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Green Location Pin Badge with Hardhat Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(NeonGreen, DarkGreenBtnBg)
                            )
                        )
                        .shadow(12.dp, CircleShape, spotColor = NeonGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Engineering,
                        contentDescription = "Obrago Logo",
                        tint = Color(0xFF021B11),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // "Obrago" Title Text
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)) {
                            append("Obra")
                        }
                        withStyle(SpanStyle(color = NeonGreen, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)) {
                            append("go")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Main Title & Subtitle
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.White)) { append("Find ") }
                    withStyle(SpanStyle(color = NeonGreen, fontWeight = FontWeight.ExtraBold)) { append("Trusted ") }
                    withStyle(SpanStyle(color = Color.White)) { append("Labour\nor Start Earning Today") }
                },
                fontSize = 24.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Connecting people with skilled workers,\nquickly and easily.",
                color = Color(0xFF8BA2B2),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Middle 5 Workers Hero Illustration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_workers_hero),
                    contentDescription = "5 Skilled Workers",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Button 1: Hire Verified Professionals (Green Pill)
            Surface(
                onClick = { onRoleChosen(Role.CUSTOMER) },
                shape = CircleShape,
                color = Color.Unspecified,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, CircleShape, spotColor = NeonGreen)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF00E676), Color(0xFF00C853))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Worker Icon Circle
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF022B1C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Engineering,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Text Column
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hire Verified Professionals",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color(0xFF022B1C)
                            )
                            Text(
                                text = "Post a Job & Get the Right Worker",
                                fontSize = 11.sp,
                                color = Color(0xFF04422E),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Right Arrow Circle Button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF022B1C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Button 2: Start Earning as a Worker (Dark Navy Pill with Cyan Border)
            Surface(
                onClick = { onRoleChosen(Role.WORKER) },
                shape = CircleShape,
                color = Color(0xFF061823),
                border = BorderStroke(1.5.dp, Color(0xFF007BFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Briefcase Icon Circle
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0D2B38)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Work,
                            contentDescription = null,
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Text Column
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Start Earning as a Worker",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Create Profile & Find Jobs",
                            fontSize = 11.sp,
                            color = Color(0xFF8BA2B2)
                        )
                    }

                    // Right Arrow Circle Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF020B10)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6. 4 Feature Highlights Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FeaturePill(icon = Icons.Default.VerifiedUser, title = "Verified", subtitle = "Workers")
                FeaturePill(icon = Icons.Default.ElectricBolt, title = "Fast", subtitle = "Hire")
                FeaturePill(icon = Icons.Default.Security, title = "Safe", subtitle = "Payments")
                FeaturePill(icon = Icons.Default.HeadsetMic, title = "24/7", subtitle = "Support")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7. Bottom Social Proof Badge
            Surface(
                shape = CircleShape,
                color = DarkCardBg,
                border = BorderStroke(1.dp, DarkBorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Overlapping Avatar Circles
                    Row {
                        listOf(Color(0xFF2E7D32), Color(0xFF1565C0), Color(0xFFD84315), Color(0xFF6A1B9A)).forEachIndexed { index, bg ->
                            Box(
                                modifier = Modifier
                                    .offset(x = (-6 * index).dp)
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(bg)
                                    .border(1.dp, Color.Black, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)) {
                                append("1M+ ")
                            }
                            withStyle(SpanStyle(color = Color(0xFFD0E0EC), fontSize = 11.sp)) {
                                append("Happy Customers & Workers ")
                            }
                        },
                        textAlign = TextAlign.Center
                    )

                    Row {
                        repeat(5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Admin Portal Access Link
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onRoleChosen(Role.CUSTOMER) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Admin Portal",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Admin Portal Login",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun FeaturePill(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = DarkCardBg,
        border = BorderStroke(1.dp, DarkBorderColor),
        modifier = Modifier.width(76.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeonGreen,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                color = Color(0xFF8BA2B2),
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
