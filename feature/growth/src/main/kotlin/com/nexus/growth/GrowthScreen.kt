package com.nexus.growth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.AnimatedStatCard
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.theme.NexusAnimation
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class Metric(val label: String, val progress: Float, val color: Color)

private val nexusMetrics = persistentListOf(
    Metric("Skill Progress", 0.68f, NexusColors.Indigo400),
    Metric("Opportunity Engagement", 0.72f, NexusColors.Cyan400),
    Metric("Consistency", 0.80f, NexusColors.Emerald400),
    Metric("Portfolio", 0.55f, NexusColors.Amber400),
    Metric("Community", 0.45f, NexusColors.Rose400),
)

private data class RadarAxis(val label: String, val value: Float)

private val radarAxes = persistentListOf(
    RadarAxis("DSA", 0.78f),
    RadarAxis("Web Dev", 0.65f),
    RadarAxis("System Design", 0.42f),
    RadarAxis("AI/ML", 0.35f),
    RadarAxis("Cloud", 0.50f),
    RadarAxis("Mobile", 0.60f),
)

private data class RoadmapPhaseItem(
    val emoji: String,
    val title: String,
    val status: String,
    val progress: Float? = null,
)

private val roadmapPhases = persistentListOf(
    RoadmapPhaseItem("✅", "HTML/CSS", "Done"),
    RoadmapPhaseItem("✅", "JavaScript", "Done"),
    RoadmapPhaseItem("🔄", "React", "68% complete", 0.68f),
    RoadmapPhaseItem("⏳", "Node.js", "Not started"),
    RoadmapPhaseItem("⏳", "System Design", "Not started"),
)

private val weekDays = listOf("M", "T", "W", "T", "F", "S", "S")
private val weekActive = listOf(true, true, true, true, true, false, false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthScreen(
    onDsaTracker: () -> Unit = {},
    onRoadmap: () -> Unit = {},
    onGoals: () -> Unit = {},
    onOracle: () -> Unit = {},
    onArena: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    var visible by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) { visible = 1f }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    "Growth",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = NexusSpacing.xl, vertical = NexusSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(NexusSpacing.md),
        ) {
            item(key = "score_card") { NexusScoreCard() }
            item(key = "radar") { SkillRadarChart(radarAxes) }
            item(key = "streak") { LearningStreak() }
            item(key = "stats") { QuickStatsRow() }
            item(key = "roadmap") { SkillRoadmapPreview(onRoadmap = onRoadmap) }
            item(key = "nexus_actions") { NexusActionsRow(onOracle = onOracle, onArena = onArena) }
            item(key = "challenge") { TodaysChallenge() }
        }
    }
}

@Composable
private fun NexusScoreCard() {
    val animatedScore by animateFloatAsState(
        targetValue = 847f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "score",
    )

    NexusCard(
        gradient = NexusGradients.Glass,
        glassEffect = true,
        shape = NexusShapes.lg,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                Canvas(modifier = Modifier.size(160.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2f
                    val topLeft = Offset((size.width - radius * 2f) / 2f, (size.height - radius * 2f) / 2f)
                    val arcSize = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f)

                    drawArc(
                        color = NexusColors.Neutral700,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )

                    val sweep = 847f / 1000f * 360f
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(NexusColors.Indigo500, NexusColors.Cyan400, NexusColors.Indigo500),
                            center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f),
                        ),
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = animatedScore.toInt().toString(),
                        style = MaterialTheme.typography.displayMedium.copy(
                            brush = Brush.linearGradient(NexusGradients.Purple),
                        ),
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        "NEXUS Score",
                        style = MaterialTheme.typography.labelMedium,
                        color = NexusColors.Neutral300,
                    )
                }
            }

            Spacer(Modifier.height(NexusSpacing.xs))

            Text(
                "↑ +12 this week · Top 8% of users",
                style = MaterialTheme.typography.labelSmall,
                color = NexusColors.Emerald400,
            )

            Spacer(Modifier.height(NexusSpacing.md))

            for (metric in nexusMetrics) {
                MetricBar(metric)
                Spacer(Modifier.height(NexusSpacing.xs))
            }
        }
    }
}

@Composable
private fun MetricBar(metric: Metric) {
    val animatedProgress by animateFloatAsState(
        targetValue = metric.progress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "metric_${metric.label}",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            metric.label,
            style = MaterialTheme.typography.labelSmall,
            color = NexusColors.Neutral300,
            modifier = Modifier.weight(1f),
        )
        Text(
            "${(metric.progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = metric.color,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.width(NexusSpacing.sm))
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(NexusColors.Neutral700),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(metric.color),
            )
        }
    }
}

@Composable
private fun SkillRadarChart(axes: ImmutableList<RadarAxis>) {
    NexusCard(shape = NexusShapes.lg) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Skill Radar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(NexusSpacing.md))

            val animatedValues = remember { axes.map { mutableFloatStateOf(0f) } }
            LaunchedEffect(Unit) {
                animatedValues.forEachIndexed { index, state ->
                    kotlinx.coroutines.delay(index * 100L)
                    state.floatValue = axes[index].value
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .padding(NexusSpacing.sm),
            ) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val radius = minOf(cx, cy) * 0.85f
                val angleStep = 2f * PI.toFloat() / axes.size
                val startAngle = -PI.toFloat() / 2f

                for (level in 1..4) {
                    val r = radius * level / 4f
                    val path = Path()
                    axes.indices.forEach { i ->
                        val angle = startAngle + i * angleStep
                        val x = cx + r * cos(angle)
                        val y = cy + r * sin(angle)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    path.close()
                    drawPath(path, NexusColors.Neutral700.copy(alpha = 0.4f), style = Stroke(1.dp.toPx()))
                }

                val dataPath = Path()
                axes.indices.forEach { i ->
                    val angle = startAngle + i * angleStep
                    val r = radius * animatedValues[i].floatValue
                    val x = cx + r * cos(angle)
                    val y = cy + r * sin(angle)
                    if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
                }
                dataPath.close()

                drawPath(
                    dataPath,
                    brush = Brush.linearGradient(
                        listOf(NexusColors.Indigo500.copy(alpha = 0.4f), NexusColors.Cyan400.copy(alpha = 0.15f)),
                    ),
                )
                drawPath(dataPath, NexusColors.Indigo400, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))

                axes.indices.forEach { i ->
                    val angle = startAngle + i * angleStep
                    val r = radius * animatedValues[i].floatValue
                    val x = cx + r * cos(angle)
                    val y = cy + r * sin(angle)

                    drawCircle(NexusColors.Indigo400, 4.dp.toPx(), Offset(x, y))
                    drawCircle(Color.White, 2.dp.toPx(), Offset(x, y))

                    val lr = radius + 16.dp.toPx()
                    val lx = cx + lr * cos(angle)
                    val ly = cy + lr * sin(angle)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                axes.forEach { axis ->
                    Text(
                        axis.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = NexusColors.Neutral400,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(40.dp),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun LearningStreak() {
    NexusCard(shape = NexusShapes.lg) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "🔥 23 Days · Best: 31 · This Month: 28",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(NexusSpacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                for ((index, day) in weekDays.withIndex()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        val isActive = weekActive[index]
                        val isToday = index == 4
                        val dotColor = if (isActive) {
                            if (isToday) NexusGradients.Purple else listOf(NexusColors.Indigo400)
                        } else {
                            listOf(NexusColors.Neutral600)
                        }

                        Box(
                            modifier = Modifier
                                .size(if (isToday) 36.dp else 28.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.linearGradient(
                                        if (isActive) dotColor else listOf(NexusColors.Neutral600),
                                    ),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                day,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isActive) Color.White else NexusColors.Neutral400,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStatsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(NexusSpacing.sm),
    ) {
        AnimatedStatCard(
            value = 142,
            label = "Problems Solved",
            modifier = Modifier.weight(1f),
            gradient = NexusGradients.Primary,
        )
        AnimatedStatCard(
            value = 5,
            label = "Certifications",
            modifier = Modifier.weight(1f),
            gradient = NexusGradients.Ocean,
        )
        AnimatedStatCard(
            value = 7,
            label = "Projects",
            modifier = Modifier.weight(1f),
            gradient = NexusGradients.Success,
        )
    }
}

@Composable
private fun SkillRoadmapPreview(onRoadmap: () -> Unit) {
    NexusCard(shape = NexusShapes.lg) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "FULL STACK DEVELOPER PATH",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "View Full →",
                    style = MaterialTheme.typography.labelMedium,
                    color = NexusColors.Indigo400,
                    modifier = Modifier.clip(NexusShapes.xs),
                )
            }

            Spacer(Modifier.height(NexusSpacing.md))

            for ((index, phase) in roadmapPhases.withIndex()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = NexusSpacing.xxs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = phase.emoji, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.width(NexusSpacing.sm))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            phase.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (phase.status == "Done") NexusColors.Emerald400
                                    else if (phase.progress != null) MaterialTheme.colorScheme.onSurface
                                    else NexusColors.Neutral500,
                        )
                        if (phase.progress != null) {
                            Spacer(Modifier.height(NexusSpacing.xxs))
                            val animProgress by animateFloatAsState(
                                targetValue = phase.progress,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                                label = "roadmap_${phase.title}",
                            )
                            LinearProgressIndicator(
                                progress = { animProgress },
                                modifier = Modifier.fillMaxWidth().height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = NexusColors.Indigo400,
                                trackColor = NexusColors.Neutral700,
                            )
                            Spacer(Modifier.height(NexusSpacing.xxs))
                            Text(
                                phase.status,
                                style = MaterialTheme.typography.labelSmall,
                                color = NexusColors.Indigo300,
                            )
                        } else {
                            Text(
                                phase.status,
                                style = MaterialTheme.typography.labelSmall,
                                color = NexusColors.Neutral500,
                            )
                        }
                    }
                }

                if (index < roadmapPhases.lastIndex) {
                    Spacer(Modifier.height(NexusSpacing.xxs))
                }
            }
        }
    }
}

@Composable
private fun NexusActionsRow(onOracle: () -> Unit, onArena: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(NexusSpacing.sm),
    ) {
        NexusCard(
            onClick = onOracle,
            modifier = Modifier.weight(1f),
            gradient = listOf(NexusColors.Indigo500.copy(alpha = 0.15f), NexusColors.Cyan400.copy(alpha = 0.05f)),
            shape = NexusShapes.md,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "\uD83D\uDD2E", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(NexusSpacing.xxs))
                Text("Oracle", style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold, color = NexusColors.Indigo400)
                Text("Career Forecast", style = MaterialTheme.typography.labelSmall,
                    color = NexusColors.Neutral400)
            }
        }
        NexusCard(
            onClick = onArena,
            modifier = Modifier.weight(1f),
            gradient = listOf(NexusColors.StellarPink.copy(alpha = 0.12f), NexusColors.PlasmaOrange.copy(alpha = 0.05f)),
            shape = NexusShapes.md,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "\uD83C\uDFC6", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(NexusSpacing.xxs))
                Text("Arena", style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold, color = NexusColors.StellarPink)
                Text("Compete & Grow", style = MaterialTheme.typography.labelSmall,
                    color = NexusColors.Neutral400)
            }
        }
    }
}

@Composable
private fun TodaysChallenge() {
    NexusCard(
        gradient = listOf(NexusColors.Indigo600.copy(alpha = 0.2f), NexusColors.Indigo900.copy(alpha = 0.1f)),
        glassEffect = true,
        shape = NexusShapes.lg,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Today's Challenge",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(NexusSpacing.xs))

            Text(
                "🟡 Medium · Two Sum II · LeetCode",
                style = MaterialTheme.typography.bodyMedium,
                color = NexusColors.Neutral300,
            )

            Spacer(Modifier.height(NexusSpacing.md))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(NexusGradients.Primary),
                    )
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Start Challenge",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
        }
    }
}
