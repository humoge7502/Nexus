package com.nexus.tribe

import androidx.compose.ui.res.stringResource
import com.nexus.tribe.R

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlin.math.roundToInt

@Composable
fun TribeScreen(
  onBack: () -> Unit,
  viewModel: TribeViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(Unit) { viewModel.loadTribe() }

  TribeContent(
    uiState = uiState,
    onBack = onBack,
    onSetup = viewModel::createOrJoinTribe,
    onLeave = viewModel::leaveTribe,
  )
}

@Composable
fun TribeContent(
  uiState: TribeUiState,
  onBack: () -> Unit,
  onSetup: (college: String, goal: String, podSize: Int) -> Unit,
  onLeave: () -> Unit,
) {
  var showSetupForm by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      NexusTopBar(
        title = stringResource(R.string.tribe_my_tribe),
        onBack = onBack,
      )
    }
  ) { padding ->
    if (uiState.isLoading) {
      LoadingState(modifier = Modifier.fillMaxSize().padding(padding))
      return@Scaffold
    }
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      uiState.error?.let { message ->
        Text(
          text = message,
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodyMedium,
        )
      }

      when {
        showSetupForm -> TribeSetupForm(
          onSetup = { college, goal, size ->
            onSetup(college, goal, size)
            showSetupForm = false
          },
          onDismiss = { showSetupForm = false },
        )
        !uiState.isInTribe -> TribeJoinCard(onJoin = { showSetupForm = true })
        else -> TribeHome(uiState = uiState, onLeave = onLeave)
      }
      Spacer(modifier = Modifier.height(80.dp))
    }
  }
}

@Composable
private fun TribeJoinCard(onJoin: () -> Unit) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
      Icon(Icons.Default.Group, null, modifier = Modifier.padding(8.dp),
        tint = MaterialTheme.colorScheme.primary)
      Spacer(modifier = Modifier.height(8.dp))
      Text(stringResource(R.string.tribe_join_your_campus_tribe), style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(4.dp))
      Text(stringResource(R.string.tribe_accountability_pods_of_57_students_from),
        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Spacer(modifier = Modifier.height(16.dp))
      Button(onClick = onJoin) { Text(stringResource(R.string.tribe_create_or_join_tribe)) }
    }
  }
}

@Composable
private fun TribeHome(uiState: TribeUiState, onLeave: () -> Unit) {
  NexusCard {
    Text(stringResource(R.string.tribe_your_tribe), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))
    Text(uiState.college, style = MaterialTheme.typography.bodyLarge)
    if (uiState.goal.isNotBlank()) {
      Text(stringResource(R.string.tribe_goal, uiState.goal), style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Text(stringResource(R.string.tribe_pod_size, uiState.podSize), style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant)
  }

  val cohort = uiState.cohort
  if (cohort != null) {
    NexusCard {
      Text(stringResource(R.string.tribe_campus_cohort), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(8.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        StatItem("Students", cohort.totalStudents.toString())
        StatItem("Placed", cohort.placedCount.toString())
        StatItem("Placement", "${placementPercent(cohort)}%")
      }
      Spacer(modifier = Modifier.height(8.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        StatItem("Avg score", cohort.avgScore.roundToInt().toString())
        StatItem("Avg streak", "${cohort.avgStreak.roundToInt()}d")
      }
      if (cohort.topSkills.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.tribe_top_skills, cohort.topSkills.take(6).joinToString(", ")),
          style = MaterialTheme.typography.bodySmall)
      }
    }
  } else {
    NexusCard {
      Text(stringResource(R.string.tribe_campus_cohort), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(8.dp))
      Text(stringResource(R.string.tribe_peer_benchmarks_for_appear_once_enough, uiState.college),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }

  Button(onClick = onLeave, modifier = Modifier.fillMaxWidth()) {
    Text(stringResource(R.string.tribe_leave_tribe))
  }
}

private fun placementPercent(cohort: CohortEntry): Int =
  if (cohort.totalStudents <= 0) 0
  else (cohort.placedCount * 100f / cohort.totalStudents).roundToInt().coerceIn(0, 100)

@Composable
private fun StatItem(label: String, value: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary)
    Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

@Composable
private fun TribeSetupForm(
  onSetup: (college: String, goal: String, size: Int) -> Unit,
  onDismiss: () -> Unit,
) {
  var college by remember { mutableStateOf("") }
  var goal by remember { mutableStateOf("Product company — 6 months") }
  var podSize by remember { mutableStateOf("5") }

  NexusCard {
    Text(stringResource(R.string.tribe_set_up_your_tribe), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(value = college, onValueChange = { college = it },
      label = { Text(stringResource(R.string.tribe_college_name)) }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(value = goal, onValueChange = { goal = it },
      label = { Text(stringResource(R.string.tribe_shared_placement_goal)) }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(value = podSize, onValueChange = { podSize = it.filter(Char::isDigit) },
      label = { Text(stringResource(R.string.tribe_pod_size_37)) }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Button(onClick = { onSetup(college, goal, (podSize.toIntOrNull() ?: 5).coerceIn(3, 7)) },
        modifier = Modifier.weight(1f), enabled = college.isNotBlank()) {
        Text(stringResource(R.string.tribe_create_tribe))
      }
      androidx.compose.material3.OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.tribe_cancel)) }
    }
  }
}
