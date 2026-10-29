package com.nexus.designsystem.theme

import com.nexus.designsystem.R

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Distinctive typographic voice — three faces, three jobs.
 *
 * Plus Jakarta Sans carries display and headings: geometric, confident.
 * Inter carries body copy and labels: the most legible UI face at 12–16sp.
 * JetBrains Mono carries anything a machine measures — scores, counters,
 * deltas, tick labels — so numbers read as instrument output rather than
 * prose. All three ship as variable fonts in res/font (OFL licensed), so
 * rendering is instant and offline with the full weight axis.
 */
private fun jakarta(vararg weights: FontWeight) = FontFamily(
    weights.map { Font(R.font.plus_jakarta_sans_variable, weight = it) }
)

private fun inter(vararg weights: FontWeight) = FontFamily(
    weights.map { Font(R.font.inter_variable, weight = it) }
)

private fun mono(vararg weights: FontWeight) = FontFamily(
    weights.map { Font(R.font.jetbrains_mono_variable, weight = it) }
)

private val JakartaDisplay = jakarta(
    FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold,
    FontWeight.Bold, FontWeight.ExtraBold,
)
private val InterBody = inter(
    FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold,
)
private val InstrumentMono = mono(
    FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold,
)

object NexusTypography {
    val DisplayLarge = TextStyle(
        fontFamily = JakartaDisplay,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 40.sp,
        lineHeight = 46.sp,
        letterSpacing = (-1.2).sp,
        fontFeatureSettings = "tnum"
    )
    val DisplayMedium = TextStyle(
        fontFamily = JakartaDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 41.sp,
        letterSpacing = (-0.6).sp,
        fontFeatureSettings = "tnum"
    )
    val HeadlineLarge = TextStyle(
        fontFamily = JakartaDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 35.sp,
        letterSpacing = (-0.3).sp
    )
    val HeadlineMedium = TextStyle(
        fontFamily = JakartaDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 31.sp,
        letterSpacing = (-0.2).sp
    )
    val HeadlineSmall = TextStyle(
        fontFamily = JakartaDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 27.sp,
        letterSpacing = (-0.1).sp
    )
    val TitleLarge = TextStyle(
        fontFamily = JakartaDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.1).sp
    )
    val TitleMedium = TextStyle(
        fontFamily = JakartaDisplay,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 23.sp
    )
    val TitleSmall = TextStyle(
        fontFamily = JakartaDisplay,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.05.sp
    )
    val BodyLarge = TextStyle(
        fontFamily = InterBody,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.1).sp
    )
    val BodyMedium = TextStyle(
        fontFamily = InterBody,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = (-0.05).sp
    )
    val BodySmall = TextStyle(
        fontFamily = InterBody,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.1.sp
    )
    val LabelLarge = TextStyle(
        fontFamily = InterBody,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
    val LabelMedium = TextStyle(
        fontFamily = InterBody,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp
    )
    val LabelSmall = TextStyle(
        fontFamily = InterBody,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.3.sp
    )

    /** Tabular numerals for dashboard values so columns never wobble. */
    val Stat = TextStyle(
        fontFamily = InstrumentMono,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 34.sp,
        letterSpacing = (-1.0).sp,
        fontFeatureSettings = "tnum"
    )

    // Instrument readouts. Mono is the material here, not decoration:
    // every glyph shares a cell, so figures align down a column and the
    // screen reads like calibrated equipment.
    val TelemetryDisplay = TextStyle(
        fontFamily = InstrumentMono,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 42.sp,
        letterSpacing = (-2.0).sp,
        fontFeatureSettings = "tnum"
    )
    val TelemetryLarge = TextStyle(
        fontFamily = InstrumentMono,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.4).sp,
        fontFeatureSettings = "tnum"
    )
    val Telemetry = TextStyle(
        fontFamily = InstrumentMono,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp,
        fontFeatureSettings = "tnum"
    )
    val TelemetryMicro = TextStyle(
        fontFamily = InstrumentMono,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.5.sp,
        fontFeatureSettings = "tnum"
    )

    val Material = Typography(
        displayLarge = DisplayLarge,
        displayMedium = DisplayMedium,
        headlineLarge = HeadlineLarge,
        headlineMedium = HeadlineMedium,
        headlineSmall = HeadlineSmall,
        titleLarge = TitleLarge,
        titleMedium = TitleMedium,
        titleSmall = TitleSmall,
        bodyLarge = BodyLarge,
        bodyMedium = BodyMedium,
        bodySmall = BodySmall,
        labelLarge = LabelLarge,
        labelMedium = LabelMedium,
        labelSmall = LabelSmall,
    )
}
