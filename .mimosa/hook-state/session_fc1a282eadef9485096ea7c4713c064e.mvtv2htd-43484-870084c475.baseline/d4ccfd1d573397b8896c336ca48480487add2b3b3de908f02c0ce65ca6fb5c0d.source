package com.nexus.applications

import androidx.compose.ui.res.stringResource
import com.nexus.applications.R

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.TelemetryItem
import com.nexus.designsystem.component.TelemetryStrip
import com.nexus.designsystem.component.hairline
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.domain.model.Application
import com.nexus.domain.model.ApplicationStatus

@Composable
fun ApplicationsScreen(
  viewModel: ApplicationsViewModel = hiltViewModel(),
  onNavigateBack: () -> Unit,
  onApplicationClick: (String) -> Unit = {},
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  Scaffold(
    topBar = {
      NexusTopBar(
        title = stringResource(R.string.applications_my_applications),
        onBack = onNavigateBack,
      )
    }
  ) { padding ->
    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
      TelemetryStrip(
        items = listOf(
          TelemetryItem(stringResource(R.string.applications_stat_applied), uiState.counts.applied),
          TelemetryItem(
            stringResource(R.string.applications_stat_interviewing),
            uiState.counts.interviewing,
            accent = NexusColors.Amber400,
          ),
          TelemetryItem(
            stringResource(R.string.applications_stat_offered),
            uiState.counts.offered,
            accent = NexusColors.Emerald400,
          ),
        ),
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp),
      )

      ApplicationTabs(
        selected = uiState.selectedTab,
        onSelect = viewModel::selectTab,
      )

      if (uiState.applications.isEmpty()) {
        EmptyApplicationsState(selectedTab = uiState.selectedTab)
      } else {
        LazyColumn(
          contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 24.dp),
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
private fun ApplicationTabs(
  selected: ApplicationTab,
  onSelect: (ApplicationTab) -> Unit,
) {
  val haptic = LocalHapticFeedback.current
  val shape = RoundedCornerShape(16.dp)
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 14.dp)
      .clip(shape)
      .background(MaterialTheme.colorScheme.surfaceContainerLow)
      .hairline(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
        cornerRadiusDp = 16f,
      )
      .padding(4.dp),
    horizontalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    ApplicationTab.entries.forEach { tab ->
      val isSelected = tab == selected
      Box(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(11.dp))
          .background(
            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            else Color.Transparent
          )
          .clickable(role = Role.Tab) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onSelect(tab)
          }
          .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = stringResource(
            when (tab) {
              ApplicationTab.ALL -> R.string.applications_tab_all
              ApplicationTab.APPLIED -> R.string.applications_tab_applied
              ApplicationTab.INTERVIEWING -> R.string.applications_tab_interviews
              ApplicationTab.OUTCOMES -> R.string.applications_tab_outcomes
            }
          ),
          style = MaterialTheme.typography.labelMedium,
          fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
          color = if (isSelected) MaterialTheme.colorScheme.primary
          else MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
        )
      }
    }
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
          text = stringResource(
            R.string.applications_match_percent,
            (application.matchScore * 100).toInt(),
          ),
          style = MaterialTheme.typography.labelMedium,
          color = NexusColors.Neutral400,
        )
        Spacer(Modifier.width(8.dp))
        LinearProgressIndicator(
          progress = { application.matchScore },
          modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)),
          color = when {
            application.matchScore > 0.8f -> NexusColors.Emerald400
            application.matchScore > 0.5f -> NexusColors.Amber400
            else -> NexusColors.Neutral400
          },
          trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
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
  val label = when (status) {
    ApplicationStatus.SAVED -> stringResource(R.string.applications_status_saved)
    ApplicationStatus.APPLIED -> stringResource(R.string.applications_status_applied)
    ApplicationStatus.INTERVIEWING -> stringResource(R.string.applications_status_interviewing)
    ApplicationStatus.REJECTED -> stringResource(R.string.applications_status_rejected)
    ApplicationStatus.OFFERED -> stringResource(R.string.applications_status_offered)
    ApplicationStatus.ACCEPTED -> stringResource(R.string.applications_status_accepted)
    else -> status.name
  }
  val color = when (status) {
    ApplicationStatus.SAVED -> NexusColors.Neutral400
    ApplicationStatus.APPLIED -> NexusColors.Blue400
    ApplicationStatus.INTERVIEWING -> NexusColors.Amber400
    ApplicationStatus.REJECTED -> NexusColors.Rose400
    ApplicationStatus.OFFERED, ApplicationStatus.ACCEPTED -> NexusColors.Emerald400
    else -> NexusColors.Neutral400
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
            ApplicationStatus.APPLIED -> stringResource(R.string.applications_action_mark_applied)
            ApplicationStatus.INTERVIEWING -> stringResource(R.string.applications_action_got_interview)
            ApplicationStatus.OFFERED -> stringResource(R.string.applications_action_got_offer)
            ApplicationStatus.ACCEPTED -> stringResource(R.string.applications_action_accept)
            ApplicationStatus.REJECTED -> stringResource(R.string.applications_action_reject)
            else -> status.name
          },
          style = MaterialTheme.typography.labelMedium,
          color = if (isPositive) NexusColors.Emerald400 else NexusColors.Rose400,
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
      modifier = Modifier.size(40.dp),
      tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
    )
    Spacer(Modifier.height(14.dp))
    Text(
      text = when (selectedTab) {
        ApplicationTab.ALL -> stringResource(R.string.applications_empty_all)
        ApplicationTab.APPLIED -> stringResource(R.string.applications_empty_applied)
        ApplicationTab.INTERVIEWING -> stringResource(R.string.applications_empty_interviewing)
        ApplicationTab.OUTCOMES -> stringResource(R.string.applications_empty_outcomes)
      },
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(6.dp))
    Text(text = stringResource(R.string.text_save_an_opportunity_and_track_your_progress_here),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
    )
  }
}
