package com.nexus.home

import androidx.compose.ui.res.stringResource
import com.nexus.home.R

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.theme.NexusColors
import com.nexus.domain.model.CareerHitCategory
import com.nexus.domain.model.DailyCareerHit

private fun CareerHitCategory.icon(): ImageVector = when (this) {
    CareerHitCategory.CAREER_TIP -> Icons.Default.RocketLaunch
    CareerHitCategory.DSA_TIP -> Icons.Default.Code
    CareerHitCategory.INTERVIEW_TIP -> Icons.Default.RecordVoiceOver
    CareerHitCategory.PRODUCTIVITY_TIP -> Icons.Default.Bolt
    CareerHitCategory.SKILL_TIP -> Icons.Default.School
    CareerHitCategory.NETWORKING_TIP -> Icons.Default.Handshake
    CareerHitCategory.RESUME_TIP -> Icons.Default.Description
}

@Composable
fun CareerHitCard(
    hit: DailyCareerHit,
    xpAwarded: Boolean,
    onView: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val onCard = MaterialTheme.colorScheme.onPrimaryContainer

    NexusCard(
        onClick = onView,
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = hit.category.icon(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hit.category.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = onCard,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.text_daily_career_hit),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = onCard,
                )
            }
            if (xpAwarded) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "+${hit.xpReward} XP",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NexusColors.Emerald600,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = hit.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = onCard,
        )

        AnimatedVisibility(
            visible = hit.isViewed,
            enter = fadeIn() + scaleIn(),
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = hit.tip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = onCard.copy(alpha = 0.76f),
                )
            }
        }

        if (!hit.isViewed) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.home_tap_to_reveal_xp, hit.xpReward),
                style = MaterialTheme.typography.labelSmall,
                color = onCard,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
