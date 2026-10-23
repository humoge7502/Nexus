package com.nexus.applications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.domain.repository.ApplicationCounts
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.domain.model.Application
import com.nexus.domain.model.ApplicationStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationsScreen(
  viewModel: ApplicationsViewModel = hiltViewModel(),
  onNavigateBack: () -> Unit,
  onApplicationClick: (String) -> Unit = {},
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("My Applications") },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate back")
          }
        },
      )
    }
  ) { padding ->
    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
      StatsSummary(counts = uiState.counts)

      TabRow(selectedTabIndex = uiState.selectedTab.ordinal) {
        ApplicationTab.entries.forEach { tab ->
          Tab(
            selected = uiState.selectedTab == tab,
            onClick = { viewModel.selectTab(tab) },
            text = {
              Text(
                when (tab) {
                  ApplicationTab.ALL -> "All"
                  ApplicationTab.APPLIED -> "Applied"
                  ApplicationTab.INTERVIEWING -> "Interviews"
                  ApplicationTab.OUTCOMES -> "Outcomes"
                }
              )
            },
          )
        }
      }

      if (uiState.applications.isEmpty()) {
        EmptyApplicationsState(selectedTab = uiState.selectedTab)
      } else {
        LazyColumn(
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          items(
            items = uiState.applications,
            key = { it.id },
            contentType = { "application" },
          ) { application ->
            ApplicationCard(
              application = application,
              onStatusChange = { newStatus ->
                viewModel.updateStatus(application.id, newStatus)
              },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun StatsSummary(counts: ApplicationCounts) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceEvenly,
  ) {
    StatChip("Applied", counts.applied, Icons.Filled.HourglassEmpty, NexusColors.Blue400)
    StatChip("Interviewing", counts.interviewing, Icons.Filled.Mic, NexusColors.Warning)
    StatChip("Offered", counts.offered, Icons.Filled.CheckCircle, NexusColors.Success)
  }
}

@Composable
private fun StatChip(label: String, count: Int, icon: ImageVector, color: Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = color,
      modifier = Modifier.size(24.dp),
    )
    Spacer(Modifier.height(4.dp))
    Text(
      text = count.toString(),
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = color,
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun ApplicationCard(
  application: Application,
  onStatusChange: (ApplicationStatus) -> Unit,
  modifier: Modifier = Modifier,
) {
  NexusCard(modifier = modifier.fillMaxWidth()) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = application.company,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
          )
          Spacer(Modifier.height(2.dp))
          Text(
            text = application.role,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        StatusChip(application.status)
      }

      Spacer(Modifier.height(12.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "${(application.matchScore * 100).toInt()}% match",
          style = MaterialTheme.typography.labelMedium,
          color = NexusColors.Neutral400,
        )
        Spacer(Modifier.width(8.dp))
        LinearProgressIndicator(
          progress = { application.matchScore },
          modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)),
          color = when {
            application.matchScore > 0.8f -> NexusColors.Success
            application.matchScore > 0.5f -> NexusColors.Warning
            else -> NexusColors.Neutral400
          },
          trackColor = MaterialTheme.colorScheme.surfaceVariant,
          strokeCap = StrokeCap.Round,
        )
      }

      AnimatedVisibility(visible = application.status.ordinal < ApplicationStatus.ACCEPTED.ordinal) {
        Spacer(Modifier.height(12.dp))
        StatusActions(
          currentStatus = application.status,
          onStatusChange = onStatusChange,
        )
      }
    }
  }
}

@Composable
private fun StatusChip(status: ApplicationStatus) {
  val (label, color) = when (status) {
    ApplicationStatus.SAVED -> "Saved" to NexusColors.Neutral400
    ApplicationStatus.APPLIED -> "Applied" to NexusColors.Blue400
    ApplicationStatus.INTERVIEWING -> "Interviewing" to NexusColors.Warning
    ApplicationStatus.REJECTED -> "Rejected" to NexusColors.Error
    ApplicationStatus.OFFERED -> "Offered" to NexusColors.Success
    ApplicationStatus.ACCEPTED -> "Accepted" to NexusColors.Success
    else -> status.name to NexusColors.Neutral400
  }
  Text(
    text = label,
    style = MaterialTheme.typography.labelMedium,
    fontWeight = FontWeight.SemiBold,
    color = color,
  )
}

@Composable
private fun StatusActions(
  currentStatus: ApplicationStatus,
  onStatusChange: (ApplicationStatus) -> Unit,
) {
  val nextStatuses = when (currentStatus) {
    ApplicationStatus.SAVED -> listOf(ApplicationStatus.APPLIED)
    ApplicationStatus.APPLIED -> listOf(ApplicationStatus.INTERVIEWING, ApplicationStatus.REJECTED)
    ApplicationStatus.INTERVIEWING -> listOf(ApplicationStatus.OFFERED, ApplicationStatus.REJECTED)
    ApplicationStatus.OFFERED -> listOf(ApplicationStatus.ACCEPTED, ApplicationStatus.REJECTED)
    else -> emptyList()
  }

  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    nextStatuses.forEach { status ->
      val isPositive = status in listOf(
        ApplicationStatus.APPLIED,
        ApplicationStatus.INTERVIEWING,
        ApplicationStatus.OFFERED,
        ApplicationStatus.ACCEPTED,
      )
      OutlinedButton(
        onClick = { onStatusChange(status) },
        modifier = Modifier.height(36.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
      ) {
        Text(
          text = when (status) {
            ApplicationStatus.APPLIED -> "Mark Applied"
            ApplicationStatus.INTERVIEWING -> "Got Interview"
            ApplicationStatus.OFFERED -> "Got Offer!"
            ApplicationStatus.ACCEPTED -> "Accept"
            ApplicationStatus.REJECTED -> "Rejected"
            else -> status.name
          },
          style = MaterialTheme.typography.labelMedium,
          color = if (isPositive) NexusColors.Success else NexusColors.Error,
        )
      }
    }
  }
}

@Composable
private fun EmptyApplicationsState(selectedTab: ApplicationTab) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      imageVector = Icons.Filled.BusinessCenter,
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = NexusColors.Neutral400,
    )
    Spacer(Modifier.height(16.dp))
    Text(
      text = when (selectedTab) {
        ApplicationTab.ALL -> "No applications tracked yet"
        ApplicationTab.APPLIED -> "No applications submitted"
        ApplicationTab.INTERVIEWING -> "No interviews scheduled"
        ApplicationTab.OUTCOMES -> "No outcomes yet"
      },
      style = MaterialTheme.typography.titleMedium,
      color = NexusColors.Neutral400,
    )
    Spacer(Modifier.height(8.dp))
    Text(
      text = "Save an opportunity and track your progress here",
      style = MaterialTheme.typography.bodyMedium,
      color = NexusColors.Neutral400,
      textAlign = TextAlign.Center,
    )
  }
}
