package com.nexus.designsystem.component.editorial

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.innerLight
import com.nexus.designsystem.component.pressScale
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus

/**
 * The narrative layer of a screen.
 *
 * These composables exist so a feature module can open a page the way an
 * article opens — with a statement — instead of dropping the user straight onto
 * a grid of widgets. They carry no state and no data: they only decide who
 * speaks in serif, who speaks in mono, and how much air a line gets.
 *
 * The rule they enforce is the one the rest of the system depends on: the
 * serif says what a thing *means*, the instrument mono says what a thing
 * *measures*, and nothing is allowed to be both.
 */

/**
 * How a page opens: a dated slug, a greeting, one serif statement, and the
 * sentence underneath that explains it.
 *
 * [headline] is the largest type on the screen and there is only ever one per
 * page — two serif statements of equal weight read as a template, not an
 * editorial voice.
 */
@Composable
fun EditorialPageHeader(
    headline: String,
    modifier: Modifier = Modifier,
    slug: String? = null,
    greeting: String? = null,
    lede: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (slug != null || trailing != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (slug != null) {
                    EditorialSlug(slug)
                } else {
                    Spacer(Modifier.width(1.dp))
                }
                trailing?.invoke()
            }
            Spacer(Modifier.height(NexusSpacing.md))
        }

        if (greeting != null) {
            Text(
                text = greeting,
                style = NexusTypography.EditorialLead,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(NexusSpacing.xs))
        }

        Text(
            text = headline,
            style = NexusTypography.EditorialDisplay,
            color = MaterialTheme.colorScheme.onBackground,
        )

        if (lede != null) {
            Spacer(Modifier.height(NexusSpacing.sm))
            Text(
                text = lede,
                style = NexusTypography.EditorialBody,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** A dated, letter-spaced mono slug — the standfirst above a masthead. */
@Composable
fun EditorialSlug(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = NexusTypography.DataLabel,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
    )
}

/**
 * A numbered section: index, serif title, and a rule that carries on to the
 * action, so a scrolling page reads as one continuous surface rather than a
 * stack of unrelated blocks.
 */
@Composable
fun EditorialSection(
    title: String,
    modifier: Modifier = Modifier,
    index: String? = null,
    lede: String? = null,
    meta: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(NexusSpacing.sm),
        ) {
            if (index != null) {
                Text(
                    text = index,
                    style = NexusTypography.TelemetryMicro,
                    color = MaterialTheme.nexus.signal.copy(alpha = 0.9f),
                )
            }
            Text(
                text = title,
                style = NexusTypography.EditorialHeadline,
                color = MaterialTheme.colorScheme.onBackground,
            )
            HorizontalDivider(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = NexusSpacing.xs),
                thickness = 1.dp,
                color = MaterialTheme.nexus.hairline,
            )
            if (meta != null) {
                Text(
                    text = meta,
                    style = NexusTypography.TelemetryMicro,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (actionText != null && onAction != null) {
                Text(
                    text = actionText.uppercase(),
                    style = NexusTypography.DataLabel,
                    color = MaterialTheme.nexus.signal,
                    modifier = Modifier
                        .clip(NexusShapes.sm)
                        .clickable(role = Role.Button) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onAction()
                        }
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                )
            }
        }

        if (lede != null) {
            Spacer(Modifier.height(NexusSpacing.xs))
            Text(
                text = lede,
                style = NexusTypography.EditorialBody,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(NexusSpacing.md))
        content()
    }
}

/** A rule with a word on it — the break between two movements of a page. */
@Composable
fun EditorialDivider(
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NexusSpacing.sm),
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.nexus.hairline,
        )
        if (label != null) {
            Text(
                text = label.uppercase(),
                style = NexusTypography.DataLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = MaterialTheme.nexus.hairline,
            )
        }
    }
}

/**
 * A flat, unadorned paper surface for prose and lists.
 *
 * Deliberately quieter than `NexusCard`: no gradient, no sheen, no grid — a
 * hairline and an inner light. Prose does not need to be packaged, and a card
 * inside a card is how a screen stops looking designed.
 */
@Composable
fun EditorialSurface(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = NexusSpacing.lg,
    content: @Composable ColumnScope.() -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val shape = NexusShapes.lg

    Column(
        modifier = modifier
            .let { if (onClick != null) it.pressScale() else it }
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .innerLight(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(role = Role.Button) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    }
                } else {
                    Modifier
                }
            )
            .padding(contentPadding),
        content = content,
    )
}

/**
 * A numbered editorial row — the shape of "your focus today".
 *
 * The index is mono and the title is sans: a serif in a list row competes with
 * the section title above it, and two loud things in one column is how a
 * screen starts looking decorated.
 */
@Composable
fun EditorialListItem(
    title: String,
    modifier: Modifier = Modifier,
    index: String? = null,
    meta: String? = null,
    caption: String? = null,
    isComplete: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current
    val shape = NexusShapes.md

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .then(
                if (onClick != null) {
                    Modifier
                        .pressScale()
                        .clickable(role = Role.Button) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onClick()
                        }
                } else {
                    Modifier
                }
            )
            .padding(vertical = NexusSpacing.sm, horizontal = NexusSpacing.xs),
        verticalAlignment = Alignment.Top,
    ) {
        if (index != null) {
            Text(
                text = index,
                style = NexusTypography.Telemetry,
                color = if (isComplete) {
                    MaterialTheme.nexus.signal
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                },
                modifier = Modifier.width(NexusSpacing.xl),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = NexusTypography.TitleMedium,
                color = if (isComplete) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            if (caption != null) {
                Spacer(Modifier.height(NexusSpacing.xxxs))
                Text(
                    text = caption,
                    style = NexusTypography.BodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (meta != null) {
            Spacer(Modifier.width(NexusSpacing.sm))
            Text(
                text = meta,
                style = NexusTypography.TelemetryMicro,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
            )
        }

        if (trailing != null) {
            Spacer(Modifier.width(NexusSpacing.sm))
            Box(contentAlignment = Alignment.Center) { trailing() }
        }
    }
}

/**
 * A measured value presented editorially: label above, figure below, delta
 * beside.
 *
 * The figure is mono because it is a reading, and a reading that changes width
 * between two renders makes a column of numbers wobble.
 */
@Composable
fun EditorialMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    delta: String? = null,
    isDeltaPositive: Boolean = true,
    caption: String? = null,
    align: Alignment.Horizontal = Alignment.Start,
) {
    val trendColor = if (isDeltaPositive) MaterialTheme.nexus.positive else MaterialTheme.nexus.attention

    Column(
        modifier = modifier,
        horizontalAlignment = align,
    ) {
        Text(
            text = label.uppercase(),
            style = NexusTypography.DataLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
        Spacer(Modifier.height(NexusSpacing.xxs))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = NexusTypography.TelemetryDisplay,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (unit != null) {
                Spacer(Modifier.width(NexusSpacing.xxs))
                Text(
                    text = unit,
                    style = NexusTypography.Telemetry,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (delta != null) {
                Spacer(Modifier.width(NexusSpacing.sm))
                Text(
                    text = delta,
                    style = NexusTypography.Telemetry,
                    color = trendColor,
                )
            }
        }
        if (caption != null) {
            Spacer(Modifier.height(NexusSpacing.xxs))
            Text(
                text = caption,
                style = NexusTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** A sentence given the weight of a quote: pull-quotes, coach lines, verdicts. */
@Composable
fun EditorialPullQuote(
    text: String,
    modifier: Modifier = Modifier,
    attribution: String? = null,
    accentColor: Color = MaterialTheme.nexus.signal,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .width(NexusSpacing.xxxs)
                .height(NexusTypography.EditorialQuote.lineHeight.value.dp)
                .background(accentColor),
        ) {
            Spacer(Modifier.size(NexusSpacing.xxxs))
        }
        Spacer(Modifier.width(NexusSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = text,
                style = NexusTypography.EditorialQuote,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (attribution != null) {
                Spacer(Modifier.height(NexusSpacing.xs))
                Text(
                    text = attribution.uppercase(),
                    style = NexusTypography.DataLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
