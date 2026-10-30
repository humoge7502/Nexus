package com.nexus.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * NEXUS signal system.
 *
 * The ground is a deep plum-black ink, not a neutral grey: every surface carries
 * a trace of violet so the product feels authored rather than defaulted. One
 * acid-lime signal carries navigation, primary action and progress. Ultramarine
 * is the structural counterweight (secondary data, links, charts); ember is
 * reserved for attention and error-adjacent states. Nothing else earns a hue —
 * surfaces separate by tone and a hairline, never by a different accent per
 * feature.
 *
 * Contrast contract:
 *  - Lime is a *light* signal. It is only ever filled with ink text (≈15:1).
 *    White on lime is 1.3:1 and is a bug, not a style choice.
 *  - In the light theme lime cannot be `primary` for text/icons, so primary is
 *    deep ink-violet and lime lives in `tertiaryContainer` and filled highlights.
 *
 * Legacy token names (`NexusBlue`, `Cyan400`, …) are intentionally preserved and
 * re-pointed rather than deleted: ~190 call sites across 24 feature modules
 * inherit the new world without a single feature-file edit.
 */
object NexusColors {
    // Grounds — plum-black ink
    val VoidBlack = Color(0xFF08060B)
    val DeepSpace = Color(0xFF0C0910)
    val Carbon = Color(0xFF120E17)
    val Graphite = Color(0xFF191320)
    val Slate = Color(0xFF211A2B)

    // Paper — warm, never clinical white
    val PearlWhite = Color(0xFFFAF7F3)
    val Cloud = Color(0xFFFFFDFB)
    val Mist = Color(0xFFF3EEE7)
    val Silver = Color(0xFFE8E1D8)
    val LavenderSmoke = Color(0xFFD8CFE2)

    // Brand — acid-lime signal (legacy "blue" names kept as aliases)
    val NexusBlue = Color(0xFFC3F53C)
    val NexusBlueDark = Color(0xFFA6D92B)
    val NexusBlueDeep = Color(0xFF6B8A10)

    // Explicit signal names for new code
    val Signal = Color(0xFFC3F53C)
    val SignalBright = Color(0xFFD6FF5E)
    val SignalDeep = Color(0xFF6B8A10)
    val SignalInk = Color(0xFF0F1405)
    val Structure = Color(0xFF4B5BD6)
    val StructureBright = Color(0xFF7C8CFF)
    val Attention = Color(0xFFFF6A3D)
    val Bone = Color(0xFFF3EDE6)
    val Ink = Color(0xFF0A0708)

    // Secondary signals
    val AuroraGreen = Color(0xFF3FE0B0)
    val AuroraGreenDark = Color(0xFF17B183)
    val PlasmaOrange = Color(0xFFFF6A3D)
    val PlasmaOrangeDark = Color(0xFFE5552A)
    val StellarPink = Color(0xFFFF5C8A)
    val StellarPinkDark = Color(0xFFE8457A)
    val QuantumWhite = Color(0xFFF3EDE6)

    // Supporting ultramarine
    val ElectricIndigo = Color(0xFF4B5BD6)
    val ElectricIndigoDark = Color(0xFF3B49B8)

    // Violet/plum ramp (formerly indigo)
    val Indigo50 = Color(0xFFF3EDFB)
    val Indigo100 = Color(0xFFE9DFF7)
    val Indigo200 = Color(0xFFD6C3F0)
    val Indigo300 = Color(0xFFBC9EE6)
    val Indigo400 = Color(0xFF9F76D9)
    val Indigo500 = Color(0xFF8553C9)
    val Indigo600 = Color(0xFF6D3EB0)
    val Indigo700 = Color(0xFF573091)
    val Indigo800 = Color(0xFF432470)
    val Indigo900 = Color(0xFF2E1A47)

    // Periwinkle / ultramarine (legacy "cyan" names)
    val Cyan400 = Color(0xFF7C8CFF)
    val Cyan500 = Color(0xFF5C6BEE)
    val Cyan600 = Color(0xFF4654D4)

    val Emerald400 = Color(0xFF4FE3B4)
    val Emerald500 = Color(0xFF2FD6A0)
    val Emerald600 = Color(0xFF17B183)

    val Amber400 = Color(0xFFFFC24B)
    val Amber500 = Color(0xFFF5A623)
    val Amber600 = Color(0xFFD98708)

    val Rose400 = Color(0xFFFF7A85)
    val Rose500 = Color(0xFFF4546A)
    val Rose600 = Color(0xFFD93A54)

    val Blue400 = Color(0xFF8C9BFF)
    val Blue500 = Color(0xFF6B7CF5)
    val Blue600 = Color(0xFF4F5FE0)

    val Success = AuroraGreen
    val Warning = Amber500
    val Error = Rose500
    val Info = Cyan400

    // Neutrals — warm plum-grey
    val Neutral950 = Color(0xFF08060B)
    val Neutral900 = Color(0xFF0C0910)
    val Neutral850 = Color(0xFF120E17)
    val Neutral800 = Color(0xFF191320)
    val Neutral750 = Color(0xFF211A2B)
    val Neutral700 = Color(0xFF2C2437)
    val Neutral600 = Color(0xFF3F3549)
    val Neutral500 = Color(0xFF5C5260)
    val Neutral400 = Color(0xFF8C8195)
    val Neutral300 = Color(0xFFB5AABC)
    val Neutral200 = Color(0xFFD4CAD9)
    val Neutral100 = Color(0xFFEAE3EC)
    val Neutral50 = Color(0xFFF7F3F5)
    val Neutral0 = Color(0xFFFFFFFF)

    val LinkedInBlue = Color(0xFF0A66C2)
    val GitHubDark = Color(0xFF24292F)
}

/**
 * Tokens that must flip with the theme and therefore cannot be plain constants:
 * hairlines, the inner top-light on a card, and the faint measurement grid.
 */
@Immutable
data class NexusExtendedColors(
    val signal: Color,
    val structure: Color,
    val attention: Color,
    val positive: Color,
    val bone: Color,
    val ink: Color,
    val hairline: Color,
    val hairlineStrong: Color,
    val innerLight: Color,
    val gridLine: Color,
    val signalContainer: Color,
    val onSignalContainer: Color,
    val attentionContainer: Color,
    val onAttentionContainer: Color,
    val structureContainer: Color,
    val onStructureContainer: Color,
)

internal val DarkExtendedColors = NexusExtendedColors(
    signal = NexusColors.Signal,
    structure = NexusColors.StructureBright,
    attention = NexusColors.PlasmaOrange,
    positive = NexusColors.AuroraGreen,
    bone = NexusColors.Bone,
    ink = NexusColors.Ink,
    hairline = Color(0xFF2A2233),
    hairlineStrong = Color(0xFF4A4052),
    innerLight = Color(0x14FFFFFF),
    gridLine = Color(0x0AFFFFFF),
    signalContainer = Color(0xFF22300A),
    onSignalContainer = Color(0xFFD8FF6E),
    attentionContainer = Color(0xFF3A1B0E),
    onAttentionContainer = Color(0xFFFFC4A8),
    structureContainer = Color(0xFF232A5C),
    onStructureContainer = Color(0xFFC9D0FF),
)

internal val LightExtendedColors = NexusExtendedColors(
    signal = NexusColors.SignalDeep,
    structure = NexusColors.Structure,
    attention = Color(0xFFC2410C),
    positive = Color(0xFF0E7A58),
    bone = NexusColors.Bone,
    ink = NexusColors.Ink,
    hairline = Color(0xFFE4DCD2),
    hairlineStrong = Color(0xFFC9BEB1),
    innerLight = Color(0x66FFFFFF),
    gridLine = Color(0x0D2E1A47),
    signalContainer = Color(0xFFDDF58F),
    onSignalContainer = Color(0xFF2A3300),
    attentionContainer = Color(0xFFFBE3D6),
    onAttentionContainer = Color(0xFF7A2A08),
    structureContainer = Color(0xFFDFE3FF),
    onStructureContainer = Color(0xFF1B2470),
)

/**
 * Gradients are reserved for signature surfaces — at most one per screen.
 * Every lime ramp here is light enough to carry ink content; white text on
 * lime is a contrast defect (≈1.3:1), so ink is the only content colour on
 * these grounds.
 */
object NexusGradients {
    /** Signature Ascent ramp: the instrument needle warming from olive to lime. */
    val Ascent = listOf(NexusColors.NexusBlueDark, NexusColors.NexusBlue, NexusColors.SignalBright)
    val Nexus = listOf(NexusColors.NexusBlue, NexusColors.SignalBright)
    val Aurora = listOf(NexusColors.AuroraGreen, NexusColors.Cyan400)
    val Stellar = listOf(NexusColors.StellarPink, NexusColors.PlasmaOrange)
    val Cosmic = listOf(NexusColors.VoidBlack, NexusColors.Carbon)
    val Primary = listOf(NexusColors.NexusBlueDark, NexusColors.NexusBlue)

    /** High-signal lime sweep — brighter terminal than [Primary], still ink-safe. */
    val PrimaryDeep = listOf(NexusColors.NexusBlue, NexusColors.SignalBright)
    val Success = listOf(NexusColors.Emerald500, NexusColors.Emerald400)
    val Warning = listOf(NexusColors.Amber500, NexusColors.Amber400)
    val Error = listOf(NexusColors.Rose500, NexusColors.Rose400)
    val Ocean = listOf(NexusColors.Cyan500, NexusColors.Blue500)
    val Sunset = listOf(NexusColors.Amber500, NexusColors.Rose500)
    val Structure = listOf(NexusColors.ElectricIndigo, NexusColors.Cyan400)
    val Glass = listOf(
        NexusColors.Neutral0.copy(alpha = 0.1f),
        NexusColors.Neutral0.copy(alpha = 0.05f)
    )
}
