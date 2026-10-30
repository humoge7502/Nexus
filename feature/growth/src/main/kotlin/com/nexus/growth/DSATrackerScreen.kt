package com.nexus.growth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.designsystem.component.AnimatedStatCard
import com.nexus.designsystem.component.CalibratedRule
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.component.SignalTag
import com.nexus.designsystem.component.SignalTone
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.component.hairline
import com.nexus.designsystem.theme.nexus
import com.nexus.domain.model.UiState
import java.util.Locale

data class DSAProblemDisplay(
    val id: String,
    val title: String,
    val difficulty: String,
    val category: String,
    val platform: String,
    val isSolved: Boolean,
)

/**
 * Filters map onto the domain difficulty names, which are the values carried in
 * [DSAProblemDisplay.difficulty] — they are data, not display copy.
 */
private enum class DsaFilter(val label: String, val difficulty: String?) {
    ALL("All", null),
    EASY("Easy", "Easy"),
    MEDIUM("Medium", "Medium"),
    HARD("Hard", "Hard"),
}

@Composable
@ReadOnlyComposable
private fun difficultyTone(difficulty: String): SignalTone = when (difficulty) {
    "Easy" -> SignalTone.Signal
    "Medium" -> SignalTone.Attention
    "Hard" -> SignalTone.Critical
    else -> SignalTone.Neutral
}

@Composable
fun DSATrackerScreen(
    onBack: () -> Unit = {},
    viewModel: GrowthViewModel = hiltViewModel(),
) {
    var selectedFilter by remember { mutableStateOf(DsaFilter.ALL) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val problems: List<DSAProblemDisplay> = when (val s = uiState.problems) {
        is UiState.Success -> s.data.map { it.toDisplay() }
        else -> emptyList()
    }

    val solvedCount = problems.count { it.isSolved }
    val totalCount = problems.size
    val solvedProgress = if (totalCount == 0) 0f else solvedCount.toFloat() / totalCount

    val filteredProblems = selectedFilter.difficulty
        ?.let { level -> problems.filter { it.difficulty == level } }
        ?: problems

    Column(modifier = Modifier.fillMaxSize()) {
        NexusTopBar(
            title = stringResource(R.string.growth_dsa_tracker),
            onBack = onBack,
        )

        LazyColumn(
            contentPadding = PaddingValues(
                start = NexusSpacing.screenEdge,
                end = NexusSpacing.screenEdge,
                top = NexusSpacing.xs,
                bottom = NexusSpacing.xxl,
            ),
            verticalArrangement = Arrangement.spacedBy(NexusSpacing.sm),
        ) {
            item {
                // The one lime slab on this screen: ink figures on a lit rule.
                NexusCard(
                    gradient = NexusGradients.Ascent,
                    shape = NexusShapes.lg,
                ) {
                    val ink = NexusColors.SignalInk
                    Column {
                        Text(
                            stringResource(R.string.growth_dsa_progress).uppercase(Locale.ROOT),
                            style = NexusTypography.DataLabel,
                            color = ink.copy(alpha = 0.62f),
                        )
                        Spacer(Modifier.height(NexusSpacing.xxs))
                        Text(
                            stringResource(
                                R.string.growth_solvedcount_totalcount,
                                solvedCount,
                                totalCount,
                            ),
                            style = NexusTypography.TelemetryDisplay,
                            color = ink,
                            maxLines = 1,
                        )
                        Text(
                            stringResource(R.string.growth_problems_solved),
                            style = MaterialTheme.typography.bodySmall,
                            color = ink.copy(alpha = 0.58f),
                        )
                        Spacer(Modifier.height(NexusSpacing.sm))
                        // One graduation per problem, so the tick field is the
                        // syllabus itself rather than a texture.
                        CalibratedRule(
                            progress = solvedProgress,
                            divisions = totalCount,
                            accent = ink,
                            trackColor = ink.copy(alpha = 0.18f),
                            tickColor = ink.copy(alpha = 0.40f),
                            height = 18.dp,
                            contentDescription = stringResource(
                                R.string.growth_solvedcount_totalcount,
                                solvedCount,
                                totalCount,
                            ),
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(NexusSpacing.xs),
                ) {
                    AnimatedStatCard(
                        value = solvedCount,
                        label = stringResource(R.string.dsa_solved),
                        modifier = Modifier.weight(1f),
                    )
                    AnimatedStatCard(
                        value = totalCount - solvedCount,
                        label = stringResource(R.string.dsa_remaining),
                        modifier = Modifier.weight(1f),
                    )
                    AnimatedStatCard(
                        value = 5,
                        label = stringResource(R.string.dsa_day_streak),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item {
                DsaFilterTabs(
                    selected = selectedFilter,
                    onSelect = { selectedFilter = it },
                )
            }

            items(filteredProblems, key = { it.id }) { problem ->
                DSAProblemCard(
                    problem = problem,
                    onToggleSolved = {
                        viewModel.updateProblemStatus(
                            problem.id,
                            if (problem.isSolved) com.nexus.domain.model.DsaStatus.ATTEMPTED
                            else com.nexus.domain.model.DsaStatus.SOLVED,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun DsaFilterTabs(
    selected: DsaFilter,
    onSelect: (DsaFilter) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val ext = MaterialTheme.nexus
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = NexusSpacing.xxs)
            .clip(NexusShapes.sm)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .hairline(color = ext.hairline, cornerRadiusDp = 10f)
            .padding(NexusSpacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(NexusSpacing.xxs),
    ) {
        DsaFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(NexusShapes.xs)
                    .background(if (isSelected) ext.signalContainer else Color.Transparent)
                    .clickable(role = Role.Tab) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelect(filter)
                    }
                    .padding(vertical = NexusSpacing.xs),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = filter.label,
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
private fun DSAProblemCard(
    problem: DSAProblemDisplay,
    onToggleSolved: () -> Unit,
) {
    // The whole row toggles: a 48dp checkbox inside a 16dp card gutter leaves
    // the glyph floating in dead space, and there is no detail screen to open.
    NexusCard(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = problem.isSolved,
                role = Role.Checkbox,
                onValueChange = { onToggleSolved() },
            ),
        shape = NexusShapes.sm,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (problem.isSolved) Icons.Default.CheckCircle
                else Icons.Default.RadioButtonUnchecked,
                contentDescription = stringResource(R.string.cd_toggle_solved),
                tint = if (problem.isSolved) MaterialTheme.nexus.positive
                else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(NexusSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = problem.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (problem.isSolved) FontWeight.Normal
                    else FontWeight.Medium,
                    color = if (problem.isSolved) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (problem.isSolved) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = listOf(problem.category, problem.platform)
                        .filter { it.isNotBlank() }
                        .joinToString(stringResource(R.string.growth_u00b7_2)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(NexusSpacing.xs))
            SignalTag(
                text = problem.difficulty,
                tone = difficultyTone(problem.difficulty),
            )
        }
    }
}

private fun com.nexus.domain.model.DSAProblem.toDisplay() = DSAProblemDisplay(
    id = id,
    title = title,
    difficulty = difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
    category = category.ifBlank { topics.joinToString(", ") },
    platform = platform,
    isSolved = status == com.nexus.domain.model.DsaStatus.SOLVED,
)
