package com.nexus.network.service

import com.nexus.network.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRequestRouter @Inject constructor(
  private val firebaseAuth: FirebaseAuth,
  private val functionsService: FirebaseFunctionsAIService,
  private val okHttpClient: OkHttpClient,
) {

  companion object {
    const val DIRECT_URL = "https://openrouter.ai/api/v1/chat/completions"
  }

  val useProxy: Boolean
    get() = if (BuildConfig.DEBUG) {
      BuildConfig.AI_PROXY_URL.isNotBlank()
    } else {
      true
    }

  suspend fun chat(
    messages: List<Map<String, String>>,
  ): Result<String> {
    if (useProxy) {
      // The raw-HTTP proxy (sagitta-ai Worker) is preferred once AI_PROXY_URL
      // is set; the callable path remains the fallback until Blaze exists.
      if (BuildConfig.AI_PROXY_URL.isNotBlank()) {
        return httpChat(messages)
      }
      return functionsService.proxyAI(
        AIProxyRequest(
          messages = messages.map { Message(role = it["role"] ?: "user", content = it["content"] ?: "") },
        )
      ).map { it.content }
    }
    return Result.failure(IllegalStateException("Direct AI calls not supported through router — use AIOpenRouterService in debug"))
  }

  /** Non-streaming call against the raw-HTTP proxy at AI_PROXY_URL. */
  private suspend fun httpChat(
    messages: List<Map<String, String>>,
  ): Result<String> = withContext(Dispatchers.IO) {
    runCatching {
      val payload = JSONObject().apply {
        put(
          "messages",
          JSONArray(messages.map { m ->
            JSONObject().apply {
              put("role", m["role"] ?: "user")
              put("content", m["content"] ?: "")
            }
          })
        )
        put("max_tokens", 1024)
      }
      val request = Request.Builder()
        .url(getEndpoint())
        .addHeader("Authorization", buildAuthHeader())
        .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
        .build()
      okHttpClient.newCall(request).execute().use { response ->
        val bodyText = response.body?.string().orEmpty()
        if (!response.isSuccessful) {
          val serverMessage = runCatching {
            JSONObject(bodyText).getJSONObject("error").optString("message")
          }.getOrNull().orEmpty()
          if (response.code == 401) {
            throw RequiresSignInException(serverMessage.ifBlank { "Sign in to use the AI assistant." })
          }
          throw IllegalStateException(
            serverMessage.ifBlank { "AI request failed (${response.code}). Please try again." }
          )
        }
        val content = JSONObject(bodyText).optString("content", "")
        if (content.isBlank()) {
          throw IllegalStateException("AI service returned an empty response")
        }
        content
      }
    }
  }

  suspend fun buildAuthHeader(): String {
    return if (useProxy) {
      val token = firebaseAuth.currentUser?.getIdToken(false)?.await()?.token
        ?: throw IllegalStateException("User not authenticated for proxy")
      "Bearer $token"
    } else {
      if (!BuildConfig.DEBUG) {
        Timber.e("SECURITY: OpenRouter API key accessed in release build — this should never happen")
      }
      "Bearer ${BuildConfig.OPENROUTER_API_KEY}"
    }
  }

  fun getEndpoint(): String {
    return if (useProxy) {
      if (BuildConfig.AI_PROXY_URL.isBlank()) {
        Timber.w("AI proxy enabled but PROXY_URL is blank — AI calls will fail. Deploy backend/functions.")
      }
      BuildConfig.AI_PROXY_URL
    } else {
      DIRECT_URL
    }
  }
}
