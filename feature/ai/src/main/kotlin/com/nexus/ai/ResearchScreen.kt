package com.nexus.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.theme.NexusGradients
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.network.service.FirebaseFunctionsAIService
import com.nexus.network.service.ResearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Deep Research — question in, cited synthesis + follow-up questions out.
 * The agent-search loop (search -> summarize -> auto-generated next
 * questions) reimagined as a career-intelligence experience.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResearchScreen(
    onBack: () -> Unit = {},
    viewModel: ResearchViewModel = hiltViewModel(),
) {
    val ui = viewModel.uiState.collectAsStateWithLifecycle().value
    var question by remember { mutableStateOf(ui.question) }

    fun run(q: String) {
        if (q.isNotBlank()) viewModel.research(q)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deep Research", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Ask anything about your career…\ne.g. What do fintech companies look for in fresher engineers?")
                    },
                    minLines = 2,
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = {
                        if (ui.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.padding(8.dp).width(20.dp).height(20.dp))
                        } else {
                            IconButton(onClick = { run(question) }, enabled = question.isNotBlank() && !ui.isLoading) {
                                Icon(Icons.Default.Search, contentDescription = "Research")
                            }
                        }
                    },
                )
            }

            ui.error?.let { err ->
                item {
                    Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }

            ui.result?.let { res ->
                item {
                    NexusCard(gradient = NexusGradients.PrimaryDeep) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                res.question,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = androidx.compose.ui.graphics.Color.White,
                            )
                            if (res.usedLiveSearch) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Live search grounded",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f),
                                )
                            }
                        }
                    }
                }
                item {
                    Text(res.answer, style = MaterialTheme.typography.bodyLarge)
                }
                if (res.sources.isNotEmpty()) {
                    item {
                        Text("Sources", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }
                    items(res.sources, key = { it.title }) { src ->
                        Column(Modifier.padding(vertical = 4.dp)) {
                            Text(src.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            src.url?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
                if (res.followUps.isNotEmpty()) {
                    item {
                        Text("Go deeper", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }
                    item {
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            res.followUps.forEach { fq ->
                                AssistChip(
                                    onClick = { run(fq) },
                                    label = { Text(fq, style = MaterialTheme.typography.bodySmall) },
                                )
                            }
                        }
                    }
                }
            }

            if (ui.history.size > 1) {
                item {
                    Text("Recent research", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
                items(ui.history.drop(1), key = { it.question + it.hashCode() }) { h ->
                    TextButton(onClick = { run(h.question) }) { Text(h.question, maxLines = 1) }
                }
            }
        }
    }
}

data class ResearchUiState(
    val question: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val result: ResearchResult? = null,
    val history: List<ResearchResult> = emptyList(),
)

@HiltViewModel
class ResearchViewModel @Inject constructor(
    private val service: FirebaseFunctionsAIService,
) : ViewModel() {
    private val _uiState = kotlinx.coroutines.flow.MutableStateFlow(ResearchUiState())
    val uiState: kotlinx.coroutines.flow.StateFlow<ResearchUiState> = _uiState.asStateFlow()

    fun research(question: String) {
        if (_uiState.value.isLoading) return
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, question = question)
        viewModelScope.launch {
            service.deepResearch(question).fold(
                onSuccess = { res ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        result = res,
                        history = listOf(res) + _uiState.value.history,
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Research failed. Check your connection.",
                    )
                },
            )
        }
    }
}
