package com.nexus.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.designsystem.component.InstrumentGauge
import com.nexus.domain.model.UserStats
import kotlin.math.roundToInt
import com.nexus.designsystem.component.NexusIconButton
import com.nexus.designsystem.component.NexusPullToRefresh
import com.nexus.designsystem.component.CareerIntelligenceOrbit
import com.nexus.designsystem.component.OrbitDimension
import com.nexus.designsystem.component.SectionHeader
import com.nexus.designsystem.component.SignalField
import com.nexus.designsystem.component.SweepReadout
import com.nexus.designsystem.component.TelemetryItem
import com.nexus.designsystem.component.TelemetryStrip
import com.nexus.designsystem.component.borderBeam
import com.nexus.designsystem.component.editorial.EditorialSurface
import com.nexus.designsystem.effect.RevealOnLoad
import com.nexus.designsystem.effect.RevealWords
import com.nexus.designsystem.effect.nexusItemPlacement
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onInternshipClick: (String) -> Unit = {},
    onCertificationClick: (String) -> Unit = {},
    onViewAllInternships: () -> Unit = {},
    onNavigateToProductivity: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToApplications: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onOpenCommandPalette: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NexusPullToRefresh(
        isRefreshing = uiState.isOpportunitiesLoading,
        onRefresh = viewModel::retryOpportunities,
        modifier = Modifier.fillMaxSize(),
    ) {
        SignalField(modifier = Modifier.fillMaxSize())
        HomeFeed(
            uiState = uiState,
            onInternshipClick = onInternshipClick,
            onViewAllInternships = onViewAllInternships,
            onNavigateToApplications = onNavigateToApplications,
            onNavigateToNotifications = onNavigateToNotifications,
            onOpenCommandPalette = onOpenCommandPalette,
            onRetryOpportunities = viewModel::retryOpportunities,
        )
    }
}

@Composable
private fun HomeFeed(
    uiState: HomeUiState,
    onInternshipClick: (String) -> Unit,
    onViewAllInternships: () -> Unit,
    onNavigateToApplications: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onOpenCommandPalette: () -> Unit,
    onRetryOpportunities: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            GreetingSection(
                userName = uiState.userName,
                streakDays = uiState.streakDays,
                onNavigateToNotifications = onNavigateToNotifications,
                onOpenCommandPalette = onOpenCommandPalette,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        item {
            RevealOnLoad(delayMillis = 40) {
                CareerReadinessCard(
                    score = uiState.stats.careerReadinessScore,
                    caption = uiState.careerNextStep,
                    metric = uiState.careerNextStepMetric,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }

        item {
            RevealOnLoad(delayMillis = 110) {
                HomeTelemetry(
                    applications = uiState.stats.totalApplications,
                    certs = uiState.stats.completedCerts,
                    streak = uiState.stats.currentStreak,
                    interviews = uiState.stats.interviewsScheduled,
                )
            }
        }

        item {
            RevealOnLoad(delayMillis = 150) {
                CareerOrbitSection(
                    stats = uiState.stats,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.home_today_s_opportunities),
                index = "01",
                actionText = stringResource(R.string.home_view_all),
                onAction = onViewAllInternships,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        item {
            RevealOnLoad(delayMillis = 180) {
                when {
                    uiState.todayOpportunities.isNotEmpty() -> LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    items(uiState.todayOpportunities) { internship ->
                        OpportunityCard(
                            internship = internship,
                            onClick = { onInternshipClick(internship.id) }
                        )
                    }
                }

                uiState.isOpportunitiesLoading -> OpportunitySkeletonRow()

                uiState.opportunitiesError != null -> OpportunityInlineState(
                    icon = Icons.Default.WifiOff,
                    title = stringResource(R.string.home_opportunities_offline),
                    message = uiState.opportunitiesError.orEmpty(),
                    actionLabel = stringResource(R.string.home_try_again),
                    onAction = onRetryOpportunities,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                else -> OpportunityInlineState(
                    icon = Icons.Default.Search,
                    title = stringResource(R.string.home_no_opportunities_yet),
                    message = stringResource(R.string.home_no_opportunities_message),
                    actionLabel = stringResource(R.string.home_browse_all),
                    onAction = onViewAllInternships,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                }
            }
        }

        if (uiState.activeApplications.isNotEmpty()) {
            item {
                SectionHeader(
                    title = stringResource(R.string.home_your_applications),
                    index = "02",
                    actionText = stringResource(R.string.home_track),
                    onAction = onNavigateToApplications,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            items(uiState.activeApplications, key = { "application_${it.id}" }) { application ->
                ApplicationSummaryCard(
                    application = application,
                    modifier = nexusItemPlacement().padding(horizontal = 24.dp),
                )
            }
        }

        uiState.dailyQuote?.let { quote ->
            item {
                MotivationalQuoteCard(
                    quote = quote,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun HomeTelemetry(
    applications: Int,
    certs: Int,
    streak: Int,
    interviews: Int,
) {
    RevealOnLoad(delayMillis = 110) {
        TelemetryStrip(
            items = listOf(
                TelemetryItem(
                    label = stringResource(R.string.home_applications),
                    value = applications,
                ),
                TelemetryItem(
                    label = stringResource(R.string.home_certs),
                    value = certs,
                ),
                TelemetryItem(
                    label = stringResource(R.string.home_day_streak),
                    value = streak,
                    accent = MaterialTheme.nexus.attention,
                ),
                TelemetryItem(
                    label = stringResource(R.string.home_interviews),
                    value = interviews,
                ),
            ),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun GreetingSection(
    userName: String,
    streakDays: Int,
    onNavigateToNotifications: () -> Unit,
    onOpenCommandPalette: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greetingRes = when (currentHour) {
        in 5..11 -> R.string.home_greeting_morning
        in 12..16 -> R.string.home_greeting_afternoon
        in 17..20 -> R.string.home_greeting_evening
        else -> R.string.home_greeting_night
    }
    val greeting = stringResource(greetingRes)
    val dateLabel = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault()))
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RevealWords(
                text = greeting,
                // The greeting is the screen's one narrative sentence — the serif
                // byline above the instrument's loudest readout, not a caption
                // competing with the telemetry for the same tiny size.
                style = NexusTypography.EditorialTitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = dateLabel,
                style = NexusTypography.TelemetryMicro,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            )
            NexusIconButton(
                icon = Icons.Default.Search,
                contentDescription = stringResource(R.string.home_cd_search),
                onClick = onOpenCommandPalette,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            NexusIconButton(
                icon = Icons.Default.Notifications,
                contentDescription = stringResource(R.string.home_cd_notifications),
                onClick = onNavigateToNotifications,
                modifier = Modifier.padding(start = 4.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = userName,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (streakDays > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = MaterialTheme.nexus.attention,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = pluralStringResource(R.plurals.home_streak_days, streakDays, streakDays),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * The orbit renders the same five readings the telemetry strip states, as
 * fractions of stated campaign milestones — five applications tracked, five
 * certifications earned, a seven-day consistency week, three interviews
 * booked — so a half-full satellite is a milestone half-met, never a score
 * the readiness model never produced.
 */
@Composable
private fun CareerOrbitSection(stats: UserStats, modifier: Modifier = Modifier) {
    val dimensions = listOf(
        OrbitDimension(
            id = "readiness",
            label = stringResource(R.string.home_orbit_readiness),
            fraction = stats.careerReadinessScore.coerceIn(0f, 1f),
            reading = "${(stats.careerReadinessScore * 100).roundToInt()}%",
        ),
        OrbitDimension(
            id = "applications",
            label = stringResource(R.string.home_applications),
            fraction = (stats.totalApplications / 5f).coerceIn(0f, 1f),
            reading = stringResource(R.string.home_orbit_reading_tracked, stats.totalApplications),
        ),
        OrbitDimension(
            id = "certs",
            label = stringResource(R.string.home_certs),
            fraction = (stats.completedCerts / 5f).coerceIn(0f, 1f),
            reading = stringResource(R.string.home_orbit_reading_earned, stats.completedCerts),
        ),
        OrbitDimension(
            id = "streak",
            label = stringResource(R.string.home_day_streak),
            fraction = (stats.currentStreak / 7f).coerceIn(0f, 1f),
            reading = stringResource(R.string.home_orbit_reading_streak, stats.currentStreak),
        ),
        OrbitDimension(
            id = "interviews",
            label = stringResource(R.string.home_interviews),
            fraction = (stats.interviewsScheduled / 3f).coerceIn(0f, 1f),
            reading = stringResource(R.string.home_orbit_reading_scheduled, stats.interviewsScheduled),
        ),
    )
    CareerIntelligenceOrbit(
        dimensions = dimensions,
        centerValue = "${(stats.careerReadinessScore * 100).roundToInt()}",
        centerLabel = stringResource(R.string.home_orbit_readiness),
        hint = stringResource(R.string.home_orbit_hint),
        contentDescription = dimensions.joinToString(separator = ", ") { "${it.label}: ${it.reading}" },
        modifier = modifier,
    )
}

@Composable
private fun CareerReadinessCard(
    score: Float,
    caption: String,
    metric: String,
    modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.nexus
    // Single-clock hero: SweepReadout's sweepValue drives both the digits
    // and the gauge arc (via onArc), so the number can never disagree
    // with the dial mid-sweep. Gauge runs with animateProgress=false so
    // it does not re-animate the already-animated value.
    var arcFraction by remember { mutableFloatStateOf(0f) }
    val readinessCaption = stringResource(R.string.home_readiness_caption)

    // Readiness sits on paper, not on a field of its own colour. A saturated
    // plate makes the loudest object on the screen the *background* of a
    // measurement, and a gauge drawn in ink on ink — which is what this was —
    // cannot be read at all. On paper the dial is the only lit thing, so it is
    // the only thing the eye measures.
    // borderBeam: the hairline, lit — the one signature plate per screen.
    EditorialSurface(modifier = modifier.fillMaxWidth().borderBeam(shape = NexusShapes.lg)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.text_career_readiness).uppercase(Locale.ROOT),
                    style = NexusTypography.DataLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    SweepReadout(
                        score = (score * 100f).coerceIn(0f, 100f),
                        decimals = 0,
                        style = NexusTypography.TelemetryDisplay.copy(
                            fontSize = 54.sp,
                            lineHeight = 56.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        onArc = { arcFraction = (it / 100f).coerceIn(0f, 1f) },
                    )
                    Text(
                        text = "%",
                        style = NexusTypography.TelemetryLarge,
                        color = ext.signal.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 4.dp, bottom = 7.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            // Left on the gauge's own defaults: signal arc, ink track. That is the
            // one combination that measures cleanly on this surface. Driven by
            // the same sweepValue as the digits (animateProgress=false).
            InstrumentGauge(
                progress = arcFraction,
                modifier = Modifier.size(112.dp),
                animateProgress = false,
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(NexusSpacing.hairline)
                .background(ext.hairline),
        )
        Spacer(modifier = Modifier.height(13.dp))
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp, end = 10.dp)
                    .size(width = 2.dp, height = 11.dp)
                    .background(ext.signal),
            )
            // The figure and the sentence travel in separate registers: the
            // metric reads first in mono, the caption speaks beneath it. One
            // merged sentence made the number something to skim past.
            Column {
                if (metric.isNotBlank()) {
                    Text(
                        text = metric.uppercase(Locale.ROOT),
                        style = NexusTypography.Telemetry,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = caption.ifBlank { readinessCaption },
                    style = NexusTypography.EditorialBody,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
