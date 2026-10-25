package com.nexus.tribe

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.domain.model.CohortEntry
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TribeScreen(
  onBack: () -> Unit,
  viewModel: TribeViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var showSetupForm by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) { viewModel.loadTribe() }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("My Tribe", fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
          }
        },
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
            viewModel.createOrJoinTribe(college, goal, size)
            showSetupForm = false
          },
          onDismiss = { showSetupForm = false },
        )
        !uiState.isInTribe -> TribeJoinCard(onJoin = { showSetupForm = true })
        else -> TribeHome(uiState = uiState, onLeave = viewModel::leaveTribe)
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
      Text("Join your Campus Tribe", style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(4.dp))
      Text("Accountability pods of 5–7 students from your college with shared placement goals.",
        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Spacer(modifier = Modifier.height(16.dp))
      Button(onClick = onJoin) { Text("Create or Join Tribe") }
    }
  }
}

@Composable
private fun TribeHome(uiState: TribeUiState, onLeave: () -> Unit) {
  NexusCard {
    Text("Your Tribe", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))
    Text(uiState.college, style = MaterialTheme.typography.bodyLarge)
    if (uiState.goal.isNotBlank()) {
      Text("Goal: ${uiState.goal}", style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Text("Pod size: ${uiState.podSize}", style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant)
  }

  val cohort = uiState.cohort
  if (cohort != null) {
    NexusCard(gradient = NexusGradients.Success) {
      Text("Campus Cohort", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
        Text("Top skills: ${cohort.topSkills.take(6).joinToString(", ")}",
          style = MaterialTheme.typography.bodySmall)
      }
    }
  } else {
    NexusCard {
      Text("Campus Cohort", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        "Peer benchmarks for ${uiState.college} appear once enough students from your college " +
          "are on NEXUS. We never invent member counts.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }

  Button(onClick = onLeave, modifier = Modifier.fillMaxWidth()) {
    Text("Leave Tribe")
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
    Text("Set Up Your Tribe", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(value = college, onValueChange = { college = it },
      label = { Text("College name") }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(value = goal, onValueChange = { goal = it },
      label = { Text("Shared placement goal") }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(value = podSize, onValueChange = { podSize = it.filter(Char::isDigit) },
      label = { Text("Pod size (3–7)") }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Button(onClick = { onSetup(college, goal, (podSize.toIntOrNull() ?: 5).coerceIn(3, 7)) },
        modifier = Modifier.weight(1f), enabled = college.isNotBlank()) {
        Text("Create Tribe")
      }
      androidx.compose.material3.OutlinedButton(onClick = onDismiss) { Text("Cancel") }
    }
  }
}
