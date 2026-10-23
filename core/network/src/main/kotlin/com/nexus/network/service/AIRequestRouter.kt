package com.nexus.network.service

import com.nexus.network.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRequestRouter @Inject constructor(
  private val firebaseAuth: FirebaseAuth,
  private val functionsService: FirebaseFunctionsAIService,
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
      return functionsService.proxyAI(
        AIProxyRequest(
          messages = messages.map { Message(role = it["role"] ?: "user", content = it["content"] ?: "") },
        )
      ).map { it.content }
    }
    return Result.failure(IllegalStateException("Direct AI calls not supported through router — use AIOpenRouterService in debug"))
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
