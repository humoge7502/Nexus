package com.nexus.profile

import com.nexus.profile.R

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.designsystem.component.AnimatedStatCard
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.SectionHeader
import com.nexus.designsystem.component.SignalField
import com.nexus.designsystem.component.hairline
import com.nexus.designsystem.component.instrumentGrid
import com.nexus.designsystem.component.sheen
import com.nexus.designsystem.effect.RevealOnLoad
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.domain.model.Achievement
import com.nexus.domain.model.AchievementType
import com.nexus.domain.model.Skill
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onAchievements: () -> Unit = {},
    onLeaderboard: () -> Unit = {},
    onGoPro: () -> Unit = {},
    onApplications: () -> Unit = {},
    onPlacementForecast: () -> Unit = {},
    onCareerMirror: () -> Unit = {},
    onCohort: () -> Unit = {},
    onInterviewLog: () -> Unit = {},
    onPortfolio: () -> Unit = {},
    onSearch: () -> Unit = {},
    onCommunity: () -> Unit = {},
    onTribe: () -> Unit = {},
    showBackArrow: Boolean = true,
    viewModel: ProfileViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val displayName = uiState.name.ifBlank { stringResource(R.string.profile_default_name) }
    val avatarInitial = displayName.trim().firstOrNull()?.uppercase() ?: "N"
    val ratedSkills = uiState.skills.filter { it.proficiency > 0 }
    val toolkit = listOf(
        ToolkitEntry(
            Icons.Default.Work,
            stringResource(R.string.profile_tool_applications),
            stringResource(R.string.profile_tool_applications_desc),
            onApplications,
        ),
        ToolkitEntry(
            Icons.Default.School,
            stringResource(R.string.profile_tool_placement_forecast),
            stringResource(R.string.profile_tool_placement_forecast_desc),
            onPlacementForecast,
        ),
        ToolkitEntry(
            Icons.Default.EmojiEvents,
            stringResource(R.string.profile_tool_career_mirror),
            stringResource(R.string.profile_tool_career_mirror_desc),
            onCareerMirror,
        ),
        ToolkitEntry(
            Icons.Default.Code,
            stringResource(R.string.profile_tool_cohort),
            stringResource(R.string.profile_tool_cohort_desc),
            onCohort,
        ),
        ToolkitEntry(
            Icons.Default.Work,
            stringResource(R.string.profile_tool_interview_log),
            stringResource(R.string.profile_tool_interview_log_desc),
            onInterviewLog,
        ),
        ToolkitEntry(
            Icons.Default.WorkspacePremium,
            stringResource(R.string.profile_tool_portfolio),
            stringResource(R.string.profile_tool_portfolio_desc),
            onPortfolio,
        ),
        ToolkitEntry(
            Icons.Default.Settings,
            stringResource(R.string.profile_tool_search),
            stringResource(R.string.profile_tool_search_desc),
            onSearch,
        ),
        ToolkitEntry(
            Icons.Default.School,
            stringResource(R.string.profile_tool_community),
            stringResource(R.string.profile_tool_community_desc),
            onCommunity,
        ),
        ToolkitEntry(
            Icons.Default.EmojiEvents,
            stringResource(R.string.profile_tool_tribe),
            stringResource(R.string.profile_tool_tribe_desc),
            onTribe,
        ),
        ToolkitEntry(
            Icons.Default.Work,
            stringResource(R.string.profile_tool_leaderboard),
            stringResource(R.string.profile_tool_leaderboard_desc),
            onLeaderboard,
        ),
    )

    Box(modifier = Modifier.fillMaxSize()) {
        SignalField(modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize()) {
            ProfileHeader(
                showBackArrow = showBackArrow,
                onBack = onBack,
                onGoPro = onGoPro,
                onSettings = onSettings,
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                RevealOnLoad {
                    ProfileIdentity(
                        displayName = displayName,
                        avatarInitial = avatarInitial,
                        uiState = uiState,
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                ProfileStats(uiState = uiState)

                ProfileSkillsSection(
                    ratedSkills = ratedSkills,
                    allSkills = uiState.skills,
                )

                ProfileAchievementsSection(
                    achievements = uiState.achievements,
                    onAchievements = onAchievements,
                )

                ProfileToolkitSection(toolkit = toolkit)
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    showBackArrow: Boolean,
    onBack: () -> Unit,
    onGoPro: () -> Unit,
    onSettings: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 12.dp, top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showBackArrow) {
            IconButton(onClick = onBack, modifier = Modifier.padding(end = 4.dp).size(32.dp)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Text(
            text = stringResource(R.string.profile_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onGoPro) {
            Icon(
                Icons.Default.WorkspacePremium,
                contentDescription = stringResource(R.string.cd_nexus_pro),
                tint = NexusColors.Amber400,
                modifier = Modifier.size(22.dp),
            )
        }
        IconButton(onClick = onSettings) {
            Icon(
                Icons.Default.Settings,
                contentDescription = stringResource(R.string.cd_settings),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun ProfileStats(uiState: ProfileUiState) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AnimatedStatCard(
            value = uiState.applicationsCount,
            label = stringResource(R.string.profile_stat_applied),
            icon = Icons.Default.Work,
            modifier = Modifier.weight(1f),
        )
        AnimatedStatCard(
            value = uiState.certificationsCount,
            label = stringResource(R.string.profile_stat_certs),
            icon = Icons.Default.EmojiEvents,
            modifier = Modifier.weight(1f),
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AnimatedStatCard(
            value = uiState.streakDays,
            label = stringResource(R.string.profile_stat_streak),
            icon = Icons.Default.LocalFireDepartment,
            modifier = Modifier.weight(1f),
        )
        AnimatedStatCard(
            value = uiState.offersCount,
            label = stringResource(R.string.profile_stat_offers),
            icon = Icons.Default.Work,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ProfileSkillsSection(
    ratedSkills: List<Skill>,
    allSkills: List<Skill>,
) {
    Spacer(modifier = Modifier.height(28.dp))
    SectionHeader(
        title = stringResource(R.string.profile_skills),
        index = "01",
        meta = ratedSkills.size.toString().padStart(2, '0'),
    )
    Spacer(modifier = Modifier.height(14.dp))
    if (ratedSkills.size >= 3) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .hairline(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                    cornerRadiusDp = 24f,
                )
                .padding(horizontal = 8.dp, vertical = 10.dp),
        ) {
            SkillRadarChart(
                skills = ratedSkills.take(6).map { it.name to (it.proficiency / 3f) },
                modifier = Modifier.fillMaxWidth().height(280.dp),
            )
        }
    } else {
        Text(
            text = if (allSkills.isEmpty()) {
                stringResource(R.string.profile_skills_empty)
            } else {
                stringResource(R.string.profile_skills_need_three)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
        if (allSkills.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = allSkills.take(8).joinToString(" · ") { it.name },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ProfileAchievementsSection(
    achievements: List<Achievement>,
    onAchievements: () -> Unit,
) {
    Spacer(modifier = Modifier.height(28.dp))
    SectionHeader(
        title = stringResource(R.string.profile_achievements),
        index = "02",
        actionText = stringResource(R.string.profile_view_all),
        onAction = onAchievements,
    )
    Spacer(modifier = Modifier.height(14.dp))
    if (achievements.isEmpty()) {
        NexusCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onAchievements),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.profile_achievements_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    } else {
        NexusCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onAchievements),
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                achievements.forEachIndexed { index, achievement ->
                    AchievementPreviewRow(achievement = achievement)
                    if (index != achievements.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 46.dp, end = 16.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileToolkitSection(toolkit: List<ToolkitEntry>) {
    Spacer(modifier = Modifier.height(28.dp))
    SectionHeader(
        title = stringResource(R.string.profile_nexus_toolkit),
        index = "03",
        meta = toolkit.size.toString().padStart(2, '0'),
    )
    Spacer(modifier = Modifier.height(14.dp))
    NexusCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            toolkit.forEachIndexed { index, entry ->
                ToolkitRow(
                    icon = entry.icon,
                    title = entry.title,
                    subtitle = entry.subtitle,
                    onClick = entry.onClick,
                )
                if (index != toolkit.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 48.dp, end = 16.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileIdentity(
    displayName: String,
    avatarInitial: String,
    uiState: ProfileUiState,
) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(brush = Brush.linearGradient(NexusGradients.PrimaryDeep), shape = shape)
            .instrumentGrid(spacingDp = 30f, alpha = 0.045f)
            .hairline(color = Color.White.copy(alpha = 0.16f), cornerRadiusDp = 28f)
            .sheen(alpha = 0.06f)
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.14f))
                        .hairline(color = Color.White.copy(alpha = 0.24f), cornerRadiusDp = 33f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = avatarInitial,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (uiState.university.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = uiState.university,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.72f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(thickness = 1.dp, color = Color.White.copy(alpha = 0.14f))
            Spacer(modifier = Modifier.height(14.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IdentitySpec(Icons.Default.School, stringResource(R.string.profile_year, uiState.year))
                IdentitySpec(Icons.Default.Star, stringResource(R.string.profile_level, uiState.level))
                IdentitySpec(Icons.Default.Code, stringResource(R.string.profile_xp, uiState.totalXP))
                uiState.githubUsername?.let { IdentitySpec(Icons.Default.AccountCircle, "@$it") }
            }
        }
    }
}

@Composable
private fun IdentitySpec(icon: ImageVector, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.62f),
            modifier = Modifier.size(13.dp),
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = value,
            style = NexusTypography.TelemetryMicro,
            color = Color.White.copy(alpha = 0.92f),
            maxLines = 1,
        )
    }
}

private data class ToolkitEntry(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit,
)

@Composable
private fun ToolkitRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun AchievementPreviewRow(
    achievement: Achievement,
    modifier: Modifier = Modifier,
) {
    val unlocked = achievement.isUnlocked
    val accent = if (unlocked) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
    }
    val progress = if (achievement.maxProgress > 0f) {
        (achievement.progress / achievement.maxProgress).coerceIn(0f, 1f)
    } else {
        0f
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = achievementIcon(achievement.type),
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (unlocked) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (unlocked) 1f else progress)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        if (unlocked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.profile_achievement_unlocked),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
        } else {
            Text(
                text = "${(progress * 100).toInt()}%",
                style = NexusTypography.TelemetryMicro,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

private fun achievementIcon(type: AchievementType): ImageVector = when (type) {
    AchievementType.FIRST_INTERNSHIP -> Icons.Default.Work
    AchievementType.FIRST_CERTIFICATION -> Icons.Default.WorkspacePremium
    AchievementType.STREAK_7 -> Icons.Default.LocalFireDepartment
    AchievementType.STREAK_30 -> Icons.Default.RocketLaunch
    AchievementType.TEN_APPLICATIONS -> Icons.Default.Send
    AchievementType.FIVE_CERTS -> Icons.Default.MilitaryTech
    AchievementType.MASTER_CODER -> Icons.Default.EmojiEvents
    AchievementType.NETWORKER -> Icons.Default.Handshake
    AchievementType.EARLY_BIRD -> Icons.Default.WbSunny
    AchievementType.NIGHT_OWL -> Icons.Default.DarkMode
}

@Composable
fun SkillRadarChart(skills: List<Pair<String, Float>>, modifier: Modifier = Modifier) {
    val description = stringResource(
        R.string.profile_skills_radar_description,
        skills.map { (name, value) ->
            stringResource(R.string.profile_skill_percent, name, (value * 100).roundToInt())
        }.joinToString(", "),
    )
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val signalColor = MaterialTheme.colorScheme.primary
    val labelStyle = NexusTypography.TelemetryMicro.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    val dotCore = MaterialTheme.colorScheme.surfaceContainerLow
    val measurer = rememberTextMeasurer()

    Canvas(modifier = modifier.semantics { contentDescription = description }) {
        if (skills.size < 3) return@Canvas
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.56f
        val angleStep = 360f / skills.size
        val labelGap = 16.dp.toPx()
        val maxLabelWidth = (size.width * 0.34f).toInt().coerceAtLeast(1)

        for (level in 1..4) {
            val levelRadius = radius * level / 4
            val path = Path()
            skills.forEachIndexed { index, _ ->
                val rad = Math.toRadians((angleStep * index - 90).toDouble())
                val x = center.x + levelRadius * cos(rad).toFloat()
                val y = center.y + levelRadius * sin(rad).toFloat()
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(
                path = path,
                color = gridColor.copy(alpha = if (level == 4) 0.8f else 0.4f),
                style = Stroke(1.dp.toPx()),
            )
        }

        skills.forEachIndexed { index, _ ->
            val rad = Math.toRadians((angleStep * index - 90).toDouble())
            drawLine(
                color = gridColor.copy(alpha = 0.28f),
                start = center,
                end = Offset(
                    center.x + radius * cos(rad).toFloat(),
                    center.y + radius * sin(rad).toFloat(),
                ),
                strokeWidth = 1.dp.toPx(),
            )
        }

        val dataPath = Path()
        skills.forEachIndexed { index, (_, value) ->
            val rad = Math.toRadians((angleStep * index - 90).toDouble())
            val x = center.x + radius * value * cos(rad).toFloat()
            val y = center.y + radius * value * sin(rad).toFloat()
            if (index == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
        }
        dataPath.close()

        drawPath(
            path = dataPath,
            brush = Brush.verticalGradient(
                listOf(
                    signalColor.copy(alpha = 0.28f),
                    signalColor.copy(alpha = 0.03f),
                ),
            ),
        )
        drawPath(dataPath, color = signalColor, style = Stroke(2.dp.toPx()))

        skills.forEachIndexed { index, (_, value) ->
            val rad = Math.toRadians((angleStep * index - 90).toDouble())
            val x = center.x + radius * value * cos(rad).toFloat()
            val y = center.y + radius * value * sin(rad).toFloat()
            drawCircle(color = signalColor, radius = 4.5.dp.toPx(), center = Offset(x, y))
            drawCircle(color = dotCore, radius = 1.8.dp.toPx(), center = Offset(x, y))
        }

        skills.forEachIndexed { index, (name, _) ->
            val rad = Math.toRadians((angleStep * index - 90).toDouble())
            val anchorX = center.x + (radius + labelGap) * cos(rad).toFloat()
            val anchorY = center.y + (radius + labelGap) * sin(rad).toFloat()
            val layout = measurer.measure(
                text = AnnotatedString(name),
                style = labelStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                constraints = Constraints(maxWidth = maxLabelWidth),
            )
            val x = (anchorX - layout.size.width / 2f)
                .coerceIn(0f, (size.width - layout.size.width).coerceAtLeast(0f))
            val y = (anchorY - layout.size.height / 2f)
                .coerceIn(0f, (size.height - layout.size.height).coerceAtLeast(0f))
            drawText(layout, topLeft = Offset(x, y))
        }
    }
}
