package com.nexus.cohortintelligence

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.designsystem.component.LoadingState
import com.nexus.designsystem.component.NexusCard
import com.nexus.domain.model.CohortEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CohortIntelligenceScreen(
  onBack: () -> Unit,
  viewModel: CohortIntelligenceViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(Unit) {
    viewModel.loadData()
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Cohort Intelligence", fontWeight = FontWeight.SemiBold) },
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        item {
          NexusCard {
            Text("College: ${uiState.collegeId}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            uiState.placementRate?.let { rate ->
              Text("Estimated Placement Rate: ${(rate * 100).toInt()}%", color = MaterialTheme.colorScheme.primary)
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
      Text(text = "Semester: ${entry.semester}", fontWeight = FontWeight.Bold)
      Text(text = "Tier: ${entry.companyTier}")
      Text(text = "Placed students: ${entry.placedCount} / ${entry.totalStudents}")
    }
  }
}
