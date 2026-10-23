package com.nexus.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import java.time.LocalTime

enum class NexusContextMode {
    MORNING, DEEP_WORK, EXPLORATION, DEADLINE_CRISIS, CELEBRATION
}

object AdaptiveTheme {
    data class ContextPalette(
        val primaryGradient: List<Color>,
        val accentColor: Color,
        val bgOverlay: Color,
        val cardStyle: CardStyle,
        val isUrgent: Boolean = false,
        val isCelebratory: Boolean = false,
    )

    enum class CardStyle { STANDARD, MINIMAL, EXPANSIVE, URGENT }

    private val morningPalette = ContextPalette(
        primaryGradient = listOf(Color(0xFFFF8C42), Color(0xFFFFB347)),
        accentColor = Color(0xFFFF8C42),
        bgOverlay = Color(0xFFFFF3E0),
        cardStyle = CardStyle.STANDARD,
    )

    private val deepWorkPalette = ContextPalette(
        primaryGradient = listOf(Color(0xFF4F8EF7), Color(0xFF3B7DE8)),
        accentColor = Color(0xFF4F8EF7),
        bgOverlay = Color(0xFFE8EDFB),
        cardStyle = CardStyle.MINIMAL,
    )

    private val explorationPalette = ContextPalette(
        primaryGradient = listOf(NexusColors.StellarPink, NexusColors.ElectricIndigo),
        accentColor = NexusColors.StellarPink,
        bgOverlay = Color(0xFFF8F0FF),
        cardStyle = CardStyle.EXPANSIVE,
    )

    private val deadlineCrisisPalette = ContextPalette(
        primaryGradient = listOf(Color(0xFFEF4444), Color(0xFFF97316)),
        accentColor = Color(0xFFEF4444),
        bgOverlay = Color(0xFFFEF2F2),
        cardStyle = CardStyle.URGENT,
        isUrgent = true,
    )

    private val celebrationPalette = ContextPalette(
        primaryGradient = listOf(Color(0xFFFBBF24), Color(0xFFF59E0B)),
        accentColor = Color(0xFFFBBF24),
        bgOverlay = Color(0xFFFFFBEB),
        cardStyle = CardStyle.EXPANSIVE,
        isCelebratory = true,
    )

    fun paletteForMode(mode: NexusContextMode): ContextPalette = when (mode) {
        NexusContextMode.MORNING -> morningPalette
        NexusContextMode.DEEP_WORK -> deepWorkPalette
        NexusContextMode.EXPLORATION -> explorationPalette
        NexusContextMode.DEADLINE_CRISIS -> deadlineCrisisPalette
        NexusContextMode.CELEBRATION -> celebrationPalette
    }

    private val darkMorningPalette = morningPalette.copy(
        bgOverlay = Color(0xFF1A1108),
    )
    private val darkDeepWorkPalette = deepWorkPalette.copy(
        bgOverlay = Color(0xFF0A0F1E),
    )
    private val darkExplorationPalette = explorationPalette.copy(
        bgOverlay = Color(0xFF120A1E),
    )
    private val darkDeadlineCrisisPalette = deadlineCrisisPalette.copy(
        bgOverlay = Color(0xFF1E0A0A),
    )
    private val darkCelebrationPalette = celebrationPalette.copy(
        bgOverlay = Color(0xFF1E150A),
    )

    fun paletteForMode(mode: NexusContextMode, isDark: Boolean): ContextPalette {
        val light = paletteForMode(mode)
        return if (!isDark) light else when (mode) {
            NexusContextMode.MORNING -> darkMorningPalette
            NexusContextMode.DEEP_WORK -> darkDeepWorkPalette
            NexusContextMode.EXPLORATION -> darkExplorationPalette
            NexusContextMode.DEADLINE_CRISIS -> darkDeadlineCrisisPalette
            NexusContextMode.CELEBRATION -> darkCelebrationPalette
        }
    }
}

fun detectContextMode(
    hour: Int = LocalTime.now().hour,
    hasUpcomingDeadline: Boolean = false,
    isCelebrating: Boolean = false,
): NexusContextMode = when {
    isCelebrating -> NexusContextMode.CELEBRATION
    hasUpcomingDeadline -> NexusContextMode.DEADLINE_CRISIS
    hour in 6..9 -> NexusContextMode.MORNING
    hour in 10..17 -> NexusContextMode.DEEP_WORK
    else -> NexusContextMode.EXPLORATION
}

@Composable
fun rememberContextMode(
    hasUpcomingDeadline: Boolean = false,
    isCelebrating: Boolean = false,
): NexusContextMode {
    val isDark = isSystemInDarkTheme()
    return remember(hasUpcomingDeadline, isCelebrating) {
        detectContextMode(
            hasUpcomingDeadline = hasUpcomingDeadline,
            isCelebrating = isCelebrating,
        )
    }
}
