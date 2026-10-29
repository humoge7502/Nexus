package com.nexus.designsystem.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private fun Color.contrastRatio(other: Color): Float {
    val a = luminance() + 0.05f
    val b = other.luminance() + 0.05f
    return if (a > b) a / b else b / a
}

/** Picks whichever of ink or white reads with more contrast on [accent]. */
internal fun onAccent(accent: Color): Color =
    if (accent.contrastRatio(NexusColors.Ink) >= accent.contrastRatio(NexusColors.Neutral0)) {
        NexusColors.Ink
    } else {
        NexusColors.Neutral0
    }

private val DarkColorScheme = darkColorScheme(
    primary = NexusColors.NexusBlue,
    onPrimary = NexusColors.Ink,
    primaryContainer = NexusColors.NexusBlue.copy(alpha = 0.18f),
    onPrimaryContainer = Color(0xFFBBD4FF),
    inversePrimary = NexusColors.NexusBlueDark,
    secondary = NexusColors.AuroraGreen,
    onSecondary = NexusColors.Ink,
    secondaryContainer = NexusColors.AuroraGreen.copy(alpha = 0.14f),
    onSecondaryContainer = Color(0xFF7FF0D2),
    tertiary = NexusColors.Amber400,
    onTertiary = NexusColors.Ink,
    tertiaryContainer = NexusColors.Amber500.copy(alpha = 0.14f),
    onTertiaryContainer = NexusColors.Amber400,
    background = NexusColors.Neutral950,
    onBackground = NexusColors.Neutral100,
    surface = NexusColors.Neutral900,
    onSurface = NexusColors.Neutral100,
    surfaceVariant = NexusColors.Neutral800,
    onSurfaceVariant = NexusColors.Neutral400,
    surfaceTint = NexusColors.NexusBlue,
    surfaceBright = NexusColors.Neutral750,
    surfaceDim = NexusColors.VoidBlack,
    surfaceContainerLowest = NexusColors.Neutral950,
    surfaceContainerLow = NexusColors.Neutral850,
    surfaceContainer = NexusColors.Neutral850,
    surfaceContainerHigh = NexusColors.Neutral800,
    surfaceContainerHighest = NexusColors.Neutral750,
    outline = NexusColors.Neutral600,
    outlineVariant = NexusColors.Neutral700,
    error = NexusColors.Rose400,
    onError = NexusColors.Ink,
    errorContainer = NexusColors.Rose500.copy(alpha = 0.16f),
    onErrorContainer = NexusColors.Rose400,
    inverseSurface = NexusColors.Neutral100,
    inverseOnSurface = NexusColors.Neutral900,
    scrim = NexusColors.Neutral950.copy(alpha = 0.6f),
)

private val LightColorScheme = lightColorScheme(
    primary = NexusColors.NexusBlueDark,
    onPrimary = NexusColors.Neutral0,
    primaryContainer = NexusColors.NexusBlue.copy(alpha = 0.12f),
    onPrimaryContainer = Color(0xFF163C82),
    inversePrimary = NexusColors.NexusBlue,
    secondary = NexusColors.Emerald600,
    onSecondary = NexusColors.Neutral0,
    secondaryContainer = NexusColors.AuroraGreen.copy(alpha = 0.16f),
    onSecondaryContainer = Color(0xFF00543F),
    tertiary = NexusColors.Amber600,
    onTertiary = NexusColors.Neutral0,
    tertiaryContainer = NexusColors.Amber500.copy(alpha = 0.16f),
    onTertiaryContainer = Color(0xFF6B4200),
    background = NexusColors.Neutral50,
    onBackground = NexusColors.Neutral950,
    surface = NexusColors.Neutral0,
    onSurface = NexusColors.Neutral950,
    surfaceVariant = NexusColors.Mist,
    onSurfaceVariant = NexusColors.Neutral500,
    surfaceTint = NexusColors.NexusBlueDark,
    surfaceBright = NexusColors.Neutral0,
    surfaceDim = NexusColors.Silver,
    surfaceContainerLowest = NexusColors.Neutral0,
    surfaceContainerLow = Color(0xFFFAFBFE),
    surfaceContainer = NexusColors.Mist,
    surfaceContainerHigh = Color(0xFFEAF0F9),
    surfaceContainerHighest = NexusColors.Silver,
    outline = Color(0xFF98A2B8),
    outlineVariant = Color(0xFFE3E9F3),
    error = NexusColors.Rose600,
    onError = NexusColors.Neutral0,
    errorContainer = NexusColors.Rose500.copy(alpha = 0.12f),
    onErrorContainer = Color(0xFF8B0A2C),
    inverseSurface = NexusColors.Neutral900,
    inverseOnSurface = NexusColors.Neutral100,
    scrim = NexusColors.Neutral950.copy(alpha = 0.35f),
)

enum class ThemeMode { LIGHT, DARK, SYSTEM, AMOLED }

private fun accentColor(name: String): Color = when (name.uppercase()) {
    "DEFAULT" -> NexusColors.NexusBlue
    "AZURE", "INDIGO" -> NexusColors.NexusBlue
    "CYAN" -> NexusColors.Cyan400
    "EMERALD" -> NexusColors.AuroraGreen
    "AMBER" -> NexusColors.Amber500
    "ROSE" -> NexusColors.Rose500
    else -> NexusColors.NexusBlue
}

@Composable
fun NexusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Material You wallpaper theming is the Android 12+ default; an explicit
    // accent choice (anything but DEFAULT) overrides the wallpaper palette.
    dynamicColor: Boolean = Build.VERSION.SDK_INT >= 31,
    amoled: Boolean = false,
    accent: String = "DEFAULT",
    content: @Composable () -> Unit
) {
    val useDynamic = dynamicColor && accent == "DEFAULT" && Build.VERSION.SDK_INT >= 31
    val baseScheme = when {
        useDynamic -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val accentColor = accentColor(accent)
    val colorScheme = baseScheme.copy(
        primary = accentColor,
        onPrimary = onAccent(accentColor),
        primaryContainer = accentColor.copy(alpha = if (darkTheme) 0.18f else 0.12f),
        onPrimaryContainer = if (darkTheme) {
            lerp(accentColor, NexusColors.Neutral0, 0.28f)
        } else {
            lerp(accentColor, NexusColors.Ink, 0.48f)
        },
    ).let { scheme ->
        if (darkTheme && amoled) {
            scheme.copy(
                background = Color(0xFF000000),
                surface = Color(0xFF05070B),
                surfaceDim = Color(0xFF000000),
                surfaceContainerLowest = Color(0xFF000000),
                surfaceContainerLow = Color(0xFF0A0D14),
                surfaceContainer = Color(0xFF0A0D14),
                surfaceContainerHigh = Color(0xFF12161F),
                surfaceContainerHighest = Color(0xFF1A1F2B),
                surfaceVariant = Color(0xFF12161F),
            )
        } else {
            scheme
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NexusTypography.Material,
        shapes = NexusShapes.Material,
        content = content
    )
}
