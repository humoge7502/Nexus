package com.nexus.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The pipeline, read as one continuous measurement.
 *
 * A career advances as a sequence of dated facts, and the layout language
 * already owns the syntax for reading one: a 1dp hairline at a fixed axis,
 * marks that sit exactly where a fact sits, and prose beside the line rather
 * than on it. [EditorialTimeline] turns that syntax vertical — a hairline
 * spine at a fixed x, one node per entry colored by tone the way [SignalTag]
 * stamps a reading, and the label, its detail and the date set beside the
 * line. A date is a measured fact, so it renders in tabular mono and never
 * in prose.
 *
 * Rules for keeping this honest:
 *  - Entries render in the order given. The caller owns the chronology; the
 *    component never re-sorts or invents history.
 *  - The spine is hairline and the node palette mirrors the tag palette, so
 *    a tone reads identically whether it labels a reading or marks a moment.
 *  - An empty timeline given a title speaks in the serif voice — the same
 *    pattern as [EmptyState], inline and compact. Without a title an empty
 *    timeline is honest silence and renders nothing.
 *  - Each entry merges its descendants, so TalkBack reads label, detail and
 *    date as one fact.
 */

/** One dated moment on the timeline. [id] must be stable across
 *  recompositions — it keys the entry's node; [label] names the event,
 *  [detail] gives its context, [timestamp] is the epoch millisecond the node
 *  sits at (null renders without a date stamp), and [tone] picks the node's
 *  container/content pair. */
data class TimelineEntry(
    val id: String,
    val label: String,
    val detail: String?,
    val timestamp: Long?,
    val tone: SignalTone,
)

private data class TimelineNodePalette(val container: Color, val content: Color)

/** Local mirror of the tag palette: the timeline node borrows exactly the
 *  container/content pairs [SignalTag] stamps with, so a tone cannot drift
 *  away from its tag and both stay contrast-checked in either theme. Kept
 *  private here rather than widening the primitives' own resolver. */
@Composable
private fun timelineNodePalette(tone: SignalTone): TimelineNodePalette {
    val ext = MaterialTheme.nexus
    return when (tone) {
        SignalTone.Signal -> TimelineNodePalette(ext.signalContainer, ext.onSignalContainer)
        SignalTone.Attention -> TimelineNodePalette(ext.attentionContainer, ext.onAttentionContainer)
        SignalTone.Critical -> TimelineNodePalette(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
        )
        SignalTone.Structure -> TimelineNodePalette(ext.structureContainer, ext.onStructureContainer)
        SignalTone.Neutral -> TimelineNodePalette(
            MaterialTheme.colorScheme.surfaceContainerHigh,
            MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * The date stamp is machine voice: `04 NOV`. SimpleDateFormat is not
 * thread-safe, but composition runs on the main thread, so one shared
 * formatter is enough for the component.
 */
private val milestoneDateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

/** The spine's fixed x is the center of this column; every node sits on the
 *  same axis no matter how far the prose beside it runs. */
private val SpineColumnWidth = 20.dp

/** Optical center of the entry's first (23sp title) line, where the node sits. */
private val SpineDotCenterY = 12.dp

private val SpineDotDiameter = 10.dp
private val SpineDotCoreDiameter = 4.dp

@Composable
fun EditorialTimeline(
    entries: List<TimelineEntry>,
    modifier: Modifier = Modifier,
    /** Title shown when the line is bare. Null keeps an empty timeline silent. */
    emptyTitle: String? = null,
    emptyDetail: String? = null,
    /** The serif aside explaining why the line is bare — see [EmptyState]. */
    emptyVoice: String? = null,
) {
    if (entries.isEmpty()) {
        if (emptyTitle != null) {
            EditorialTimelineEmpty(
                title = emptyTitle,
                detail = emptyDetail,
                voice = emptyVoice,
                modifier = modifier,
            )
        }
        return
    }

    Column(modifier = modifier) {
        entries.forEachIndexed { index, entry ->
            // Stable keys by id: a re-sorted timeline moves nodes instead of
            // rebuilding them, which keeps state and recomposition honest.
            key(entry.id) {
                EditorialTimelineNode(
                    entry = entry,
                    isLast = index == entries.lastIndex,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun EditorialTimelineNode(
    entry: TimelineEntry,
    isLast: Boolean,
    modifier: Modifier = Modifier,
) {
    val palette = timelineNodePalette(entry.tone)
    val spineColor = MaterialTheme.nexus.hairline
    Row(
        modifier = modifier
            // Min intrinsic height lets the spine canvas stretch to whatever
            // the prose beside it needs, so the line never breaks between
            // entries.
            .height(IntrinsicSize.Min)
            .semantics(mergeDescendants = true) {},
    ) {
        Canvas(
            modifier = Modifier
                .width(SpineColumnWidth)
                .fillMaxHeight(),
        ) {
            val spineX = size.width / 2f
            val dotY = SpineDotCenterY.toPx()
            val dotRadius = SpineDotDiameter.toPx() / 2f

            // The spine runs node to node; the last entry terminates the
            // measurement, so nothing is drawn below its dot.
            if (!isLast) {
                drawLine(
                    color = spineColor,
                    start = Offset(spineX, dotY),
                    end = Offset(spineX, size.height),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            // The node: a container field with the tone's content ink at its
            // center — the same two-color construction as a SignalTag.
            drawCircle(color = palette.container, radius = dotRadius, center = Offset(spineX, dotY))
            drawCircle(
                color = palette.content,
                radius = SpineDotCoreDiameter.toPx() / 2f,
                center = Offset(spineX, dotY),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                // The gap between entries lives inside the row, not between
                // rows, so the spine carries it instead of breaking across it.
                .padding(bottom = if (isLast) 0.dp else NexusSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(NexusSpacing.xxs),
        ) {
            Text(
                text = entry.label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (entry.detail != null) {
                Text(
                    text = entry.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (entry.timestamp != null) {
                Text(
                    text = milestoneDateFormat.format(Date(entry.timestamp)).uppercase(Locale.ROOT),
                    style = NexusTypography.DataLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** The inline-compact sibling of [EmptyState]: the serif aside first, then the
 *  factual title and detail — enough voice to explain a bare line, without
 *  the full block's ceremony. */
@Composable
private fun EditorialTimelineEmpty(
    title: String,
    detail: String?,
    voice: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (voice != null) {
            Text(
                text = voice,
                style = NexusTypography.Voice,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(NexusSpacing.xs))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (detail != null) {
            Spacer(Modifier.height(NexusSpacing.xxs))
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
