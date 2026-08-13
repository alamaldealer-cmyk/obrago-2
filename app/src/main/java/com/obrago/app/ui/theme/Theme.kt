package com.obrago.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = ObragoGreen,
    onPrimary = Color.White,
    secondary = ObragoGreenDark,
    background = Color.White,
    surface = Color.White,
    error = ObragoRed,
    onBackground = ObragoGray900,
    onSurface = ObragoGray900
)

private val DarkColors = darkColorScheme(
    primary = ObragoGreen,
    onPrimary = Color.White,
    secondary = ObragoGreenDark,
    background = Color(0xFF0B0F19),
    surface = Color(0xFF111827),
    error = ObragoRed
)

val ObragoTypography = Typography(
    headlineSmall = TextStyle(fontSize = 22.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
    bodyMedium = TextStyle(fontSize = 14.sp)
)

@Composable
fun ObragoTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, typography = ObragoTypography, content = content)
}
