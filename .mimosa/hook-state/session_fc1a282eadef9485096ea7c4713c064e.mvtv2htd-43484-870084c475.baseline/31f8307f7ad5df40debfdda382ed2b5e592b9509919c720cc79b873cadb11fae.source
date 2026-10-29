package com.nexus.network.ai

import com.nexus.network.service.AIRequestRouter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIOpenRouterService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val aiRequestRouter: AIRequestRouter,
) {

    private val jsonMediaType = "application/json".toMediaType()

    suspend fun sendMessage(
        messages: List<ChatMessage>,
        onStream: ((String) -> Unit)? = null,
    ): String = withContext(Dispatchers.IO) {
        if (aiRequestRouter.useProxy) {
            val result = aiRequestRouter.chat(messages.map { mapOf("role" to it.role, "content" to it.content) })
            return@withContext result.getOrThrow()
        }
        val jsonBody = JSONObject().apply {
            put("model", "openai/gpt-4o-mini")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "You are NEXUS AI, a career assistant for CSE students. Help with internships, certifications, DSA, career advice, skill development, and interview prep. Be concise, practical, and encouraging.")
                })
                messages.forEach { msg ->
                    put(JSONObject().apply {
                        put("role", msg.role)
                        put("content", msg.content)
                    })
                }
            })
            put("temperature", 0.7)
            put("max_tokens", 1024)
            put("stream", false)
        }

        if (onStream != null) {
            @Suppress("DEPRECATION")
            Timber.w("Streaming via onStream parameter is not supported in sendMessage(). Use NexusAIService.streamChat() instead.")
        }

        val request = Request.Builder()
            .url(aiRequestRouter.getEndpoint())
            .addHeader("Authorization", aiRequestRouter.buildAuthHeader())
            .addHeader("HTTP-Referer", "https://nexus.app")
            .addHeader("X-Title", "NEXUS Career App")
            .post(jsonBody.toString().toRequestBody(jsonMediaType))
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                throw AIServiceException(
                    message = "AI service temporarily unavailable: HTTP ${response.code}",
                    isRetryable = response.code >= 500,
                )
            }
            val body = response.body?.string() ?: ""
            val json = JSONObject(body)
            val choices = json.getJSONArray("choices")
            if (choices.length() > 0) {
                val message = choices.getJSONObject(0).getJSONObject("message")
                message.getString("content")
            } else {
                throw AIServiceException("AI service returned an empty response", isRetryable = true)
            }
        } catch (e: Exception) {
            if (e is AIServiceException) throw e
            throw AIServiceException(
                message = "AI service temporarily unavailable",
                cause = e,
                isRetryable = true,
            )
        }
    }

    suspend fun sendMessage(prompt: String): String {
        return sendMessage(listOf(ChatMessage("user", prompt)))
    }

    data class ChatMessage(val role: String, val content: String)
}

class AIServiceException(
    message: String,
    cause: Throwable? = null,
    val isRetryable: Boolean = false,
) : Exception(message, cause)
