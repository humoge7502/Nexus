package com.nexus.designsystem.component.opportunity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.nexus.designsystem.component.innerLight
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * The opportunity card family.
 *
 * Everything here follows one discipline: a field is shown when the source
 * published it and omitted when it did not. A stipend no company stated is not
 * rounded down to "₹0" and not invented as a range — it is absent, and the
 * layout absorbs the gap. A deadline that does not exist reads "Rolling", which
 * is a true statement about rolling admissions rather than a blank.
 */

/** Word-initials, capped at two letters, for a company with no logo. */
private fun monogramFor(name: String): String {
    val words = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        words.isEmpty() -> "?"
        words.size == 1 -> words[0].take(2).uppercase()
        else -> (words[0].take(1) + words[1].take(1)).uppercase()
    }
}

@Composable
private fun CompanyMonogram(name: String, size: Dp) {
    // Set against the mark it has to fit inside, so one component serves the
    // 44dp card identity and the 28dp list row without a second style.
    val glyphSize = with(LocalDensity.current) { (size * 0.34f).toSp() }
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = monogramFor(name),
            style = NexusTypography.Telemetry.copy(
                fontSize = glyphSize,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
            ),
            color = MaterialTheme.nexus.signal,
        )
    }
}

/**
 * A company's identity: the logo when the feed carries one, a milled monogram
 * until it arrives or if it never does, and the domain underneath so the name is
 * never the only clue.
 */
@Composable
fun CompanyIdentity(
    name: String,
    modifier: Modifier = Modifier,
    logoUrl: String? = null,
    domain: String = "",
    markSize: Dp = 44.dp,
    showDomain: Boolean = true,
) {
    val shape = NexusShapes.sm
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(markSize)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .innerLight(shape),
            contentAlignment = Alignment.Center,
        ) {
            if (logoUrl.isNullOrBlank()) {
                CompanyMonogram(name = name, size = markSize)
            } else {
                // The monogram is the loading state and the failure state, so a
                // slow or dead CDN never leaves an empty square in a card.
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(logoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = { CompanyMonogram(name = name, size = markSize) },
                    error = { CompanyMonogram(name = name, size = markSize) },
                )
            }
        }
        if (showDomain) {
            Spacer(Modifier.width(NexusSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = NexusTypography.TitleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (domain.isNotBlank()) {
                    Text(
                        text = domain.removePrefix("www."),
                        style = NexusTypography.TelemetryMicro,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** A single dotted-leader fact row: label · · · value, with the value measured. */
@Composable
fun FactRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = label,
            style = NexusTypography.BodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(NexusSpacing.sm))
        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 4.dp),
            thickness = 1.dp,
            color = MaterialTheme.nexus.hairline,
        )
        Spacer(Modifier.width(NexusSpacing.sm))
        Text(
            text = value,
            style = NexusTypography.Telemetry,
            color = valueColor,
        )
    }
}

/**
 * How close a deadline is, said in the unit a student actually decides in.
 *
 * "Rolling" keeps the neutral tone: an admissions process with no published
 * close date is not an urgent one, and colouring it amber would train the user
 * to ignore the amber.
 */
@Composable
fun DeadlineIndicator(
    deadline: LocalDate?,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
) {
    val days = deadline?.let { ChronoUnit.DAYS.between(today, it) }
    val isUrgent = days != null && days <= 3L
    val text = when {
        deadline == null -> "ROLLING"
        days!! < 0L -> "CLOSED"
        days == 0L -> "CLOSES TODAY"
        else -> "CLOSES IN $days DAYS"
    }
    val color = if (isUrgent) MaterialTheme.nexus.attention else MaterialTheme.colorScheme.onSurfaceVariant

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (isUrgent) {
            // A non-colour cue as well, so urgency survives greyscale and TalkBack.
            Box(
                modifier = Modifier
                    .size(width = 2.dp, height = 11.dp)
                    .background(color),
            )
            Spacer(Modifier.width(NexusSpacing.xs))
        }
        Text(
            text = text,
            style = NexusTypography.DataLabel,
            color = color,
        )
    }
}

/**
 * Skill overlap stated as counts, never as a percentage.
 *
 * A "92% match" implies the app knows how a recruiter weighs a CV; it does not.
 * "4 of 6" is checkable against the listing, which is the whole point of
 * showing it at all.
 */
@Composable
fun SkillMatchIndicator(
    requiredSkills: List<String>,
    heldSkills: Set<String>,
    modifier: Modifier = Modifier,
) {
    if (requiredSkills.isEmpty()) return

    fun isHeld(name: String) = heldSkills.any { it.equals(name, ignoreCase = true) }
    val held = requiredSkills.count(::isHeld)
    val total = requiredSkills.size
    val complete = held == total

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "SKILLS IN THE LISTING",
                style = NexusTypography.DataLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
            Text(
                text = "$held of $total",
                style = NexusTypography.Telemetry,
                color = if (complete) MaterialTheme.nexus.positive else MaterialTheme.nexus.signal,
            )
        }
        Spacer(Modifier.height(NexusSpacing.xs))
        // One cell per required skill, filled only where the user actually has it.
        Row(
            modifier = Modifier.height(3.dp),
            horizontalArrangement = Arrangement.spacedBy(NexusSpacing.xxs),
        ) {
            requiredSkills.forEach { name ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            if (isHeld(name)) {
                                MaterialTheme.nexus.signal
                            } else {
                                MaterialTheme.nexus.hairlineStrong
                            }
                        ),
                )
            }
        }
    }
}

/** The decisive facts about an opening, as a column of readings. */
@Composable
fun OpportunityMetadata(
    modifier: Modifier = Modifier,
    locationLine: String? = null,
    mode: String? = null,
    duration: String? = null,
    stipend: String? = null,
    sourceName: String? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (!locationLine.isNullOrBlank()) {
            FactRow("Location", locationLine)
            Spacer(Modifier.height(NexusSpacing.xxs))
        }
        if (!mode.isNullOrBlank()) {
            FactRow("Mode", mode)
            Spacer(Modifier.height(NexusSpacing.xxs))
        }
        if (!duration.isNullOrBlank()) {
            FactRow("Duration", duration)
            Spacer(Modifier.height(NexusSpacing.xxs))
        }
        // An unpublished stipend renders nothing at all — silence, not a zero.
        if (!stipend.isNullOrBlank()) {
            FactRow("Stipend", stipend, valueColor = MaterialTheme.nexus.positive)
            Spacer(Modifier.height(NexusSpacing.xxs))
        }
        if (!sourceName.isNullOrBlank()) {
            Spacer(Modifier.height(NexusSpacing.xs))
            Text(
                text = "SOURCE · ${sourceName.uppercase()}",
                style = NexusTypography.DataLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * The application pipeline as a measured track rather than a coloured badge.
 *
 * Cells fill forward from the first stage and the current stage is named in
 * text, so the row reads as progress without relying on colour alone.
 */
@Composable
fun ApplicationTimeline(
    stages: List<String>,
    currentIndex: Int,
    modifier: Modifier = Modifier,
) {
    if (stages.isEmpty()) return
    val reached = currentIndex.coerceIn(0, stages.lastIndex)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.height(3.dp),
            horizontalArrangement = Arrangement.spacedBy(NexusSpacing.xxs),
        ) {
            stages.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            when {
                                index < reached -> MaterialTheme.nexus.signal
                                index == reached -> MaterialTheme.nexus.signal.copy(alpha = 0.45f)
                                else -> MaterialTheme.nexus.hairline
                            }
                        ),
                )
            }
        }
        Spacer(Modifier.height(NexusSpacing.xs))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stages[reached].uppercase(),
                style = NexusTypography.DataLabel,
                color = MaterialTheme.nexus.signal,
            )
            Text(
                text = "${reached + 1} / ${stages.size}",
                style = NexusTypography.TelemetryMicro,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** A small tag in the near-square instrument style — never a pill. */
@Composable
fun OpportunityTag(
    text: String,
    modifier: Modifier = Modifier,
    tone: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier
            .clip(NexusShapes.tag)
            .background(tone.copy(alpha = 0.10f))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        style = NexusTypography.DataLabel,
        color = tone,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
