package com.nexus.designsystem.theme

import androidx.compose.ui.graphics.Color

object NexusColors {
    // Dark Theme Backgrounds
    val VoidBlack = Color(0xFF080B14)
    val DeepSpace = Color(0xFF0D1117)
    val Carbon = Color(0xFF161B27)
    val Graphite = Color(0xFF1E2536)
    val Slate = Color(0xFF252D40)

    // Light Theme Backgrounds
    val PearlWhite = Color(0xFFF8FAFF)
    val Cloud = Color(0xFFFFFFFF)
    val Mist = Color(0xFFF1F4FD)
    val Silver = Color(0xFFE8ECF6)
    val LavenderSmoke = Color(0xFFD4DAF0)

    // Accent System - "Signal" Colors
    val NexusBlue = Color(0xFF4F8EF7)
    val NexusBlueDark = Color(0xFF3B7DE8)
    val ElectricIndigo = Color(0xFF7B5EA7)
    val ElectricIndigoDark = Color(0xFF6B4FA0)
    val AuroraGreen = Color(0xFF00D9A3)
    val AuroraGreenDark = Color(0xFF00B88A)
    val PlasmaOrange = Color(0xFFFF6B35)
    val PlasmaOrangeDark = Color(0xFFE55A25)
    val StellarPink = Color(0xFFF72585)
    val StellarPinkDark = Color(0xFFE01E75)
    val QuantumWhite = Color(0xFFE8EDFB)
    val Ink = Color(0xFF0A0F1E)

    // Legacy colors (keep for backward compatibility)
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

    // Neutrals
    val Neutral950 = Color(0xFF0A0A0F)
    val Neutral900 = Color(0xFF111118)
    val Neutral850 = Color(0xFF18181F)
    val Neutral800 = Color(0xFF1F1F28)
    val Neutral750 = Color(0xFF26263A)
    val Neutral700 = Color(0xFF2E2E42)
    val Neutral600 = Color(0xFF3F3F56)
    val Neutral500 = Color(0xFF5A5A76)
    val Neutral400 = Color(0xFF7A7A96)
    val Neutral300 = Color(0xFFA0A0B8)
    val Neutral200 = Color(0xFFC8C8D8)
    val Neutral100 = Color(0xFFE8E8F0)
    val Neutral50 = Color(0xFFF5F5FA)
    val Neutral0 = Color(0xFFFFFFFF)

    val LinkedInBlue = Color(0xFF0A66C2)
    val GitHubDark = Color(0xFF24292F)
}

object NexusGradients {
    val Nexus = listOf(NexusColors.NexusBlue, NexusColors.ElectricIndigo)
    val Aurora = listOf(NexusColors.AuroraGreen, NexusColors.NexusBlue)
    val Stellar = listOf(NexusColors.StellarPink, NexusColors.PlasmaOrange)
    val Cosmic = listOf(NexusColors.VoidBlack, NexusColors.Carbon)
    val Primary = listOf(NexusColors.Indigo500, NexusColors.Indigo400)
    val PrimaryDeep = listOf(NexusColors.Indigo600, NexusColors.Indigo500)
    val Success = listOf(NexusColors.Emerald500, NexusColors.Emerald400)
    val Warning = listOf(NexusColors.Amber500, NexusColors.Amber400)
    val Error = listOf(NexusColors.Rose500, NexusColors.Rose400)
    val Ocean = listOf(NexusColors.Cyan500, NexusColors.Blue500)
    val Sunset = listOf(NexusColors.Amber500, NexusColors.Rose500)
    val Purple = listOf(NexusColors.Indigo500, NexusColors.Cyan500)
    val Glass = listOf(
        NexusColors.Neutral0.copy(alpha = 0.1f),
        NexusColors.Neutral0.copy(alpha = 0.05f)
    )
}
