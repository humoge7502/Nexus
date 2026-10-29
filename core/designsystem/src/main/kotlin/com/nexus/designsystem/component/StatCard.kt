package com.nexus.designsystem.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.designsystem.theme.NexusTypography

/**
 * Quiet dashboard tile: the label reads first, the value carries the mass,
 * and an optional [accent] tints only the icon — never the whole surface.
 */
@Composable
fun AnimatedStatCard(
    value: Int,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    suffix: String = "",
    accent: Color? = null,
) {
    var animatedValue by remember { mutableIntStateOf(0) }

    LaunchedEffect(value) {
        animate(
            initialValue = 0f,
            targetValue = value.toFloat(),
            animationSpec = tween(1200, easing = FastOutSlowInEasing)
        ) { currentValue, _ ->
            animatedValue = currentValue.toInt()
        }
    }

    NexusCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = NexusTypography.TelemetryMicro,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (icon != null) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = (accent ?: MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.8f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(Modifier.height(9.dp))
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$animatedValue",
                    style = NexusTypography.Stat.copy(fontSize = 26.sp, lineHeight = 30.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                if (suffix.isNotEmpty()) {
                    Text(
                        text = suffix,
                        style = NexusTypography.TelemetryMicro,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 3.dp, bottom = 4.dp),
                    )
                }
            }
        }
    }
}
