package com.nexus.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.domain.model.ScorePulseEvent
import kotlinx.coroutines.delay

@Composable
fun ScorePulseOverlay(
  event: ScorePulseEvent,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val haptic = LocalHapticFeedback.current
  var visible by remember { mutableStateOf(false) }

  LaunchedEffect(event) {
    visible = true
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    delay(4000L)
    visible = false
    delay(350L)
    onDismiss()
  }

  AnimatedVisibility(
    visible = visible,
    enter = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(400)) + fadeIn(tween(300)),
    exit = slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(300)) + fadeOut(tween(250)),
    modifier = modifier.fillMaxWidth(),
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      shape = RoundedCornerShape(16.dp),
      tonalElevation = 4.dp,
      color = Color.Transparent,
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            brush = Brush.horizontalGradient(NexusGradients.Nexus),
            shape = RoundedCornerShape(16.dp),
          )
          .padding(16.dp),
      ) {
        Column {
          // Score change row
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "${event.oldScore}",
              style = MaterialTheme.typography.titleMedium,
              color = Color.White.copy(alpha = 0.7f),
              fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
              Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "${event.newScore}",
              style = MaterialTheme.typography.headlineMedium,
              color = Color.White,
              fontWeight = FontWeight.ExtraBold,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              color = Color.White.copy(alpha = 0.25f),
              shape = RoundedCornerShape(8.dp),
            ) {
              Text(
                text = "+${event.delta}",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              )
            }
          }
          // Trigger detail
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = event.triggerDetail,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.9f),
          )
          // Next milestone
          if (event.nextMilestone.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Outlined.TrendingUp,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(14.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = event.nextMilestone,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.75f),
              )
            }
          }
        }
      }
    }
  }
}
