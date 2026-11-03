package com.nexus.designsystem.theme

import com.nexus.designsystem.R

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Distinctive typographic voice — four faces, four jobs.
 *
 * Newsreader carries the editorial voice: the statements a screen opens with and
 * the sentences a user is meant to read rather than scan. It is set light and
 * large, which is how a serif earns its place — a heavy serif at 40sp turns into
 * a logo. Plus Jakarta Sans carries UI headings and controls: geometric,
 * confident. Inter carries body copy and labels: the most legible UI face at
 * 12–16sp. JetBrains Mono carries anything a machine measures — scores, counters,
 * deltas, tick labels — so numbers read as instrument output rather than prose.
 *
 * All four ship as variable fonts in res/font (OFL licensed), so rendering is
 * instant and offline with the full weight axis.
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

private fun newsreader(vararg weights: FontWeight) = FontFamily(
    weights.map { Font(R.font.newsreader_variable, weight = it) }
)

private val NewsreaderRoman = newsreader(
    FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold,
)
private val NewsreaderItalic = FontFamily(
    Font(R.font.newsreader_italic_variable, weight = FontWeight.Normal),
    Font(R.font.newsreader_italic_variable, weight = FontWeight.Medium),
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
        fontSize = 42.sp,
        lineHeight = 46.sp,
        letterSpacing = (-1.7).sp,
        fontFeatureSettings = "tnum"
    )
    val DisplayMedium = TextStyle(
        fontFamily = JakartaDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 35.sp,
        lineHeight = 40.sp,
        letterSpacing = (-1.1).sp,
        fontFeatureSettings = "tnum"
    )
    val HeadlineLarge = TextStyle(
        fontFamily = JakartaDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.6).sp
    )
    val HeadlineMedium = TextStyle(
        fontFamily = JakartaDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 23.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.4).sp
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

    /**
     * Field label for a measured value: `SCORE`, `DELTA`, `CURRENT STREAK`.
     * Always sits *above data*, never above a headline — this is a label on an
     * instrument, not a marketing eyebrow.
     */
    val DataLabel = TextStyle(
        fontFamily = InstrumentMono,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        letterSpacing = 1.1.sp,
        fontFeatureSettings = "tnum"
    )

    /** Alias with intent: the number a component is actually reporting. */
    val Readout = TelemetryLarge

    /** The instrument family on its own, for inline spans inside prose — code,
     *  shell commands, identifiers. A span needs a family, not a whole style. */
    val Mono: FontFamily = InstrumentMono

    // ── Editorial voice ──────────────────────────────────────────────────────
    // The serif is the *narrative* layer: it says the thing the instrument
    // measures. It is set light and large with generous leading, because a serif
    // earns authority from size and air, not from weight. Nothing measured lives
    // here — a score in Newsreader would be a score pretending to be prose.

    /** The statement a screen opens with. One per screen, never two. */
    val EditorialDisplay = TextStyle(
        fontFamily = NewsreaderRoman,
        fontWeight = FontWeight.Normal,
        fontSize = 44.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.9).sp,
    )

    /** Section-level statement — still prose, one rung quieter. */
    val EditorialHeadline = TextStyle(
        fontFamily = NewsreaderRoman,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 37.sp,
        letterSpacing = (-0.5).sp,
    )

    /** A lead paragraph: readable measure, relaxed leading. */
    val EditorialLead = TextStyle(
        fontFamily = InterBody,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 27.sp,
        letterSpacing = (-0.15).sp,
    )

    /** Running editorial copy under a headline. */
    val EditorialBody = TextStyle(
        fontFamily = InterBody,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.05.sp,
    )

    /** A voice speaking — pull quotes, AI answers, motivational lines. */
    val EditorialQuote = TextStyle(
        fontFamily = NewsreaderItalic,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.2).sp,
    )

    /** Small serif title where a card wants a byline rather than a label. */
    val EditorialTitle = TextStyle(
        fontFamily = NewsreaderRoman,
        fontWeight = FontWeight.Medium,
        fontSize = 21.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.2).sp,
    )

    /**
     * The aside rung: a sentence of narrative set inside an instrument's card —
     * the line that says *why the dial is empty*, not what the dial reads.
     * Quiet italic, card-caption scale; if it ever carries a number it has
     * stopped being a voice and become a readout.
     */
    val Voice = TextStyle(
        fontFamily = NewsreaderItalic,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
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
