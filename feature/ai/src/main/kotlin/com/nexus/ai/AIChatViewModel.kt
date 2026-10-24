package com.nexus.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.analytics.AnalyticsTracker
import com.nexus.analytics.ResearchEvent
import com.nexus.common.AIRateLimiter
import com.nexus.common.RateLimitResult
import com.nexus.data.repository.AIConversationRepository
import com.nexus.data.repository.UserXpRepository
import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.AIConversation
import com.nexus.domain.model.AIMessage
import com.nexus.domain.model.AiMessageRole
import com.nexus.domain.repository.ISkillGraphRepository
import com.nexus.domain.usecase.AIResponseValidator
import com.nexus.domain.usecase.AIWarning
import com.nexus.data.usecase.CareerMemoryManager
import com.nexus.domain.usecase.ConversationMemoryManager
import com.nexus.domain.usecase.WarningSeverity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.collections.immutable.toImmutableList
import timber.log.Timber
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import java.time.Instant
import javax.inject.Inject

@Immutable
data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val isError: Boolean = false,
    val warnings: List<AIWarning> = emptyList(),
)

@Stable
data class AIChatUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage("ai_0", "Hey there! I'm your NEXUS AI career mentor. What would you like help with today?", false),
    ),
    val isThinking: Boolean = false,
    val streamingText: String? = null,
    val currentConversationId: String? = null,
    val conversations: List<AIConversation> = emptyList(),
)

sealed interface AIChatEffect {
    data class ShowRateLimitDialog(val isPro: Boolean, val limit: Int) : AIChatEffect
}

@HiltViewModel
class AIChatViewModel @Inject constructor(
private val repository: AIConversationRepository,
private val nexusAIService: NexusAIService,
private val rateLimiter: AIRateLimiter,
private val userPreferences: UserPreferences,
private val xpRepository: UserXpRepository,
private val analyticsTracker: AnalyticsTracker,
private val memoryManager: ConversationMemoryManager,
private val aiResponseValidator: AIResponseValidator,
private val careerMemoryManager: CareerMemoryManager,
private val skillGraphRepository: ISkillGraphRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIChatUiState())
    val uiState: StateFlow<AIChatUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<AIChatEffect>()
    val effect: SharedFlow<AIChatEffect> = _effect.asSharedFlow()

    init {
        analyticsTracker.track("screen_view", mapOf("screen" to "ai_chat"))
        loadConversations()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            repository.getAllConversations()
                .catch { Timber.e(it, "Failed to load conversations") }
                .collect { list ->
                    _uiState.value = _uiState.value.copy(conversations = list)
                }
        }
    }

    fun createConversation(title: String = "New Chat") {
        viewModelScope.launch {
            val id = repository.createConversation(title)
            _uiState.value = _uiState.value.copy(
                currentConversationId = id,
                messages = listOf(
                    ChatMessage("ai_0", "Hey there! I'm your NEXUS AI career mentor. What would you like help with today?", false),
                ),
            )
        }
    }

    fun loadConversation(conversationId: String) {
        viewModelScope.launch {
            val conversation = repository.getConversation(conversationId)
            if (conversation != null) {
                _uiState.value = _uiState.value.copy(
                    currentConversationId = conversationId,
                    messages = conversation.messages.map { msg ->
                        ChatMessage(msg.id, msg.content, msg.role == AiMessageRole.USER)
                    },
                )
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            val isPro = userPreferences.isPro.first()
            when (val limitResult = rateLimiter.canMakeCall(isPro)) {
                is RateLimitResult.LimitExceeded -> {
                    analyticsTracker.trackResearchEvent(
                        ResearchEvent.AIRateLimitHit,
                        mapOf("screen" to "ai_chat", "calls_today" to limitResult.callsToday)
                    )
                    _effect.emit(AIChatEffect.ShowRateLimitDialog(
                        isPro = limitResult.isPro,
                        limit = limitResult.limit
                    ))
                    return@launch
                }
                is RateLimitResult.Allowed -> Unit
            }

            analyticsTracker.trackResearchEvent(
                ResearchEvent.AISessionStarted,
                mapOf("message_length" to text.length)
            )

            val userMessage = ChatMessage("u_${System.currentTimeMillis()}", text, isUser = true)
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + userMessage,
                isThinking = true,
            )

            val convId = _uiState.value.currentConversationId
                ?: repository.createConversation().also {
                    _uiState.value = _uiState.value.copy(currentConversationId = it)
                }

            try {
                val userSkills = skillGraphRepository.getUserSkills()
                val userInterests = userSkills.map { it.name }
                val userProfile = com.nexus.domain.model.UserProfile(
                    id = userPreferences.userId.first(),
                    firstName = userPreferences.displayName.first(),
                    email = userPreferences.email.first(),
                    skills = userSkills.toImmutableList(),
                    interests = userInterests.toImmutableList(),
                )

                val historyMessages = _uiState.value.messages.filter { it.id != "ai_0" }.map { msg ->
                    AIMessage(
                        id = msg.id,
                        role = if (msg.isUser) AiMessageRole.USER else AiMessageRole.ASSISTANT,
                        content = msg.text,
                        timestamp = Instant.now(),
                    )
                }
                val contextMessages = memoryManager.buildContext(historyMessages)
var fullResponse = ""
nexusAIService.streamChat(
message = text,
history = contextMessages,
userProfile = userProfile,
).collect { chunk ->
fullResponse += chunk
_uiState.update { it.copy(streamingText = fullResponse) }
}
val convIdForMemory = _uiState.value.currentConversationId ?: convId
careerMemoryManager.extractAndStoreMemories(
aiResponse = fullResponse,
userMessage = text,
conversationId = convIdForMemory,
tool = "chat",
)

                val validated = aiResponseValidator.validate(fullResponse)
                val aiMessage = ChatMessage(
                    id = "ai_${System.currentTimeMillis()}",
                    text = validated.content,
                    isUser = false,
                    warnings = validated.warnings,
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + aiMessage,
                    isThinking = false,
                    streamingText = null,
                )

                if (validated.highestSeverity == WarningSeverity.HIGH) {
                    analyticsTracker.track("ai_safety_warning", mapOf(
                        "type" to (validated.warnings.firstOrNull { it.severity == WarningSeverity.HIGH }?.type?.name ?: "unknown"),
                        "response_length" to fullResponse.length,
                    ))
                }

                rateLimiter.recordCall()

                repository.addMessage(convId, AIMessage(
                    id = userMessage.id, role = AiMessageRole.USER,
                    content = userMessage.text, timestamp = Instant.now(),
                ))
                repository.addMessage(convId, AIMessage(
                    id = aiMessage.id, role = AiMessageRole.ASSISTANT,
                    content = aiMessage.text, timestamp = Instant.now(),
                ))

                analyticsTracker.trackResearchEvent(
                    ResearchEvent.AIResponseReceived,
                    mapOf("response_length" to fullResponse.length, "had_warnings" to validated.warnings.isNotEmpty())
                )
            } catch (e: Exception) {
                Timber.e(e, "AI chat failed")
                _uiState.update { it.copy(
                    isThinking = false,
                    streamingText = null,
                    messages = it.messages + ChatMessage(
                        id = "ai_err_${System.currentTimeMillis()}",
                        text = "I'm having trouble connecting right now. Please check your internet connection and try again.",
                        isUser = false,
                        isError = true,
                    )
                )}
            }
        }
    }
}
