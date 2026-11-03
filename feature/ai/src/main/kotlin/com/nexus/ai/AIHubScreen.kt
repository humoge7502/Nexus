package com.nexus.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.NexusButton
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusIconButton
import com.nexus.designsystem.component.SectionHeader
import com.nexus.designsystem.component.SignalField
import com.nexus.designsystem.component.hairline
import com.nexus.designsystem.component.instrumentGrid
import com.nexus.designsystem.component.sheen
import com.nexus.designsystem.effect.RevealOnLoad
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus

data class AITool(
    val title: String,
    val subtitle: String,
    val route: String,
)

private val aiTools = listOf(
    AITool("Deep Research", "Cited answers + smart follow-ups", "research"),
    AITool("Sagitta AI Chat", "Your personal career mentor", "chat"),
    AITool("Resume Analyzer", "Get ATS score + tips", "resume"),
    AITool("Roadmap Generator", "Personalized learning path", "roadmap"),
    AITool("LinkedIn Post Gen", "Auto-craft achievements", "linkedin"),
    AITool("Skill Gap Analysis", "What to learn next", "skillgap"),
    AITool("Interview Coach", "AI mock interviews", "interview"),
    AITool("Career Insights", "Predictive analytics", "insights"),
)

@Composable
fun AIHubScreen(
    onToolRoute: (String) -> Unit = {},
    onBack: () -> Unit = {},
    showBackArrow: Boolean = true,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        SignalField(modifier = Modifier.fillMaxSize())

        // The roster below is a static editorial list rather than a lazy grid,
        // so this column scrolls itself to keep every row reachable on small
        // screens the way the grid's internal scroll used to.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showBackArrow) {
                    NexusIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        onClick = onBack,
                        modifier = Modifier.padding(end = 4.dp).size(32.dp),
                    )
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
                    onChatClick = { onToolRoute("chat") },
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

            // The roster reads as one instrument plate instead of a card grid:
            // hairline rules between full-width rows, each led by its mono
            // index, because §7 bans the identical icon-title-description
            // cards this section used to be and §4 names hairline tables as
            // the replacement.
            NexusCard(modifier = Modifier.fillMaxWidth()) {
                aiTools.forEachIndexed { index, tool ->
                    AIToolRow(
                        tool = tool,
                        index = index + 1,
                        onClick = { onToolRoute(tool.route) },
                    )
                    if (index != aiTools.lastIndex) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.nexus.hairline,
                        )
                    }
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
        // No floating ornament sits on the plate: the measurement grid, the
        // hairline, and the sheen are its whole surface treatment, and §7
        // bans decorative shapes that measure nothing.
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
            // The promise is this screen's one narrative sentence, so it
            // speaks in the Newsreader voice rather than the instrument's
            // sans (§3) — the weight tuning came with the sans style and the
            // serif defines its own.
            Text(
                text = stringResource(R.string.ai_your_personal_career_mentor),
                style = NexusTypography.EditorialHeadline,
                color = NexusColors.Bone,
            )
            Spacer(Modifier.height(18.dp))
            // The hero is this screen's one signature surface, so its CTA earns
            // the PrimaryDeep ramp the rest of the app reserves for primary
            // actions; ink content on the lime terminal stays readable.
            NexusButton(
                text = stringResource(R.string.ai_start_conversation_u2192),
                onClick = onChatClick,
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                gradient = NexusGradients.PrimaryDeep,
                shape = NexusShapes.sm,
                expand = false,
            )
        }
    }
}

@Composable
private fun AIToolRow(
    tool: AITool,
    index: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            // 14dp of vertical air keeps the row past the 48dp touch floor.
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = index.toString().padStart(2, '0'),
            style = NexusTypography.DataLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tool.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = tool.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        // The row's text carries the meaning, so the chevron stays quiet in
        // onSurfaceVariant and hides from screen readers.
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
    }
}
