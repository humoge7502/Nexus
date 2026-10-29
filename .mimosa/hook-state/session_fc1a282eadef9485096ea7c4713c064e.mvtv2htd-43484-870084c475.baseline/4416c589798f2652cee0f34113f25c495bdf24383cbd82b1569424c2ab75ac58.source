package com.nexus.network.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFunctionsAIService @Inject constructor(
    private val functions: FirebaseFunctions,
    private val firebaseAuth: FirebaseAuth,
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
            ?: throw IllegalStateException("User must be authenticated for AI proxy")

        val result = functions
            .getHttpsCallable("proxyAI")
            .call(request)
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
        val uid = firebaseAuth.currentUser?.uid
            ?: throw IllegalStateException("Sign in to use Deep Research")
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

