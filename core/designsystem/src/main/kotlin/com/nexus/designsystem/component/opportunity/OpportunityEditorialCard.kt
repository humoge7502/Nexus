package com.nexus.designsystem.component.opportunity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.innerLight
import com.nexus.designsystem.component.pressScale
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus
import com.nexus.domain.model.Internship
import java.time.LocalDate
import java.util.Locale

/**
 * The card a listing becomes.
 *
 * Nothing here rounds an unknown into a zero: an unpublished stipend is absent,
 * a rolling deadline says so, and the only number shown against the user's own
 * skills is a count they can check against the listing. A relevance score is
 * deliberately not rendered as a percentage — the pipeline ranks listings, it
 * does not know what a recruiter will decide, and printing a "92% match" would
 * turn a sort key into a promise.
 */

@Composable
fun OpportunityEditorialCard(
    opportunity: Internship,
    modifier: Modifier = Modifier,
    heldSkills: Set<String> = emptySet(),
    today: LocalDate = LocalDate.now(),
    whyShown: String? = null,
    onSave: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val ext = MaterialTheme.nexus
    val haptic = LocalHapticFeedback.current
    val shape = NexusShapes.lg

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.pressScale() else Modifier)
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
            .padding(NexusSpacing.lg)
            .semantics(mergeDescendants = true) {},
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            CompanyIdentity(
                name = opportunity.company.name,
                logoUrl = opportunity.company.logoUrl,
                domain = opportunity.company.domain,
                modifier = Modifier.weight(1f),
            )
            if (onSave != null) {
                Spacer(Modifier.width(NexusSpacing.sm))
                SaveToggle(
                    isSaved = opportunity.isSaved,
                    onToggle = { onSave(!opportunity.isSaved) },
                )
            }
        }

        Spacer(Modifier.height(NexusSpacing.md))
        Text(
            text = opportunity.title,
            style = NexusTypography.EditorialHeadline,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        // The two facts that decide whether to keep reading go on their own line.
        Spacer(Modifier.height(NexusSpacing.xs))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = listOf(
                    opportunity.location.city,
                    if (opportunity.isRemote) "Remote" else opportunity.location.state,
                ).filter { it.isNotBlank() }.joinToString(" · "),
                style = NexusTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            DeadlineIndicator(deadline = opportunity.deadline, today = today)
        }

        Spacer(Modifier.height(NexusSpacing.md))
        HorizontalDivider(thickness = NexusSpacing.hairline, color = ext.hairline)
        Spacer(Modifier.height(NexusSpacing.md))

        OpportunityMetadata(
            locationLine = null,
            mode = if (opportunity.isRemote) "Remote" else "On-site",
            duration = opportunity.durationLabel.ifBlank {
                opportunity.duration.formatted()
            },
            // Published figure or published wording, never a computed average.
            stipend = opportunity.stipend?.formatted()
                ?: opportunity.stipendLabel.takeIf { it.isNotBlank() },
            sourceName = opportunity.sourceName.takeIf { it.isNotBlank() },
        )

        if (opportunity.requiredSkills.isNotEmpty()) {
            Spacer(Modifier.height(NexusSpacing.md))
            SkillMatchIndicator(
                requiredSkills = opportunity.requiredSkills.map { it.name },
                heldSkills = heldSkills,
            )
        }

        if (opportunity.tags.isNotEmpty()) {
            Spacer(Modifier.height(NexusSpacing.md))
            OpportunityTagRail(tags = opportunity.tags.toList())
        }

        if (!whyShown.isNullOrBlank()) {
            Spacer(Modifier.height(NexusSpacing.md))
            WhyShownNote(reason = whyShown)
        }

        // A verified link gets a date; an unverified one says nothing, because
        // "link may be dead" is not a fact anyone checked.
        if (opportunity.verifiedAt != null) {
            Spacer(Modifier.height(NexusSpacing.sm))
            Text(
                text = "LINK CONFIRMED ${opportunity.verifiedAt.toString().take(10)}",
                style = NexusTypography.TelemetryMicro,
                color = ext.positive.copy(alpha = 0.8f),
            )
        }
    }
}

/** The bookmark, as a shape change rather than only a colour change. */
@Composable
private fun SaveToggle(
    isSaved: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.nexus
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .size(36.dp)
            .clip(NexusShapes.xs)
            .background(
                if (isSaved) ext.signalContainer else scheme.surfaceContainerLow,
            )
            .border(
                NexusSpacing.hairline,
                if (isSaved) ext.signal.copy(alpha = 0.5f) else ext.hairlineStrong,
                NexusShapes.xs,
            )
            .clickable(role = Role.Button) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggle()
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
            contentDescription = if (isSaved) "Remove from saved" else "Save this listing",
            tint = if (isSaved) ext.onSignalContainer else scheme.onSurfaceVariant,
            modifier = Modifier.size(17.dp),
        )
    }
}

/** The ranking reason, in the product's voice, attributed as such. */
@Composable
private fun WhyShownNote(reason: String) {
    val ext = MaterialTheme.nexus
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(NexusShapes.xs)
            .background(ext.structureContainer.copy(alpha = 0.4f))
            .padding(horizontal = NexusSpacing.md, vertical = NexusSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(26.dp)
                .background(ext.structure),
        )
        Spacer(Modifier.width(NexusSpacing.md))
        Column {
            Text(
                text = "WHY THIS APPEARED".uppercase(Locale.ROOT),
                style = NexusTypography.TelemetryMicro,
                color = ext.structure,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = reason,
                style = NexusTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OpportunityTagRail(tags: List<String>) {
    if (tags.isEmpty()) return
    FlowRow(horizontalArrangement = Arrangement.spacedBy(NexusSpacing.xxs)) {
        tags.forEach { tag ->
            OpportunityTag(text = tag.removePrefix("#"))
        }
    }
}

/** One eligibility rule and whether the app can actually confirm it. */
data class EligibilityRule(
    val label: String,
    val verdict: EligibilityVerdict,
    val detail: String = "",
)

/**
 * `Unknown` is a required state.
 *
 * A rule the app cannot check must not default to a pass — a student who turns
 * out to be ineligible after applying loses more than a filter would have cost.
 */
enum class EligibilityVerdict { Met, NotMet, Unknown }

/**
 * The eligibility block, stated as a checklist the user can argue with.
 *
 * Every verdict is named in words and drawn with a distinct shape, so the
 * summary survives a greyscale screenshot and TalkBack alike.
 */
@Composable
fun EligibilitySummary(
    rules: List<EligibilityRule>,
    modifier: Modifier = Modifier,
    headline: String = "WHAT THE LISTING REQUIRES",
) {
    val ext = MaterialTheme.nexus
    if (rules.isEmpty()) return

    val met = rules.count { it.verdict == EligibilityVerdict.Met }
    val unknown = rules.count { it.verdict == EligibilityVerdict.Unknown }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(NexusShapes.md)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(NexusSpacing.hairline, ext.hairline, NexusShapes.md)
            .innerLight(NexusShapes.md)
            .padding(NexusSpacing.lg),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = headline.uppercase(Locale.ROOT),
                style = NexusTypography.DataLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "$met of ${rules.size} met",
                style = NexusTypography.Telemetry,
                color = ext.signal,
            )
        }
        Spacer(Modifier.height(NexusSpacing.md))
        rules.forEach { rule ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = NexusSpacing.xs),
                verticalAlignment = Alignment.Top,
            ) {
                MarkFor(rule.verdict)
                Spacer(Modifier.width(NexusSpacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rule.label,
                        style = NexusTypography.BodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (rule.detail.isNotBlank()) {
                        Text(
                            text = rule.detail,
                            style = NexusTypography.TelemetryMicro,
                            color = when (rule.verdict) {
                                EligibilityVerdict.NotMet -> ext.attention
                                EligibilityVerdict.Unknown ->
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                EligibilityVerdict.Met -> ext.positive
                            },
                        )
                    }
                }
            }
        }
        if (unknown > 0) {
            Spacer(Modifier.height(NexusSpacing.xs))
            HorizontalDivider(thickness = NexusSpacing.hairline, color = ext.hairline)
            Spacer(Modifier.height(NexusSpacing.sm))
            Text(
                text = "$unknown rule(s) could not be checked from your profile. " +
                    "Confirm against the listing before you apply.",
                style = NexusTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MarkFor(verdict: EligibilityVerdict) {
    val ext = MaterialTheme.nexus
    val mark = when (verdict) {
        EligibilityVerdict.Met ->
            Mark(ext.positive, Icons.Default.Check, "Requirement met")
        EligibilityVerdict.NotMet ->
            Mark(ext.attention, Icons.Default.Close, "Requirement not met")
        EligibilityVerdict.Unknown -> Mark(
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.AutoMirrored.Filled.HelpOutline,
            "Not checkable from your profile",
        )
    }
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(NexusShapes.tag)
            .background(mark.color.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = mark.icon,
            contentDescription = mark.description,
            tint = mark.color,
            modifier = Modifier.size(11.dp),
        )
    }
}

private class Mark(val color: Color, val icon: ImageVector, val description: String)

/**
 * A saved listing with its pipeline position attached.
 *
 * The stage row is drawn from [stages] and the index the caller supplies; a
 * listing with no recorded stages shows only the save, not a fabricated track.
 */
@Composable
fun SavedOpportunityCard(
    opportunity: Internship,
    stages: List<String>,
    currentStage: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val ext = MaterialTheme.nexus
    val haptic = LocalHapticFeedback.current
    val shape = NexusShapes.md

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.pressScale() else Modifier)
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            CompanyIdentity(
                name = opportunity.company.name,
                logoUrl = opportunity.company.logoUrl,
                domain = opportunity.company.domain,
                markSize = 32.dp,
                showDomain = false,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(NexusSpacing.sm))
            OpportunityTag(
                text = opportunity.kind.name.replace('_', ' '),
                tone = ext.structure,
            )
        }
        Spacer(Modifier.height(NexusSpacing.sm))
        Text(
            text = opportunity.title,
            style = NexusTypography.TitleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (stages.isNotEmpty()) {
            Spacer(Modifier.height(NexusSpacing.md))
            ApplicationTimeline(stages = stages, currentIndex = currentStage)
        }
    }
}

/** A filter control that reports a value rather than storing one. */
data class FilterOption(
    val key: String,
    val label: String,
)

/**
 * The filter surface as content, not as a sheet.
 *
 * The panel takes and returns selections and knows nothing about modals, so a
 * feature can host it in a bottom sheet, a side sheet on a tablet, or inline on
 * a wide screen — the same controls, three different shells.
 */
@Composable
fun OpportunityFilterPanel(
    groups: List<Pair<String, List<FilterOption>>>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
    onClear: (() -> Unit)? = null,
) {
    val ext = MaterialTheme.nexus
    val haptic = LocalHapticFeedback.current

    Column(modifier = modifier.fillMaxWidth()) {
        groups.forEach { (groupName, options) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = NexusSpacing.md, bottom = NexusSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = groupName.uppercase(Locale.ROOT),
                    style = NexusTypography.DataLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(NexusSpacing.sm))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(NexusSpacing.hairline)
                        .background(ext.hairline),
                )
            }
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(NexusSpacing.xs),
                verticalArrangement = Arrangement.spacedBy(NexusSpacing.xs),
            ) {
                options.forEach { option ->
                    val isSelected = option.key in selected
                    Text(
                        text = option.label,
                        style = NexusTypography.BodySmall,
                        color = if (isSelected) {
                            ext.onSignalContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier
                            .clip(NexusShapes.xs)
                            .background(
                                if (isSelected) {
                                    ext.signalContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                },
                            )
                            .border(
                                NexusSpacing.hairline,
                                if (isSelected) {
                                    ext.signal.copy(alpha = 0.5f)
                                } else {
                                    ext.hairlineStrong
                                },
                                NexusShapes.xs,
                            )
                            .clickable(role = Role.Button) {
                                haptic.performHapticFeedback(
                                    HapticFeedbackType.TextHandleMove,
                                )
                                onToggle(option.key)
                            }
                            .padding(horizontal = NexusSpacing.md, vertical = NexusSpacing.sm),
                    )
                }
            }
        }
        if (onClear != null && selected.isNotEmpty()) {
            Spacer(Modifier.height(NexusSpacing.lg))
            Text(
                text = "CLEAR ${selected.size}".uppercase(Locale.ROOT),
                style = NexusTypography.DataLabel,
                color = ext.attention,
                modifier = Modifier
                    .clip(NexusShapes.xs)
                    .clickable(role = Role.Button) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClear()
                    }
                    .padding(horizontal = NexusSpacing.sm, vertical = NexusSpacing.xs),
            )
        }
    }
}
