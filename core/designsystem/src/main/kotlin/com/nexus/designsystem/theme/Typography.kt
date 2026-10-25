package com.nexus.designsystem.theme

import com.nexus.designsystem.R

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Distinctive typographic voice (the Compose equivalent of a "font pairing"):
 * Plus Jakarta Sans for display/headings — geometric, confident, modern;
 * Inter for body/labels — the most readable UI text face at small sizes.
 * Both are variable fonts bundled in res/font (OFL licensed), so rendering
 * is instant and offline with full weight-axis support.
 */
private fun jakarta(vararg weights: FontWeight) = FontFamily(
    weights.map { Font(R.font.plus_jakarta_sans_variable, weight = it) }
)

private fun inter(vararg weights: FontWeight) = FontFamily(
    weights.map { Font(R.font.inter_variable, weight = it) }
)

private val JakartaDisplay = jakarta(
    FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold,
    FontWeight.Bold, FontWeight.ExtraBold,
)
private val InterBody = inter(
    FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold,
)

object NexusTypography {
    val DisplayLarge = TextStyle(
        fontFamily = JakartaDisplay,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-1.5).sp
    )
    val DisplayMedium = TextStyle(
        fontFamily = JakartaDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.5).sp
    )
    val HeadlineLarge = TextStyle(
        fontFamily = JakartaDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    )
    val HeadlineMedium = TextStyle(
        fontFamily = JakartaDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    )
    val HeadlineSmall = TextStyle(
        fontFamily = JakartaDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    )
    val TitleLarge = TextStyle(
        fontFamily = JakartaDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp
    )
    val TitleMedium = TextStyle(
        fontFamily = JakartaDisplay,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    )
    val TitleSmall = TextStyle(
        fontFamily = JakartaDisplay,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
    val BodyLarge = TextStyle(
        fontFamily = InterBody,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.15.sp
    )
    val BodyMedium = TextStyle(
        fontFamily = InterBody,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.25.sp
    )
    val BodySmall = TextStyle(
        fontFamily = InterBody,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.4.sp
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
        letterSpacing = 0.5.sp
    )
    val LabelSmall = TextStyle(
        fontFamily = InterBody,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.8.sp
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
