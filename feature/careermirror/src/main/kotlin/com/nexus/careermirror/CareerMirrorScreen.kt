package com.nexus.careermirror

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.theme.NexusGradients

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerMirrorScreen(
  onBack: () -> Unit,
  viewModel: CareerMirrorViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(Unit) {
    viewModel.generateMirror()
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Career Mirror", fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
          }
        },
      )
    }
  ) { padding ->
    if (uiState.isLoading) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Generating your Career Mirror...", style = MaterialTheme.typography.bodyLarge)
      }
      return@Scaffold
    }

    val mirror = uiState.mirror
    if (mirror == null) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text("Not enough data yet", style = MaterialTheme.typography.headlineSmall)
          Spacer(modifier = Modifier.height(8.dp))
          Text("Use NEXUS for 90 days to generate your first mirror")
        }
      }
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
      // Period card
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text(
            text = mirror.periodLabel,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Score: ${uiState.currentScore} (${if (uiState.scoreDelta >= 0) "+" else ""}${uiState.scoreDelta})",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
          )
        }
      }

      // Narrative card
      NexusCard {
        Text(
          text = mirror.narrative,
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }

      // Key Insights
      if (mirror.keyInsights.isNotEmpty()) {
        NexusCard {
          Text("Key Insights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))
          mirror.keyInsights.forEach { insight ->
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
                    brush = Brush.linearGradient(com.nexus.designsystem.theme.NexusGradients.Nexus),
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

      // Action Items
      if (mirror.actionItems.isNotEmpty()) {
        NexusCard(gradient = NexusGradients.Success) {
          Text("Next Quarter Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                modifier = Modifier.padding(end = 8.dp),
              )
              Text(
                text = action,
                style = MaterialTheme.typography.bodyMedium,
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(80.dp))
    }
  }
}
