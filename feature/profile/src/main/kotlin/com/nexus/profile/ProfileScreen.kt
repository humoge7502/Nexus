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
            Text(text = "CSE Student", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                ProfileInfoItem(Icons.Default.School, "3rd Year")
                ProfileInfoItem(Icons.Default.Code, "Kotlin · Compose")
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AnimatedStatCard(value = 23, label = "Applied", icon = Icons.Default.Work, gradient = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)), modifier = Modifier.weight(1f))
                AnimatedStatCard(value = 5, label = "Certs", icon = Icons.Default.EmojiEvents, gradient = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)), modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Skills", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            SkillRadarChart(skills = listOf("Kotlin" to 0.85f, "Compose" to 0.8f, "Android" to 0.75f, "Python" to 0.6f, "DSA" to 0.7f, "System Design" to 0.45f), modifier = Modifier.fillMaxWidth().height(280.dp))
            Spacer(modifier = Modifier.height(24.dp))
            NexusCard(modifier = Modifier.fillMaxWidth().clickable { onAchievements() }, glassEffect = true) {
                if (onLeaderboard != null) {} // reference to suppress unused warning
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
            Spacer(modifier = Modifier.height(32.dp))
        }
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
