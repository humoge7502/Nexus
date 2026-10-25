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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NexusColors.Indigo500,
    onPrimary = NexusColors.Neutral0,
    primaryContainer = NexusColors.Indigo500.copy(alpha = 0.15f),
    onPrimaryContainer = NexusColors.Indigo400,
    secondary = NexusColors.Cyan500,
    onSecondary = NexusColors.Neutral950,
    secondaryContainer = NexusColors.Cyan500.copy(alpha = 0.12f),
    onSecondaryContainer = NexusColors.Cyan500,
    tertiary = NexusColors.Emerald500,
    background = NexusColors.Neutral950,
    onBackground = NexusColors.Neutral100,
    surface = NexusColors.Neutral900,
    onSurface = NexusColors.Neutral100,
    surfaceVariant = NexusColors.Neutral800,
    onSurfaceVariant = NexusColors.Neutral400,
    outline = NexusColors.Neutral700,
    outlineVariant = NexusColors.Neutral750,
    error = NexusColors.Error,
    onError = NexusColors.Neutral0,
    errorContainer = NexusColors.Error.copy(alpha = 0.15f),
    onErrorContainer = NexusColors.Error,
    inverseSurface = NexusColors.Neutral100,
    inverseOnSurface = NexusColors.Neutral900,
    scrim = NexusColors.Neutral950.copy(alpha = 0.5f),
)

private val LightColorScheme = lightColorScheme(
    primary = NexusColors.Indigo600,
    onPrimary = NexusColors.Neutral0,
    primaryContainer = NexusColors.Indigo100,
    onPrimaryContainer = NexusColors.Indigo800,
    secondary = NexusColors.Cyan600,
    onSecondary = NexusColors.Neutral0,
    secondaryContainer = NexusColors.Cyan500.copy(alpha = 0.1f),
    onSecondaryContainer = NexusColors.Cyan600,
    tertiary = NexusColors.Emerald600,
    background = NexusColors.Neutral50,
    onBackground = NexusColors.Neutral950,
    surface = NexusColors.Neutral0,
    onSurface = NexusColors.Neutral950,
    surfaceVariant = NexusColors.Neutral100,
    onSurfaceVariant = NexusColors.Neutral600,
    outline = NexusColors.Neutral300,
    outlineVariant = NexusColors.Neutral200,
    error = NexusColors.Error,
    onError = NexusColors.Neutral0,
    errorContainer = NexusColors.Error.copy(alpha = 0.1f),
    onErrorContainer = NexusColors.Rose600,
    inverseSurface = NexusColors.Neutral900,
    inverseOnSurface = NexusColors.Neutral100,
    scrim = NexusColors.Neutral950.copy(alpha = 0.3f),
)

enum class ThemeMode { LIGHT, DARK, SYSTEM, AMOLED }

private fun accentColor(name: String): Color = when (name.uppercase()) {
    "DEFAULT" -> NexusColors.Indigo500
    "CYAN" -> NexusColors.Cyan500
    "EMERALD" -> NexusColors.Emerald500
    "AMBER" -> Color(0xFFF59E0B)
    "ROSE" -> Color(0xFFF43F5E)
    else -> NexusColors.Indigo500
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
        primaryContainer = accentColor.copy(alpha = if (darkTheme) 0.18f else 0.12f),
    ).let { scheme ->
        if (darkTheme && amoled) {
            scheme.copy(
                background = Color(0xFF000000),
                surface = Color(0xFF050508),
                surfaceVariant = Color(0xFF121216),
            )
        } else scheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NexusTypography.Material,
        shapes = NexusShapes.Material,
        content = content
    )
}
