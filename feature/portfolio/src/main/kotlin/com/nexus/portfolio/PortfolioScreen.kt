package com.nexus.portfolio

import android.content.Intent
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.nexus.common.Constants
import com.nexus.data.repository.PortfolioRepository
import com.nexus.data.repository.UserXpRepository
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.component.hairline
import com.nexus.designsystem.component.instrumentGrid
import com.nexus.designsystem.component.sheen
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.domain.model.PortfolioProject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@Immutable
data class PortfolioUiState(
    val projects: List<PortfolioProject> = emptyList(),
    val isLoading: Boolean = true,
)

/** What the add-project dialog collects; only [title] is required. */
@Immutable
data class ProjectDraft(
    val title: String = "",
    val description: String = "",
    val githubUrl: String = "",
    val liveUrl: String = "",
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

    fun addProject(draft: ProjectDraft) {
        if (draft.title.isBlank()) return
        viewModelScope.launch {
            portfolioRepository.addProject(
                PortfolioProject(
                    id = UUID.randomUUID().toString(),
                    title = draft.title.trim(),
                    description = draft.description.trim(),
                    githubUrl = draft.githubUrl.trim().ifBlank { null },
                    liveUrl = draft.liveUrl.trim().ifBlank { null },
                )
            )
            xpRepository.awardXP(Constants.XP_PER_PORTFOLIO_PROJECT, "portfolio_project_added")
        }
    }
}

@Composable
fun PortfolioScreen(
    onBack: () -> Unit = {},
    viewModel: PortfolioViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        NexusTopBar(
            title = stringResource(R.string.portfolio_portfolio),
            onBack = onBack,
            actions = {
                if (uiState.projects.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    portfolioShareText(uiState.projects),
                                )
                            }
                            context.startActivity(
                                Intent.createChooser(
                                    send,
                                    context.getString(R.string.portfolio_share_title),
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.portfolio_share),
                        )
                    }
                }
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.portfolio_add_project),
                    )
                }
            },
        )

        LazyColumn(
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                PortfolioSignature(
                    projectCount = uiState.projects.size,
                    onAdd = { showAddDialog = true },
                )
            }
            if (uiState.projects.isEmpty() && !uiState.isLoading) {
                item {
                    PortfolioEmpty(onAdd = { showAddDialog = true })
                }
            } else {
                itemsIndexed(uiState.projects, key = { _, project -> project.id }) { index, project ->
                    ProjectCard(index = index + 1, project = project)
                }
            }
        }
    }

    if (showAddDialog) {
        AddProjectDialog(
            onDismiss = { showAddDialog = false },
            onSubmit = { draft ->
                viewModel.addProject(draft)
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun PortfolioSignature(
    projectCount: Int,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                brush = Brush.linearGradient(NexusGradients.PrimaryDeep),
                shape = shape,
            )
            .instrumentGrid(spacingDp = 30f, alpha = 0.045f)
            .hairline(color = Color.White.copy(alpha = 0.16f), cornerRadiusDp = 24f)
            .sheen(alpha = 0.07f)
            .padding(horizontal = 22.dp, vertical = 20.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.portfolio_my_projects),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White.copy(alpha = 0.70f),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$projectCount",
                            style = NexusTypography.TelemetryDisplay.copy(
                                fontSize = 54.sp,
                                lineHeight = 56.sp,
                            ),
                            color = Color.White,
                        )
                        Text(
                            text = pluralStringResource(
                                R.plurals.portfolio_project_noun,
                                projectCount,
                            ),
                            style = NexusTypography.TelemetryLarge,
                            color = Color.White.copy(alpha = 0.62f),
                            modifier = Modifier.padding(start = 6.dp, bottom = 7.dp),
                        )
                    }
                }
                GlassAddButton(onClick = onAdd)
            }
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.14f)),
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp, end = 10.dp)
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(NexusColors.Cyan400),
                )
                Text(
                    text = stringResource(R.string.portfolio_signature_caption),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.92f),
                )
            }
        }
    }
}

@Composable
private fun GlassAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .clip(shape)
            .background(Color.White.copy(alpha = 0.16f))
            .hairline(color = Color.White.copy(alpha = 0.28f), cornerRadiusDp = 50f)
            .clickable(role = Role.Button) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.portfolio_add),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
        )
    }
}

@Composable
private fun PortfolioEmpty(
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 56.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.size(44.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.portfolio_empty_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.portfolio_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onAdd) {
            Text(stringResource(R.string.portfolio_add_first))
        }
    }
}

@Composable
private fun ProjectCard(
    index: Int,
    project: PortfolioProject,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    NexusCard(modifier = modifier.fillMaxWidth()) {
        Row {
            Text(
                text = index.toString().padStart(2, '0'),
                style = NexusTypography.TelemetryMicro,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 3.dp, end = 12.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (project.isFeatured) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = stringResource(R.string.cd_featured),
                            tint = NexusColors.Amber400,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(16.dp),
                        )
                    }
                }
                if (project.description.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = project.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (project.techStack.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        project.techStack.take(4).forEach { tech ->
                            Text(
                                text = tech,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                    .padding(horizontal = 7.dp, vertical = 3.dp),
                            )
                        }
                    }
                }
                val links = listOfNotNull(project.githubUrl, project.liveUrl)
                if (links.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        links.forEach { url ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { openPortfolioLink(context, url) }
                                    .minimumInteractiveComponentSize()
                                    .padding(vertical = 3.dp, horizontal = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = url,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddProjectDialog(
    onDismiss: () -> Unit,
    onSubmit: (ProjectDraft) -> Unit,
) {
    var draft by remember { mutableStateOf(ProjectDraft()) }
    val haptic = LocalHapticFeedback.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.portfolio_new_project)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                OutlinedTextField(
                    value = draft.title,
                    onValueChange = { draft = draft.copy(title = it) },
                    label = { Text(stringResource(R.string.portfolio_project_title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = draft.description,
                    onValueChange = { draft = draft.copy(description = it) },
                    label = { Text(stringResource(R.string.portfolio_project_description)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = draft.githubUrl,
                    onValueChange = { draft = draft.copy(githubUrl = it) },
                    label = { Text(stringResource(R.string.portfolio_project_code_link)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Uri,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = draft.liveUrl,
                    onValueChange = { draft = draft.copy(liveUrl = it) },
                    label = { Text(stringResource(R.string.portfolio_project_live_link)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Uri,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = draft.title.isNotBlank(),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSubmit(draft)
                },
            ) {
                Text(stringResource(R.string.portfolio_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.portfolio_cancel))
            }
        },
    )
}
