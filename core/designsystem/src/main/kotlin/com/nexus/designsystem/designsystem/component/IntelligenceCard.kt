package com.nexus.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusShapes

enum class IntelligenceCardType {
    ORACLE, SIGNAL, MIRROR, MOMENTUM
}

data class IntelligenceCardData(
    val type: IntelligenceCardType,
    val emoji: String,
    val title: String,
    val description: String,
    val primaryAction: String? = null,
    val secondaryAction: String? = null,
    val priority: Int = 0,
    val gradient: List<Color>? = null,
)

@Composable
fun IntelligenceCard(
    data: IntelligenceCardData,
    onPrimaryAction: () -> Unit = {},
    onSecondaryAction: () -> Unit = {},
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val borderTransition = rememberInfiniteTransition(label = "intelBorder")
    val borderAlpha by borderTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "borderAlpha",
    )

    val (cardGradient, emojiBg) = when (data.type) {
        IntelligenceCardType.ORACLE -> Pair(
            listOf(Color(0xFF6366F1).copy(alpha = 0.15f), Color(0xFF8B5CF6).copy(alpha = 0.05f)),
            NexusGradients.Purple,
        )
        IntelligenceCardType.SIGNAL -> Pair(
            listOf(Color(0xFFEF4444).copy(alpha = 0.12f), Color(0xFFF97316).copy(alpha = 0.05f)),
            NexusGradients.Warning,
        )
        IntelligenceCardType.MIRROR -> Pair(
            listOf(Color(0xFF06B6D4).copy(alpha = 0.12f), Color(0xFF0891B2).copy(alpha = 0.05f)),
            NexusGradients.Ocean,
        )
        IntelligenceCardType.MOMENTUM -> Pair(
            listOf(Color(0xFF10B981).copy(alpha = 0.12f), Color(0xFF34D399).copy(alpha = 0.05f)),
            NexusGradients.Success,
        )
    }

    NexusCard(
        modifier = modifier.fillMaxWidth(),
        shape = NexusShapes.md,
        glassEffect = true,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(cardGradient),
                    shape = NexusShapes.md,
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            data.gradient?.getOrElse(0) { NexusColors.Indigo400 }
                                ?: NexusColors.Indigo400,
                            data.gradient?.getOrElse(1) { NexusColors.Cyan400 }
                                ?: NexusColors.Cyan400,
                        ).map { it.copy(alpha = borderAlpha) }
                    ),
                    shape = NexusShapes.md,
                )
                .padding(16.dp),
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(emojiBg)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = data.emoji, fontSize = MaterialTheme.typography.titleMedium.fontSize)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            data.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        when (data.type) {
                            IntelligenceCardType.ORACLE -> Text("NEXUS Oracle",
                                style = MaterialTheme.typography.labelSmall,
                                color = NexusColors.Indigo400)
                            IntelligenceCardType.SIGNAL -> Text("NEXUS Signal",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFF97316))
                            IntelligenceCardType.MIRROR -> Text("NEXUS Mirror",
                                style = MaterialTheme.typography.labelSmall,
                                color = NexusColors.Cyan400)
                            IntelligenceCardType.MOMENTUM -> Text("NEXUS Momentum",
                                style = MaterialTheme.typography.labelSmall,
                                color = NexusColors.Emerald400)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    data.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                )

                if (data.primaryAction != null || data.secondaryAction != null) {
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        data.primaryAction?.let { action ->
                            Button(
                                onClick = onPrimaryAction,
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when (data.type) {
                                        IntelligenceCardType.ORACLE -> NexusColors.Indigo500
                                        IntelligenceCardType.SIGNAL -> Color(0xFFEF4444)
                                        IntelligenceCardType.MIRROR -> NexusColors.Cyan500
                                        IntelligenceCardType.MOMENTUM -> NexusColors.Emerald500
                                    }
                                ),
                            ) {
                                Text(action, style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1)
                            }
                        }
                        data.secondaryAction?.let { action ->
                            Button(
                                onClick = onSecondaryAction,
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            ) {
                                Text(action, style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IntelligenceCardStack(
    cards: List<IntelligenceCardData>,
    onPrimaryAction: (Int) -> Unit = {},
    onSecondaryAction: (Int) -> Unit = {},
    onDismiss: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        cards.forEachIndexed { index, card ->
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(dampingRatio = 0.6f),
                ) + fadeIn(),
            ) {
                IntelligenceCard(
                    data = card,
                    onPrimaryAction = { onPrimaryAction(index) },
                    onSecondaryAction = { onSecondaryAction(index) },
                    onDismiss = { onDismiss(index) },
                )
            }
        }
    }
}

object IntelligenceCardDefaults {
    fun oracleCard(
        prediction: String,
        actionLabel: String = "Start Now",
        secondaryLabel: String = "Later",
    ) = IntelligenceCardData(
        type = IntelligenceCardType.ORACLE,
        emoji = "\uD83D\uDD2E",
        title = "NEXUS Oracle",
        description = prediction,
        primaryAction = actionLabel,
        secondaryAction = secondaryLabel,
    )

    fun signalCard(
        opportunity: String,
        matchScore: Int,
        actionLabel: String = "Prepare & Apply",
        secondaryLabel: String = "Remind me",
    ) = IntelligenceCardData(
        type = IntelligenceCardType.SIGNAL,
        emoji = "\u26A1",
        title = "Opportunity Alert",
        description = "$opportunity\nMatch: $matchScore%\u00B7 Optimal window: Next 36 hours",
        primaryAction = actionLabel,
        secondaryAction = secondaryLabel,
    )

    fun mirrorCard(
        insight: String,
        actionLabel: String = "Practice Now",
        secondaryLabel: String = "Schedule",
    ) = IntelligenceCardData(
        type = IntelligenceCardType.MIRROR,
        emoji = "\uD83E\uDED0",
        title = "Blind Spot Detected",
        description = insight,
        primaryAction = actionLabel,
        secondaryAction = secondaryLabel,
    )

    fun momentumCard(
        insight: String,
        actionLabel: String = "Go to Roadmap",
    ) = IntelligenceCardData(
        type = IntelligenceCardType.MOMENTUM,
        emoji = "\uD83D\uDD25",
        title = "Momentum Detected",
        description = insight,
        primaryAction = actionLabel,
    )
}
