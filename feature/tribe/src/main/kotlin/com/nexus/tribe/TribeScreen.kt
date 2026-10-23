package com.nexus.tribe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.nexus.analytics.NexusAnalytics
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.theme.NexusGradients

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
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      if (!uiState.isInTribe && !showSetupForm) {
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
            Button(onClick = { showSetupForm = true }) { Text("Create or Join Tribe") }
          }
        }
      } else if (uiState.isInTribe) {
        NexusCard {
          Text("Tribe Stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StatItem("Members", uiState.memberCount.toString())
            StatItem("Avg Streak", "${uiState.avgStreak}d")
            StatItem("Active Today", uiState.activeToday.toString())
          }
        }
        NexusCard(gradient = NexusGradients.Success) {
          Text("Recent Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))
          uiState.recentActivity.forEach { activity ->
            Text("• $activity", modifier = Modifier.padding(vertical = 2.dp),
              style = MaterialTheme.typography.bodySmall)
          }
        }
        Button(onClick = { viewModel.leaveTribe() }, modifier = Modifier.fillMaxWidth()) {
          Text("Leave Tribe")
        }
      } else if (showSetupForm) {
        TribeSetupForm(
          onSetup = { college, goal, size ->
            viewModel.createOrJoinTribe(college, goal)
            showSetupForm = false
          },
          onDismiss = { showSetupForm = false },
        )
      }
      Spacer(modifier = Modifier.height(80.dp))
    }
  }
}

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
    OutlinedTextField(value = podSize, onValueChange = { podSize = it },
      label = { Text("Pod size (5–7)") }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Button(onClick = { onSetup(college, goal, podSize.toIntOrNull() ?: 5) },
        modifier = Modifier.weight(1f), enabled = college.isNotBlank()) {
        Text("Create Tribe")
      }
      androidx.compose.material3.OutlinedButton(onClick = onDismiss) { Text("Cancel") }
    }
  }
}
