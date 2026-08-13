package com.obrago.app.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.obrago.app.R
import com.obrago.app.data.model.Role
import com.obrago.app.data.repository.SessionManager
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: (targetRoute: String) -> Unit
) {
    var progress by remember { mutableFloatStateOf(0f) }

    // Smooth loading progress animation over 2.5 seconds
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 2200, easing = LinearEasing),
        label = "splashProgress"
    )

    val currentUser by SessionManager.currentUser.collectAsState()
    val isLoggedIn by SessionManager.isLoggedIn.collectAsState()

    LaunchedEffect(Unit) {
        progress = 1.0f
        delay(2500)
        
        // Determine target destination based on session state
        val target = if (isLoggedIn && currentUser != null) {
            when (currentUser?.role) {
                Role.CUSTOMER.value -> com.obrago.app.ui.navigation.Routes.HOME_CUSTOMER
                Role.WORKER.value -> com.obrago.app.ui.navigation.Routes.HOME_WORKER
                Role.ADMIN.value -> com.obrago.app.ui.navigation.Routes.ADMIN_PANEL
                else -> com.obrago.app.ui.navigation.Routes.ROLE_SELECT
            }
        } else {
            com.obrago.app.ui.navigation.Routes.ROLE_SELECT
        }
        onSplashFinished(target)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF031D10),
                        Color(0xFF0A3C23),
                        Color(0xFF062314),
                        Color(0xFF02120A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // Obrago 3D Logo Emblem Icon
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0D472B))
                    .border(2.dp, Color(0xFF22C55E).copy(alpha = 0.5f), CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_app_icon_1786457000485),
                    contentDescription = "Obrago Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Obrago Title
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Obra",
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "go",
                    color = Color(0xFF22C55E),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Tagline
            Text(
                text = "Professional Services\non Demand",
                color = Color(0xFFE2E8F0),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Hero Artwork (Worker looking at site)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFF22C55E).copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_splash_worker_1786457016425),
                    contentDescription = "Obrago Construction",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Subtle dark vignette at the bottom of artwork
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x8002120A),
                                    Color(0xFF02120A)
                                ),
                                startY = 300f
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Loading Bar & Progress Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Loading bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF0D3B23))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF16A34A),
                                        Color(0xFF22C55E),
                                        Color(0xFF4ADE80)
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Percentage text & animated dots
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val percent = (animatedProgress * 100).toInt()
                    Text(
                        text = "Loading $percent%",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Powered By Branding
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Powered by ",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = "Obrago",
                        color = Color(0xFF22C55E),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
