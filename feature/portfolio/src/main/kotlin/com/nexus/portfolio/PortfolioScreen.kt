package com.nexus.portfolio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.common.Constants
import com.nexus.data.repository.PortfolioRepository
import com.nexus.data.repository.UserXpRepository
import com.nexus.designsystem.component.NexusCard
import com.nexus.domain.model.PortfolioProject
import com.nexus.domain.model.SkillCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class PortfolioUiState(
    val projects: List<PortfolioProject> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val portfolioRepository: PortfolioRepository,
    private val xpRepository: UserXpRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PortfolioUiState())
    val uiState: StateFlow<PortfolioUiState> = _uiState.asStateFlow()

    init { loadProjects() }

    private fun loadProjects() {
        viewModelScope.launch {
            portfolioRepository.projects.collect { projects ->
                _uiState.value = PortfolioUiState(isLoading = false, projects = projects)
            }
        }
    }

    fun addProject(project: PortfolioProject) {
        viewModelScope.launch {
            portfolioRepository.addProject(project)
            xpRepository.awardXP(Constants.XP_PER_PORTFOLIO_PROJECT, "portfolio_project_added")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    onBack: () -> Unit = {},
    viewModel: PortfolioViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Portfolio", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                NexusCard(modifier = Modifier.fillMaxWidth(), gradient = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("My Projects", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.height(4.dp))
                        Text("${uiState.projects.size} projects showcased", color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
            items(uiState.projects, key = { it.id }) { project ->
                ProjectCard(project = project)
            }
        }
    }
}

@Composable
private fun ProjectCard(project: PortfolioProject) {
    NexusCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(project.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    project.description?.let { desc ->
                        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (project.isFeatured) {
                    Icon(Icons.Default.Star, contentDescription = "Featured", tint = Color(0xFFF59E0B))
                }
            }
        }
    }
}
