package org.example.project.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val mythTypography = Typography(
    displayLarge = TextStyle(fontSize = 44.sp, fontWeight = FontWeight.Light, letterSpacing = 4.sp),
    displayMedium = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Light, letterSpacing = 3.sp),
    headlineLarge = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
    headlineMedium = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp),
    titleLarge = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.4.sp),
    bodyLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.2.sp),
    bodyMedium = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.2.sp),
    bodySmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.3.sp),
    labelLarge = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp),
    labelMedium = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
    labelSmall = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.6.sp),
)

@Composable
fun MythTheme(content: @Composable () -> Unit) {
    // Read ThemeState.mode so the whole tree recomposes when the user toggles themes.
    val mode = ThemeState.mode
    val scheme = when (mode) {
        ThemeMode.Dark -> darkColorScheme(
            primary = MythColors.Cyan,
            onPrimary = MythColors.BgAbyss,
            primaryContainer = MythColors.SurfaceGlow,
            onPrimaryContainer = MythColors.TextPrimary,
            secondary = MythColors.Azure,
            onSecondary = MythColors.TextPrimary,
            secondaryContainer = MythColors.Surface,
            onSecondaryContainer = MythColors.TextPrimary,
            tertiary = MythColors.Gold,
            onTertiary = MythColors.BgAbyss,
            background = MythColors.BgAbyss,
            onBackground = MythColors.TextPrimary,
            surface = MythColors.Surface,
            onSurface = MythColors.TextPrimary,
            surfaceVariant = MythColors.SurfaceHigh,
            onSurfaceVariant = MythColors.TextSecondary,
            outline = MythColors.DividerSoft,
            error = MythColors.Crimson,
            onError = MythColors.TextPrimary,
        )
        ThemeMode.Light -> lightColorScheme(
            primary = MythColors.Cyan,
            onPrimary = MythColors.TextPrimary,
            primaryContainer = MythColors.SurfaceGlow,
            onPrimaryContainer = MythColors.TextPrimary,
            secondary = MythColors.Azure,
            onSecondary = MythColors.TextPrimary,
            secondaryContainer = MythColors.Surface,
            onSecondaryContainer = MythColors.TextPrimary,
            tertiary = MythColors.Gold,
            onTertiary = MythColors.TextPrimary,
            background = MythColors.BgAbyss,
            onBackground = MythColors.TextPrimary,
            surface = MythColors.Surface,
            onSurface = MythColors.TextPrimary,
            surfaceVariant = MythColors.SurfaceHigh,
            onSurfaceVariant = MythColors.TextSecondary,
            outline = MythColors.DividerSoft,
            error = MythColors.Crimson,
            onError = MythColors.TextPrimary,
        )
    }
    MaterialTheme(
        colorScheme = scheme,
        typography = mythTypography,
        content = content,
    )
}
