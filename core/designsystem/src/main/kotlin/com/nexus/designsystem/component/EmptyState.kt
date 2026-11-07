package com.nexus.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.nexus.designsystem.theme.NexusTypography

@Composable
@Suppress("ComposableLambdaParameterNaming")
fun EmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    lottieResId: Int? = null,
    icon: @Composable (() -> Unit)? = null,
    art: EmptyStateArt? = null,
    /** The aside in the product's serif voice — why this space is quiet, in
     *  one sentence. Null keeps the empty state purely informational. */
    voice: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when {
            lottieResId != null -> {
                val composition = rememberLottieComposition(LottieCompositionSpec.RawRes(lottieResId))
                // IterateForever is an infinite animation, so it obeys the
                // design-system rule: under reduced motion the art plays once
                // and settles on its end frame instead of looping forever.
                val reducedMotion = com.nexus.designsystem.theme.LocalReducedMotion.current
                val progress = animateLottieCompositionAsState(
                    composition = composition.value,
                    iterations = if (reducedMotion) 1 else LottieConstants.IterateForever
                )
                LottieAnimation(
                    composition = composition.value,
                    progress = { progress.value },
                    modifier = Modifier.size(200.dp)
                )
            }
            icon != null -> icon()
            art != null -> EmptyArtPlate(art)
            else -> EmptyMark()
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (voice != null) {
            Text(
                text = voice,
                style = NexusTypography.Voice,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            NexusButton(
                text = actionLabel,
                onClick = onAction,
                modifier = Modifier.widthIn(max = 280.dp),
            )
        }
    }
}

@Composable
private fun EmptyMark() {
    val track = MaterialTheme.colorScheme.outlineVariant
    val signal = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(72.dp)) {
            val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            val inset = stroke.width / 2
            drawArc(
                color = track,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke.width, size.height - stroke.width),
                style = stroke,
            )
            drawArc(
                color = signal,
                startAngle = -90f,
                sweepAngle = 110f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke.width, size.height - stroke.width),
                style = stroke,
            )
            drawCircle(color = signal, radius = 3.dp.toPx())
        }
    }
}

/** Named instrument drawings for the three recurring quiet states. Each is
 *  hairline ink with exactly one signal mark — the illustration equivalent
 *  of the empty state's voice: it says what the space is FOR, not that it
 *  is empty. Static by construction, so reduced motion needs no gate. */
enum class EmptyStateArt {
    /** The applications ledger before its first entry: ruled rows, the
     *  first checkbox drawn in signal, waiting. */
    Ledger,
    /** The opportunity radar mid-sweep: rings and graticule, no blips yet. */
    Radar,
    /** The learning dial at zero: graduated ticks, the first one lit. */
    Progress,
}

@Composable
private fun EmptyArtPlate(art: EmptyStateArt) {
    val track = MaterialTheme.colorScheme.outlineVariant
    val faint = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    val signal = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(72.dp)) {
            val ink = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            when (art) {
                EmptyStateArt.Ledger -> {
                    val left = size.width * 0.14f
                    val right = size.width * 0.86f
                    listOf(0.28f, 0.52f, 0.76f).forEachIndexed { index, fraction ->
                        val y = size.height * fraction
                        drawLine(
                            color = if (index == 0) faint else track,
                            start = Offset(left, y),
                            end = Offset(right, y),
                            strokeWidth = ink.width,
                            cap = StrokeCap.Round,
                        )
                    }
                    // The first row's checkbox: signal-outlined, empty —
                    // the ledger asking for its first entry.
                    val box = size.minDimension * 0.13f
                    drawRoundRect(
                        color = signal,
                        topLeft = Offset(left, size.height * 0.28f - box / 2),
                        size = Size(box, box),
                        style = ink,
                    )
                }
                EmptyStateArt.Radar -> {
                    val inset = ink.width / 2
                    val ring = Size(size.width - ink.width, size.height - ink.width)
                    drawArc(track, 0f, 360f, false, Offset(inset, inset), ring, style = ink)
                    val inner = Size(ring.width * 0.55f, ring.height * 0.55f)
                    val innerOffset = Offset(
                        (size.width - inner.width) / 2f,
                        (size.height - inner.height) / 2f,
                    )
                    drawArc(faint, 0f, 360f, false, innerOffset, inner, style = ink)
                    val cx = size.width / 2f
                    drawLine(
                        color = faint,
                        start = Offset(cx, inset),
                        end = Offset(cx, size.height - inset),
                        strokeWidth = 1f,
                    )
                    // The sweep itself: a signal arc frozen mid-rotation,
                    // the instrument doing its work before anything appears.
                    drawArc(
                        color = signal,
                        startAngle = -60f,
                        sweepAngle = 42f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = ring,
                        style = ink,
                    )
                }
                EmptyStateArt.Progress -> {
                    val inset = ink.width / 2
                    val ring = Size(size.width - ink.width, size.height - ink.width)
                    val radius = ring.minDimension / 2f
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    // Graduated ticks around the dial, the first three lit:
                    // the instrument at zero, already calibrated.
                    val tickCount = 24
                    repeat(tickCount) { tick ->
                        val angle = Math.toRadians((tick * 360.0 / tickCount) - 90.0)
                        val inner = radius * 0.82f
                        val outer = radius * (if (tick < 3) 0.98f else 0.92f)
                        drawLine(
                            color = if (tick < 3) signal else track,
                            start = Offset(
                                cx + inner * cos(angle).toFloat(),
                                cy + inner * sin(angle).toFloat(),
                            ),
                            end = Offset(
                                cx + outer * cos(angle).toFloat(),
                                cy + outer * sin(angle).toFloat(),
                            ),
                            strokeWidth = if (tick < 3) ink.width else 1.5f,
                            cap = StrokeCap.Round,
                        )
                    }
                }
            }
        }
    }
}
