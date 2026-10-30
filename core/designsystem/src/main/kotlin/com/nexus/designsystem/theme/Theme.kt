package com.nexus.designsystem.theme

import android.animation.ValueAnimator
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
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
    primary = NexusColors.Signal,
    onPrimary = NexusColors.SignalInk,
    primaryContainer = Color(0xFF22300A),
    onPrimaryContainer = Color(0xFFD8FF6E),
    inversePrimary = NexusColors.SignalDeep,
    secondary = NexusColors.StructureBright,
    onSecondary = Color(0xFF0A0A20),
    secondaryContainer = Color(0xFF232A5C),
    onSecondaryContainer = Color(0xFFC9D0FF),
    tertiary = Color(0xFFFF8A5C),
    onTertiary = Color(0xFF1A0A04),
    tertiaryContainer = Color(0xFF3A1B0E),
    onTertiaryContainer = Color(0xFFFFC4A8),
    background = NexusColors.VoidBlack,
    onBackground = NexusColors.Bone,
    surface = NexusColors.DeepSpace,
    onSurface = NexusColors.Bone,
    surfaceVariant = NexusColors.Graphite,
    onSurfaceVariant = Color(0xFFA99FAE),
    surfaceTint = NexusColors.Signal,
    surfaceBright = NexusColors.Slate,
    surfaceDim = NexusColors.VoidBlack,
    surfaceContainerLowest = NexusColors.VoidBlack,
    surfaceContainerLow = NexusColors.DeepSpace,
    surfaceContainer = NexusColors.Carbon,
    surfaceContainerHigh = NexusColors.Graphite,
    surfaceContainerHighest = NexusColors.Slate,
    outline = Color(0xFF4A4052),
    outlineVariant = Color(0xFF2A2233),
    error = NexusColors.Rose400,
    onError = Color(0xFF2A0508),
    errorContainer = Color(0xFF3A1218),
    onErrorContainer = Color(0xFFFFB3B8),
    inverseSurface = NexusColors.Bone,
    inverseOnSurface = NexusColors.Carbon,
    scrim = Color(0xFF000000).copy(alpha = 0.6f),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2E1A47),
    onPrimary = Color(0xFFFFFDF9),
    primaryContainer = Color(0xFFECE3F7),
    onPrimaryContainer = Color(0xFF2E1A47),
    inversePrimary = NexusColors.Signal,
    secondary = Color(0xFF3F4FB5),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDFE3FF),
    onSecondaryContainer = Color(0xFF1B2470),
    tertiary = Color(0xFF566600),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDDF58F),
    onTertiaryContainer = Color(0xFF2A3300),
    background = NexusColors.PearlWhite,
    onBackground = Color(0xFF171018),
    surface = NexusColors.Cloud,
    onSurface = Color(0xFF171018),
    surfaceVariant = Color(0xFFF1EAE1),
    onSurfaceVariant = Color(0xFF5E5550),
    surfaceTint = Color(0xFF2E1A47),
    surfaceBright = NexusColors.Cloud,
    surfaceDim = NexusColors.Silver,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFBF7F2),
    surfaceContainer = Color(0xFFF4EEE7),
    surfaceContainerHigh = Color(0xFFEDE6DD),
    surfaceContainerHighest = Color(0xFFE5DCD1),
    outline = Color(0xFF8C8279),
    outlineVariant = Color(0xFFDCD3C8),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    inverseSurface = Color(0xFF171018),
    inverseOnSurface = NexusColors.Bone,
    scrim = Color(0xFF000000).copy(alpha = 0.35f),
)

/** Theme-aware tokens that are not part of the Material colour roles. */
val LocalNexusExtendedColors = staticCompositionLocalOf { DarkExtendedColors }

/** `MaterialTheme.nexus.hairline` — the theme-aware half of the token set. */
val MaterialTheme.nexus: NexusExtendedColors
    @Composable @ReadOnlyComposable get() = LocalNexusExtendedColors.current

enum class ThemeMode { LIGHT, DARK, SYSTEM, AMOLED }

private fun accentColor(name: String): Color = when (name.uppercase()) {
    "DEFAULT", "AZURE", "INDIGO", "LIME", "SIGNAL" -> NexusColors.Signal
    "CYAN", "ULTRA", "STRUCTURE" -> NexusColors.Cyan400
    "EMERALD" -> NexusColors.AuroraGreen
    "AMBER", "EMBER" -> NexusColors.PlasmaOrange
    "ROSE" -> NexusColors.StellarPink
    "PLUM", "VIOLET" -> NexusColors.Indigo500
    else -> NexusColors.Signal
}

@Composable
fun NexusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Off by default. Material You would repaint primary from the wallpaper and
    // erase the one thing that makes the product recognisable; it stays opt-in.
    dynamicColor: Boolean = false,
    amoled: Boolean = false,
    accent: String = "DEFAULT",
    content: @Composable () -> Unit
) {
    val hasOverride = accent.uppercase() != "DEFAULT"
    val useDynamic = dynamicColor && !hasOverride && Build.VERSION.SDK_INT >= 31
    val baseScheme = when {
        useDynamic -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val colorScheme = if (hasOverride) {
        val accentColor = accentColor(accent)
        baseScheme.copy(
            primary = accentColor,
            onPrimary = onAccent(accentColor),
            primaryContainer = if (darkTheme) {
                accentColor.copy(alpha = 0.18f)
            } else {
                accentColor.copy(alpha = 0.14f)
            },
            onPrimaryContainer = if (darkTheme) {
                lerp(accentColor, NexusColors.Neutral0, 0.28f)
            } else {
                lerp(accentColor, NexusColors.Ink, 0.48f)
            },
        )
    } else {
        baseScheme
    }.let { scheme ->
        if (darkTheme && amoled) {
            scheme.copy(
                background = Color(0xFF000000),
                surface = Color(0xFF05040A),
                surfaceDim = Color(0xFF000000),
                surfaceContainerLowest = Color(0xFF000000),
                surfaceContainerLow = Color(0xFF09070D),
                surfaceContainer = Color(0xFF09070D),
                surfaceContainerHigh = Color(0xFF110D16),
                surfaceContainerHighest = Color(0xFF191320),
                surfaceVariant = Color(0xFF110D16),
            )
        } else {
            scheme
        }
    }

    val extended = when {
        !darkTheme -> LightExtendedColors
        amoled -> DarkExtendedColors.copy(
            hairline = Color(0xFF241C2D),
            hairlineStrong = Color(0xFF3D3447),
        )
        else -> DarkExtendedColors
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

    val reducedMotion = !ValueAnimator.areAnimatorsEnabled()

    CompositionLocalProvider(
        LocalNexusExtendedColors provides extended,
        LocalReducedMotion provides reducedMotion,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NexusTypography.Material,
            shapes = NexusShapes.Material,
            content = content
        )
    }
}
