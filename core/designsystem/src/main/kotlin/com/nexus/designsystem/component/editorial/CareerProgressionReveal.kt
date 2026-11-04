package com.nexus.designsystem.component.editorial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nexus.common.Constants
import com.nexus.designsystem.component.InstrumentGauge
import com.nexus.designsystem.theme.NexusReveal
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus
import com.nexus.designsystem.theme.revealClock
import com.nexus.designsystem.theme.revealFraction
import com.nexus.designsystem.theme.sweepValue
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private val scoreColumnLabel: String = "${Constants.APP_NAME} SCORE".uppercase(Locale.ROOT)

/**
 * The signature interaction: a career score arriving as an argument.
 *
 * A score on its own is a number the user cannot interrogate. So the reveal runs
 * as one gesture with a definite order — the statement, then the instrument
 * sweeping to its value while the digits inside count to the same value, then the
 * ledger of what earns it, then what moved since the last reading. Each element
 * owns a window of a single clock ([revealClock]), which is what keeps a gauge and
 * the number inside it from disagreeing mid-sweep: they read one animated value.
 *
 * The ledger states points earned against points available, never a bare
 * percentage. "172 of 250" can be checked against the weighting the app actually
 * uses; "69%" cannot, and invites the reader to believe the app knows what a
 * recruiter will decide.
 *
 * [weeklyChange] and [percentile] are nullable on purpose. The score calculator
 * does not compute them in every build, and a `0` there is an unmeasured quantity
 * wearing the clothes of a measurement — which is worse than an absent line.
 *
 * Under reduced motion the clock is already settled on the first frame, so the
 * whole composition appears finished, with no sweep and no counting.
 */
@Composable
fun CareerProgressionReveal(
    score: Int,
    maxScore: Int,
    readings: List<ProgressionReading>,
    modifier: Modifier = Modifier,
    headline: String = "",
    weeklyChange: Int? = null,
    percentile: Int? = null,
    gaugeSize: Int = 140,
) {
    val clock = revealClock()
    // One flip, in the same frame the gauge starts its own sweep, so the digits and
    // the arc run the same curve from the same first frame.
    var settled by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { settled = true }

    val fraction = if (maxScore <= 0) 0f else (score.toFloat() / maxScore).coerceIn(0f, 1f)
    val shown = (sweepValue(fraction, settled) * maxScore).roundToInt()
    val heroEase = revealFraction(clock, NexusReveal.Window.hero)
    val statementEase = revealFraction(clock, NexusReveal.Window.headline)

    Column(modifier = modifier.fillMaxWidth()) {
        if (headline.isNotBlank()) {
            Text(
                text = headline,
                style = NexusTypography.EditorialHeadline,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .alpha(statementEase)
                    .offset(y = 10.dp * (1f - statementEase)),
            )
            Spacer(Modifier.height(NexusSpacing.lg))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.alpha(heroEase)) {
                InstrumentGauge(
                    progress = fraction,
                    modifier = Modifier.size(gaugeSize.dp),
                    contentDescription = "$score of $maxScore",
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Hidden from TalkBack: the gauge already reports the value
                        // once, and a counting glyph would announce mid-sweep.
                        Text(
                            text = "$shown",
                            style = NexusTypography.TelemetryLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.clearAndSetSemantics { },
                        )
                        Text(
                            text = "OF $maxScore",
                            style = NexusTypography.TelemetryMicro,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.clearAndSetSemantics { },
                        )
                    }
                }
            }

            Spacer(Modifier.width(NexusSpacing.lg))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scoreColumnLabel,
                    style = NexusTypography.DataLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
                Spacer(Modifier.height(NexusSpacing.sm))
                if (weeklyChange != null) {
                    ChangeReading(change = weeklyChange, ease = revealFraction(clock, NexusReveal.Window.footnote))
                }
                if (percentile != null) {
                    Spacer(Modifier.height(NexusSpacing.sm))
                    RankReading(percentile = percentile, ease = revealFraction(clock, NexusReveal.Window.footnote, rank = 1))
                }
            }
        }

        if (readings.isNotEmpty()) {
            Spacer(Modifier.height(NexusSpacing.xl))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(NexusSpacing.md),
            ) {
                readings.forEachIndexed { index, reading ->
                    ReadingRow(
                        reading = reading,
                        ease = revealFraction(clock, NexusReveal.Window.readings, rank = index),
                    )
                }
            }
        }
    }
}

/** One line of the ledger: what a component of the score is worth, and what has
 * been earned against it. */
@Composable
private fun ReadingRow(reading: ProgressionReading, ease: Float) {
    val ext = MaterialTheme.nexus
    val settledFraction = reading.fraction

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(ease)
            .offset(y = 8.dp * (1f - ease)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = reading.label.uppercase(),
                style = NexusTypography.DataLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(NexusSpacing.md))
            // Earned against available, so the figure is checkable arithmetic.
            Text(
                text = "${reading.earned} / ${reading.available}",
                style = NexusTypography.Telemetry,
                color = if (reading.isComplete) ext.positive else MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(Modifier.height(NexusSpacing.xs))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                // The rule restates the numbers beside it; a screen reader would
                // hear the same figure twice.
                .clearAndSetSemantics { }
                // The ink track anchors the fill: a sliver like 1/200 must read
                // as a rule barely started, not a stray mark floating on the
                // plate. Same idiom as the gauges' lit arc on its ink track.
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    // Grows to its own measured share as the row arrives.
                    .fillMaxWidth(settledFraction * ease)
                    .background(if (reading.isComplete) ext.positive else ext.signal),
            )
        }
        if (reading.detail.isNotBlank()) {
            Spacer(Modifier.height(NexusSpacing.xs))
            Text(
                text = reading.detail,
                style = NexusTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            )
        }
    }
}

/** The week-over-week move, signed, with the direction stated in words too. */
@Composable
private fun ChangeReading(change: Int, ease: Float) {
    val ext = MaterialTheme.nexus
    val isGain = change > 0
    val isLoss = change < 0
    val color = when {
        isGain -> ext.positive
        isLoss -> ext.attention
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier.alpha(ease),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(width = 2.dp, height = 14.dp)
                .background(color),
        )
        Spacer(Modifier.width(NexusSpacing.sm))
        Column {
            Text(
                text = if (change == 0) "NO CHANGE" else "${if (isGain) "+" else "−"}${abs(change)}",
                style = NexusTypography.TelemetryLarge.copy(fontWeight = FontWeight.Bold),
                color = color,
            )
            Text(
                text = "SINCE LAST WEEK",
                style = NexusTypography.DataLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        }
    }
}

/**
 * Rank among peers, or nothing at all.
 *
 * A percentile only means something against a cohort the app actually measured.
 * Where that comparison has not been computed, the caller passes null and this
 * line does not exist — the alternative is a number that looks like an insight.
 */
@Composable
private fun RankReading(percentile: Int, ease: Float) {
    Column(modifier = Modifier.alpha(ease)) {
        Text(
            text = "TOP ${100 - percentile.coerceIn(1, 99)}%",
            style = NexusTypography.Telemetry,
            color = MaterialTheme.nexus.signal,
        )
        Text(
            text = "OF RANKED USERS",
            style = NexusTypography.DataLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )
    }
}

/**
 * One weighted component of the score.
 *
 * [earned] and [available] are points, not fractions, because the arithmetic a
 * user could repeat is the whole reason the line is worth printing.
 */
data class ProgressionReading(
    val label: String,
    val earned: Int,
    val available: Int,
    val detail: String = "",
) {
    /** The share earned, clamped so a mis-scaled caller cannot draw past the rule. */
    val fraction: Float
        get() = if (available <= 0) 0f else (earned.toFloat() / available).coerceIn(0f, 1f)

    val isComplete: Boolean
        get() = available > 0 && earned >= available
}
