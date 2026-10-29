package com.nexus.ai

import androidx.compose.ui.res.stringResource
import com.nexus.ai.R

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
import com.nexus.designsystem.component.LoadingState
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.domain.usecase.InterviewIntelligenceEngine
import com.nexus.network.ai.AIOpenRouterService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

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
  val rateLimited: Boolean = false,
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
    _uiState.value = _uiState.value.copy(targetRole = role)
  }

  fun updateTargetCompany(company: String) {
    _uiState.value = _uiState.value.copy(
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

  fun updateQuestionType(type: String) {
    _uiState.value = _uiState.value.copy(questionType = type)
  }

  suspend fun checkRateLimit(rateLimiter: AIRateLimiter, isPro: Boolean): Boolean {
    return rateLimiter.canMakeCall(isPro) is RateLimitResult.LimitExceeded
  }

  fun generateQuestion() {
    val role = _uiState.value.targetRole
    val type = _uiState.value.questionType
    if (role.isBlank()) return
    viewModelScope.launch {
      val isPro = userPreferences.isPro.first()
      if (checkRateLimit(rateLimiter, isPro)) {
        _uiState.value = _uiState.value.copy(rateLimited = true, error = "Daily AI limit reached. Upgrade to Pro for unlimited access.")
        return@launch
      }
      _uiState.value = _uiState.value.copy(isLoading = true, error = null, question = "", userAnswer = "", feedback = "")
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
        _uiState.value = _uiState.value.copy(isLoading = false, question = response)
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Generation failed")
      }
    }
  }

  fun updateAnswer(answer: String) {
    _uiState.value = _uiState.value.copy(userAnswer = answer)
  }

  fun getFeedback() {
    val answer = _uiState.value.userAnswer
    val question = _uiState.value.question
    if (answer.isBlank() || question.isBlank()) return
    viewModelScope.launch {
      val isPro = userPreferences.isPro.first()
      if (checkRateLimit(rateLimiter, isPro)) {
        _uiState.value = _uiState.value.copy(rateLimited = true, error = "Daily AI limit reached. Upgrade to Pro for unlimited access.")
        return@launch
      }
      _uiState.value = _uiState.value.copy(isLoading = true, error = null)
      try {
        val prompt = "Interview question: $question\n\nCandidate's answer: $answer\n\nProvide constructive feedback. Rate out of 10. Suggest improvements and give a model answer."
        val response = aiService.sendMessage(prompt)
        rateLimiter.recordCall()
        _uiState.value = _uiState.value.copy(isLoading = false, feedback = response)
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Feedback failed")
      }
    }
  }
}

@Composable
fun InterviewCoachScreen(
  onBack: () -> Unit = {},
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
      OutlinedTextField(
        value = uiState.targetRole,
        onValueChange = { viewModel.updateTargetRole(it) },
        label = { Text(stringResource(R.string.ai_target_role)) },
        placeholder = { Text(stringResource(R.string.ai_e_g_android_developer_data_analyst)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
      )
      Spacer(Modifier.height(12.dp))
      OutlinedTextField(
        value = uiState.targetCompany,
        onValueChange = viewModel::updateTargetCompany,
        label = { Text(stringResource(R.string.ai_target_company_optional)) },
        placeholder = { Text(stringResource(R.string.ai_e_g_google_razorpay)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
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
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        questionTypes.forEach { type ->
          FilterChip(
            selected = uiState.questionType == type,
            onClick = { viewModel.updateQuestionType(type) },
            label = { Text(type) },
          )
        }
      }
      Spacer(Modifier.height(16.dp))
      Button(
        onClick = { viewModel.generateQuestion() },
        modifier = Modifier.fillMaxWidth(),
        enabled = uiState.targetRole.isNotBlank() && !uiState.isLoading,
      ) {
        Text(if (uiState.isLoading) "Generating..." else "Generate Question")
      }
      Spacer(Modifier.height(16.dp))

      if (uiState.isLoading) {
        LoadingState()
      }

      uiState.error?.let { error ->
        Text(text = error, color = MaterialTheme.colorScheme.error)
      }

      if (uiState.question.isNotBlank()) {
        NexusCard(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = uiState.question,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
          )
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
          value = uiState.userAnswer,
          onValueChange = { viewModel.updateAnswer(it) },
          label = { Text(stringResource(R.string.ai_your_answer)) },
          modifier = Modifier.fillMaxWidth(),
          minLines = 3,
          maxLines = 6,
        )
        Spacer(Modifier.height(12.dp))
        Button(
          onClick = { viewModel.getFeedback() },
          modifier = Modifier.fillMaxWidth(),
          enabled = uiState.userAnswer.isNotBlank() && !uiState.isLoading,
        ) {
          Text(stringResource(R.string.ai_get_feedback))
        }
      }

      if (uiState.feedback.isNotBlank()) {
        Spacer(Modifier.height(16.dp))
        NexusCard(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = uiState.feedback,
            style = MaterialTheme.typography.bodyMedium,
          )
        }
      }
    }
  }
}
