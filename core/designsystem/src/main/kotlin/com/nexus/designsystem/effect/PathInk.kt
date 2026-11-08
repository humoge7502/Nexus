package com.nexus.designsystem.effect

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Inks a stroke along a path, in step with a reveal clock.
 *
 * The instrument vocabulary already treats arrival as drafting — a gauge sweeps,
 * a headline lands in words — and this extends that grammar to a plate's own
 * frame: the border is *drawn on*, like a pen tracing the drawing's outline,
 * rather than fading in with everything else. Combined with [revealClock] the
 * caller picks the window, so an inked frame lands while its contents still
 * stream in, and one gesture carries the plate.
 *
 * The fraction is the caller's reveal value (usually `revealFraction(clock,
 * window)`), so under reduced motion it arrives at 1 on the first frame and the
 * full frame is simply present.
 *
 * Draw cost: the traced geometry is cached against size; the per-frame work is
 * one path segment extraction, bounded to the reveal window — settled frames
 * allocate nothing.
 *
 * @param inked fraction of the path to draw, 0 before, 1 after
 * @param accent the ink colour; a hairline ink reads as calibration, never glow
 * @param path the outline to trace, built against the laid-out [Size]
 */
fun Modifier.inkedPath(
    inked: Float,
    accent: Color,
    path: (Size) -> Path,
    strokeWidth: Dp = 1.dp,
    cap: StrokeCap = StrokeCap.Round,
): Modifier = drawWithCache {
    val frame = path(size)
    val measure = PathMeasure()
    measure.setPath(frame, false)
    val length = measure.length
    val drawn = Path()
    val stroke = Stroke(width = strokeWidth.toPx(), cap = cap)

    onDrawWithContent {
        drawContent()
        if (length <= 0f) return@onDrawWithContent
        val fraction = inked.coerceIn(0f, 1f)
        if (fraction <= 0f) return@onDrawWithContent
        drawn.rewind()
        measure.getSegment(0f, length * fraction, drawn, startWithMoveTo = true)
        if (!drawn.isEmpty) {
            drawPath(drawn, accent, style = stroke)
        }
    }
}

/**
 * The four registration brackets of an instrument plate, arriving in order.
 *
 * Brackets stamp on only after the frame they belong to has been inked, in
 * reading order (top-left, top-right, bottom-left, bottom-right), so the
 * registration reads as placed, not as printed. At [inked] = 1 the full set is
 * present; at 0 none are. The settled state keeps the plate's standing
 * two-lit/two-idle asymmetry: top-left and bottom-right speak in signal.
 */
fun Modifier.inkedBrackets(
    inked: Float,
    lit: Color,
    idle: Color,
    inset: Dp = 9.dp,
    length: Dp = 9.dp,
    strokeWidth: Dp = 1.dp,
): Modifier = drawWithCache {
    val markInset = inset.toPx()
    val markLength = length.toPx()
    val stroke = strokeWidth.toPx()
    val fraction = inked.coerceIn(0f, 1f)

    onDrawWithContent {
        drawContent()
        val w = size.width
        val h = size.height
        if (w <= (markInset + markLength) * 2 || h <= (markInset + markLength) * 2) {
            return@onDrawWithContent
        }

        // Each bracket owns one quarter of the arrival. A bracket that has not
        // started yet stays invisible; one in flight fades up; a finished one
        // restores the plate's standing asymmetry.
        for (bracket in 0 until 4) {
            val local = ((fraction - bracket * 0.25f) / 0.25f).coerceIn(0f, 1f)
            if (local <= 0f) continue
            val isLit = bracket == 0 || bracket == 3
            val color = (if (isLit) lit else idle).copy(alpha = local)
            when (bracket) {
                0 -> {
                    drawLine(color, Offset(markInset, markInset), Offset(markInset + markLength, markInset), stroke, StrokeCap.Round)
                    drawLine(color, Offset(markInset, markInset), Offset(markInset, markInset + markLength), stroke, StrokeCap.Round)
                }
                1 -> {
                    drawLine(color, Offset(w - markInset, markInset), Offset(w - markInset - markLength, markInset), stroke, StrokeCap.Round)
                    drawLine(color, Offset(w - markInset, markInset), Offset(w - markInset, markInset + markLength), stroke, StrokeCap.Round)
                }
                2 -> {
                    drawLine(color, Offset(markInset, h - markInset), Offset(markInset + markLength, h - markInset), stroke, StrokeCap.Round)
                    drawLine(color, Offset(markInset, h - markInset), Offset(markInset, h - markInset - markLength), stroke, StrokeCap.Round)
                }
                else -> {
                    drawLine(color, Offset(w - markInset, h - markInset), Offset(w - markInset - markLength, h - markInset), stroke, StrokeCap.Round)
                    drawLine(color, Offset(w - markInset, h - markInset), Offset(w - markInset, h - markInset - markLength), stroke, StrokeCap.Round)
                }
            }
        }
    }
}

/** A rounded-rect outline for [inkedPath] frame reveals. */
fun plateFramePath(radius: Float = 0f): (Size) -> Path = { size ->
    Path().apply {
        if (radius <= 0f) {
            addRect(Rect(0f, 0f, size.width, size.height))
        } else {
            addRoundRect(RoundRect(0f, 0f, size.width, size.height, CornerRadius(radius, radius)))
        }
    }
}
