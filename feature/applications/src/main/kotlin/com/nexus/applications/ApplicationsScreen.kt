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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.designsystem.component.CalibratedRule
import com.nexus.designsystem.component.EditorialTimeline
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusOutlineButton
import com.nexus.designsystem.component.SectionHeader
import com.nexus.designsystem.component.SignalTag
import com.nexus.designsystem.component.SignalTone
import com.nexus.designsystem.component.TelemetryItem
import com.nexus.designsystem.component.TelemetryStrip
import com.nexus.designsystem.component.TimelineEntry
import com.nexus.designsystem.component.hairline
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus
import com.nexus.domain.model.Application
import com.nexus.domain.model.ApplicationStatus
import java.util.Locale

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
            accent = MaterialTheme.nexus.attention,
          ),
          TelemetryItem(
            stringResource(R.string.applications_stat_offered),
            uiState.counts.offered,
            accent = MaterialTheme.nexus.positive,
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
            Box(modifier = Modifier.animateItem()) {
              ApplicationCard(
                application = application,
                onStatusChange = { newStatus ->
                  viewModel.updateStatus(application.id, newStatus)
                },
                onRemove = { viewModel.remove(application.id) },
              )
            }
          }

          // The pipeline section reads the same list as the cards above, but
          // as history: one dated event per node, newest first, so the screen
          // answers both "what do I act on" and "how did I get here". It sits
          // at the end of the scroll with a full section gap above it, and the
          // empty branch of the tab above it means it only ever renders with
          // at least one application present.
          item(key = "pipeline", contentType = "timeline") {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = NexusSpacing.sm),
            ) {
              SectionHeader(
                title = stringResource(R.string.applications_timeline_section_title),
                index = "01",
                modifier = Modifier.padding(bottom = NexusSpacing.sm),
              )
              EditorialTimeline(
                entries = applicationTimelineEntries(uiState.applications),
                emptyTitle = stringResource(R.string.applications_timeline_empty_title),
                emptyDetail = stringResource(R.string.applications_timeline_empty_detail),
                emptyVoice = stringResource(R.string.applications_timeline_empty_voice),
              )
            }
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
  val ext = MaterialTheme.nexus
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 14.dp)
      .clip(NexusShapes.sm)
      .background(MaterialTheme.colorScheme.surfaceContainerLow)
      .hairline(
        color = ext.hairline,
        cornerRadiusDp = 10f,
      )
      .padding(4.dp),
    horizontalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    ApplicationTab.entries.forEach { tab ->
      val isSelected = tab == selected
      Box(
        modifier = Modifier
          .weight(1f)
          .clip(NexusShapes.xs)
          .background(if (isSelected) ext.signalContainer else Color.Transparent)
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
          color = if (isSelected) ext.onSignalContainer
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
  onRemove: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val score = application.matchScore
  NexusCard(modifier = modifier.fillMaxWidth()) {
    Column(modifier = Modifier.fillMaxWidth()) {
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

      if (score != null) {
        Spacer(Modifier.height(14.dp))
        val matchLabel = stringResource(
            R.string.applications_match_percent,
            (score * 100).toInt(),
        )
        val matchAccent = when {
          score > 0.8f -> MaterialTheme.nexus.positive
          score > 0.5f -> MaterialTheme.nexus.attention
          else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = matchLabel,
            style = NexusTypography.DataLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
          )
          Spacer(Modifier.width(12.dp))
          CalibratedRule(
            progress = score,
            modifier = Modifier.weight(1f),
            divisions = 10,
            accent = matchAccent,
            height = 16.dp,
            contentDescription = matchLabel,
          )
        }
      }

      AnimatedVisibility(visible = application.status.ordinal < ApplicationStatus.ACCEPTED.ordinal) {
        Spacer(Modifier.height(12.dp))
        StatusActions(
          currentStatus = application.status,
          onStatusChange = onStatusChange,
        )
      }

      // Applied, interviewing and offered rows carry dates a student would not
      // want lost to a mis-tap. A saved row is the mis-tap worth undoing, and a
      // closed outcome is the row they want off the list.
      val isRemovable = application.status == ApplicationStatus.SAVED ||
        application.status == ApplicationStatus.REJECTED ||
        application.status == ApplicationStatus.ACCEPTED
      if (isRemovable) {
        Spacer(Modifier.height(10.dp))
        TextButton(
          onClick = onRemove,
          shape = NexusShapes.sm,
          contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
        ) {
          Text(
            text = stringResource(R.string.applications_action_remove).uppercase(Locale.ROOT),
            style = NexusTypography.TelemetryMicro,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
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
  val tone = when (status) {
    ApplicationStatus.SAVED -> SignalTone.Neutral
    ApplicationStatus.APPLIED -> SignalTone.Structure
    ApplicationStatus.INTERVIEWING -> SignalTone.Attention
    ApplicationStatus.REJECTED -> SignalTone.Critical
    ApplicationStatus.OFFERED, ApplicationStatus.ACCEPTED -> SignalTone.Signal
    else -> SignalTone.Neutral
  }
  SignalTag(text = label, tone = tone)
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
      NexusOutlineButton(
        text = when (status) {
          ApplicationStatus.APPLIED -> stringResource(R.string.applications_action_mark_applied)
          ApplicationStatus.INTERVIEWING -> stringResource(R.string.applications_action_got_interview)
          ApplicationStatus.OFFERED -> stringResource(R.string.applications_action_got_offer)
          ApplicationStatus.ACCEPTED -> stringResource(R.string.applications_action_accept)
          ApplicationStatus.REJECTED -> stringResource(R.string.applications_action_reject)
          else -> status.name
        },
        onClick = { onStatusChange(status) },
        modifier = Modifier.height(NexusSpacing.minTouchTarget),
        expand = false,
      )
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

/**
 * Flattens the visible applications into dated pipeline events. Each
 * application contributes up to three entries — applied, interview, outcome —
 * and an event without a date is omitted, never invented: the timeline charts
 * only history the user actually recorded. Stage labels reuse the status
 * vocabulary the cards already speak, with the company and role as the entry's
 * context line; tone follows the stage (applied is structural bookkeeping, the
 * interview carries the loud signal, and an outcome takes its verdict's tone).
 * Newest first, because a pipeline is read backwards from where you stand now.
 */
@Composable
private fun applicationTimelineEntries(applications: List<Application>): List<TimelineEntry> {
  val appliedLabel = stringResource(R.string.applications_status_applied)
  val interviewLabel = stringResource(R.string.applications_status_interviewing)
  val offeredLabel = stringResource(R.string.applications_status_offered)
  val acceptedLabel = stringResource(R.string.applications_status_accepted)
  val rejectedLabel = stringResource(R.string.applications_status_rejected)
  val otherOutcomeLabel = stringResource(R.string.applications_timeline_outcome_other)

  return applications
    .flatMap { application ->
      val context = "${application.company} — ${application.role}"
      buildList {
        application.appliedDate?.let { timestamp ->
          add(
            TimelineEntry(
              id = "applied-${application.id}",
              label = appliedLabel,
              detail = context,
              timestamp = timestamp,
              tone = SignalTone.Structure,
            )
          )
        }
        application.interviewDate?.let { timestamp ->
          add(
            TimelineEntry(
              id = "interview-${application.id}",
              label = interviewLabel,
              detail = context,
              timestamp = timestamp,
              tone = SignalTone.Signal,
            )
          )
        }
        application.outcomeDate?.let { timestamp ->
          val (label, tone) = when (application.status) {
            ApplicationStatus.OFFERED -> offeredLabel to SignalTone.Signal
            ApplicationStatus.ACCEPTED -> acceptedLabel to SignalTone.Signal
            ApplicationStatus.REJECTED -> rejectedLabel to SignalTone.Critical
            else -> otherOutcomeLabel to SignalTone.Neutral
          }
          add(
            TimelineEntry(
              id = "outcome-${application.id}",
              label = label,
              detail = context,
              timestamp = timestamp,
              tone = tone,
            )
          )
        }
      }
    }
    .sortedByDescending { it.timestamp ?: 0L }
}
