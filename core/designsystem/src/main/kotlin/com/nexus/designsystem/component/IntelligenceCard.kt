package com.nexus.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.R
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.onAccent
import com.nexus.designsystem.theme.nexus

enum class IntelligenceCardType {
    ORACLE, SIGNAL, MIRROR, MOMENTUM
}

data class IntelligenceCardData(
    val type: IntelligenceCardType,
    val title: String,
    val description: String,
    val primaryAction: String? = null,
    val secondaryAction: String? = null,
    val priority: Int = 0,
)

/**
 * Each intelligence type speaks in a different signal so a feed of these cards
 * can be scanned by colour before it is read. The accent lives on the icon and
 * the tag; the card surface itself stays neutral, which keeps four stacked
 * cards from turning into a carnival.
 */
private data class TypeVoice(
    val accent: Color,
    val icon: ImageVector,
    val labelRes: Int,
    val tone: SignalTone,
    val iconTint: Color,
)

@Composable
private fun voiceFor(type: IntelligenceCardType): TypeVoice {
    val ext = MaterialTheme.nexus
    return when (type) {
        IntelligenceCardType.ORACLE -> TypeVoice(
            accent = ext.signal,
            icon = Icons.Default.AutoAwesome,
            labelRes = R.string.designsystem_nexus_oracle,
            tone = SignalTone.Signal,
            iconTint = ext.signal,
        )
        IntelligenceCardType.SIGNAL -> TypeVoice(
            accent = ext.attention,
            icon = Icons.Default.Bolt,
            labelRes = R.string.designsystem_nexus_signal,
            tone = SignalTone.Attention,
            iconTint = ext.attention,
        )
        IntelligenceCardType.MIRROR -> TypeVoice(
            accent = ext.structure,
            icon = Icons.Default.Visibility,
            labelRes = R.string.designsystem_nexus_mirror,
            tone = SignalTone.Structure,
            iconTint = ext.structure,
        )
        IntelligenceCardType.MOMENTUM -> TypeVoice(
            accent = ext.positive,
            icon = Icons.Default.TrendingUp,
            labelRes = R.string.designsystem_nexus_momentum,
            tone = SignalTone.Neutral,
            iconTint = ext.positive,
        )
    }
}

@Composable
fun IntelligenceCard(
    data: IntelligenceCardData,
    modifier: Modifier = Modifier,
    onPrimaryAction: () -> Unit = {},
    onSecondaryAction: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val voice = voiceFor(data.type)

    NexusCard(
        modifier = modifier.fillMaxWidth(),
        shape = NexusShapes.md,
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = voice.icon,
                    contentDescription = null,
                    tint = voice.iconTint,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(NexusSpacing.sm))
                Column(verticalArrangement = Arrangement.spacedBy(NexusSpacing.xxs)) {
                    Text(
                        data.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    SignalTag(
                        text = stringResource(voice.labelRes),
                        tone = voice.tone,
                        iconTint = voice.iconTint,
                    )
                }
            }

            Spacer(Modifier.height(NexusSpacing.sm))
            Text(
                data.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (data.primaryAction != null || data.secondaryAction != null) {
                Spacer(Modifier.height(NexusSpacing.md))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(NexusSpacing.xs),
                ) {
                    data.primaryAction?.let { action ->
                        Button(
                            onClick = onPrimaryAction,
                            modifier = Modifier.weight(1f).height(NexusSpacing.minTouchTarget),
                            shape = NexusShapes.sm,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = voice.accent,
                                contentColor = onAccent(voice.accent),
                            ),
                        ) {
                            Text(
                                action,
                                style = NexusTypography.LabelMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    data.secondaryAction?.let { action ->
                        Button(
                            onClick = onSecondaryAction,
                            modifier = Modifier.weight(1f).height(NexusSpacing.minTouchTarget),
                            shape = NexusShapes.sm,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        ) {
                            Text(
                                action,
                                style = NexusTypography.LabelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
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
        title = "Ascent Oracle",
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
        title = "Momentum Detected",
        description = insight,
        primaryAction = actionLabel,
    )
}
