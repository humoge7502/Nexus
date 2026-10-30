package com.nexus.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.NexusAnimation
import com.nexus.designsystem.theme.motionMillis
import com.nexus.designsystem.theme.nexus
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * One node of the [SkillConstellation]: a skill category with its normalised
 * proficiency ([value] in 0f..1f).
 */
data class ConstellationNode(
    val label: String,
    val value: Float,
)

/**
 * The career constellation: skill categories as tappable nodes on a star-map —
 * the plan's signature interactive skill visualisation.
 *
 * Nodes sit on a ring at equal angles; the spoke fill strength encodes
 * proficiency. Tapping a node selects it (the caller renders the detail as
 * real text). The map draws in with one left-to-right sweep; with reduced
 * motion the sweep is instant. Accessibility: the canvas exposes a summary
 * content description listing every node.
 */
@Composable
fun SkillConstellation(
    nodes: List<ConstellationNode>,
    selected: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val extended = MaterialTheme.nexus
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { revealed = true }
    val reveal by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(
            durationMillis = motionMillis(NexusAnimation.Duration.long),
            easing = NexusAnimation.Easing.emphasizedDecelerate,
        ),
        label = "constellationReveal",
    )

    val summary = remember(nodes) {
        if (nodes.isEmpty()) {
            "Skill map"
        } else {
            "Skill map: " + nodes.joinToString { "${it.label} ${(it.value * 100).toInt()} percent" }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .semantics { contentDescription = summary }
            .pointerInput(nodes) {
                detectTapGestures { tap ->
                    if (nodes.isEmpty()) return@detectTapGestures
                    val w = size.width
                    val h = size.height
                    val center = Offset(w / 2f, h / 2f)
                    val ringRadius = min(w, h) / 2f * 0.72f
                    val step = 2.0 * PI / nodes.size
                    var best = -1
                    var bestDist = Double.MAX_VALUE
                    nodes.forEachIndexed { i, _ ->
                        val angle = -PI / 2 + i * step
                        val node = Offset(
                            center.x + (ringRadius * cos(angle)).toFloat(),
                            center.y + (ringRadius * sin(angle)).toFloat(),
                        )
                        val d = (tap - node).getDistance().toDouble()
                        if (d < bestDist) {
                            bestDist = d
                            best = i
                        }
                    }
                    if (best >= 0 && bestDist <= 28.dp.toPx()) onSelect(best)
                }
            },
    ) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)
        val ringRadius = min(w, h) / 2f * 0.72f

        // Orbital guides: two concentric dashed rings.
        val gridEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 10f))
        drawCircle(
            color = extended.gridLine,
            radius = ringRadius,
            center = center,
            style = Stroke(width = 1f, pathEffect = gridEffect),
        )
        drawCircle(
            color = extended.gridLine,
            radius = ringRadius * 0.5f,
            center = center,
            style = Stroke(width = 1f, pathEffect = gridEffect),
        )

        if (nodes.isEmpty()) return@Canvas
        val step = 2.0 * PI / nodes.size

        // Spokes from center to each node; the bright run encodes proficiency.
        nodes.forEachIndexed { i, node ->
            val angle = -PI / 2 + i * step
            val end = Offset(
                center.x + (ringRadius * cos(angle)).toFloat(),
                center.y + (ringRadius * sin(angle)).toFloat(),
            )
            drawLine(color = extended.hairlineStrong, start = center, end = end)
            drawLine(
                color = extended.signal.copy(alpha = 0.55f),
                start = center,
                end = Offset(
                    center.x + (ringRadius * node.value.coerceIn(0f, 1f) * cos(angle)).toFloat(),
                    center.y + (ringRadius * node.value.coerceIn(0f, 1f) * sin(angle)).toFloat(),
                ),
                strokeWidth = 3.dp.toPx(),
            )
        }

        // Reveal wipe: draw the left fraction of the map.
        clipRect(right = w * reveal) {
            nodes.forEachIndexed { i, _ ->
                val angle = -PI / 2 + i * step
                val nodeOffset = Offset(
                    center.x + (ringRadius * cos(angle)).toFloat(),
                    center.y + (ringRadius * sin(angle)).toFloat(),
                )
                val isSelected = i == selected
                drawCircle(
                    color = extended.signalContainer,
                    radius = if (isSelected) 16.dp.toPx() else 12.dp.toPx(),
                    center = nodeOffset,
                )
                drawCircle(
                    color = extended.signal,
                    radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                    center = nodeOffset,
                )
            }
        }
    }
}
