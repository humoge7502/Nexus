package com.nexus.designsystem.component

import androidx.compose.animation.core.animate
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.designsystem.theme.LocalReducedMotion
import com.nexus.designsystem.theme.NexusAnimation
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus
import java.util.Locale

/**
 * Dashboard tile built like a small instrument panel: the field label sits
 * above a hairline, the figure carries the mass, and the [accent] tints only
 * the icon and the label tick — never the whole surface.
 *
 * [NexusCard] already supplies the gutter padding, so this composable adds
 * none; nesting padding here is what previously produced 32dp of dead space.
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
    val reducedMotion = LocalReducedMotion.current
    var animatedValue by remember { mutableIntStateOf(value) }

    LaunchedEffect(value, reducedMotion) {
        if (reducedMotion) {
            animatedValue = value
            return@LaunchedEffect
        }
        // Animate from wherever the figure currently sits so a refresh
        // nudges the number instead of resetting it to zero.
        animate(
            initialValue = animatedValue.toFloat(),
            targetValue = value.toFloat(),
            animationSpec = NexusAnimation.Tween.numberCount,
        ) { currentValue, _ ->
            animatedValue = currentValue.toInt()
        }
    }

    NexusCard(
        modifier = modifier,
        shape = NexusShapes.md,
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = label.uppercase(Locale.ROOT),
                    style = NexusTypography.DataLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    // Equal-height tiles: the longest label in the row sets
                    // the height and short labels reserve the same two lines.
                    minLines = 2,
                    maxLines = 2,
                    modifier = Modifier.weight(1f),
                )
                if (icon != null) {
                    Spacer(Modifier.width(NexusSpacing.xxs))
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent ?: MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            Spacer(Modifier.height(NexusSpacing.xs))
            HorizontalDivider(
                thickness = NexusSpacing.hairline,
                color = MaterialTheme.nexus.hairlineStrong,
            )
            Spacer(Modifier.height(NexusSpacing.xs))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Start,
            ) {
                Text(
                    text = animatedValue.toString(),
                    style = NexusTypography.Stat.copy(fontSize = 26.sp, lineHeight = 28.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                if (suffix.isNotEmpty()) {
                    Text(
                        text = suffix,
                        style = NexusTypography.DataLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .alignByBaseline(),
                    )
                }
            }
        }
    }
}
