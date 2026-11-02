package com.nexus.designsystem.component.intelligence

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.innerLight
import com.nexus.designsystem.component.pressScale
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus
import java.util.Locale

/**
 * The advice layer.
 *
 * Everything here states a lever and the evidence under it. The rule that holds
 * the family together: a figure presented as intelligence must also show how it
 * was obtained, because a readiness score with no factors behind it is an
 * opinion wearing a decimal point.
 */

/** How much of a measured total this factor accounts for. */
data class ReadinessFactor(
    val label: String,
    val fraction: Float,
    val detail: String,
    val isImprovableNow: Boolean = false,
)

/**
 * One weighted contributor to a readiness score.
 *
 * Weight is drawn as a filled rule and stated again in words, so the reading
 * survives both a colour-blind pass and TalkBack.
 */
@Composable
fun ReadinessFactorCard(
    factor: ReadinessFactor,
    modifier: Modifier = Modifier,
    rank: Int? = null,
    onClick: (() -> Unit)? = null,
) {
    val ext = MaterialTheme.nexus
    val shape = NexusShapes.md
    val haptic = LocalHapticFeedback.current
    val accent = if (factor.isImprovableNow) ext.signal else ext.hairlineStrong

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pressScale()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(NexusSpacing.hairline, ext.hairline, shape)
            .innerLight(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(role = Role.Button) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    }
                } else {
                    Modifier
                },
            )
            .padding(NexusSpacing.md),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            if (rank != null) {
                Text(
                    text = rank.toString().padStart(2, '0'),
                    style = NexusTypography.TelemetryMicro,
                    color = ext.signal,
                    modifier = Modifier.padding(end = NexusSpacing.sm, bottom = 3.dp),
                )
            }
            Text(
                text = factor.label,
                style = NexusTypography.TitleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${(factor.fraction * 100).toInt()}%",
                style = NexusTypography.Telemetry,
                color = accent,
            )
        }
        Spacer(Modifier.height(NexusSpacing.sm))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(ext.hairline, NexusShapes.tag),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(factor.fraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(accent, NexusShapes.tag),
            )
        }
        Spacer(Modifier.height(NexusSpacing.sm))
        Text(
            text = factor.detail,
            style = NexusTypography.BodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (factor.isImprovableNow) {
            Spacer(Modifier.height(NexusSpacing.xs))
            Text(
                text = "MOVABLE THIS WEEK".uppercase(Locale.ROOT),
                style = NexusTypography.DataLabel,
                color = ext.signal,
            )
        }
    }
}

/**
 * A projected range, never a promised number.
 *
 * The band is the whole point: an estimate drawn as one line implies the model
 * knows more than it does. [low] and [high] are required so that a caller
 * cannot pass a single value and turn this back into a prediction.
 */
@Composable
fun ConfidenceBand(
    label: String,
    low: String,
    high: String,
    lowFraction: Float,
    highFraction: Float,
    modifier: Modifier = Modifier,
    caption: String? = null,
    currentFraction: Float? = null,
    domainLabel: String? = null,
) {
    val ext = MaterialTheme.nexus
    val from = lowFraction.coerceIn(0f, 1f)
    val to = highFraction.coerceIn(from, 1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = label.uppercase(Locale.ROOT),
                style = NexusTypography.DataLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "$low – $high",
                style = NexusTypography.Telemetry,
                color = ext.structure,
            )
        }
        Spacer(Modifier.height(NexusSpacing.md))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp),
        ) {
            val hairline = with(density) { NexusSpacing.hairline.toPx() }
            val bandHeight = with(density) { 10.dp.toPx() }
            val markerWidth = with(density) { 2.dp.toPx() }
            val bandTop = (size.height - bandHeight) / 2f
            // End ticks make the axis a floor and a ceiling rather than a track
            // waiting to be filled — a band is a range of outcomes, not progress.
            listOf(0f, size.width - hairline).forEach { x ->
                drawLine(
                    color = ext.hairlineStrong,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = hairline,
                )
            }
            drawRoundRect(
                color = ext.structure.copy(alpha = 0.28f),
                topLeft = Offset(size.width * from, bandTop),
                size = Size(size.width * (to - from), bandHeight),
                cornerRadius = CornerRadius(with(density) { 2.dp.toPx() }),
            )
            currentFraction?.let { current ->
                // "Where you are now" shares the band's axis, so the gap between
                // the two is readable without a second chart.
                val x = size.width * current.coerceIn(0f, 1f)
                drawLine(
                    color = ext.signal,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = markerWidth,
                )
            }
        }
        if (!caption.isNullOrBlank()) {
            Spacer(Modifier.height(NexusSpacing.sm))
            Text(
                text = caption,
                style = NexusTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            )
        }
        if (!domainLabel.isNullOrBlank()) {
            Text(
                text = domainLabel.uppercase(Locale.ROOT),
                style = NexusTypography.TelemetryMicro,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            )
        }
    }
}

/** A single piece of advice, with the cost of acting on it stated up front. */
data class ImprovementAdvice(
    val title: String,
    val rationale: String,
    val effort: String,
    val impact: String,
)

/**
 * An actionable recommendation.
 *
 * Effort is shown beside impact deliberately: advice that only ever quotes the
 * upside is advertising, and a student allocating evenings needs both terms.
 */
@Composable
fun ImprovementRecommendation(
    advice: ImprovementAdvice,
    onAct: () -> Unit,
    modifier: Modifier = Modifier,
    rank: Int? = null,
) {
    val ext = MaterialTheme.nexus
    val haptic = LocalHapticFeedback.current
    val shape = NexusShapes.md

    Row(
        modifier = modifier
            .fillMaxWidth()
            .pressScale()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(NexusSpacing.hairline, ext.hairline, shape)
            .innerLight(shape)
            .clickable(role = Role.Button) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onAct()
            }
            .padding(NexusSpacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        if (rank != null) {
            Text(
                text = rank.toString().padStart(2, '0'),
                style = NexusTypography.TelemetryMicro,
                color = ext.signal,
                modifier = Modifier.padding(end = NexusSpacing.md, top = 4.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = advice.title,
                style = NexusTypography.TitleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(NexusSpacing.xxs))
            Text(
                text = advice.rationale,
                style = NexusTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(NexusSpacing.sm))
            HorizontalDivider(thickness = NexusSpacing.hairline, color = ext.hairline)
            Spacer(Modifier.height(NexusSpacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(NexusSpacing.lg)) {
                MicroDatum("EFFORT", advice.effort)
                MicroDatum("IMPACT", advice.impact, isAccent = true)
            }
        }
    }
}

@Composable
private fun MicroDatum(label: String, value: String, isAccent: Boolean = false) {
    val ext = MaterialTheme.nexus
    Column {
        Text(
            text = label,
            style = NexusTypography.TelemetryMicro,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = NexusTypography.Telemetry,
            color = if (isAccent) ext.signal else MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * A finding, its evidence, and the confidence the evidence actually supports.
 *
 * [strength] is a label rather than a percentage because "based on 3 of your
 * saved roles" is checkable and "78% confidence" is not.
 */
@Composable
fun IntelligenceInsight(
    headline: String,
    evidence: String,
    strength: String,
    modifier: Modifier = Modifier,
    source: String? = null,
    onOpen: (() -> Unit)? = null,
) {
    val ext = MaterialTheme.nexus
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(NexusShapes.md)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(NexusSpacing.hairline, ext.hairline, NexusShapes.md)
            .innerLight(NexusShapes.md)
            .then(
                if (onOpen != null) {
                    Modifier.clickable(role = Role.Button) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onOpen()
                    }
                } else {
                    Modifier
                },
            )
            .padding(NexusSpacing.lg),
    ) {
        // The serif carries the claim; the mono carries the provenance. Keeping
        // them in different faces stops an insight and its evidence reading alike.
        Text(
            text = headline,
            style = NexusTypography.EditorialHeadline,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(NexusSpacing.sm))
        HorizontalDivider(thickness = NexusSpacing.hairline, color = ext.hairlineStrong)
        Spacer(Modifier.height(NexusSpacing.sm))
        Text(
            text = evidence,
            style = NexusTypography.EditorialBody,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(NexusSpacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = strength.uppercase(Locale.ROOT),
                style = NexusTypography.DataLabel,
                color = ext.signal,
            )
            if (!source.isNullOrBlank()) {
                Text(
                    text = source.uppercase(Locale.ROOT),
                    style = NexusTypography.TelemetryMicro,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }
    }
}

/**
 * A step the product can take on the user's behalf.
 *
 * The consequence of tapping is named in [detail] before the tap, not after —
 * an action card that only reveals it submits an application is not an action
 * card, it is a trap.
 */
@Composable
fun CareerActionCard(
    title: String,
    detail: String,
    actionLabel: String,
    onAct: () -> Unit,
    modifier: Modifier = Modifier,
    isUrgent: Boolean = false,
    cost: String? = null,
) {
    val ext = MaterialTheme.nexus
    val haptic = LocalHapticFeedback.current
    val shape = NexusShapes.md
    val edge = if (isUrgent) ext.attention.copy(alpha = 0.55f) else ext.signal.copy(alpha = 0.35f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .pressScale()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(NexusSpacing.hairline, edge, shape)
            .innerLight(shape)
            .clickable(role = Role.Button) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onAct()
            }
            .padding(NexusSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Urgency is a filled block, ordinary actions are an outlined one, so
        // the difference is shape as well as hue.
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 34.dp)
                .background(if (isUrgent) ext.attention else ext.hairlineStrong),
        )
        Spacer(Modifier.width(NexusSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = NexusTypography.TitleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(NexusSpacing.xxxs))
            Text(
                text = detail,
                style = NexusTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!cost.isNullOrBlank()) {
                Spacer(Modifier.height(NexusSpacing.xs))
                Text(
                    text = cost.uppercase(Locale.ROOT),
                    style = NexusTypography.TelemetryMicro,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                )
            }
        }
        Spacer(Modifier.width(NexusSpacing.sm))
        Text(
            text = actionLabel.uppercase(Locale.ROOT),
            style = NexusTypography.DataLabel,
            color = if (isUrgent) ext.attention else ext.signal,
        )
    }
}
