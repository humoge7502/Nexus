package com.nexus.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.R
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.onAccent

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

private data class TypeVoice(
    val accent: Color,
    val icon: ImageVector,
    val labelRes: Int,
)

private fun voiceFor(type: IntelligenceCardType): TypeVoice = when (type) {
    IntelligenceCardType.ORACLE -> TypeVoice(
        NexusColors.NexusBlue,
        Icons.Default.AutoAwesome,
        R.string.designsystem_nexus_oracle,
    )
    IntelligenceCardType.SIGNAL -> TypeVoice(
        NexusColors.Amber500,
        Icons.Default.Bolt,
        R.string.designsystem_nexus_signal,
    )
    IntelligenceCardType.MIRROR -> TypeVoice(
        NexusColors.Cyan400,
        Icons.Default.Visibility,
        R.string.designsystem_nexus_mirror,
    )
    IntelligenceCardType.MOMENTUM -> TypeVoice(
        NexusColors.AuroraGreen,
        Icons.Default.TrendingUp,
        R.string.designsystem_nexus_momentum,
    )
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
    val accent = voice.accent

    NexusCard(
        modifier = modifier.fillMaxWidth(),
        shape = NexusShapes.md,
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = voice.icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        data.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        stringResource(voice.labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                    )
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
                            modifier = Modifier.weight(1f).height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accent,
                                contentColor = onAccent(accent),
                            ),
                        ) {
                            Text(
                                action,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                            )
                        }
                    }
                    data.secondaryAction?.let { action ->
                        Button(
                            onClick = onSecondaryAction,
                            modifier = Modifier.weight(1f).height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        ) {
                            Text(
                                action,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
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
