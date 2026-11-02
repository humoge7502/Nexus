package com.nexus.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.LocalReducedMotion
import com.nexus.designsystem.theme.NexusAnimation
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.nexus

/**
 * A surface with registration marks.
 *
 * Every plate carries two calibration cues that a filled card does not: a
 * hairline graticule under the content, and four corner brackets on top of it.
 * Two brackets are lit in signal, two stay neutral, which is enough asymmetry to
 * read as machined rather than printed. Use it for a hero panel, an icon
 * housing, or any surface that should look bolted down.
 */
@Composable
fun InstrumentPlate(
    modifier: Modifier = Modifier,
    shape: Shape = NexusShapes.lg,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    edgeColor: Color = MaterialTheme.nexus.hairlineStrong,
    accent: Color = MaterialTheme.nexus.signal,
    graticuleSpacing: Float = 26f,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit,
) {
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.035f)

    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .drawBehind {
                val step = graticuleSpacing.dp.toPx()
                var x = step
                while (x < size.width) {
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f,
                    )
                    x += step
                }
            }
            .border(1.dp, edgeColor.copy(alpha = 0.75f), shape)
            .drawWithContent {
                drawContent()
                val inset = 9.dp.toPx()
                val len = 9.dp.toPx()
                val stroke = 1.dp.toPx()
                val w = size.width
                val h = size.height
                if (w > (inset + len) * 2 && h > (inset + len) * 2) {
                    val lit = accent.copy(alpha = 0.7f)
                    val idle = edgeColor
                    drawLine(lit, Offset(inset, inset), Offset(inset + len, inset), stroke, StrokeCap.Round)
                    drawLine(lit, Offset(inset, inset), Offset(inset, inset + len), stroke, StrokeCap.Round)
                    drawLine(lit, Offset(w - inset, h - inset), Offset(w - inset - len, h - inset), stroke, StrokeCap.Round)
                    drawLine(lit, Offset(w - inset, h - inset), Offset(w - inset, h - inset - len), stroke, StrokeCap.Round)
                    drawLine(idle, Offset(w - inset, inset), Offset(w - inset - len, inset), stroke, StrokeCap.Round)
                    drawLine(idle, Offset(w - inset, inset), Offset(w - inset, inset + len), stroke, StrokeCap.Round)
                    drawLine(idle, Offset(inset, h - inset), Offset(inset + len, h - inset), stroke, StrokeCap.Round)
                    drawLine(idle, Offset(inset, h - inset), Offset(inset, h - inset - len), stroke, StrokeCap.Round)
                }
            },
        contentAlignment = contentAlignment,
        content = content,
    )
}

/**
 * A pager indicator built as a bank of calibrated rules.
 *
 * Each page is a small scale with graduations and a needle. Passing a page fills
 * its scale; the current page's needle stands taller. It replaces the bouncing
 * dot row: the same mark the gauges use, at indicator scale.
 */
@Composable
fun CalibratedPageScale(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.nexus.signal,
    contentDescription: String? = null,
) {
    if (pageCount <= 0) return
    val idle = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)
    val reducedMotion = LocalReducedMotion.current
    val semanticsModifier = if (contentDescription != null) {
        Modifier.semantics { this.contentDescription = contentDescription }
    } else {
        Modifier
    }

    Row(
        modifier = modifier.then(semanticsModifier),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val active = index == currentPage
            val passed = index < currentPage
            val fill by animateFloatAsState(
                targetValue = if (active || passed) 1f else 0f,
                animationSpec = tween(
                    durationMillis = if (reducedMotion) 0 else NexusAnimation.Duration.fast,
                    easing = NexusAnimation.Easing.emphasizedDecelerate,
                ),
                label = "pageScale",
            )

            Canvas(modifier = Modifier.width(24.dp).height(16.dp)) {
                val ruleY = size.height * 0.40f
                drawLine(idle, Offset(0f, ruleY), Offset(size.width, ruleY), 1.dp.toPx())

                val litWidth = size.width * fill
                if (litWidth > 0f) {
                    drawLine(
                        color = accent.copy(alpha = if (active) 1f else 0.45f),
                        start = Offset(0f, ruleY),
                        end = Offset(litWidth, ruleY),
                        strokeWidth = if (active) 2.5.dp.toPx() else 2.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }

                val ticks = 4
                for (t in 0..ticks) {
                    val x = size.width * t / ticks
                    val major = t == 0 || t == ticks
                    val lit = x <= litWidth + 0.5f
                    drawLine(
                        color = if (lit) accent.copy(alpha = if (active) 0.9f else 0.45f) else idle,
                        start = Offset(x, ruleY),
                        end = Offset(x, ruleY + if (major) 7.dp.toPx() else 4.dp.toPx()),
                        strokeWidth = if (major) 1.5.dp.toPx() else 1.dp.toPx(),
                    )
                }

                if (active) {
                    val needleX = size.width - 1.dp.toPx()
                    drawLine(
                        color = accent,
                        start = Offset(needleX, ruleY - 3.dp.toPx()),
                        end = Offset(needleX, ruleY + 9.dp.toPx()),
                        strokeWidth = 1.5.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }
        }
    }
}
