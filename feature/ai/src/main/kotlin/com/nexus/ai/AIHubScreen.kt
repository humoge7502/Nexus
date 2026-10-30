package com.nexus.ai

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.SectionHeader
import com.nexus.designsystem.component.SignalField
import com.nexus.designsystem.component.hairline
import com.nexus.designsystem.component.instrumentGrid
import com.nexus.designsystem.component.sheen
import com.nexus.designsystem.effect.RevealOnLoad
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusTypography

data class AITool(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String,
)

private val aiTools = listOf(
    AITool("Deep Research", "Cited answers + smart follow-ups", Icons.Default.Search, "research"),
    AITool("Ascent AI Chat", "Your personal career mentor", Icons.Default.Bolt, "chat"),
    AITool("Resume Analyzer", "Get ATS score + tips", Icons.Default.Description, "resume"),
    AITool("Roadmap Generator", "Personalized learning path", Icons.Default.Map, "roadmap"),
    AITool("LinkedIn Post Gen", "Auto-craft achievements", Icons.Default.PostAdd, "linkedin"),
    AITool("Skill Gap Analysis", "What to learn next", Icons.Default.TrackChanges, "skillgap"),
    AITool("Interview Coach", "AI mock interviews", Icons.Default.RecordVoiceOver, "interview"),
    AITool("Career Insights", "Predictive analytics", Icons.Default.Analytics, "insights"),
)

@Composable
fun AIHubScreen(
    onChatClick: () -> Unit = {},
    onResumeAnalyzer: () -> Unit = {},
    onResearchClick: () -> Unit = {},
    onBack: () -> Unit = {},
    showBackArrow: Boolean = true,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        SignalField(modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 12.dp),
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
                    text = stringResource(R.string.ai_hub_title),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            RevealOnLoad(delayMillis = 40) {
                AssistantHero(
                    onChatClick = onChatClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                )
            }

            SectionHeader(
                title = stringResource(R.string.ai_ai_tools),
                index = "01",
                meta = aiTools.size.toString().padStart(2, '0'),
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 12.dp),
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(aiTools) { index, tool ->
                    AIToolCard(
                        tool = tool,
                        index = index + 1,
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
}

@Composable
private fun AssistantHero(
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(NexusShapes.lg)
            .background(
                brush = Brush.linearGradient(
                    listOf(NexusColors.Carbon, NexusColors.VoidBlack),
                ),
                shape = NexusShapes.lg,
            )
            .instrumentGrid(spacingDp = 28f, alpha = 0.05f, color = NexusColors.Bone)
            .hairline(
                color = NexusColors.Bone.copy(alpha = 0.14f),
                cornerRadiusDp = 18f,
            )
            .sheen(alpha = 0.06f)
            .padding(20.dp)
    ) {
        Canvas(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(120.dp)
        ) {
            val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
            listOf(52f, 38f, 24f).forEachIndexed { index, radiusDp ->
                val inset = radiusDp.dp.toPx()
                drawArc(
                    color = NexusColors.Signal.copy(alpha = 0.5f - index * 0.12f),
                    startAngle = 180f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(size.width - inset, 0f),
                    size = Size(inset * 2, inset * 2),
                    style = stroke,
                )
            }
            drawCircle(
                color = NexusColors.Signal,
                radius = 2.5.dp.toPx(),
                center = Offset(size.width - 18.dp.toPx(), 18.dp.toPx()),
            )
        }

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NexusColors.Bone.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = NexusColors.Signal,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.ai_nexus_ai).uppercase(java.util.Locale.ROOT),
                    style = NexusTypography.DataLabel,
                    color = NexusColors.Bone.copy(alpha = 0.62f),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.ai_your_personal_career_mentor),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = NexusColors.Bone,
            )
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onChatClick,
                shape = NexusShapes.sm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NexusColors.Signal,
                    contentColor = NexusColors.SignalInk,
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
            ) {
                Text(
                    text = stringResource(R.string.ai_start_conversation_u2192),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun AIToolCard(tool: AITool, index: Int, onClick: () -> Unit) {
    NexusCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = NexusShapes.md,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = index.toString().padStart(2, '0'),
                style = NexusTypography.TelemetryMicro,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = tool.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = tool.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
        )
    }
}
