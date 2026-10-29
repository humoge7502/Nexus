package com.nexus.network.service

import com.google.firebase.auth.FirebaseAuth
import com.nexus.network.BuildConfig
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

sealed interface AIStreamEvent {
    data class Delta(val text: String) : AIStreamEvent
    data class Completed(val totalTokens: Int) : AIStreamEvent
    data class Failed(val message: String, val httpStatus: Int = 0) : AIStreamEvent
}

/**
 * SSE client for the `streamAI` Cloud Function. The upstream OpenRouter key
 * stays server-side; this client authenticates with the Firebase ID token and
 * surfaces incremental deltas so the chat UI renders tokens as they arrive.
 */
@Singleton
class AIStreamClient @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    baseClient: OkHttpClient,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    private val jsonMediaType = "application/json".toMediaType()

    // SSE responses can idle between events far longer than a normal API call;
    // the server bounds the stream (120s) and cancels upstream itself.
    private val streamingClient = baseClient.newBuilder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun stream(
        messages: List<Map<String, String>>,
        model: String = DEFAULT_MODEL,
        maxTokens: Int = DEFAULT_MAX_TOKENS,
    ): Flow<AIStreamEvent> {
        val token = try {
            firebaseAuth.currentUser?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            Timber.w(e, "Could not fetch an ID token for AI streaming")
            null
        }
        if (token.isNullOrBlank()) {
            return flowOf(AIStreamEvent.Failed("Sign in required to use NEXUS AI.", 401))
        }
        return streamWithToken(token, messages, streamUrl(), model, maxTokens)
    }

    internal fun streamWithToken(
        token: String,
        messages: List<Map<String, String>>,
        baseUrl: String,
        model: String = DEFAULT_MODEL,
        maxTokens: Int = DEFAULT_MAX_TOKENS,
    ): Flow<AIStreamEvent> = callbackFlow {
        val payload = buildJsonObject {
            put("messages", buildJsonArray {
                messages.forEach { message ->
                    add(buildJsonObject {
                        put("role", message["role"] ?: "user")
                        put("content", message["content"] ?: "")
                    })
                }
            })
            put("model", model)
            put("max_tokens", maxTokens)
        }

        val request = Request.Builder()
            .url(baseUrl)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "text/event-stream")
            .post(payload.toString().toRequestBody(jsonMediaType))
            .build()

        val call = streamingClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (call.isCanceled()) return
                Timber.w(e, "AI stream connection failed")
                trySend(AIStreamEvent.Failed("Could not reach NEXUS AI. Check your connection and try again."))
                close()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { resp ->
                    if (!resp.isSuccessful) {
                        trySend(AIStreamEvent.Failed(errorMessage(resp), resp.code))
                        close()
                        return
                    }
                    val body = resp.body
                    if (body == null) {
                        trySend(AIStreamEvent.Failed("AI stream returned no content.", resp.code))
                        close()
                        return
                    }
                    try {
                        var totalTokens = 0
                        val source = body.source()
                        while (true) {
                            val line = source.readUtf8Line() ?: break
                            if (line.startsWith("data:")) {
                                when (val chunk = parseSseData(line.removePrefix("data:"))) {
                                    is StreamChunk.Delta -> trySend(AIStreamEvent.Delta(chunk.text))
                                    is StreamChunk.Usage -> totalTokens = chunk.totalTokens
                                    is StreamChunk.Error -> {
                                        trySend(AIStreamEvent.Failed(chunk.message, resp.code))
                                        close()
                                        return
                                    }
                                    StreamChunk.Done -> {
                                        trySend(AIStreamEvent.Completed(totalTokens))
                                        close()
                                        return
                                    }
                                    StreamChunk.Ignored -> Unit
                                }
                            }
                        }
                        trySend(AIStreamEvent.Completed(totalTokens))
                        close()
                    } catch (e: IOException) {
                        if (!call.isCanceled()) {
                            Timber.w(e, "AI stream interrupted")
                            trySend(AIStreamEvent.Failed("AI stream interrupted. Please try again."))
                        }
                        close()
                    }
                }
            }
        })

        awaitClose { call.cancel() }
    }.buffer(Channel.UNLIMITED)

    internal fun streamUrl(
        proxyUrl: String = BuildConfig.AI_PROXY_URL,
        projectId: String? = firebaseAuth.app.options.projectId,
    ): String {
        val configuredProxy = proxyUrl.trim().trimEnd('/')
        if (configuredProxy.endsWith("/$PROXY_FUNCTION")) {
            return configuredProxy.removeSuffix("/$PROXY_FUNCTION") + "/$STREAM_FUNCTION"
        }
        requireNotNull(projectId) { "Firebase project is not configured" }
        return "https://us-central1-$projectId.cloudfunctions.net/$STREAM_FUNCTION"
    }

    internal fun parseSseData(raw: String): StreamChunk {
        val data = raw.trim()
        if (data.isEmpty()) return StreamChunk.Ignored
        if (data == "[DONE]") return StreamChunk.Done
        val obj = try {
            json.parseToJsonElement(data).jsonObject
        } catch (_: Exception) {
            return StreamChunk.Ignored
        }
        obj["error"]?.let { error ->
            val message = try {
                error.jsonObject["message"]?.jsonPrimitive?.contentOrNull
            } catch (_: Exception) {
                null
            }
            return StreamChunk.Error(message ?: "AI stream failed.")
        }
        val delta = try {
            obj["choices"]?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("delta")?.jsonObject
                ?.get("content")?.jsonPrimitive?.contentOrNull
        } catch (_: Exception) {
            null
        }
        if (!delta.isNullOrEmpty()) return StreamChunk.Delta(delta)
        val usage = try {
            obj["usage"]?.jsonObject?.get("total_tokens")?.jsonPrimitive?.intOrNull
        } catch (_: Exception) {
            null
        }
        return if (usage != null) StreamChunk.Usage(usage) else StreamChunk.Ignored
    }

    internal fun errorMessage(response: Response): String {
        val serverMessage = try {
            response.body?.string()
                ?.let { json.parseToJsonElement(it).jsonObject }
                ?.get("error")?.jsonObject
                ?.get("message")?.jsonPrimitive?.contentOrNull
        } catch (_: Exception) {
            null
        }
        if (!serverMessage.isNullOrBlank()) return serverMessage
        return when (response.code) {
            401 -> "Sign in again to keep using NEXUS AI."
            429 -> "Daily AI limit reached. Try again tomorrow."
            503 -> "NEXUS AI is not available right now. Try again later."
            else -> "AI request failed (${response.code}). Please try again."
        }
    }

    internal sealed interface StreamChunk {
        data class Delta(val text: String) : StreamChunk
        data class Usage(val totalTokens: Int) : StreamChunk
        data class Error(val message: String) : StreamChunk
        data object Done : StreamChunk
        data object Ignored : StreamChunk
    }

    companion object {
        const val DEFAULT_MODEL = "openai/gpt-4o-mini"
        const val DEFAULT_MAX_TOKENS = 1024
        const val PROXY_FUNCTION = "proxyAI"
        const val STREAM_FUNCTION = "streamAI"
    }
}
