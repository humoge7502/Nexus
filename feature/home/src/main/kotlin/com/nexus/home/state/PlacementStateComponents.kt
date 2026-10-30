package com.nexus.home.state

import androidx.compose.ui.res.stringResource
import com.nexus.home.R

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.NexusColors
import com.nexus.domain.usecase.PlacementStateEngine
import java.time.Instant
import java.time.temporal.ChronoUnit

@Composable
fun PlacementStateBanner(
  state: PlacementStateEngine.PlacementState,
  modifier: Modifier = Modifier,
) {
  val (text, color) = when (state) {
    is PlacementStateEngine.PlacementState.EarlyPreparation ->
      "Building Phase · ${state.daysToSeason} days to season" to NexusColors.Neutral400
    is PlacementStateEngine.PlacementState.ActivePreparation ->
      "Preparation Mode · ${state.daysToSeason} days to season" to NexusColors.NexusBlue
    is PlacementStateEngine.PlacementState.PlacementSeason ->
      "Placement Season · ${state.daysToSeason} days left" to NexusColors.Warning
    is PlacementStateEngine.PlacementState.ActiveInterview ->
      "Interview Mode · ${state.interviewCount} active" to NexusColors.NexusBlue
    is PlacementStateEngine.PlacementState.OfferReceived ->
      "Offer Received! Congratulations" to NexusColors.Success
    is PlacementStateEngine.PlacementState.PostPlacement ->
      "What's next? Your journey continues." to NexusColors.Neutral300
  }

  Row(
    modifier = modifier
      .fillMaxWidth()
      .background(color.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
      .padding(horizontal = 12.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier
        .size(6.dp)
        .background(color, CircleShape),
    )
    Spacer(Modifier.width(8.dp))
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall,
      color = color,
      fontWeight = FontWeight.Medium,
    )
  }
}

@Composable
fun InterviewModeCard(
  companies: List<String>,
  nextInterviewDate: Long?,
  onPrepClick: () -> Unit,
  onViewApplicationsClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val companiesText = companies.take(2).joinToString(", ") +
    if (companies.size > 2) " +${companies.size - 2} more" else ""
  val daysUntil = nextInterviewDate?.let {
    val now = Instant.now()
    val target = Instant.ofEpochMilli(it)
    ChronoUnit.DAYS.between(now, target)
  }

  com.nexus.designsystem.component.NexusCard(
    modifier = modifier.fillMaxWidth(),
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        Icons.Default.Psychology,
        contentDescription = null,
        tint = NexusColors.NexusBlue,
        modifier = Modifier.padding(end = 8.dp),
      )
      Text(stringResource(R.string.home_interview_mode_active),
        style = MaterialTheme.typography.labelMedium,
        color = NexusColors.NexusBlue,
        fontWeight = FontWeight.SemiBold,
      )
    }
    Spacer(Modifier.height(8.dp))
    Text(stringResource(R.string.home_you_have_active_interviews_at, companiesText),
      style = MaterialTheme.typography.titleSmall,
    )
    if (daysUntil != null && daysUntil > 0) {
      Spacer(Modifier.height(4.dp))
      Text(stringResource(R.string.home_next_interview_in_days, daysUntil),
        style = MaterialTheme.typography.bodySmall,
        color = NexusColors.Warning,
      )
    }
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      androidx.compose.material3.Button(
        onClick = onPrepClick,
        modifier = Modifier.weight(1f),
      ) { Text(stringResource(R.string.home_prep_now)) }
      androidx.compose.material3.OutlinedButton(
        onClick = onViewApplicationsClick,
        modifier = Modifier.weight(1f),
      ) { Text(stringResource(R.string.home_applications)) }
    }
  }
}

@Composable
fun PlacementSeasonCard(
  daysLeft: Int,
  onApplicationsClick: () -> Unit,
  onInterviewPrepClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  com.nexus.designsystem.component.NexusCard(
    modifier = modifier.fillMaxWidth(),
  ) {
    Text(stringResource(R.string.home_days_to_placement_season, daysLeft),
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
    )
    Spacer(Modifier.height(4.dp))
    Text(stringResource(R.string.home_focus_on_applications_and_interview_preparation),
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      androidx.compose.material3.Button(
        onClick = onApplicationsClick,
        modifier = Modifier.weight(1f),
      ) {
        Icon(Icons.Default.Work, contentDescription = null)
        Spacer(Modifier.width(4.dp))
        Text(stringResource(R.string.home_applications))
      }
      androidx.compose.material3.OutlinedButton(
        onClick = onInterviewPrepClick,
        modifier = Modifier.weight(1f),
      ) { Text(stringResource(R.string.home_interview_prep)) }
    }
  }
}

@Composable
fun OfferReceivedCard(
  company: String,
  role: String,
  onEvaluateClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  com.nexus.designsystem.component.NexusCard(
    modifier = modifier.fillMaxWidth(),
  ) {
    Text(stringResource(R.string.home_offer_received),
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = NexusColors.Success,
    )
    Spacer(Modifier.height(4.dp))
    Text(stringResource(R.string.home_company_role, company, role), style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(8.dp))
    Text(stringResource(R.string.home_evaluate_your_offer_with_nexus_ai),
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(12.dp))
    androidx.compose.material3.Button(
      onClick = onEvaluateClick,
      modifier = Modifier.fillMaxWidth(),
    ) { Text(stringResource(R.string.home_evaluate_offer)) }
  }
}
