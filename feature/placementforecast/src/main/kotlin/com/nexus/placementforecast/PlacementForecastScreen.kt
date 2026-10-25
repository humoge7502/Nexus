package com.nexus.placementforecast

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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import com.nexus.domain.model.PlacementForecast
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacementForecastScreen(
  onBack: () -> Unit,
  viewModel: PlacementForecastViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Placement Forecast", fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
          }
        },
        actions = {
          IconButton(onClick = viewModel::refresh, enabled = !uiState.isLoading) {
            Icon(Icons.Default.Refresh, contentDescription = "Recalculate forecast")
          }
        },
      )
    }
  ) { padding ->
    val error = uiState.error
    when {
      uiState.isLoading -> LoadingState(modifier = Modifier.fillMaxSize())
      error != null -> Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding)
          .padding(24.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = error,
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.error,
        )
      }
      uiState.forecasts.isEmpty() -> Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding)
          .padding(24.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "Not enough activity yet. Solve problems, log applications, and refresh to build a forecast.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      else -> LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding)
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        if (uiState.currentScore > 0) {
          item {
            Text(
              text = "Current NEXUS Score: ${uiState.currentScore}",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }
        uiState.modelReadinessPct?.let { readiness ->
          item {
            ReadinessCard(
              readinessPct = readiness,
              factors = uiState.modelFactors,
              modelVersion = uiState.modelVersion,
              whatIfMocks = uiState.whatIfMocks,
              whatIfPct = uiState.whatIfPct,
              onWhatIfChange = viewModel::updateWhatIfMocks,
            )
          }
        }
        items(uiState.forecasts, key = { it.id }) { forecast ->
          ForecastRow(forecast)
        }
      }
    }
  }
}

@Composable
private fun ReadinessCard(
  readinessPct: Int,
  factors: List<String>,
  modelVersion: String,
  whatIfMocks: Int,
  whatIfPct: Int?,
  onWhatIfChange: (Int) -> Unit,
) {
  NexusCard {
    Column {
      Text(text = "Placement readiness", fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "$readinessPct% predicted probability of placement this season",
        style = MaterialTheme.typography.bodyMedium,
      )
      Spacer(modifier = Modifier.height(8.dp))
      LinearProgressIndicator(
        progress = { readinessPct / 100f },
        modifier = Modifier.fillMaxWidth(),
      )
      if (factors.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Top drivers:", style = MaterialTheme.typography.labelSmall)
        factors.forEach { factor ->
          Text(text = "• $factor", style = MaterialTheme.typography.bodySmall)
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = "What if I add $whatIfMocks mock interviews?",
        style = MaterialTheme.typography.labelMedium,
      )
      Slider(
        value = whatIfMocks.toFloat(),
        onValueChange = { onWhatIfChange(it.roundToInt()) },
        valueRange = 0f..20f,
        steps = 19,
      )
      if (whatIfPct != null) {
        val delta = whatIfPct - readinessPct
        Text(
          text = if (delta == 0) {
            "Readiness stays at $whatIfPct%"
          } else {
            "Readiness would move to $whatIfPct% (${if (delta > 0) "+" else ""}$delta pts)"
          },
          style = MaterialTheme.typography.bodySmall,
          color = if (delta > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "On-device model v$modelVersion · explainable, no data leaves your device",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun ForecastRow(forecast: PlacementForecast) {
  NexusCard {
    Column {
      Text(text = "Tier: ${forecast.companyTier}", fontWeight = FontWeight.Bold)
      Text(text = "Probability: ${(forecast.probabilityScore * 100).toInt()}%")
      Spacer(modifier = Modifier.height(8.dp))
      Text(text = "Factors:", style = MaterialTheme.typography.labelSmall)
      forecast.determiningFactors.forEach { factor ->
        Text(text = "• $factor", style = MaterialTheme.typography.bodySmall)
      }
    }
  }
}
