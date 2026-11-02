package com.nexus.placementforecast

import androidx.compose.ui.res.stringResource
import com.nexus.placementforecast.R

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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import com.nexus.designsystem.component.MomentumChart
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.component.intelligence.ReadinessFactor
import com.nexus.designsystem.component.intelligence.ReadinessFactorCard
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus
import com.nexus.domain.model.PlacementForecast
import com.nexus.domain.usecase.PlacementReadinessModel
import kotlin.math.abs
import kotlin.math.roundToInt

/** Coupled to the model's feature labels: college tier is an input the student cannot
 *  change, so it must never be lit up as something to work on today. */
private const val FIXED_FACTOR_LABEL = "College tier"

@Composable
fun PlacementForecastScreen(
  onBack: () -> Unit,
  viewModel: PlacementForecastViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  PlacementForecastContent(
    uiState = uiState,
    onBack = onBack,
    onRefresh = viewModel::refresh,
    onWhatIfChange = viewModel::updateWhatIfMocks,
  )
}

@Composable
fun PlacementForecastContent(
  uiState: ForecastUiState,
  onBack: () -> Unit,
  onRefresh: () -> Unit,
  onWhatIfChange: (Int) -> Unit,
) {
  Scaffold(
    topBar = {
      NexusTopBar(
        title = stringResource(R.string.placementforecast_placement_forecast),
        onBack = onBack,
        actions = {
          IconButton(onClick = onRefresh, enabled = !uiState.isLoading) {
            Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.cd_recalculate_forecast))
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
        Text(text = stringResource(R.string.text_not_enough_activity_yet_solve_problems_log),
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
              text = stringResource(R.string.placementforecast_current_nexus_score, uiState.currentScore),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }
        if (uiState.scoreTrend.size >= 2) {
          item(key = "score_trend") {
            NexusCard(modifier = Modifier.fillMaxWidth()) {
              Text(
                text = stringResource(R.string.placementforecast_score_trend_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
              )
              MomentumChart(values = uiState.scoreTrend, modifier = Modifier.fillMaxWidth())
              Spacer(Modifier.height(4.dp))
              Text(
                text = stringResource(R.string.placementforecast_score_trend_caption),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
        uiState.modelReadinessPct?.let { readiness ->
          item {
            ReadinessCard(
              readinessPct = readiness,
              factors = uiState.modelTopFactors,
              modelVersion = uiState.modelVersion,
              whatIfMocks = uiState.whatIfMocks,
              whatIfPct = uiState.whatIfPct,
              onWhatIfChange = onWhatIfChange,
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
  factors: List<PlacementReadinessModel.Factor>,
  modelVersion: String,
  whatIfMocks: Int,
  whatIfPct: Int?,
  onWhatIfChange: (Int) -> Unit,
) {
  NexusCard {
    Column {
      Text(text = stringResource(R.string.text_placement_readiness), fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = stringResource(R.string.placementforecast_predicted_probability, readinessPct),
        style = MaterialTheme.typography.bodyMedium,
      )
      Spacer(modifier = Modifier.height(8.dp))
      LinearProgressIndicator(
        progress = { readinessPct / 100f },
        modifier = Modifier.fillMaxWidth(),
      )
      if (factors.isNotEmpty()) {
        Spacer(modifier = Modifier.height(NexusSpacing.lg))
        Text(
          text = stringResource(R.string.text_top_drivers),
          style = NexusTypography.DataLabel,
          color = MaterialTheme.nexus.signal,
        )
        Spacer(modifier = Modifier.height(NexusSpacing.xs))
        // The bar is a share of the measured push, and a lone driver therefore shows 100%.
        // Said out loud, so nobody reads it as a chance of getting placed.
        Text(
          text = stringResource(R.string.placementforecast_factor_share),
          style = NexusTypography.BodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        // Each bar is that driver's share of the total push the three add up to, and the
        // copy states the absolute effect, so the two can be checked against each other.
        val totalPush = factors.sumOf { abs(it.deltaPoints) }.coerceAtLeast(1)
        factors.forEachIndexed { index, factor ->
          Spacer(modifier = Modifier.height(NexusSpacing.md))
          ReadinessFactorCard(
            factor = ReadinessFactor(
              label = factor.label,
              fraction = abs(factor.deltaPoints).toFloat() / totalPush,
              detail = factorDetail(factor.deltaPoints, factor.label == FIXED_FACTOR_LABEL),
              isImprovableNow = factor.label != FIXED_FACTOR_LABEL,
            ),
            rank = index + 1,
          )
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = stringResource(R.string.placementforecast_what_if_mock_interviews, whatIfMocks),
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
            stringResource(R.string.placementforecast_readiness_unchanged, whatIfPct)
          } else {
            stringResource(
              R.string.placementforecast_readiness_would_move,
              whatIfPct,
              if (delta > 0) "+$delta" else delta.toString(),
            )
          },
          style = MaterialTheme.typography.bodySmall,
          color = if (delta > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = stringResource(R.string.placementforecast_on_device_model_note, modelVersion),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

/**
 * States the effect in points rather than adjectives. A bar alone reads as
 * importance; the number says how much, and the fixed-input line says when a
 * driver is not a to-do.
 */
@Composable
private fun factorDetail(deltaPoints: Int, isFixedInput: Boolean): String = when {
  isFixedInput -> stringResource(R.string.placementforecast_factor_fixed)
  deltaPoints > 0 -> stringResource(R.string.placementforecast_factor_raises, deltaPoints)
  deltaPoints < 0 -> stringResource(R.string.placementforecast_factor_lowers, -deltaPoints)
  else -> stringResource(R.string.placementforecast_factor_none)
}

@Composable
private fun ForecastRow(forecast: PlacementForecast) {
  NexusCard {
    Column {
      Text(text = stringResource(R.string.placementforecast_tier, forecast.companyTier), fontWeight = FontWeight.Bold)
      Text(text = stringResource(R.string.placementforecast_probability, (forecast.probabilityScore * 100).toInt()))
      Spacer(modifier = Modifier.height(8.dp))
      Text(text = stringResource(R.string.text_factors), style = MaterialTheme.typography.labelSmall)
      forecast.determiningFactors.forEach { factor ->
        Text(text = "• $factor", style = MaterialTheme.typography.bodySmall)
      }
    }
  }
}
