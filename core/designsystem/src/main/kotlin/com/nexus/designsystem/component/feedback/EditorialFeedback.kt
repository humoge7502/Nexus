package com.nexus.designsystem.component.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.NexusButton
import com.nexus.designsystem.component.shimmer
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus
import java.util.Locale

/**
 * The failure layer.
 *
 * An error here names what failed, says which part is still usable, and offers
 * exactly one action. A cached screen with a stale banner is a different event
 * from a screen with nothing on it, and the two should not share a component —
 * silently showing the same card for both teaches the user that the app's
 * error states are noise.
 */

@Composable
fun EditorialErrorState(
    headline: String,
    reason: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    secondaryHint: String? = null,
    isRecoverable: Boolean = true,
) {
    val ext = MaterialTheme.nexus
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(NexusShapes.md)
            .background(ext.attentionContainer)
            .padding(NexusSpacing.lg),
    ) {
        // A 2dp edge is the non-colour signal that this block is a fault, so the
        // state survives greyscale and colour-blind passes.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(13.dp)
                    .background(ext.onAttentionContainer),
            )
            Spacer(Modifier.width(NexusSpacing.sm))
            Text(
                text = (if (isRecoverable) "Couldn't load" else "Unavailable").uppercase(Locale.ROOT),
                style = NexusTypography.DataLabel,
                color = ext.onAttentionContainer.copy(alpha = 0.8f),
            )
        }
        Spacer(Modifier.height(NexusSpacing.sm))
        Text(
            text = headline,
            style = NexusTypography.EditorialTitle,
            color = ext.onAttentionContainer,
        )
        Spacer(Modifier.height(NexusSpacing.xs))
        Text(
            text = reason,
            style = NexusTypography.BodyMedium,
            color = ext.onAttentionContainer.copy(alpha = 0.85f),
        )
        if (!secondaryHint.isNullOrBlank()) {
            Spacer(Modifier.height(NexusSpacing.sm))
            Text(
                text = secondaryHint,
                style = NexusTypography.BodySmall,
                // 0.65 measured 3.35:1 on the light attention container — under
                // AA for small text. 0.8 is the floor that clears it in both
                // themes while keeping this line quieter than the reason.
                color = ext.onAttentionContainer.copy(alpha = 0.8f),
            )
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(NexusSpacing.md))
            NexusButton(
                text = actionLabel,
                onClick = onAction,
                modifier = Modifier.fillMaxWidth(0.5f),
            )
        }
    }
}

/**
 * One skeleton line, sized in text units rather than pixels so it tracks the
 * type role it is standing in for.
 */
@Composable
fun EditorialSkeletonLine(
    modifier: Modifier = Modifier,
    width: Dp = Dp.Unspecified,
    height: Dp = 12.dp,
    fraction: Float = 1f,
) {
    Box(
        modifier = modifier
            .then(if (width != Dp.Unspecified) Modifier.width(width) else Modifier)
            .fillMaxWidth(fraction)
            .height(height)
            .clip(NexusShapes.xs)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f))
            .shimmer(),
    )
}

/**
 * A placeholder card whose block heights are the real ones.
 *
 * Rows, not rectangles: a shimmering card that is 40dp shorter than the content
 * it replaces makes the list jump on arrival, which reads as a bug rather than
 * as loading.
 */
@Composable
fun EditorialSkeletonCard(
    modifier: Modifier = Modifier,
    lines: Int = 3,
    showTitle: Boolean = true,
) {
    val ext = MaterialTheme.nexus
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(NexusShapes.md)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(NexusSpacing.hairline, ext.hairline, NexusShapes.md)
            .padding(NexusSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(NexusSpacing.sm),
    ) {
        if (showTitle) {
            EditorialSkeletonLine(width = 140.dp, height = 18.dp)
            Spacer(Modifier.height(NexusSpacing.xxs))
        }
        repeat(lines) { index ->
            EditorialSkeletonLine(
                height = 12.dp,
                // The last line of a paragraph is short in real text; a skeleton
                // that fills every row reads as a table, not as prose.
                fraction = if (index == lines - 1 && lines > 1) 0.62f else 1f,
            )
        }
    }
}

/** A stack of placeholder cards for a list that has not arrived yet. */
@Composable
fun EditorialSkeletonList(
    modifier: Modifier = Modifier,
    count: Int = 3,
    loadingLabel: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (loadingLabel != null) {
                    Modifier.semantics { contentDescription = loadingLabel }
                } else {
                    Modifier
                },
            ),
        verticalArrangement = Arrangement.spacedBy(NexusSpacing.md),
    ) {
        repeat(count) {
            EditorialSkeletonCard()
        }
    }
}

/** A one-line status band: cached, partial, or working offline. */
@Composable
fun EditorialStatusBand(
    label: String,
    detail: String,
    modifier: Modifier = Modifier,
    tone: StatusTone = StatusTone.Neutral,
) {
    val ext = MaterialTheme.nexus
    val (edge, onEdge) = when (tone) {
        StatusTone.Neutral -> ext.hairlineStrong to MaterialTheme.colorScheme.onSurfaceVariant
        StatusTone.Signal -> ext.signal.copy(alpha = 0.4f) to ext.signal
        StatusTone.Fault -> ext.attention.copy(alpha = 0.5f) to ext.attention
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(NexusShapes.xs)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(NexusSpacing.hairline, edge, NexusShapes.xs)
            .padding(horizontal = NexusSpacing.md, vertical = NexusSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(NexusShapes.tag)
                .background(onEdge),
        )
        Spacer(Modifier.width(NexusSpacing.sm))
        Text(
            text = label.uppercase(Locale.ROOT),
            style = NexusTypography.DataLabel,
            color = onEdge,
        )
        Spacer(Modifier.width(NexusSpacing.md))
        Text(
            text = detail,
            style = NexusTypography.BodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
        )
    }
}

enum class StatusTone { Neutral, Signal, Fault }
