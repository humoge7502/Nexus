package com.nexus.careermirror

import androidx.compose.ui.res.stringResource
import com.nexus.careermirror.R

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.designsystem.component.EmptyState
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.component.feedback.EditorialErrorState
import com.nexus.designsystem.component.feedback.EditorialSkeletonList

@Composable
fun CareerMirrorScreen(
  onBack: () -> Unit,
  viewModel: CareerMirrorViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(Unit) {
    viewModel.generateMirror()
  }

  CareerMirrorContent(
    uiState = uiState,
    onBack = onBack,
    onRetry = viewModel::generateMirror,
  )
}

@Composable
fun CareerMirrorContent(
  uiState: CareerMirrorUiState,
  onBack: () -> Unit,
  onRetry: () -> Unit = {},
) {
  Scaffold(
    topBar = {
      NexusTopBar(
        title = stringResource(R.string.careermirror_career_mirror),
        onBack = onBack,
      )
    }
  ) { padding ->
    if (uiState.isLoading) {
      EditorialSkeletonList(
        count = 3,
        loadingLabel = stringResource(R.string.careermirror_loading),
        modifier = Modifier
          .padding(padding)
          .padding(horizontal = 16.dp, vertical = 20.dp),
      )
      return@Scaffold
    }

    // The fault has to be checked before the missing-data branch, or a failed
    // generation is indistinguishable from a user who simply has no history.
    val error = uiState.error
    if (error != null) {
      EditorialErrorState(
        headline = stringResource(R.string.careermirror_unable_to_generate),
        reason = stringResource(R.string.careermirror_error_reason),
        actionLabel = stringResource(R.string.careermirror_try_again),
        onAction = onRetry,
        secondaryHint = error.takeIf { it.isNotBlank() },
        modifier = Modifier
          .padding(padding)
          .padding(horizontal = 16.dp, vertical = 20.dp),
      )
      return@Scaffold
    }

    val mirror = uiState.mirror
    if (mirror == null) {
      EmptyState(
        title = stringResource(R.string.careermirror_not_enough_data_yet),
        description = stringResource(R.string.careermirror_use_nexus_for_90_days_to),
        voice = stringResource(R.string.careermirror_empty_voice),
        modifier = Modifier
          .padding(padding)
          .fillMaxSize(),
      )
      return@Scaffold
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      MirrorHeadline(
        periodLabel = mirror.periodLabel,
        score = uiState.currentScore,
        scoreDelta = uiState.scoreDelta,
        narrative = mirror.narrative,
      )

      // Key Insights
      MirrorInsightsCard(mirror.keyInsights)

      // Action Items
      if (mirror.actionItems.isNotEmpty()) {
        NexusCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
          Text(
            stringResource(R.string.careermirror_next_quarter_actions),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
          )
          Spacer(modifier = Modifier.height(8.dp))
          mirror.actionItems.forEachIndexed { index, action ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            ) {
              Text(
                text = "${index + 1}.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(end = 8.dp),
              )
              Text(
                text = action,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(80.dp))
    }
  }
}

@Composable
private fun MirrorHeadline(
  periodLabel: String,
  score: Int,
  scoreDelta: Int,
  narrative: String,
) {
  NexusCard(
    modifier = Modifier.fillMaxWidth(),
    containerColor = MaterialTheme.colorScheme.primaryContainer,
  ) {
    Text(
      text = periodLabel,
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.onPrimaryContainer,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = stringResource(
        R.string.careermirror_score_with_delta,
        score,
        if (scoreDelta >= 0) "+$scoreDelta" else scoreDelta.toString(),
      ),
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onPrimaryContainer,
    )
  }

  NexusCard {
    Text(
      text = narrative,
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface,
    )
  }
}

/** Absent rather than empty: no insights means the card does not appear. */
@Composable
private fun MirrorInsightsCard(insights: List<String>) {
  if (insights.isEmpty()) return
  NexusCard {
    Text(
      stringResource(R.string.careermirror_key_insights),
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
    )
    Spacer(modifier = Modifier.height(8.dp))
    insights.forEach { insight ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
      ) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .background(
              color = MaterialTheme.colorScheme.primary,
              shape = MaterialTheme.shapes.small,
            ),
        )
        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
        Text(
          text = insight,
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}
