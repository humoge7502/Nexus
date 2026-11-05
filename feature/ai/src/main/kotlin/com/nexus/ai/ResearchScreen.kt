package com.nexus.ai

import androidx.compose.ui.res.stringResource
import com.nexus.ai.R

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusIconButton
import com.nexus.designsystem.component.NexusTextField
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.component.conversation.PromptSuggestionRow
import com.nexus.designsystem.component.feedback.EditorialErrorState
import com.nexus.designsystem.component.feedback.EditorialSkeletonList
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.network.service.FirebaseFunctionsAIService
import com.nexus.network.service.RequiresSignInException
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
            NexusTopBar(
                title = stringResource(R.string.ai_deep_research),
                onBack = onBack,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                NexusTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = stringResource(R.string.ai_ask_nexus_anything),
                    placeholder = stringResource(R.string.ai_ask_anything_about_your_career_ne),
                    singleLine = false,
                    trailingIcon = {
                        // No spinner here: the skeleton below the field is the
                        // in-flight signal, so the control only has to say
                        // "not again yet" while a question is being answered.
                        NexusIconButton(
                            icon = Icons.Default.Search,
                            contentDescription = stringResource(R.string.cd_research),
                            onClick = { run(question) },
                            enabled = question.isNotBlank() && !ui.isLoading,
                        )
                    },
                )
            }

            // Before a question is asked this screen is a field over a void, which
            // makes the tool look broken rather than idle. Say what comes back and
            // hand over a question worth asking.
            if (ui.result == null && !ui.isLoading && ui.error == null) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = NexusSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(NexusSpacing.sm),
                    ) {
                        Text(
                            text = stringResource(R.string.ai_research_idle_headline),
                            style = NexusTypography.DataLabel,
                            color = MaterialTheme.nexus.signal,
                        )
                        Text(
                            text = stringResource(R.string.ai_research_idle_body),
                            style = NexusTypography.BodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                item {
                    PromptSuggestionRow(
                        prompts = listOf(
                            stringResource(R.string.ai_research_prompt_1),
                            stringResource(R.string.ai_research_prompt_2),
                            stringResource(R.string.ai_research_prompt_3),
                        ),
                        label = stringResource(R.string.ai_research_starters),
                        onPrompt = {
                            question = it
                            run(it)
                        },
                    )
                }
            }

            if (ui.isLoading) {
                item {
                    EditorialSkeletonList(
                        count = 2,
                        loadingLabel = stringResource(R.string.ai_research_loading),
                    )
                }
            }

            ui.error?.let { err ->
                item {
                    if (ui.requiresSignIn) {
                        // A missing account is not a dropped request: the question
                        // never left the phone, so the card says UNAVAILABLE and
                        // offers no Retry, which could only fail the same way.
                        EditorialErrorState(
                            headline = stringResource(R.string.ai_research_signin_headline),
                            reason = stringResource(R.string.ai_research_signin_reason),
                            isRecoverable = false,
                        )
                    } else {
                        EditorialErrorState(
                            headline = stringResource(R.string.ai_fault_headline),
                            reason = stringResource(R.string.ai_research_fault_reason),
                            actionLabel = stringResource(R.string.ai_retry),
                            onAction = { run(ui.question) },
                            secondaryHint = err,
                        )
                    }
                }
            }

            ui.result?.let { res -> researchResult(res) { run(it) } }

            if (ui.history.size > 1) {
                item {
                    // The same rail the starters use: a past question is re-run,
                    // not edited, so it is a prompt and not a list row.
                    PromptSuggestionRow(
                        prompts = ui.history.drop(1).map { it.question },
                        label = stringResource(R.string.ai_recent_research),
                        onPrompt = { run(it) },
                    )
                }
            }
        }
    }
}

private fun LazyListScope.researchResult(
    res: ResearchResult,
    onFollowUp: (String) -> Unit,
) {
    item {
        NexusCard(
            gradient = NexusGradients.PrimaryDeep,
            shape = NexusShapes.lg,
        ) {
            Column {
                Text(
                    res.question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NexusColors.SignalInk,
                )
                if (res.usedLiveSearch) {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .width(2.dp)
                                .height(11.dp)
                                .clip(NexusShapes.tag)
                                .background(NexusColors.SignalInk),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stringResource(R.string.ai_live_search_grounded),
                            style = NexusTypography.DataLabel,
                            color = NexusColors.SignalInk.copy(alpha = 0.86f),
                        )
                    }
                }
            }
        }
    }
    item {
        Text(res.answer, style = MaterialTheme.typography.bodyLarge)
    }
    if (res.sources.isNotEmpty()) {
        item {
            Text(
                stringResource(R.string.ai_sources),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
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
            Text(
                stringResource(R.string.ai_go_deeper),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        item {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                res.followUps.forEach { fq ->
                    AssistChip(
                        onClick = { onFollowUp(fq) },
                        label = { Text(fq, style = MaterialTheme.typography.bodySmall) },
                    )
                }
            }
        }
    }
}

data class ResearchUiState(
    val question: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    /** The call was never sent because this device has no signed-in user. Kept
     *  apart from [error] so the screen can drop Retry — pressing it changes
     *  nothing, and a control that cannot work is not an affordance. */
    val requiresSignIn: Boolean = false,
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
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null,
            requiresSignIn = false,
            question = question,
        )
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
                    // Only the cause travels here. The sentence the user reads is
                    // the screen's, so a null message cannot become an empty red
                    // line and a provider string cannot become the whole answer.
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message,
                        requiresSignIn = e is RequiresSignInException,
                    )
                },
            )
        }
    }
}
