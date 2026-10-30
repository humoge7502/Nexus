package com.nexus.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.NexusAnimation
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.motionMillis
import com.nexus.designsystem.theme.nexus
import java.util.Locale

/**
 * The calibrated line.
 *
 * NEXUS reports on the user's progress, so its most repeated visual idea is a
 * *measuring instrument*: a rule with graduations, a lit span, and a needle that
 * sits exactly where the value sits. Three primitives carry that idea
 * everywhere — [SignalTag] labels a reading, [CalibratedRule] plots one, and
 * [Readout] states one in tabular figures.
 *
 * Rules for keeping this honest:
 *  - Ticks mark real scale divisions; they are never scattered decoration.
 *  - A tick that the value has passed is lit, so the tick field itself encodes
 *    magnitude and not just texture.
 *  - Mono is only used for things a machine measures. Prose never gets it.
 */

/** Which signal a tag is speaking for. Each tone owns a container/content pair
 *  that is contrast-checked in both themes, so a tag can never wash out. */
enum class SignalTone { Signal, Attention, Critical, Structure, Neutral }

private data class TagPalette(val container: Color, val content: Color)

@Composable
private fun tagPalette(tone: SignalTone): TagPalette {
    val ext = MaterialTheme.nexus
    return when (tone) {
        SignalTone.Signal -> TagPalette(ext.signalContainer, ext.onSignalContainer)
        SignalTone.Attention -> TagPalette(ext.attentionContainer, ext.onAttentionContainer)
        SignalTone.Critical -> TagPalette(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
        )
        SignalTone.Structure -> TagPalette(ext.structureContainer, ext.onStructureContainer)
        SignalTone.Neutral -> TagPalette(
            MaterialTheme.colorScheme.surfaceContainerHigh,
            MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * A near-square instrument tag: uppercase mono, 2dp corners, no border.
 *
 * This replaces the pill chip. It reads as a stamped label on equipment rather
 * than a marketing badge, and it is the only place in the system where the
 * smallest type size is acceptable, because it is a field name, not prose.
 */
@Composable
fun SignalTag(
    text: String,
    modifier: Modifier = Modifier,
    tone: SignalTone = SignalTone.Signal,
    icon: ImageVector? = null,
    iconTint: Color? = null,
) {
    val palette = tagPalette(tone)
    Row(
        modifier = modifier
            .background(palette.container, NexusShapes.tag)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint ?: palette.content,
                modifier = Modifier.size(11.dp),
            )
        }
        Text(
            text = text.uppercase(Locale.ROOT),
            style = NexusTypography.DataLabel,
            color = palette.content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * A horizontal rule with graduations, a lit span and a needle.
 *
 * The rule sits at 35% of the height; ticks hang below it and the needle rises
 * through it. Passed graduations light up, so the density of lit ticks is a
 * second, coarser reading of the same value — the way a real scale lets you
 * read a measurement two ways.
 *
 * Use this instead of a generic progress bar anywhere the number matters:
 * weekly momentum, syllabus coverage, match confidence, streak proximity.
 */
@Composable
fun CalibratedRule(
    progress: Float,
    modifier: Modifier = Modifier,
    divisions: Int = 20,
    accent: Color = MaterialTheme.nexus.signal,
    height: Dp = 18.dp,
    trackColor: Color = Color.Unspecified,
    tickColor: Color = Color.Unspecified,
    contentDescription: String? = null,
) {
    val target = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(
            durationMillis = motionMillis(NexusAnimation.Duration.emphasized),
            easing = NexusAnimation.Easing.emphasizedDecelerate,
        ),
        label = "calibratedRule",
    )
    val steps = divisions.coerceIn(1, 80)
    // A dark hero slab in a light theme still needs a light rule, so callers on
    // fixed-dark surfaces pass bone tones explicitly instead of inheriting
    // onSurface.
    val resolvedTrack = if (trackColor == Color.Unspecified) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
    } else {
        trackColor
    }
    val idleTick = if (tickColor == Color.Unspecified) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)
    } else {
        tickColor
    }
    val litTick = accent.copy(alpha = 0.55f)

    Canvas(
        modifier = modifier
            .height(height)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics {
                        this.contentDescription = contentDescription
                        progressBarRangeInfo = ProgressBarRangeInfo(target, 0f..1f)
                    }
                } else {
                    Modifier
                }
            )
    ) {
        val ruleY = size.height * 0.35f
        val litWidth = size.width * animated
        val tickMajor = 7.dp.toPx()
        val tickMinor = 4.dp.toPx()
        val needleAbove = 3.dp.toPx()
        val needleBelow = 9.dp.toPx()

        // The unlit rule, one hairline.
        drawLine(
            color = resolvedTrack,
            start = Offset(0f, ruleY),
            end = Offset(size.width, ruleY),
            strokeWidth = 1.dp.toPx(),
        )

        // Graduations. A tick is lit only if the needle has passed it, so the
        // tick field is a second encoding of the value rather than texture.
        for (i in 0..steps) {
            val x = size.width * i / steps
            val major = i % 5 == 0
            val passed = x <= litWidth + 0.5f
            drawLine(
                color = if (passed) litTick else idleTick,
                start = Offset(x, ruleY),
                end = Offset(x, ruleY + if (major) tickMajor else tickMinor),
                strokeWidth = if (major) 1.4.dp.toPx() else 1.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        // The lit span: thicker than the rule so the reading is unambiguous.
        if (litWidth > 0f) {
            drawLine(
                color = accent,
                start = Offset(0f, ruleY),
                end = Offset(litWidth, ruleY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        // The needle sits at the value, not at the end of the animation.
        val needleX = litWidth.coerceIn(0f, size.width)
        drawLine(
            color = accent,
            start = Offset(needleX, ruleY - needleAbove),
            end = Offset(needleX, ruleY + needleBelow),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

/**
 * A measured value with its field label above it.
 *
 * Label above value is the instrument convention: you read what the field *is*
 * before you read the number. The label is 10sp mono with wide tracking; the
 * value is tabular so a column of readouts lines up to the pixel.
 */
@Composable
fun Readout(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    style: TextStyle = NexusTypography.Readout,
) {
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(NexusSpacing.xxxs),
    ) {
        Text(
            text = label.uppercase(Locale.ROOT),
            style = NexusTypography.DataLabel,
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row {
            Text(
                text = value,
                style = style,
                color = valueColor,
                maxLines = 1,
                modifier = Modifier.alignByBaseline(),
            )
            if (unit != null) {
                Spacer(Modifier.width(3.dp))
                Text(
                    text = unit,
                    style = NexusTypography.TelemetryMicro,
                    color = labelColor,
                    maxLines = 1,
                    modifier = Modifier.alignByBaseline(),
                )
            }
        }
    }
}
