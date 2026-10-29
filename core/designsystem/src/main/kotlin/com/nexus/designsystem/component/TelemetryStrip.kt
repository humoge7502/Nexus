package com.nexus.designsystem.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.NexusTypography

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
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.9f),
        ),
    ) {
        Box {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.09f),
                                Color.Transparent,
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                items.forEach { item -> TelemetryRow(item) }
            }
        }
    }
}

@Composable
private fun TelemetryRow(item: TelemetryItem) {
    var animatedValue by remember { mutableIntStateOf(0) }
    LaunchedEffect(item.value) {
        animate(
            initialValue = 0f,
            targetValue = item.value.toFloat(),
            animationSpec = tween(900, easing = FastOutSlowInEasing),
        ) { current, _ -> animatedValue = current.toInt() }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item.accent?.let { tint ->
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(tint)
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
    color: Color = MaterialTheme.colorScheme.outlineVariant,
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
