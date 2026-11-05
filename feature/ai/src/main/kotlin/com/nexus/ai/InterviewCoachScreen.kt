package com.nexus.ai

import androidx.compose.ui.res.stringResource
import com.nexus.ai.R

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.common.AIRateLimiter
import com.nexus.common.RateLimitResult
import com.nexus.datastore.UserPreferences
import com.nexus.designsystem.component.EmptyState
import com.nexus.designsystem.component.NexusButton
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusChip
import com.nexus.designsystem.component.NexusTextField
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.component.feedback.EditorialErrorState
import com.nexus.designsystem.component.feedback.EditorialSkeletonList
import com.nexus.domain.usecase.InterviewIntelligenceEngine
import com.nexus.network.ai.AIOpenRouterService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Which of the coach's two calls is in flight, so a retry re-runs that one and
 *  does not quietly throw away the answer typed for the other. */
enum class CoachOperation { Question, Feedback }

@Immutable
data class InterviewCoachUiState(
  val isLoading: Boolean = false,
  val targetRole: String = "",
  val targetCompany: String = "",
  val companyIntel: InterviewIntelligenceEngine.CompanyIntel? = null,
  val questionType: String = "Technical",
  val question: String = "",
  val userAnswer: String = "",
  val feedback: String = "",
  val error: String? = null,
  val isLimitReached: Boolean = false,
  val failedOperation: CoachOperation? = null,
)

@HiltViewModel
class InterviewCoachViewModel @Inject constructor(
  private val aiService: AIOpenRouterService,
  private val rateLimiter: AIRateLimiter,
  private val userPreferences: UserPreferences,
  private val intelligenceEngine: InterviewIntelligenceEngine,
) : ViewModel() {
  private val _uiState = MutableStateFlow(InterviewCoachUiState())
  val uiState: StateFlow<InterviewCoachUiState> = _uiState.asStateFlow()

  fun updateTargetRole(role: String) {
    _uiState.update { it.copy(targetRole = role) }
  }

  fun updateTargetCompany(company: String) {
    _uiState.update {
      it.copy(
        targetCompany = company,
        companyIntel = if (company.isBlank()) {
          null
        } else {
          intelligenceEngine.generateCompanyIntel(
            company = company,
            userKnownWeaknesses = emptyList(),
            userDsaHistory = emptyMap(),
          )
        },
      )
    }
  }

  fun updateQuestionType(type: String) {
    _uiState.update { it.copy(questionType = type) }
  }

  fun updateAnswer(answer: String) {
    _uiState.update { it.copy(userAnswer = answer) }
  }

  fun generateQuestion() {
    val role = _uiState.value.targetRole
    val type = _uiState.value.questionType
    if (role.isBlank()) return
    viewModelScope.launch {
      if (limitReached()) {
        _uiState.update { it.copy(isLimitReached = true, error = null, failedOperation = CoachOperation.Question) }
        return@launch
      }
      _uiState.update {
        it.copy(
          isLoading = true,
          error = null,
          isLimitReached = false,
          failedOperation = CoachOperation.Question,
          question = "",
          userAnswer = "",
          feedback = "",
        )
      }
      try {
        val company = _uiState.value.targetCompany
        val prompt = if (company.isNotBlank()) {
          intelligenceEngine.buildMockInterviewPrompt(company, role) +
            "\nAsk exactly one $type question and nothing else."
        } else {
          "Act as an interview coach. Generate a $type interview question for a $role position. Just give the question, nothing else."
        }
        val response = aiService.sendMessage(prompt)
        rateLimiter.recordCall()
        _uiState.update { it.copy(isLoading = false, question = response, failedOperation = null) }
      } catch (e: Exception) {
        _uiState.update { it.copy(isLoading = false, error = e.message) }
      }
    }
  }

  fun getFeedback() {
    val answer = _uiState.value.userAnswer
    val question = _uiState.value.question
    if (answer.isBlank() || question.isBlank()) return
    viewModelScope.launch {
      if (limitReached()) {
        _uiState.update { it.copy(isLimitReached = true, error = null, failedOperation = CoachOperation.Feedback) }
        return@launch
      }
      _uiState.update {
        it.copy(isLoading = true, error = null, isLimitReached = false, failedOperation = CoachOperation.Feedback)
      }
      try {
        val prompt = "Interview question: $question\n\nCandidate's answer: $answer\n\nProvide constructive feedback. Rate out of 10. Suggest improvements and give a model answer."
        val response = aiService.sendMessage(prompt)
        rateLimiter.recordCall()
        _uiState.update { it.copy(isLoading = false, feedback = response, failedOperation = null) }
      } catch (e: Exception) {
        _uiState.update { it.copy(isLoading = false, error = e.message) }
      }
    }
  }

  /** Re-runs whichever call failed, so Retry never costs the user their answer. */
  fun retry() {
    when (_uiState.value.failedOperation) {
      CoachOperation.Question -> generateQuestion()
      CoachOperation.Feedback -> getFeedback()
      null -> Unit
    }
  }

  private suspend fun limitReached(): Boolean =
    rateLimiter.canMakeCall(userPreferences.isPro.first()) is RateLimitResult.LimitExceeded
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun InterviewCoachScreen(
  onBack: () -> Unit = {},
  onGoPro: () -> Unit,
  viewModel: InterviewCoachViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val questionTypes = listOf("Technical", "Behavioral", "System Design", "HR", "Problem Solving")

  Column(modifier = Modifier.fillMaxSize()) {
    NexusTopBar(
      title = stringResource(R.string.ai_interview_coach),
      onBack = onBack,
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
    ) {
      NexusTextField(
        value = uiState.targetRole,
        onValueChange = { viewModel.updateTargetRole(it) },
        label = stringResource(R.string.ai_target_role),
        placeholder = stringResource(R.string.ai_e_g_android_developer_data_analyst),
      )
      Spacer(Modifier.height(12.dp))
      NexusTextField(
        value = uiState.targetCompany,
        onValueChange = viewModel::updateTargetCompany,
        label = stringResource(R.string.ai_target_company_optional),
        placeholder = stringResource(R.string.ai_e_g_google_razorpay),
      )
      uiState.companyIntel?.let { intel ->
        Spacer(Modifier.height(12.dp))
        NexusCard(modifier = Modifier.fillMaxWidth()) {
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = intel.prepMessage,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
            )
            intel.rounds.forEach { round ->
              Text(
                text = "• ${round.roundName} (${round.avgDifficulty}) — ${round.topics.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }
      Spacer(Modifier.height(12.dp))
      Text(stringResource(R.string.ai_question_type), style = MaterialTheme.typography.labelLarge)
      Spacer(Modifier.height(8.dp))
      FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        questionTypes.forEach { type ->
          NexusChip(
            text = type,
            isSelected = uiState.questionType == type,
            onToggle = { viewModel.updateQuestionType(type) },
          )
        }
      }
      Spacer(Modifier.height(16.dp))
      NexusButton(
        text = stringResource(
          if (uiState.isLoading) R.string.ai_generating_button else R.string.ai_generate_question_button,
        ),
        onClick = { viewModel.generateQuestion() },
        enabled = uiState.targetRole.isNotBlank() && !uiState.isLoading,
      )
      Spacer(Modifier.height(16.dp))

      if (uiState.question.isBlank()) {
        CoachOutcome(
          uiState = uiState,
          onRetry = viewModel::retry,
          onGoPro = onGoPro,
          loadingLabel = stringResource(R.string.ai_coach_writing_question),
          showEmpty = true,
        )
      } else {
        NexusCard(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = uiState.question,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
          )
        }
        Spacer(Modifier.height(16.dp))
        NexusTextField(
          value = uiState.userAnswer,
          onValueChange = { viewModel.updateAnswer(it) },
          label = stringResource(R.string.ai_your_answer),
          singleLine = false,
          minLines = 3,
        )
        Spacer(Modifier.height(12.dp))
        NexusButton(
          text = stringResource(R.string.ai_get_feedback),
          onClick = { viewModel.getFeedback() },
          enabled = uiState.userAnswer.isNotBlank() && !uiState.isLoading,
        )
        Spacer(Modifier.height(16.dp))
        CoachOutcome(
          uiState = uiState,
          onRetry = viewModel::retry,
          onGoPro = onGoPro,
          loadingLabel = stringResource(R.string.ai_coach_reading_answer),
          showEmpty = false,
        )
      }
    }
  }
}

@Composable
private fun CoachOutcome(
  uiState: InterviewCoachUiState,
  onRetry: () -> Unit,
  onGoPro: () -> Unit,
  loadingLabel: String,
  showEmpty: Boolean,
) {
  when {
    uiState.isLoading -> EditorialSkeletonList(count = 1, loadingLabel = loadingLabel)

    uiState.isLimitReached -> EditorialErrorState(
      headline = stringResource(R.string.ai_daily_limit_headline),
      reason = stringResource(R.string.ai_daily_limit_shared_reason),
      actionLabel = stringResource(R.string.ai_unlock_pro),
      onAction = onGoPro,
      isRecoverable = false,
    )

    uiState.error != null -> EditorialErrorState(
      headline = stringResource(R.string.ai_fault_headline),
      reason = stringResource(R.string.ai_fault_reason_inputs),
      actionLabel = stringResource(R.string.ai_retry),
      onAction = onRetry,
      secondaryHint = uiState.error,
    )

    uiState.feedback.isNotBlank() -> NexusCard(modifier = Modifier.fillMaxWidth()) {
      Text(
        text = uiState.feedback,
        style = MaterialTheme.typography.bodyMedium,
      )
    }

    showEmpty -> EmptyState(
      title = stringResource(R.string.ai_coach_empty_title),
      description = stringResource(R.string.ai_coach_empty_body),
      voice = stringResource(R.string.ai_coach_empty_voice),
    )
  }
}
