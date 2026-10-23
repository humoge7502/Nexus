package com.nexus.ai

import com.nexus.domain.model.AIMessage
import com.nexus.domain.model.UserProfile
import com.nexus.domain.usecase.ConversationMemoryManager
import com.nexus.data.usecase.CareerMemoryManager
import com.nexus.network.service.AIRequestRouter
import com.nexus.network.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSource
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NexusAIService @Inject constructor(
    private val conversationMemoryManager: ConversationMemoryManager,
    private val systemPromptBuilder: SystemPromptBuilder,
    private val okHttpClient: OkHttpClient,
    private val careerMemoryManager: CareerMemoryManager,
    private val aiRequestRouter: AIRequestRouter,
) {
    private val jsonMediaType = "application/json".toMediaType()

    init {
        if (!BuildConfig.DEBUG && BuildConfig.OPENROUTER_API_KEY.isNotBlank()) {
            Timber.e(
                "SECURITY: OpenRouter API key present in release build. " +
                "This key is exposed in the APK. " +
                "Ensure release builds use the Firebase Functions proxy. " +
                "The app will refuse to make direct API calls."
            )
        }
    }

    suspend fun streamChat(
        message: String,
        history: List<AIMessage>,
        userProfile: UserProfile? = null,
    ): Flow<String> = flow {
        if (!BuildConfig.DEBUG) {
            if (!aiRequestRouter.useProxy) {
                emit("AI features are not available in release mode without a configured backend proxy.")
                return@flow
            }
            if (BuildConfig.OPENROUTER_API_KEY.isNotBlank()) {
                emit("AI features are temporarily unavailable. Please ensure the backend proxy is configured.")
                return@flow
            }
        }

        val contextMessages = conversationMemoryManager.buildContext(history)
        val systemPrompt = userProfile?.let {
            systemPromptBuilder.buildSystemPrompt(it)
        } ?: "You are NEXUS AI, a career assistant for CSE students. Help with internships, certifications, DSA, career advice, skill development, and interview prep. Be concise, practical, and encouraging."
        val messages = buildList {
            add(ChatMessage("system", systemPrompt))
            addAll(contextMessages.map { ChatMessage(it.role.name.lowercase(), it.content) })
            add(ChatMessage("user", message))
        }
        val jsonBody = JSONObject().apply {
            put("model", "openai/gpt-4o-mini")
            put("messages", JSONArray().apply {
                messages.forEach { m ->
                    put(JSONObject().apply { put("role", m.role); put("content", m.content) })
                }
            })
            put("temperature", 0.7); put("max_tokens", 1024); put("stream", true)
        }
        val request = Request.Builder()
            .url(aiRequestRouter.getEndpoint())
            .addHeader("Authorization", aiRequestRouter.buildAuthHeader())
            .addHeader("HTTP-Referer", "https://nexus.app")
            .addHeader("X-Title", "NEXUS Career App")
            .post(jsonBody.toString().toRequestBody(jsonMediaType))
            .build()

        val response = withContext(Dispatchers.IO) { okHttpClient.newCall(request).execute() }
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: ""
            val errorMessage = try {
                JSONObject(errorBody).optString("error", errorBody)
            } catch (_: Exception) { errorBody }
            emit("I'm having trouble connecting: $errorMessage")
            return@flow
        }

        val body = response.body ?: run {
            emit("I'm having trouble connecting right now. Please try again.")
            return@flow
        }

        val source = body.source()
        val buffer = okio.Buffer()
        var partial = ""
        try {
            while (!source.exhausted()) {
                source.read(buffer, 2048)
                partial += buffer.readUtf8()
                val lines = partial.split("\n")
                val completeLines = if (lines.lastOrNull()?.startsWith("data: ") == false) {
                    partial = lines.last()
                    lines.dropLast(1)
                } else {
                    lines
                }
                for (line in completeLines) {
                    val trimmed = line.trim()
                    if (!trimmed.startsWith("data: ")) continue
                    val data = trimmed.removePrefix("data: ").trim()
                    if (data == "[DONE]") continue
                    try {
                        val json = JSONObject(data)
                        val content = json.getJSONArray("choices")
                            .getJSONObject(0)
                            .optJSONObject("delta")
                            ?.optString("content", "") ?: ""
                        if (content.isNotEmpty()) emit(content)
                    } catch (e: Exception) { Timber.w(e, "Failed to parse stream chunk") }
                }
            }
        } finally {
            source.close()
        }
    }.flowOn(Dispatchers.IO)

    data class ChatMessage(val role: String, val content: String)
}
