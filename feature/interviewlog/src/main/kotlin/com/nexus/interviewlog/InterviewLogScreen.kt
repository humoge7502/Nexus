package com.nexus.interviewlog

import androidx.compose.ui.res.stringResource
import com.nexus.interviewlog.R

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.designsystem.component.LoadingState
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.domain.model.InterviewOutcome

@Composable
fun InterviewLogScreen(
  onBack: () -> Unit,
  viewModel: InterviewLogViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  InterviewLogContent(uiState = uiState, onBack = onBack)
}

@Composable
fun InterviewLogContent(
  uiState: InterviewLogUiState,
  onBack: () -> Unit,
) {
  Scaffold(
    topBar = {
      NexusTopBar(
        title = stringResource(R.string.interviewlog_interview_log),
        onBack = onBack,
      )
    }
  ) { padding ->
    if (uiState.isLoading) {
      LoadingState(modifier = Modifier.fillMaxSize())
    } else {
      LazyColumn(
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

        items(uiState.outcomes) { outcome ->
          InterviewOutcomeRow(outcome)
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
          com.nexus.designsystem.theme.NexusColors.Emerald500
        } else {
          MaterialTheme.colorScheme.onSurface
        },
      )
    }
  }
}
