package com.nexus.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.nexus.designsystem.theme.nexus
import com.nexus.domain.model.Goal
import com.nexus.domain.model.GoalCategory
import com.nexus.domain.model.UiState
import java.time.format.DateTimeFormatter

data class GoalItem(
    val id: String,
    val title: String,
    val category: GoalCategory,
    val progress: Float,
    val targetDate: String?,
    val isCompleted: Boolean,
)

private fun categoryIcon(cat: GoalCategory): ImageVector = when (cat) {
    GoalCategory.CERTIFICATION -> Icons.Default.School
    GoalCategory.DSA -> Icons.Default.Code
    GoalCategory.INTERNSHIP -> Icons.Default.Work
    GoalCategory.PROJECT -> Icons.Default.Build
    GoalCategory.SKILL -> Icons.Default.Star
    GoalCategory.HACKATHON -> Icons.Default.Flag
    GoalCategory.PLACEMENT -> Icons.Default.Work
    GoalCategory.OTHER -> Icons.Default.Star
}

@Composable
private fun categoryLabel(cat: GoalCategory): String = stringResource(
    when (cat) {
        GoalCategory.CERTIFICATION -> R.string.goals_category_certification
        GoalCategory.DSA -> R.string.goals_category_dsa
        GoalCategory.INTERNSHIP -> R.string.goals_category_internship
        GoalCategory.PROJECT -> R.string.goals_category_project
        GoalCategory.SKILL -> R.string.goals_category_skill
        GoalCategory.HACKATHON -> R.string.goals_category_hackathon
        GoalCategory.PLACEMENT -> R.string.goals_category_placement
        GoalCategory.OTHER -> R.string.goals_category_other
    }
)

@Composable
fun GoalsScreen(
    onBack: () -> Unit = {},
    viewModel: GoalsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val goals: List<GoalItem> = when (val s = uiState) {
        is UiState.Success -> s.data.map { it.toItem() }
        else -> emptyList()
    }
    val isLoading = uiState is UiState.Loading
    val completedCount = goals.count { it.progress >= 1f }
    val averageProgress = if (goals.isEmpty()) 0f
    else goals.sumOf { it.progress.toDouble() }.toFloat() / goals.size

    var isComposing by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            NexusTopBar(
                title = stringResource(R.string.goals_goals_habits),
                onBack = onBack,
            )

            LazyColumn(
                contentPadding = PaddingValues(
                    start = NexusSpacing.screenEdge,
                    end = NexusSpacing.screenEdge,
                    top = NexusSpacing.xs,
                    // Clear the floating action rather than letting it cover
                    // the final row's footer.
                    bottom = NexusSpacing.massive + NexusSpacing.md,
                ),
                verticalArrangement = Arrangement.spacedBy(NexusSpacing.sm),
            ) {
                item {
                    GoalsSummaryPlate(
                        averageProgress = averageProgress,
                        total = goals.size,
                        completed = completedCount,
                    )
                }

                when {
                    isLoading && goals.isEmpty() -> item {
                        Text(
                            stringResource(R.string.goals_loading_your_goals),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = NexusSpacing.huge),
                            textAlign = TextAlign.Center,
                        )
                    }

                    goals.isEmpty() -> item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = NexusSpacing.huge),
                        ) {
                            Text(
                                stringResource(R.string.goals_no_goals_yet),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.height(NexusSpacing.xxs))
                            Text(
                                stringResource(R.string.goals_tap_to_set_your_first_goal),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    else -> items(goals, key = { it.id }) { goal ->
                        GoalCard(
                            goal = goal,
                            onToggleComplete = {
                                if (!goal.isCompleted) viewModel.completeGoal(goal.id)
                                else viewModel.updateProgress(goal.id, 0f)
                            },
                            onDelete = { viewModel.deleteGoal(goal.id) },
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { isComposing = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(NexusSpacing.screenEdge),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = NexusShapes.sm,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.goals_add_goal),
            )
        }
    }

    if (isComposing) {
        GoalComposerDialog(
            onDismiss = { isComposing = false },
            onCreate = { title, category ->
                viewModel.createGoal(
                    Goal(
                        id = "goal_${System.currentTimeMillis()}",
                        title = title,
                        category = category,
                    )
                )
                isComposing = false
            },
        )
    }
}

/**
 * The one lime slab on this screen: a single headline reading, the count that
 * qualifies it, and the rule that plots it. Ink on lime, never white.
 */
@Composable
private fun GoalsSummaryPlate(
    averageProgress: Float,
    total: Int,
    completed: Int,
) {
    val ink = NexusColors.SignalInk
    val percent = (averageProgress * 100).toInt()

    NexusCard(
        gradient = NexusGradients.Primary,
        shape = NexusShapes.lg,
    ) {
        Column {
            Text(
                text = stringResource(R.string.goals_active_goals).uppercase(),
                style = NexusTypography.DataLabel,
                color = ink.copy(alpha = 0.85f),
            )
            Spacer(Modifier.height(NexusSpacing.xxs))
            Row {
                Text(
                    text = percent.toString(),
                    style = NexusTypography.TelemetryDisplay,
                    color = ink,
                    maxLines = 1,
                    modifier = Modifier.alignByBaseline(),
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text = "%",
                    style = NexusTypography.DataLabel,
                    color = ink.copy(alpha = 0.85f),
                    modifier = Modifier.alignByBaseline(),
                )
            }
            Text(
                text = stringResource(
                    R.string.goals_goals_u00b7_completed,
                    total,
                    completed,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = ink.copy(alpha = 0.85f),
            )
            Spacer(Modifier.height(NexusSpacing.sm))
            CalibratedRule(
                progress = averageProgress,
                divisions = 10,
                accent = ink,
                trackColor = ink.copy(alpha = 0.18f),
                tickColor = ink.copy(alpha = 0.40f),
                height = 18.dp,
                contentDescription = stringResource(
                    R.string.goals_overall_progress,
                    percent,
                ),
            )
        }
    }
}

/**
 * One goal, read as an instrument line: identity chip, title, calibrated rule,
 * then the numeric footer. The whole row toggles completion; destructive
 * actions live behind the overflow so a stray tap cannot delete a goal.
 */
@Composable
private fun GoalCard(
    goal: GoalItem,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val ext = MaterialTheme.nexus
    val accent = if (goal.isCompleted) ext.positive else MaterialTheme.colorScheme.primary
    val percent = (goal.progress * 100).toInt()

    NexusCard(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = goal.isCompleted,
                role = Role.Checkbox,
                onValueChange = { onToggleComplete() },
            ),
        shape = NexusShapes.md,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(NexusShapes.sm)
                    .background(
                        if (goal.isCompleted) accent.copy(alpha = 0.14f)
                        else MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (goal.isCompleted) Icons.Default.CheckCircle
                    else categoryIcon(goal.category),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(NexusSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (goal.isCompleted) TextDecoration.LineThrough else null,
                    color = if (goal.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(NexusSpacing.xs))
                CalibratedRule(
                    progress = goal.progress,
                    divisions = 10,
                    accent = accent,
                    height = 16.dp,
                    contentDescription = stringResource(
                        R.string.goals_goal_progress,
                        percent,
                    ),
                )
                Spacer(Modifier.height(NexusSpacing.xs))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (goal.isCompleted) {
                        SignalTag(
                            text = stringResource(R.string.goals_completed),
                            tone = SignalTone.Signal,
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.goals_goal_progress, percent),
                            style = NexusTypography.Telemetry,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                    Text(
                        text = goal.targetDate
                            ?: stringResource(R.string.goals_no_deadline),
                        style = NexusTypography.DataLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.goals_goal_actions),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.goals_delete_goal)) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        },
                    )
                }
            }
        }
    }
}

/**
 * Goal entry, built from the same parts as the list it feeds: one text field,
 * one row of category tags, no date picker (a goal without a deadline is legal
 * and `targetDate` is nullable).
 */
@Composable
private fun GoalComposerDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, category: GoalCategory) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var categoryName by rememberSaveable { mutableStateOf(GoalCategory.CERTIFICATION.name) }
    val category = GoalCategory.valueOf(categoryName)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = NexusShapes.lg,
        title = {
            Text(
                text = stringResource(R.string.goals_new_goal),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 80) title = it },
                    label = { Text(stringResource(R.string.goals_goal_title)) },
                    placeholder = { Text(stringResource(R.string.goals_goal_title_hint)) },
                    singleLine = true,
                    shape = NexusShapes.sm,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(NexusSpacing.md))
                Text(
                    text = stringResource(R.string.goals_goal_category).uppercase(),
                    style = NexusTypography.DataLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(NexusSpacing.xs))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(NexusSpacing.xs),
                    verticalArrangement = Arrangement.spacedBy(NexusSpacing.xs),
                ) {
                    GoalCategory.entries.forEach { option ->
                        CategoryOption(
                            label = categoryLabel(option),
                            selected = option == category,
                            onClick = { categoryName = option.name },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(title.trim(), category) },
                enabled = title.isNotBlank(),
                shape = NexusShapes.sm,
            ) {
                Text(stringResource(R.string.goals_create_goal))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shape = NexusShapes.sm) {
                Text(stringResource(R.string.goals_cancel))
            }
        },
    )
}

/**
 * A category selector that reads like the rest of the app's tags: uppercase
 * mono at 2dp corners, one selected plate, everything else quiet.
 */
@Composable
private fun CategoryOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val ext = MaterialTheme.nexus
    val container = if (selected) ext.signalContainer
    else MaterialTheme.colorScheme.surfaceContainerHigh
    val content = if (selected) ext.onSignalContainer
    else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (selected) ext.signal else ext.hairline

    Box(
        modifier = Modifier
            .clip(NexusShapes.xs)
            .background(container)
            .border(NexusSpacing.hairline, borderColor, NexusShapes.xs)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .heightIn(min = NexusSpacing.minTouchTarget)
            .padding(horizontal = NexusSpacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label.uppercase(),
            style = NexusTypography.DataLabel,
            color = content,
            maxLines = 1,
        )
    }
}

private fun Goal.toItem(): GoalItem = GoalItem(
    id = id,
    title = title,
    category = category,
    progress = progress,
    targetDate = targetDate?.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
    isCompleted = isCompleted,
)
