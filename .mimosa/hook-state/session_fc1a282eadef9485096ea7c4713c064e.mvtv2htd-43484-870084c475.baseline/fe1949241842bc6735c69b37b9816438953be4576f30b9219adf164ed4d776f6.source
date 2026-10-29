package com.nexus.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * NEXUS signal system.
 *
 * One brand accent (azure) carries navigation, primary actions and progress.
 * Mint, amber and rose are semantic signals only — success/growth, attention,
 * and error. Everything else in the product is ink and paper; surfaces are
 * separated by tone and a hairline, not by a different hue per feature.
 */
object NexusColors {
    // Grounds — blue-graphite ink
    val VoidBlack = Color(0xFF06090F)
    val DeepSpace = Color(0xFF0B1018)
    val Carbon = Color(0xFF121824)
    val Graphite = Color(0xFF19202F)
    val Slate = Color(0xFF212A3C)

    // Paper
    val PearlWhite = Color(0xFFF6F8FD)
    val Cloud = Color(0xFFFFFFFF)
    val Mist = Color(0xFFF0F3FA)
    val Silver = Color(0xFFE6EAF4)
    val LavenderSmoke = Color(0xFFD6DCEB)

    // Brand — signal azure
    val NexusBlue = Color(0xFF4F8EF7)
    val NexusBlueDark = Color(0xFF2F6BE0)
    val NexusBlueDeep = Color(0xFF1B3F8F)

    // Secondary signals
    val AuroraGreen = Color(0xFF00D9A3)
    val AuroraGreenDark = Color(0xFF00B88A)
    val PlasmaOrange = Color(0xFFFF6B35)
    val PlasmaOrangeDark = Color(0xFFE55A25)
    val StellarPink = Color(0xFFF72585)
    val StellarPinkDark = Color(0xFFE01E75)
    val QuantumWhite = Color(0xFFE8EDFB)
    val Ink = Color(0xFF060A12)

    // Supporting deep blue (formerly a violet-leaning indigo)
    val ElectricIndigo = Color(0xFF4C5FA8)
    val ElectricIndigoDark = Color(0xFF3E4F8F)

    // Legacy indigo ramp (kept for existing references; no longer the primary)
    val Indigo50 = Color(0xFFEEF2FF)
    val Indigo100 = Color(0xFFE0E7FF)
    val Indigo200 = Color(0xFFC7D2FE)
    val Indigo300 = Color(0xFFA5B4FC)
    val Indigo400 = Color(0xFF818CF8)
    val Indigo500 = Color(0xFF6366F1)
    val Indigo600 = Color(0xFF4F46E5)
    val Indigo700 = Color(0xFF4338CA)
    val Indigo800 = Color(0xFF3730A3)
    val Indigo900 = Color(0xFF312E81)

    val Cyan400 = Color(0xFF22D3EE)
    val Cyan500 = Color(0xFF06B6D4)
    val Cyan600 = Color(0xFF0891B2)

    val Emerald400 = Color(0xFF34D399)
    val Emerald500 = Color(0xFF10B981)
    val Emerald600 = Color(0xFF059669)

    val Amber400 = Color(0xFFFBBF24)
    val Amber500 = Color(0xFFF59E0B)
    val Amber600 = Color(0xFFD97706)

    val Rose400 = Color(0xFFFB7185)
    val Rose500 = Color(0xFFF43F5E)
    val Rose600 = Color(0xFFE11D48)

    val Blue400 = Color(0xFF60A5FA)
    val Blue500 = Color(0xFF3B82F6)
    val Blue600 = Color(0xFF2563EB)

    val Success = AuroraGreen
    val Warning = Amber500
    val Error = Rose500
    val Info = NexusBlue

    // Neutrals — cool graphite
    val Neutral950 = Color(0xFF070A11)
    val Neutral900 = Color(0xFF0D111A)
    val Neutral850 = Color(0xFF131926)
    val Neutral800 = Color(0xFF1A2130)
    val Neutral750 = Color(0xFF222A3B)
    val Neutral700 = Color(0xFF2C3549)
    val Neutral600 = Color(0xFF3E4862)
    val Neutral500 = Color(0xFF5C6683)
    val Neutral400 = Color(0xFF8A93AC)
    val Neutral300 = Color(0xFFB3BBD0)
    val Neutral200 = Color(0xFFD2D8E6)
    val Neutral100 = Color(0xFFE8ECF5)
    val Neutral50 = Color(0xFFF4F6FB)
    val Neutral0 = Color(0xFFFFFFFF)

    val LinkedInBlue = Color(0xFF0A66C2)
    val GitHubDark = Color(0xFF24292F)
}

/**
 * Gradients are reserved for signature surfaces — at most one per screen.
 * The brand sweep moves from deep azure to light azure, never indigo-violet.
 */
object NexusGradients {
    val Nexus = listOf(NexusColors.NexusBlue, NexusColors.Cyan400)
    val Aurora = listOf(NexusColors.AuroraGreen, NexusColors.NexusBlue)
    val Stellar = listOf(NexusColors.StellarPink, NexusColors.PlasmaOrange)
    val Cosmic = listOf(NexusColors.VoidBlack, NexusColors.Carbon)
    val Primary = listOf(NexusColors.NexusBlueDark, NexusColors.NexusBlue)
    val PrimaryDeep = listOf(NexusColors.NexusBlueDeep, NexusColors.NexusBlueDark)
    val Success = listOf(NexusColors.Emerald500, NexusColors.Emerald400)
    val Warning = listOf(NexusColors.Amber500, NexusColors.Amber400)
    val Error = listOf(NexusColors.Rose500, NexusColors.Rose400)
    val Ocean = listOf(NexusColors.Cyan500, NexusColors.Blue500)
    val Sunset = listOf(NexusColors.Amber500, NexusColors.Rose500)
    val Glass = listOf(
        NexusColors.Neutral0.copy(alpha = 0.1f),
        NexusColors.Neutral0.copy(alpha = 0.05f)
    )
}
