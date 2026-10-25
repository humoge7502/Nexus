package com.nexus.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.AnimatedStatCard
import com.nexus.designsystem.component.NexusCard
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
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
    viewModel: ProfileViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val displayName = uiState.name.ifBlank { "CSE Student" }
    val avatarInitial = displayName.trim().firstOrNull()?.uppercase() ?: "N"

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Profile", fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
            actions = {
                IconButton(onClick = onGoPro) { Icon(Icons.Default.WorkspacePremium, contentDescription = "NEXUS Pro", tint = Color(0xFFF59E0B)) }
                IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, contentDescription = "Settings") }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(
                brush = Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)))
            ), contentAlignment = Alignment.Center) {
                Text(text = avatarInitial, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = displayName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                text = listOfNotNull(
                    uiState.university.takeIf { it.isNotBlank() },
                    "Year ${uiState.year}",
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                ProfileInfoItem(Icons.Default.School, "Level ${uiState.level}")
                ProfileInfoItem(Icons.Default.Code, "${uiState.totalXP} XP")
                uiState.githubUsername?.let { ProfileInfoItem(Icons.Default.Code, "@$it") }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AnimatedStatCard(value = uiState.applicationsCount, label = "Applied", icon = Icons.Default.Work, gradient = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)), modifier = Modifier.weight(1f))
                AnimatedStatCard(value = uiState.certificationsCount, label = "Certs", icon = Icons.Default.EmojiEvents, gradient = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)), modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AnimatedStatCard(value = uiState.streakDays, label = "Day streak", icon = Icons.Default.School, gradient = listOf(Color(0xFF10B981), Color(0xFF34D399)), modifier = Modifier.weight(1f))
                AnimatedStatCard(value = uiState.offersCount, label = "Offers", icon = Icons.Default.Work, gradient = listOf(Color(0xFF06B6D4), Color(0xFF22D3EE)), modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Skills", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            val ratedSkills = uiState.skills.filter { it.proficiency > 0 }
            if (ratedSkills.size >= 3) {
                SkillRadarChart(
                    skills = ratedSkills.take(6).map { it.name to (it.proficiency / 3f) },
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                )
            } else {
                Text(
                    text = if (uiState.skills.isEmpty()) {
                        "No skills added yet. Complete onboarding or mark skills in Growth to build your radar."
                    } else {
                        "Add at least 3 rated skills to unlock your radar."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (uiState.skills.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.skills.take(8).joinToString(" · ") { it.name },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            NexusCard(modifier = Modifier.fillMaxWidth().clickable { onAchievements() }, glassEffect = true) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Achievements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("View All →", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("🔥", "💪", "🌟").forEach { emoji ->
                            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                Text(text = emoji, style = MaterialTheme.typography.titleLarge)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            NexusCard(modifier = Modifier.fillMaxWidth(), glassEffect = true) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        "NEXUS Toolkit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    ToolkitRow(Icons.Default.Work, "Applications", "Track every application and interview", onApplications)
                    ToolkitRow(Icons.Default.School, "Placement Forecast", "Readiness by company tier", onPlacementForecast)
                    ToolkitRow(Icons.Default.EmojiEvents, "Career Mirror", "Weekly retrospective and narrative", onCareerMirror)
                    ToolkitRow(Icons.Default.Code, "Cohort Intelligence", "See where you stand among peers", onCohort)
                    ToolkitRow(Icons.Default.Work, "Interview Log", "Record outcomes and calibrate confidence", onInterviewLog)
                    ToolkitRow(Icons.Default.WorkspacePremium, "Portfolio", "Export a shareable career portfolio", onPortfolio)
                    ToolkitRow(Icons.Default.Settings, "Search", "Semantic search across opportunities", onSearch)
                    ToolkitRow(Icons.Default.School, "Community", "Peer circles and discussions", onCommunity)
                    ToolkitRow(Icons.Default.EmojiEvents, "Tribe", "Small accountability group", onTribe)
                    ToolkitRow(Icons.Default.Work, "Leaderboard", "Compete on the NEXUS score", onLeaderboard)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

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
            tint = MaterialTheme.colorScheme.primary,
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
        Text("→", color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ProfileInfoItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SkillRadarChart(skills: List<Pair<String, Float>>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.6f
        val angleStep = 360f / skills.size

        for (level in 1..5) {
            val levelRadius = radius * level / 5
            val path = Path()
            skills.forEachIndexed { index, _ ->
                val rad = Math.toRadians((angleStep * index - 90).toDouble())
                val x = center.x + levelRadius * cos(rad).toFloat()
                val y = center.y + levelRadius * sin(rad).toFloat()
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, color = Color.Gray.copy(alpha = 0.15f), style = Stroke(1.dp.toPx()))
        }

        val dataPath = Path()
        skills.forEachIndexed { index, (_, value) ->
            val rad = Math.toRadians((angleStep * index - 90).toDouble())
            val x = center.x + radius * value * cos(rad).toFloat()
            val y = center.y + radius * value * sin(rad).toFloat()
            if (index == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
        }
        dataPath.close()

        drawPath(dataPath, brush = Brush.verticalGradient(listOf(Color(0xFF6366F1).copy(alpha = 0.4f), Color(0xFF8B5CF6).copy(alpha = 0.2f))))
        drawPath(dataPath, color = Color(0xFF6366F1), style = Stroke(2.dp.toPx()))

        skills.forEachIndexed { index, (_, value) ->
            val rad = Math.toRadians((angleStep * index - 90).toDouble())
            val x = center.x + radius * value * cos(rad).toFloat()
            val y = center.y + radius * value * sin(rad).toFloat()
            drawCircle(color = Color(0xFF6366F1), radius = 5.dp.toPx(), center = Offset(x, y))
            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(x, y))
        }
    }
}
