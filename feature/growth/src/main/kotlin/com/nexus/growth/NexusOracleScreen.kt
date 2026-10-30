package com.nexus.growth

import androidx.compose.ui.res.stringResource
import com.nexus.growth.R

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.IntelligenceCard
import com.nexus.designsystem.component.IntelligenceCardDefaults
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.component.hairline
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import java.util.Locale

@Composable
fun NexusOracleScreen(onBack: () -> Unit = {}) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = NexusSpacing.xl, vertical = NexusSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(NexusSpacing.md),
    ) {
        item {
            NexusTopBar(
                onBack = onBack,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.growth_nexus_oracle), fontWeight = FontWeight.SemiBold)
                    }
                },
            )
        }

        item { CareerForecastCard() }
        item { OracleIntelligenceCards() }
        item { SkillTrajectoryCard() }
        item { CareerPathProbabilities() }
        item { OpportunityInsightsCard() }

        item { Spacer(Modifier.height(NexusSpacing.xl)) }
    }
}

@Composable
private fun CareerForecastCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(NexusShapes.lg)
            .background(Brush.linearGradient(NexusGradients.PrimaryDeep), NexusShapes.lg)
            .hairline(color = NexusColors.SignalInk.copy(alpha = 0.16f), cornerRadiusDp = 18f)
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(R.string.growth_your_career_forecast),
                style = NexusTypography.DataLabel,
                color = NexusColors.SignalInk.copy(alpha = 0.86f),
            )
            Spacer(Modifier.height(NexusSpacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                ForecastColumn("30 day", "847", "\u2192 ~890", "+27 projected")
                ForecastColumn("90 day", "890", "\u2192 ~945", "Top 5% batch")
            }
        }
    }
}

@Composable
private fun ForecastColumn(label: String, current: String, projected: String, note: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label.uppercase(Locale.ROOT),
            style = NexusTypography.DataLabel,
            color = NexusColors.SignalInk.copy(alpha = 0.86f),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            current,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = NexusColors.SignalInk,
        )
        Text(
            projected,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = NexusColors.SignalInk.copy(alpha = 0.86f),
        )
        Text(
            note,
            style = MaterialTheme.typography.labelSmall,
            color = NexusColors.SignalInk.copy(alpha = 0.86f),
        )
    }
}

@Composable
private fun OracleIntelligenceCards() {
    val cards = listOf(
        IntelligenceCardDefaults.oracleCard(
            "If you study System Design for 2 hours today, you'll be interview-ready for Amazon in 23 days vs. 41 days at current pace.",
        ),
        IntelligenceCardDefaults.signalCard(
            "Microsoft Explore Intern opens TODAY. You: 91% match. Competition: Lower than average.",
            91,
        ),
        IntelligenceCardDefaults.mirrorCard(
            "You haven't practiced graphs in 18 days. Graph problems appear in 34% of FAANG rounds.",
        ),
        IntelligenceCardDefaults.momentumCard(
            "Your last 5 coding sessions were above average. This is the time to tackle your hardest goal.",
        ),
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        cards.forEach { card ->
            IntelligenceCard(data = card)
        }
    }
}

@Composable
private fun SkillTrajectoryCard() {
    NexusCard(shape = NexusShapes.lg) {
        Text(stringResource(R.string.growth_skill_trajectory), style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(NexusSpacing.md))

        val skills = listOf(
            Triple("System Design", 0.35f, 0.72f),
            Triple("DSA", 0.78f, 0.88f),
            Triple("Web Dev", 0.65f, 0.80f),
            Triple("AI/ML", 0.35f, 0.55f),
            Triple("Cloud", 0.50f, 0.68f),
        )

        skills.forEach { (name, current, projected) ->
            Spacer(Modifier.height(NexusSpacing.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(name, modifier = Modifier.width(100.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(
                    modifier = Modifier.weight(1f).height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(current).height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }
                Spacer(Modifier.width(NexusSpacing.sm))
                Text(stringResource(R.string.growth_projected_range,
                    (current * 100).toInt(), (projected * 100).toInt()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun CareerPathProbabilities() {
    NexusCard(shape = NexusShapes.lg) {
        Text(stringResource(R.string.growth_career_path_probability), style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(NexusSpacing.md))

        val paths = listOf(
            "SWE at product company" to 0.68f,
            "ML Engineer role" to 0.45f,
            "Startup founding team" to 0.28f,
            "Research/PhD path" to 0.21f,
        )

        paths.forEach { (path, prob) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(path, modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(
                    modifier = Modifier.width(120.dp).height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(prob).height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }
                Spacer(Modifier.width(NexusSpacing.sm))
                Text(stringResource(R.string.growth_progress_percent, (prob * 100).toInt()),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun OpportunityInsightsCard() {
    NexusCard(shape = NexusShapes.lg) {
        Text(stringResource(R.string.growth_opportunity_intel), style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(NexusSpacing.sm))
        Text(stringResource(R.string.growth_opportunity_google_step),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(NexusSpacing.md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.growth_apply_in_next_48_hours_u2014),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary)
        }
    }
}
