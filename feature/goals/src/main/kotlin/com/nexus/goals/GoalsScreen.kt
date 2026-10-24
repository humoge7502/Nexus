package com.nexus.goals

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.domain.model.Goal
import com.nexus.domain.model.GoalCategory
import com.nexus.domain.model.UiState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.format.DateTimeFormatter

data class GoalItem(
    val id: String,
    val title: String,
    val category: GoalCategory,
    val progress: Float,
    val targetDate: String,
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

@OptIn(ExperimentalMaterial3Api::class)
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

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Goals & Habits", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                NexusCard(
                    gradient = NexusGradients.Nexus,
                    shape = NexusShapes.lg,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Active Goals",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.9f))
                        Spacer(Modifier.height(4.dp))
                        Text("${goals.size} goals \u00B7 ${goals.count { it.progress >= 1f }} completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f))
                        Spacer(Modifier.height(12.dp))
                        val avgProgress by animateFloatAsState(
                            targetValue = if (goals.isEmpty()) 0f
                                else goals.sumOf { it.progress.toDouble() }.toFloat() / goals.size,
                            animationSpec = spring(), label = "avgProgress",
                        )
                        LinearProgressIndicator(
                            progress = { avgProgress },
                            modifier = Modifier.fillMaxWidth().height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.2f),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("${(avgProgress * 100).toInt()}% overall progress",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f))
                    }
                }
            }

            when {
                isLoading && goals.isEmpty() -> item {
                    Text(
                        "Loading your goals...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
                goals.isEmpty() -> item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    ) {
                        Text(
                            "No goals yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Tap + to set your first goal — certification, DSA count, anything.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
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

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun GoalCard(
    goal: GoalItem,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
) {
    val animProgress by animateFloatAsState(
        targetValue = goal.progress,
        animationSpec = spring(), label = "goalProgress",
    )

    NexusCard(
        modifier = Modifier.fillMaxWidth(),
        glassEffect = goal.isCompleted,
        shape = NexusShapes.md,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                    .background(
                        if (goal.isCompleted) NexusGradients.Success[0].copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(categoryIcon(goal.category), contentDescription = null,
                    tint = if (goal.isCompleted) NexusGradients.Success[0]
                        else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    goal.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (goal.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { animProgress },
                    modifier = Modifier.fillMaxWidth().height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (goal.isCompleted) NexusGradients.Success[0]
                        else NexusColors.NexusBlue,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()) {
                    Text(
                        if (goal.isCompleted) "Completed" else "${(goal.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (goal.isCompleted) NexusGradients.Success[0]
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        goal.targetDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onToggleComplete) {
                Icon(
                    if (goal.isCompleted) Icons.Default.CheckCircle else Icons.Default.Edit,
                    contentDescription = if (goal.isCompleted) "Completed" else "Edit",
                    tint = if (goal.isCompleted) NexusGradients.Success[0]
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun Goal.toItem(): GoalItem = GoalItem(
    id = id,
    title = title,
    category = category,
    progress = progress,
    targetDate = targetDate?.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) ?: "No deadline",
    isCompleted = isCompleted,
)
