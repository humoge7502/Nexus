package com.nexus.domain.repository

import com.nexus.common.ApiResult
import com.nexus.domain.model.AIConversation
import com.nexus.domain.model.AIMessage
import kotlinx.coroutines.flow.Flow

interface IAIConversationRepository {
    fun getAllConversations(): Flow<List<AIConversation>>
    suspend fun getConversation(id: String): AIConversation?
    suspend fun createConversation(title: String = "New Chat"): String
    suspend fun deleteConversation(id: String)
    suspend fun sendMessage(conversationId: String, message: String): ApiResult<String>
    suspend fun addMessage(conversationId: String, message: AIMessage)
}
