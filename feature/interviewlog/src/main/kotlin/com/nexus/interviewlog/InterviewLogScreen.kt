package com.nexus.interviewlog

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.nexus.domain.model.InterviewOutcome

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewLogScreen(
  onBack: () -> Unit,
  viewModel: InterviewLogViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Interview Log", fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
          }
        },
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
            Text("Statistics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            uiState.topCompanies.forEach { (company, count) ->
              Text("$company: $count interviews")
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
      Text(text = "Role: ${outcome.role}")
      Text(text = "Round: ${outcome.interviewRound}")
      Text(text = "Result: ${outcome.result}", color = if (outcome.result == "SELECTED") com.nexus.designsystem.theme.NexusColors.Emerald500 else MaterialTheme.colorScheme.onSurface)
    }
  }
}
