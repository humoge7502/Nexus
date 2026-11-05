package com.nexus.interviewlog

import androidx.compose.ui.res.stringResource
import com.nexus.interviewlog.R

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.nexus.designsystem.theme.nexus
import com.nexus.domain.model.InterviewOutcome

@Composable
fun InterviewLogScreen(
  onBack: () -> Unit,
  viewModel: InterviewLogViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  InterviewLogContent(
    uiState = uiState,
    onBack = onBack,
    onRetry = viewModel::retry,
  )
}

@Composable
fun InterviewLogContent(
  uiState: InterviewLogUiState,
  onBack: () -> Unit,
  onRetry: () -> Unit = {},
) {
  Scaffold(
    topBar = {
      NexusTopBar(
        title = stringResource(R.string.interviewlog_interview_log),
        onBack = onBack,
      )
    }
  ) { padding ->
    // Read before the empty branch: a failed read carries no outcomes, so
    // `outcomes.isEmpty()` alone would render an outage as "nothing logged yet".
    val loadFault = uiState.error

    when {
      uiState.isLoading -> EditorialSkeletonList(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding)
          .padding(16.dp),
        count = 3,
        loadingLabel = stringResource(R.string.interviewlog_loading_log),
      )

      // The statistics plate counts rounds out of the same read that failed, so
      // this branch carries the fault alone rather than asserting a count the
      // input can no longer justify.
      loadFault != null -> Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding)
          .padding(16.dp),
      ) {
        EditorialErrorState(
          headline = stringResource(R.string.interviewlog_unable_to_load),
          reason = stringResource(R.string.interviewlog_error_reason),
          actionLabel = stringResource(R.string.interviewlog_try_again),
          onAction = onRetry,
          secondaryHint = loadFault.takeIf { it.isNotBlank() },
        )
      }

      else -> LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding)
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        item {
          NexusCard {
            Text(
                stringResource(R.string.interviewlog_statistics),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            uiState.topCompanies.forEach { (company, count) ->
              Text(stringResource(R.string.interviewlog_company_count_interviews, company, count))
            }
          }
        }

        if (uiState.outcomes.isEmpty()) {
          item {
            EmptyState(
              title = stringResource(R.string.interviewlog_empty_title),
              description = stringResource(R.string.interviewlog_empty_body),
              voice = stringResource(R.string.interviewlog_empty_voice),
              modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
            )
          }
        } else {
          items(uiState.outcomes) { outcome ->
            InterviewOutcomeRow(outcome)
          }
        }
      }
    }
  }
}

@Composable
private fun InterviewOutcomeRow(outcome: InterviewOutcome) {
  NexusCard {
    Column {
      Text(text = outcome.company, fontWeight = FontWeight.Bold)
      Text(text = stringResource(R.string.interviewlog_role, outcome.role))
      Text(text = stringResource(R.string.interviewlog_round, outcome.interviewRound))
      Text(
        text = stringResource(R.string.interviewlog_result, outcome.result),
        color = if (outcome.result == "SELECTED") {
          MaterialTheme.nexus.positive
        } else {
          MaterialTheme.colorScheme.onSurface
        },
      )
    }
  }
}
