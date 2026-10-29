package com.nexus.cohortintelligence

import androidx.compose.ui.res.stringResource
import com.nexus.cohortintelligence.R

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.nexus.designsystem.component.LoadingState
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.domain.model.CohortEntry

@Composable
fun CohortIntelligenceScreen(
  onBack: () -> Unit,
  viewModel: CohortIntelligenceViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(Unit) {
    viewModel.loadData()
  }

  CohortIntelligenceContent(uiState = uiState, onBack = onBack)
}

@Composable
fun CohortIntelligenceContent(
  uiState: CohortUiState,
  onBack: () -> Unit,
) {
  Scaffold(
    topBar = {
      NexusTopBar(
        title = stringResource(R.string.cohortintelligence_cohort_intelligence),
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        item {
          NexusCard {
            Text(
                stringResource(R.string.cohortintelligence_college, uiState.collegeId),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            uiState.placementRate?.let { rate ->
              Text(
                stringResource(
                  R.string.cohortintelligence_estimated_placement_rate,
                  (rate * 100).toInt(),
                ),
                color = MaterialTheme.colorScheme.primary,
              )
            }
          }
        }

        items(uiState.entries) { entry ->
          CohortEntryRow(entry)
        }
      }
    }
  }
}

@Composable
private fun CohortEntryRow(entry: CohortEntry) {
  NexusCard {
    Column {
      Text(text = stringResource(R.string.cohortintelligence_semester, entry.semester), fontWeight = FontWeight.Bold)
      Text(text = stringResource(R.string.cohortintelligence_tier, entry.companyTier))
      Text(text = stringResource(R.string.cohortintelligence_placed_students, entry.placedCount, entry.totalStudents))
    }
  }
}
