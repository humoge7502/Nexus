package com.nexus.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.LocalReducedMotion
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus
import java.util.Locale

/**
 * A single instrument reading.
 *
 * The number is the reading; [onArc] publishes the same value (0–100) to the
 * caller's shared reveal clock so a surrounding [InstrumentGauge] and these
 * digits sweep together instead of on two independent animations. With no
 * gauge supplied the component draws its own hairline rule beneath the value,
 * which is what the previews and standalone uses want.
 *
 * Under [LocalReducedMotion] the value is rendered at its final state with no
 * travel, per the design system's motion rules.
 */
@Composable
fun SweepReadout(
    score: Float,
    modifier: Modifier = Modifier,
    decimals: Int = 1,
    unit: String? = null,
    style: TextStyle = NexusTypography.Stat,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onArc: ((Float) -> Unit)? = null,
    contentDescription: String? = null,
) {
    val target = (score / 100f).coerceIn(0f, 1f)
    val progress by animateFloatAsState(
        targetValue = if (LocalReducedMotion.current) target else target,
        animationSpec = tween(
            durationMillis = if (LocalReducedMotion.current) 0 else 700,
        ),
        label = "sweepReadout",
    )
    val safeScore = score.coerceIn(0f, 100f)
    val formatted = formatSweep(safeScore, decimals)
    val spoken = contentDescription
        ?: buildString {
            append(formatted)
            unit?.let { append(it) }
            append(" out of 100")
        }
    // Resolve theme colours before entering DrawScope: MaterialTheme is
    // @Composable and cannot be read from a draw lambda.
    val hairline = MaterialTheme.nexus.hairline
    val signal = MaterialTheme.nexus.signal
    val signalBright = NexusColors.SignalBright

    LaunchedEffect(onArc, safeScore) {
        onArc?.invoke(safeScore)
    }

    Column(modifier = modifier) {
        Text(
            text = if (unit.isNullOrBlank()) formatted else "$formatted$unit",
            style = style,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
        if (onArc == null) {
            Spacer(Modifier.height(8.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clearAndSetSemantics { this.contentDescription = spoken },
            ) {
                val stroke = 2.dp.toPx()
                val y = size.height / 2f
                val startX = stroke / 2f
                val endX = size.width - stroke / 2f
                drawLine(
                    color = hairline,
                    start = Offset(startX, y),
                    end = Offset(endX, y),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
                val tip = startX + (endX - startX) * progress
                drawLine(
                    color = signal,
                    start = Offset(startX, y),
                    end = Offset(tip, y),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
                // A tick marks the exact reading instead of leaving the end of
                // the fill to be estimated against the rule.
                drawCircle(
                    color = signalBright,
                    radius = 3.dp.toPx(),
                    center = Offset(tip, y),
                )
                drawArc(
                    color = signal.copy(alpha = 0.35f),
                    startAngle = -140f,
                    sweepAngle = 100f * progress,
                    useCenter = false,
                    topLeft = Offset(size.width - 22.dp.toPx(), -6.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(18.dp.toPx(), 18.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round),
                )
            }
        }
    }
}

/**
 * Pure formatting for the readout string, kept free of Compose runtime so
 * JVM tests can pin the rounding contract. `decimals` is coerced to 0..6;
 * `Locale.US` pins the decimal separator; negative zero never renders.
 */
fun formatSweep(value: Float, decimals: Int = 1, unit: String? = null): String {
    val safeDecimals = decimals.coerceIn(0, 6)
    val digits = String.format(Locale.US, "%.${safeDecimals}f", value)
    val normalized = if (digits.startsWith("-0") && digits.toDoubleOrNull() == 0.0) {
        digits.removePrefix("-")
    } else {
        digits
    }
    return if (unit.isNullOrEmpty()) normalized else normalized + unit
}

/** Half-up integer rendering for percentage-style readouts. */
fun formatSweepInt(value: Float): String = kotlin.math.round(value).toInt().toString()

