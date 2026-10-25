package com.nexus.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusShapes

data class AITool(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val gradient: List<Color>,
    val route: String,
)

private val aiTools = listOf(
    AITool("\uD83D\uDD0D", "Deep Research", "Cited answers + smart follow-ups", Icons.Default.Search,
        listOf(Color(0xFF6366F1), Color(0xFF06B6D4)), "research"),
    AITool("\uD83E\uDD16", "NEXUS AI Chat", "Your personal career mentor", Icons.Default.Bolt,
        NexusGradients.Nexus, "chat"),
    AITool("\uD83D\uDCC4", "Resume Analyzer", "Get ATS Score + Tips", Icons.Default.Description,
        listOf(Color(0xFF00D9A3), Color(0xFF4F8EF7)), "resume"),
    AITool("\uD83D\uDDFA\uFE0F", "Roadmap Generator", "Personalized learning path", Icons.Default.Map,
        listOf(Color(0xFF7B5EA7), Color(0xFFF72585)), "roadmap"),
    AITool("\uD83D\uDCBC", "LinkedIn Post Gen", "Auto-craft achievements", Icons.Default.PostAdd,
        listOf(Color(0xFF0A66C2), Color(0xFF4F8EF7)), "linkedin"),
    AITool("\uD83C\uDFAF", "Skill Gap Analysis", "What to learn next", Icons.Default.TrackChanges,
        listOf(Color(0xFFFF6B35), Color(0xFFF59E0B)), "skillgap"),
    AITool("\uD83C\uDFA4", "Interview Coach", "AI mock interviews", Icons.Default.RecordVoiceOver,
        listOf(Color(0xFF06B6D4), Color(0xFF6366F1)), "interview"),
    AITool("\uD83D\uDCCA", "Career Insights", "Predictive analytics", Icons.Default.Analytics,
        listOf(Color(0xFF10B981), Color(0xFF059669)), "insights"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIHubScreen(
    onChatClick: () -> Unit = {},
    onResumeAnalyzer: () -> Unit = {},
    onResearchClick: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("AI Hub", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )

        NexusCard(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            gradient = NexusGradients.Nexus,
            shape = NexusShapes.lg,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "\uD83E\uDD16", style = MaterialTheme.typography.headlineLarge)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "NEXUS AI",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    "Your personal career mentor",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                )
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable(onClick = onChatClick)
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                ) {
                    Text(
                        "Start Conversation \u2192",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            }
        }

        Text(
            "AI Tools",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(aiTools) { tool ->
                AIToolCard(
                    tool = tool,
                    onClick = {
                        when (tool.route) {
                            "chat" -> onChatClick()
                            "resume" -> onResumeAnalyzer()
                            "research" -> onResearchClick()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun AIToolCard(tool: AITool, onClick: () -> Unit) {
    NexusCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = NexusShapes.md,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape)
                    .background(Brush.linearGradient(tool.gradient)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = tool.emoji, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                tool.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
            Text(
                tool.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}
