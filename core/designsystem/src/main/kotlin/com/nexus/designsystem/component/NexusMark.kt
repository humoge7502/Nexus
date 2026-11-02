package com.nexus.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus

/**
 * The NEXUS mark, drawn rather than typed.
 *
 * A monogram N sits inside four registration brackets — the same marks a
 * calibration plate uses to locate a part. The diagonal is split: the upper
 * segment is lit in signal, so the glyph always carries one live reading. It is
 * vector-drawn, so it stays crisp from a 20dp header glyph to a hero mark.
 *
 * Decorative by default; pass [contentDescription] only when the mark is the
 * sole label for a control.
 */
@Composable
fun NexusMark(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    accent: Color = MaterialTheme.nexus.signal,
    contentDescription: String? = null,
) {
    val semanticsModifier = if (contentDescription != null) {
        Modifier.semantics { this.contentDescription = contentDescription }
    } else {
        Modifier
    }

    Canvas(modifier = modifier.size(size).then(semanticsModifier)) {
        val w = this.size.width
        val h = this.size.height
        val hairline = 1.dp.toPx()
        val glyphStroke = (w * 0.055f).coerceAtLeast(1.4.dp.toPx())

        // Registration brackets: four corners, each an L that stops short of
        // the edge. They frame the glyph without closing it into a box.
        val inset = w * 0.08f
        val bracket = w * 0.15f
        val bracketColor = tint.copy(alpha = 0.55f)
        fun corner(x: Float, y: Float, dx: Float, dy: Float) {
            drawLine(bracketColor, Offset(x, y), Offset(x + dx, y), hairline, StrokeCap.Round)
            drawLine(bracketColor, Offset(x, y), Offset(x, y + dy), hairline, StrokeCap.Round)
        }
        corner(inset, inset, bracket, bracket)
        corner(w - inset, inset, -bracket, bracket)
        corner(inset, h - inset, bracket, -bracket)
        corner(w - inset, h - inset, -bracket, -bracket)

        val left = w * 0.33f
        val right = w * 0.67f
        val top = h * 0.30f
        val bottom = h * 0.70f
        val midX = (left + right) / 2f
        val midY = (top + bottom) / 2f
        val gapX = w * 0.035f
        val gapY = h * 0.035f

        // Rails.
        drawLine(tint, Offset(left, bottom), Offset(left, top), glyphStroke, StrokeCap.Round)
        drawLine(tint, Offset(right, bottom), Offset(right, top), glyphStroke, StrokeCap.Round)

        // Diagonal, broken at the midpoint: the lower half is structure, the
        // upper half is the live signal, ending in the terminal bead.
        drawLine(tint, Offset(left, bottom), Offset(midX - gapX, midY + gapY), glyphStroke, StrokeCap.Round)
        drawLine(accent, Offset(midX + gapX, midY - gapY), Offset(right, top), glyphStroke, StrokeCap.Round)
        drawCircle(accent, radius = w * 0.045f, center = Offset(right, top))
    }
}

/**
 * Header wordmark: the mark, a tracked NEXUS label, then a rule that runs to the
 * far edge so the header reads as a calibrated strip rather than a logo lockup.
 * Trailing actions (skip, step counter) are placed in [content].
 */
@Composable
fun NexusWordmark(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit = {},
) {
    val ext = MaterialTheme.nexus
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NexusMark(size = 20.dp)
        Spacer(modifier = Modifier.width(NexusSpacing.xs))
        Text(
            text = label,
            style = NexusTypography.DataLabel.copy(fontSize = 11.sp, letterSpacing = 2.6.sp),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.width(NexusSpacing.sm))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(NexusSpacing.hairline)
                .background(
                    Brush.horizontalGradient(listOf(ext.hairlineStrong, Color.Transparent)),
                ),
        )
        content()
    }
}
