package com.nexus.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.NexusAnimation
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.motionMillis
import com.nexus.designsystem.theme.nexus
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** One satellite of the career orbit: a real dimension of the user's
 *  progress, its fill as an honest fraction of a stated milestone, and the
 *  mono reading the user would act on. */
data class OrbitDimension(
    val id: String,
    val label: String,
    val fraction: Float,
    val reading: String,
)

/**
 * The career intelligence orbit: the readiness dial in the middle, the
 * dimensions of a career campaign as satellites around it, each filled by
 * its own real progress. It draws the same table the telemetry strip states
 * — the numbers stay mono, the fractions stay honest, and a tap selects a
 * satellite and reads it out below.
 *
 * The center is the user's readiness, so the composition reads from the
 * inside out: what I am worth, then what is moving around it.
 */
@Composable
fun CareerIntelligenceOrbit(
    dimensions: List<OrbitDimension>,
    centerValue: String,
    centerLabel: String,
    hint: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    var selectedId by remember { mutableStateOf<String?>(null) }
    val selected = dimensions.firstOrNull { it.id == selectedId }
    val selectionAlpha by animateFloatAsState(
        targetValue = if (selectedId != null) 1f else 0f,
        animationSpec = tween(durationMillis = motionMillis(NexusAnimation.Duration.fast)),
        label = "orbitSelection",
    )

    Column(modifier) {
        OrbitDial(
            dimensions = dimensions,
            selectedId = selectedId,
            selectionAlpha = selectionAlpha,
            centerValue = centerValue,
            centerLabel = centerLabel,
            contentDescription = contentDescription,
            onSelect = { id -> selectedId = if (id == selectedId) null else id },
        )
        Spacer(Modifier.height(4.dp))
        OrbitReadout(selected = selected, hint = hint)
    }
}

/** The dial: the graduated ring, the satellites filled by their own real
 *  progress, and the readiness at the centre. Tapping near a satellite
 *  selects it; tapping it again lets it go. */
@Composable
private fun OrbitDial(
    dimensions: List<OrbitDimension>,
    selectedId: String?,
    selectionAlpha: Float,
    centerValue: String,
    centerLabel: String,
    contentDescription: String,
    onSelect: (String?) -> Unit,
) {
    val textMeasurer = rememberTextMeasurer()
    val ext = MaterialTheme.nexus
    val scheme = MaterialTheme.colorScheme

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(horizontal = 28.dp, vertical = 8.dp)
            .pointerInput(dimensions) {
                detectTapGestures { tap ->
                    // Nearest satellite wins if the finger lands close
                    // enough to mean it.
                    val side = min(size.width, size.height)
                    val centre = Offset(size.width / 2f, size.height / 2f)
                    val ringRadius = side * 0.33f
                    val step = (2.0 * Math.PI / dimensions.size)
                    var bestId: String? = null
                    var bestDistance = Float.MAX_VALUE
                    dimensions.forEachIndexed { index, dimension ->
                        val angle = -Math.PI / 2 + index * step
                        val node = Offset(
                            centre.x + (ringRadius * cos(angle)).toFloat(),
                            centre.y + (ringRadius * sin(angle)).toFloat(),
                        )
                        val distance = (tap - node).getDistance()
                        if (distance <= side * 0.09f && distance < bestDistance) {
                            bestId = dimension.id
                            bestDistance = distance
                        }
                    }
                    onSelect(bestId)
                }
            }
            .semantics { this.contentDescription = contentDescription },
    ) {
        val side = min(size.width, size.height)
        val centre = Offset(size.width / 2f, size.height / 2f)
        val ringRadius = side * 0.33f
        val step = (2.0 * Math.PI / dimensions.size)

        // The ring is a dial: sixty graduations mark real positions on it,
        // drawn in the faint grid tone so they read as structure.
        repeat(60) { tick ->
            val angle = tick * (2.0 * Math.PI / 60)
            val inner = ringRadius - side * 0.012f
            val outer = ringRadius + side * 0.012f
            drawLine(
                color = ext.gridLine,
                start = Offset(
                    centre.x + (inner * cos(angle)).toFloat(),
                    centre.y + (inner * sin(angle)).toFloat(),
                ),
                end = Offset(
                    centre.x + (outer * cos(angle)).toFloat(),
                    centre.y + (outer * sin(angle)).toFloat(),
                ),
                strokeWidth = 1.5f,
            )
        }

        dimensions.forEachIndexed { index, dimension ->
            val angle = -Math.PI / 2 + index * step
            val node = Offset(
                centre.x + (ringRadius * cos(angle)).toFloat(),
                centre.y + (ringRadius * sin(angle)).toFloat(),
            )
            val isSelected = dimension.id == selectedId
            val fill = dimension.fraction.coerceIn(0f, 1f)

            // The satellite: a track, then the progress itself as the lit
            // core — bigger means further along, never decoration.
            drawCircle(
                color = scheme.surfaceContainerHigh,
                radius = side * 0.045f,
                center = node,
            )
            drawCircle(
                color = ext.signal,
                radius = side * (0.012f + 0.030f * fill),
                center = node,
            )
            if (isSelected) {
                drawCircle(
                    color = ext.signal.copy(alpha = selectionAlpha),
                    radius = side * 0.062f,
                    center = node,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
                )
            }

            drawMonoLabel(
                text = dimension.label,
                textStyle = NexusTypography.DataLabel,
                weight = FontWeight.Medium,
                color = scheme.onSurfaceVariant,
                textMeasurer = textMeasurer,
                centre = Offset(
                    centre.x + ((ringRadius + side * 0.085f) * cos(angle)).toFloat(),
                    centre.y + ((ringRadius + side * 0.085f) * sin(angle)).toFloat(),
                ),
            )
        }

        drawMonoLabel(
            text = centerValue,
            textStyle = NexusTypography.TelemetryLarge,
            weight = FontWeight.Bold,
            color = scheme.onSurface,
            textMeasurer = textMeasurer,
            centre = Offset(centre.x, centre.y - side * 0.02f),
        )
        drawMonoLabel(
            text = centerLabel,
            textStyle = NexusTypography.DataLabel,
            weight = FontWeight.Medium,
            color = scheme.onSurfaceVariant,
            textMeasurer = textMeasurer,
            centre = Offset(centre.x, centre.y + side * 0.045f),
        )
    }
}

/** A mono label centred on [centre] — the only way text lands on a Canvas
 *  without fighting the layout system for its position. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMonoLabel(
    text: String,
    textStyle: TextStyle,
    weight: FontWeight,
    color: androidx.compose.ui.graphics.Color,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    centre: Offset,
) {
    val layout = textMeasurer.measure(
        text = text.uppercase(),
        style = textStyle.copy(
            fontFamily = NexusTypography.Mono,
            fontWeight = weight,
            color = color,
        ),
    )
    drawText(
        textLayoutResult = layout,
        topLeft = Offset(
            centre.x - layout.size.width / 2f,
            centre.y - layout.size.height / 2f,
        ),
    )
}

/** The reading: the selected satellite's number over its calibrated rule,
 *  or the invitation while nothing is selected. Lives below the dial so
 *  the tap target and the readout never fight for the same pixels. */
@Composable
private fun OrbitReadout(selected: OrbitDimension?, hint: String) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        if (selected != null) {
            Text(
                text = selected.reading,
                style = NexusTypography.TelemetryLarge,
                color = scheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(6.dp))
            CalibratedRule(
                progress = selected.fraction,
                modifier = Modifier.fillMaxWidth(),
                contentDescription = selected.label,
            )
        } else {
            Text(
                text = hint,
                style = NexusTypography.Voice,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
