package com.nexus.designsystem.component

import androidx.compose.animation.core.animate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.LocalReducedMotion
import com.nexus.designsystem.theme.NexusAnimation
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus

/** One readout in a [TelemetryStrip]. */
data class TelemetryItem(
    val label: String,
    val value: Int,
    val suffix: String = "",
    val accent: Color? = null,
)

/**
 * The dashboard as a spec sheet: each measure sits on its own line, joined to
 * its value by a dotted leader. One housing, no card grid — this is how a
 * calibrated readout behaves, and it lets full labels breathe where four
 * crowded columns would force truncation.
 */
@Composable
fun TelemetryStrip(
    items: List<TelemetryItem>,
    modifier: Modifier = Modifier,
) {
    val extended = MaterialTheme.nexus

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = NexusShapes.lg,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(NexusSpacing.hairline, extended.hairline),
    ) {
        Box {
            // The same top-light every instrument housing carries, so the strip
            // reads as a milled plate rather than a flat rectangle.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(NexusSpacing.hairline)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                extended.innerLight,
                                Color.Transparent,
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier.padding(
                    horizontal = NexusSpacing.gutter,
                    vertical = NexusSpacing.xxs,
                ),
            ) {
                items.forEach { item -> TelemetryRow(item) }
            }
        }
    }
}

@Composable
private fun TelemetryRow(item: TelemetryItem) {
    var animatedValue by remember { mutableIntStateOf(0) }
    val reducedMotion = LocalReducedMotion.current

    LaunchedEffect(item.value) {
        if (reducedMotion) {
            animatedValue = item.value
        } else {
            animate(
                initialValue = animatedValue.toFloat(),
                targetValue = item.value.toFloat(),
                animationSpec = NexusAnimation.Tween.numberCount,
            ) { current, _ -> animatedValue = current.toInt() }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NexusSpacing.xs),
    ) {
        item.accent?.let { tint ->
            // A gauge tick, not a bullet: the measure is on a scale.
            Box(
                modifier = Modifier
                    .size(width = 2.dp, height = 11.dp)
                    .background(tint, RoundedCornerShape(1.dp))
            )
        }
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        LeaderRule(modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "$animatedValue",
                style = NexusTypography.TelemetryLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            if (item.suffix.isNotEmpty()) {
                Text(
                    text = item.suffix,
                    style = NexusTypography.TelemetryMicro,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 2.dp, bottom = 2.dp),
                )
            }
        }
    }
}

@Composable
fun LeaderRule(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.nexus.hairlineStrong,
) {
    Canvas(modifier = modifier.height(2.dp)) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(1.6.dp.toPx(), 5.dp.toPx()),
                phase = 0f,
            ),
        )
    }
}
