package com.nexus.growth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.domain.model.UiState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.AnimatedStatCard
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusShapes

data class DSAProblemDisplay(
    val id: String,
    val title: String,
    val difficulty: String,
    val category: String,
    val platform: String,
    val isSolved: Boolean,
)


private val difficultyColors = mapOf(
    "Easy" to NexusGradients.Success[0],
    "Medium" to NexusColors.Amber400,
    "Hard" to Color(0xFFEF4444),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DSATrackerScreen(
    onBack: () -> Unit = {},
    viewModel: GrowthViewModel = hiltViewModel(),
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val problems: List<DSAProblemDisplay> = when (val s = uiState.problems) {
        is UiState.Success -> s.data.map { it.toDisplay() }
        else -> emptyList()
    }
    val filters = listOf("All", "Easy", "Medium", "Hard")

    val solvedCount = problems.count { it.isSolved }
    val totalCount = problems.size
    val solvedProgress by animateFloatAsState(
        targetValue = if (totalCount == 0) 0f else solvedCount.toFloat() / totalCount,
        animationSpec = spring(), label = "solvedProgress",
    )

    val filteredProblems = when (selectedFilter) {
        "All" -> problems
        else -> problems.filter { it.difficulty == selectedFilter }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("DSA Tracker", fontWeight = FontWeight.Bold) },
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
                        Text("DSA Progress",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.9f))
                        Spacer(Modifier.height(8.dp))
                        Text("$solvedCount / $totalCount",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text("Problems Solved",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.7f))
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { solvedProgress },
                            modifier = Modifier.fillMaxWidth().height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.2f),
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AnimatedStatCard(value = solvedCount, label = "Solved",
                        gradient = NexusGradients.Success, modifier = Modifier.weight(1f))
                    AnimatedStatCard(value = totalCount - solvedCount, label = "Remaining",
                        gradient = NexusGradients.Warning, modifier = Modifier.weight(1f))
                    AnimatedStatCard(value = 5, label = "Day Streak",
                        gradient = NexusGradients.Ocean, modifier = Modifier.weight(1f))
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    filters.forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NexusColors.NexusBlue.copy(alpha = 0.15f),
                                selectedLabelColor = NexusColors.NexusBlue,
                            ),
                        )
                    }
                }
            }

            items(filteredProblems) { problem ->
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
private fun DSAProblemCard(
    problem: DSAProblemDisplay,
    onToggleSolved: () -> Unit,
) {
    NexusCard(
        modifier = Modifier.fillMaxWidth(),
        shape = NexusShapes.sm,
        glassEffect = problem.isSolved,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onToggleSolved) {
                Icon(
                    if (problem.isSolved) Icons.Default.CheckCircle
                    else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle solved",
                    tint = if (problem.isSolved) NexusGradients.Success[0]
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    problem.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (problem.isSolved) FontWeight.Normal
                        else FontWeight.Medium,
                    color = if (problem.isSolved) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(problem.category, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("\u00B7", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(problem.platform, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        (difficultyColors[problem.difficulty] ?: NexusColors.Neutral500)
                            .copy(alpha = 0.15f)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    problem.difficulty,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = difficultyColors[problem.difficulty] ?: NexusColors.Neutral500,
                )
            }
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
