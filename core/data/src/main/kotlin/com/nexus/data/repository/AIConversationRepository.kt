package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.database.dao.AIDao
import com.nexus.database.entity.AIConversationEntity
import com.nexus.domain.model.AIConversation
import com.nexus.domain.model.AIMessage
import com.nexus.domain.model.AiMessageRole
import com.nexus.network.api.NexusApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIConversationRepository @Inject constructor(
    private val api: NexusApiService,
    private val dao: AIDao,
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun getAllConversations(): Flow<List<AIConversation>> =
        dao.getAllConversations().map { entities -> entities.map { it.toDomain() } }

    suspend fun getConversation(id: String): AIConversation? =
        dao.getConversationById(id)?.toDomain()

    suspend fun createConversation(title: String = "New Chat"): String {
        val id = "conv_${System.currentTimeMillis()}"
        dao.insertConversation(AIConversationEntity(id = id, title = title))
        return id
    }

    suspend fun deleteConversation(id: String) = dao.deleteConversation(id)

    suspend fun sendMessage(conversationId: String, message: String): ApiResult<String> {
        return try {
            val response = api.sendChatMessage(
                com.nexus.network.dto.ChatRequestDto(
                    conversationId = conversationId, message = message,
                )
            )
            if (response.isSuccessful) {
                val reply = response.body()?.reply ?: "I'm here to help!"
                val userMsg = AIMessage(
                    id = "m_${System.currentTimeMillis()}", role = AiMessageRole.USER,
                    content = message, timestamp = Instant.now(),
                )
                val aiMsg = AIMessage(
                    id = "m_${System.currentTimeMillis() + 1}", role = AiMessageRole.ASSISTANT,
                    content = reply, timestamp = Instant.now(),
                )
                addMessage(conversationId, userMsg)
                addMessage(conversationId, aiMsg)
                ApiResult.Success(reply)
            } else {
                ApiResult.Error("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun addMessage(conversationId: String, message: AIMessage) {
        val conv = dao.getConversationById(conversationId) ?: return
        val messages = if (conv.messages.isBlank()) emptyList()
        else json.decodeFromString<List<SerializableMessage>>(conv.messages)
        val updatedMessages = messages + SerializableMessage(
            message.id, message.role.name, message.content, message.timestamp.toEpochMilli()
        )
        dao.updateMessages(conversationId, json.encodeToString(updatedMessages))
    }

    private fun AIConversationEntity.toDomain() = AIConversation(
        id = id, title = title,
        messages = if (messages.isNotBlank()) {
            json.decodeFromString<List<SerializableMessage>>(messages).map { msg ->
                AIMessage(id = msg.id,
                    role = try { AiMessageRole.valueOf(msg.role) } catch (_: Exception) { AiMessageRole.USER },
                    content = msg.content, timestamp = Instant.ofEpochMilli(msg.timestamp),
                )
            }.toImmutableList()
        } else persistentListOf(),
        createdAt = Instant.ofEpochMilli(createdAt),
        lastMessageAt = Instant.ofEpochMilli(lastMessageAt),
    )

    @kotlinx.serialization.Serializable
    data class SerializableMessage(
        val id: String, val role: String, val content: String, val timestamp: Long,
    )
}
