package com.nexus.network.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.nexus.network.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFunctionsAIService @Inject constructor(
    private val functions: FirebaseFunctions,
    private val firebaseAuth: FirebaseAuth,
    private val okHttpClient: OkHttpClient,
) {
    // Timeout is configured server-side in Firebase Console (default 60s).
    // All public methods use runCatching to safely surface network errors,
    // timeouts, and invalid responses as kotlin.Result. Callers should
    // inspect the Result for success/failure.

    suspend fun healthCheck(): Result<HealthResult> = runCatching {
        val result = functions
            .getHttpsCallable("health")
            .call()
            .await()
        @Suppress("UNCHECKED_CAST")
        (result.getData() as? Map<String, Any?>)?.let { data ->
            HealthResult(
                status = data["status"] as? String ?: "unknown",
                timestamp = (data["timestamp"] as? Number)?.toLong() ?: 0L,
                version = data["version"] as? String ?: "",
            )
        } ?: throw IllegalStateException("Unexpected health check response")
    }

    suspend fun proxyAI(request: AIProxyRequest): Result<AIProxyResult> = runCatching {
        val uid = firebaseAuth.currentUser?.uid
            ?: throw RequiresSignInException("User must be authenticated for AI proxy")

        // The backend reads snake_case (max_tokens). Sending the Kotlin data
        // class directly serialises `maxTokens`, which the server ignores and
        // silently resets to its default token budget. Build the payload
        // explicitly so the wire shape is exact.
        val payload = mutableMapOf<String, Any?>(
            "messages" to request.messages.map { mapOf("role" to it.role, "content" to it.content) }
        ).apply {
            request.model?.let { put("model", it) }
            request.maxTokens?.let { put("max_tokens", it) }
            request.temperature?.let { put("temperature", it) }
        }
        val result = functions
            .getHttpsCallable("proxyAI")
            .call(payload)
            .await()
        @Suppress("UNCHECKED_CAST")
        (result.getData() as? Map<String, Any?>)?.let { data ->
            AIProxyResult(
                id = data["id"] as? String ?: "",
                content = data["content"] as? String ?: "",
                finishReason = data["finishReason"] as? String ?: "stop",
                usage = (data["usage"] as? Map<String, Any?>)?.let { u ->
                    Usage(
                        promptTokens = (u["promptTokens"] as? Number)?.toInt() ?: 0,
                        completionTokens = (u["completionTokens"] as? Number)?.toInt() ?: 0,
                        totalTokens = (u["totalTokens"] as? Number)?.toInt() ?: 0,
                    )
                },
            )
        } ?: throw IllegalStateException("Unexpected proxy AI response")
    }

    suspend fun getFeed(request: FeedRequest): Result<FeedResult> = runCatching {
        firebaseAuth.currentUser?.uid
            ?: throw RequiresSignInException("Sign in required to load the opportunity feed")
        val result = functions
            .getHttpsCallable("getOpportunityFeed")
            .call(request)
            .await()
        @Suppress("UNCHECKED_CAST")
        (result.getData() as? Map<String, Any?>)?.let { data ->
            FeedResult(
                results = (data["results"] as? List<Map<String, Any?>>) ?: emptyList(),
                total = (data["total"] as? Number)?.toInt() ?: 0,
                offset = (data["offset"] as? Number)?.toInt() ?: 0,
            )
        } ?: throw IllegalStateException("Unexpected feed response")
    }

    suspend fun getOutcomes(request: OutcomesRequest): Result<OutcomesResult> = runCatching {
        firebaseAuth.currentUser?.uid
            ?: throw RequiresSignInException("Sign in required to load interview outcomes")
        val result = functions
            .getHttpsCallable("getIntelligenceOutcomes")
            .call(request)
            .await()
        @Suppress("UNCHECKED_CAST")
        (result.getData() as? Map<String, Any?>)?.let { data ->
            OutcomesResult(
                results = (data["results"] as? List<Map<String, Any?>>) ?: emptyList(),
                total = (data["total"] as? Number)?.toInt() ?: 0,
            )
        } ?: throw IllegalStateException("Unexpected outcomes response")
    }

    suspend fun getCohortIntelligence(request: CohortRequest): Result<CohortResult> = runCatching {
        firebaseAuth.currentUser?.uid
            ?: throw RequiresSignInException("Sign in required to load cohort intelligence")
        val result = functions
            .getHttpsCallable("getCohortIntelligence")
            .call(request)
            .await()
        @Suppress("UNCHECKED_CAST")
        (result.getData() as? Map<String, Any?>)?.let { data ->
            CohortResult(
                results = (data["results"] as? List<Map<String, Any?>>) ?: emptyList(),
                total = (data["total"] as? Number)?.toInt() ?: 0,
            )
        } ?: throw IllegalStateException("Unexpected cohort response")
    }

    suspend fun deepResearch(
        question: String,
        depth: String = "standard",
    ): Result<ResearchResult> = runCatching {
        val proxyBase = proxyEndpointBase()
        if (proxyBase != null) {
            return@runCatching deepResearchHttp(question, depth, proxyBase)
        }
        val uid = firebaseAuth.currentUser?.uid
            ?: throw RequiresSignInException("Sign in to use Deep Research")
        val payload = mapOf("question" to question, "depth" to depth)
        val result = functions
            .getHttpsCallable("deepResearchFn")
            .call(payload)
            .await()
        @Suppress("UNCHECKED_CAST")
        val data = result.getData() as? Map<String, Any?>
            ?: throw IllegalStateException("Empty research response")
        fun str(v: Any?): String = v?.toString() ?: ""
        ResearchResult(
            question = str(data["question"]),
            answer = str(data["answer"]),
            sources = (data["sources"] as? List<Map<String, Any?>>)?.map { s ->
                ResearchSource(
                    title = str(s["title"]),
                    url = s["url"]?.toString(),
                    snippet = s["snippet"]?.toString(),
                )
            } ?: emptyList(),
            followUps = (data["followUps"] as? List<Any?>)?.map { str(it) } ?: emptyList(),
            usedLiveSearch = data["usedLiveSearch"] == true,
        )
    }

    /**
     * Raw-HTTP proxy base derived from AI_PROXY_URL (which ends in "/proxyAI"),
     * or null when unset — mirroring AIStreamClient.streamUrl()'s convention.
     */
    private fun proxyEndpointBase(): String? {
        val configured = BuildConfig.AI_PROXY_URL.trim().trimEnd('/')
        if (configured.isBlank()) return null
        return if (configured.endsWith("/proxyAI")) {
            configured.removeSuffix("/proxyAI")
        } else {
            configured
        }
    }

    private suspend fun deepResearchHttp(
        question: String,
        depth: String,
        base: String,
    ): ResearchResult = withContext(Dispatchers.IO) {
        val token = firebaseAuth.currentUser?.getIdToken(false)?.await()?.token
            ?: throw RequiresSignInException("Sign in to use Deep Research")
        val payload = JSONObject().apply {
            put("question", question)
            put("depth", depth)
        }
        val request = Request.Builder()
            .url("$base/deepResearchFn")
            .addHeader("Authorization", "Bearer $token")
            .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()
        okHttpClient.newCall(request).execute().use { response ->
            val bodyText = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val serverMessage = runCatching {
                    JSONObject(bodyText).getJSONObject("error").optString("message")
                }.getOrNull().orEmpty()
                if (response.code == 401) {
                    throw RequiresSignInException(serverMessage.ifBlank { "Sign in to use Deep Research" })
                }
                throw IllegalStateException(serverMessage.ifBlank { "Research failed. Try again." })
            }
            val data = JSONObject(bodyText)
            fun str(v: Any?): String = v?.toString() ?: ""
            val sources = mutableListOf<ResearchSource>()
            val sourcesArray = data.optJSONArray("sources")
            for (i in 0 until (sourcesArray?.length() ?: 0)) {
                val obj = sourcesArray?.optJSONObject(i) ?: JSONObject()
                sources.add(
                    ResearchSource(
                        title = str(obj.opt("title")),
                        url = obj.optString("url", "").ifBlank { null },
                        snippet = obj.optString("snippet", "").ifBlank { null },
                    )
                )
            }
            val followUps = mutableListOf<String>()
            val followUpsArray = data.optJSONArray("followUps")
            for (i in 0 until (followUpsArray?.length() ?: 0)) {
                followUps.add(str(followUpsArray?.opt(i)))
            }
            ResearchResult(
                question = str(data.opt("question")),
                answer = str(data.opt("answer")),
                sources = sources,
                followUps = followUps,
                usedLiveSearch = data.optBoolean("usedLiveSearch", false),
            )
        }
    }

}

data class HealthResult(
    val status: String,
    val timestamp: Long,
    val version: String,
)

data class AIProxyRequest(
    val model: String? = null,
    val messages: List<Message>,
    val maxTokens: Int? = null,
    val temperature: Double? = null,
)

data class Message(
    val role: String,
    val content: String,
)

data class AIProxyResult(
    val id: String,
    val content: String,
    val finishReason: String,
    val usage: Usage? = null,
)

data class Usage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
)

data class FeedRequest(
    val opportunityType: String = "all",
    val limit: Int = 20,
    val offset: Int = 0,
    val skills: List<String>? = null,
)

data class FeedResult(
    val results: List<Map<String, Any?>>,
    val total: Int,
    val offset: Int,
)

data class OutcomesRequest(
    val company: String? = null,
    val limit: Int = 20,
)

data class OutcomesResult(
    val results: List<Map<String, Any?>>,
    val total: Int,
)

data class CohortRequest(
    val collegeId: String? = null,
    val companyTier: String? = null,
    val semester: String? = null,
)

data class CohortResult(
    val results: List<Map<String, Any?>>,
    val total: Int,
)

// ---- Deep Research (agent-search pattern: question -> cited synthesis -> follow-ups) ----

data class ResearchSource(
    val title: String,
    val url: String? = null,
    val snippet: String? = null,
)

data class ResearchResult(
    val question: String = "",
    val answer: String = "",
    val sources: List<ResearchSource> = emptyList(),
    val followUps: List<String> = emptyList(),
    val usedLiveSearch: Boolean = false,
)

/**
 * The call was never sent, because this device holds no signed-in user.
 *
 * A transport failure and a missing local precondition read the same way if both
 * surface as a generic exception, and the screen then offers Retry on the one
 * thing Retry cannot fix. Students who tap "Skip" on the sign-in screen land here.
 */
class RequiresSignInException(message: String) : Exception(message)

