package com.pixelcam.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ---- Pastel pixel palette ----
val PastelBlue = Color(0xFFB8E8FC)
val PastelBlueDeep = Color(0xFF8AD4F5)
val PastelPink = Color(0xFFFFD3E8)
val PastelPinkDeep = Color(0xFFFFB5D8)
val PastelPurple = Color(0xFFC9B6F8)
val PastelPurpleDeep = Color(0xFFA694F0)
val PastelMint = Color(0xFFBDF4D8)
val PastelMintDeep = Color(0xFF8FE3B4)
val PastelYellow = Color(0xFFFFF6BF)
val PastelYellowDeep = Color(0xFFFFE08A)
val PastelPeach = Color(0xFFFFE0CC)
val PastelPeachDeep = Color(0xFFFFC7A8)
val PastelLavender = Color(0xFFE6DEFF)
val InkDark = Color(0xFF2E2A45)
val InkMedium = Color(0xFF594F80)
val PaperLight = Color(0xFFFDF6FF)
val PaperDark = Color(0xFF1C1930)
val PanelDark = Color(0xFF2A2545)

private val LightColors = lightColorScheme(
    primary = PastelPurpleDeep,
    onPrimary = Color.White,
    secondary = PastelPinkDeep,
    onSecondary = InkDark,
    tertiary = PastelMintDeep,
    background = PaperLight,
    onBackground = InkDark,
    surface = Color.White,
    onSurface = InkDark,
    surfaceVariant = PastelLavender,
    onSurfaceVariant = InkMedium,
    outline = InkDark
)

private val DarkColors = darkColorScheme(
    primary = PastelPurple,
    onPrimary = InkDark,
    secondary = PastelPink,
    onSecondary = InkDark,
    tertiary = PastelMint,
    background = PaperDark,
    onBackground = PaperLight,
    surface = PanelDark,
    onSurface = PaperLight,
    surfaceVariant = InkMedium,
    onSurfaceVariant = PastelLavender,
    outline = PastelLavender
)

/** Blocky, monospaced type ramp evoking 8-bit UI text. */
val PixelTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        letterSpacing = 2.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = 1.5.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        letterSpacing = 1.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 1.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 1.sp
    )
)

@Composable
fun PixelCamTheme(
    darkPixelMode: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkPixelMode) DarkColors else LightColors,
        typography = PixelTypography,
        content = content
    )
}
