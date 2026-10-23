package com.nexus.domain.usecase

import com.nexus.domain.model.AIMessage
import com.nexus.domain.model.AiMessageRole
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationMemoryManager @Inject constructor() {
    fun buildContext(
        messages: List<AIMessage>,
        maxMessages: Int = 20,
        summaryMessageCount: Int = 10,
    ): List<AIMessage> {
        if (messages.size <= maxMessages) return messages
        val oldMessages = messages.dropLast(summaryMessageCount)
        val recentMessages = messages.takeLast(summaryMessageCount)
        val summary = summarizeOldMessages(oldMessages)
        return listOf(
            AIMessage(id = "summary", role = AiMessageRole.SYSTEM, content = summary)
        ) + recentMessages
    }

    private fun summarizeOldMessages(messages: List<AIMessage>): String {
        val userCount = messages.count { it.role == AiMessageRole.USER }
        val assistantCount = messages.count { it.role == AiMessageRole.ASSISTANT }
        val topics = messages.filter { it.role == AiMessageRole.USER }
            .map { it.content.take(50) }
            .take(5)
        return "Previous conversation: $userCount user messages, $assistantCount AI responses. " +
               "Topics discussed: ${topics.joinToString("; ")}."
    }
}
