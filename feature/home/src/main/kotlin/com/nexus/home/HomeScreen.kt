package com.nexus.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.designsystem.component.InstrumentGauge
import com.nexus.designsystem.component.SectionHeader
import com.nexus.designsystem.component.SignalField
import com.nexus.designsystem.component.TelemetryItem
import com.nexus.designsystem.component.TelemetryStrip
import com.nexus.designsystem.component.hairline
import com.nexus.designsystem.component.instrumentGrid
import com.nexus.designsystem.component.sheen
import com.nexus.designsystem.effect.RevealOnLoad
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusTypography
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
    onNavigateToNotifications: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        SignalField(modifier = Modifier.fillMaxSize())

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
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            item {
                RevealOnLoad(delayMillis = 40) {
                    CareerReadinessCard(
                        score = uiState.stats.careerReadinessScore,
                        caption = uiState.careerNextStep,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            item {
                HomeTelemetry(
                    applications = uiState.stats.totalApplications,
                    certs = uiState.stats.completedCerts,
                    streak = uiState.stats.currentStreak,
                    interviews = uiState.stats.interviewsScheduled,
                )
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
                        onAction = viewModel::retryOpportunities,
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

            if (uiState.activeApplications.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = stringResource(R.string.home_your_applications),
                        index = "02",
                        actionText = stringResource(R.string.home_track),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                items(uiState.activeApplications) { application ->
                    ApplicationSummaryCard(
                        application = application,
                        modifier = Modifier.padding(horizontal = 24.dp)
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

        if (!uiState.tourCompleted) {
            TourOverlay(onFinish = { viewModel.completeTour() })
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
                    accent = NexusColors.Amber400,
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
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = dateLabel,
                style = NexusTypography.TelemetryMicro,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            )
            IconButton(
                onClick = onNavigateToNotifications,
                modifier = Modifier.padding(start = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = stringResource(R.string.home_cd_notifications),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
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
                    tint = NexusColors.Amber400,
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

@Composable
private fun CareerReadinessCard(
    score: Float,
    caption: String,
    modifier: Modifier = Modifier,
) {
    val animatedScore by animateFloatAsState(
        targetValue = score,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "score"
    )
    val readinessCaption = stringResource(R.string.home_readiness_caption)
    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                brush = Brush.linearGradient(NexusGradients.Ascent),
                shape = shape
            )
            .instrumentGrid(spacingDp = 30f, alpha = 0.045f)
            .hairline(
                color = Color.White.copy(alpha = 0.16f),
                cornerRadiusDp = 28f
            )
            .sheen(alpha = 0.07f)
            .padding(horizontal = 22.dp, vertical = 20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.text_career_readiness),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White.copy(alpha = 0.70f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${(animatedScore * 100).toInt()}",
                            style = NexusTypography.TelemetryDisplay.copy(
                                fontSize = 54.sp,
                                lineHeight = 56.sp,
                            ),
                            color = Color.White,
                        )
                        Text(
                            text = "%",
                            style = NexusTypography.TelemetryLarge,
                            color = Color.White.copy(alpha = 0.62f),
                            modifier = Modifier.padding(start = 4.dp, bottom = 7.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                InstrumentGauge(
                    progress = score,
                    modifier = Modifier.size(112.dp),
                    accent = NexusColors.Cyan400,
                    trackColor = Color.White.copy(alpha = 0.15f),
                    tickColor = Color.White.copy(alpha = 0.32f),
                    stroke = 8.dp,
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.14f))
            )
            Spacer(modifier = Modifier.height(13.dp))
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp, end = 10.dp)
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(NexusColors.Cyan400)
                )
                Text(
                    text = caption.ifBlank { readinessCaption },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.92f)
                )
            }
        }
    }
}
