package com.nexus.network.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.nexus.network.api.NexusApiService
import com.nexus.network.dto.EmbeddingConfigDto
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt
import timber.log.Timber

@Singleton
class EmbeddingService @Inject constructor(
    private val api: NexusApiService
) {
    companion object {
        const val MODEL_VERSION = "nexus-tfidf-v1"
        const val EMBEDDING_DIM = 128
    }

    private var semanticGroups: Map<Set<String>, Float> = emptyMap()
    private var idfWeights: Map<String, Float> = emptyMap()
    private var defaultIdf = 1.0f
    private val configLoaded = java.util.concurrent.atomic.AtomicBoolean(false)

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        // Load default config immediately (synchronous fallback). Remote config
        // is fetched lazily on first use — launching network I/O from a singleton
        // constructor hangs test JVMs and violates DI construction discipline.
        loadDefaultConfig()
    }

    private fun ensureRemoteConfig() {
        if (configLoaded.compareAndSet(false, true)) {
            serviceScope.launch { loadRemoteConfig() }
        }
    }

    private fun loadDefaultConfig() {
        // Dimension and model version are fixed for the lifetime of the app so
        // embeddings persisted under an older configuration always decode.
        semanticGroups = mapOf(
            setOf(
                "kotlin", "android", "java", "python", "javascript", "typescript",
                "react", "angular", "vue", "node", "django", "flask", "spring",
                "hibernate", "sql", "nosql", "mongodb", "redis", "docker",
                "kubernetes", "aws", "gcp", "azure", "git", "linux", "rest",
                "graphql", "api", "microservices", "devops", "machine",
                "learning", "deep", "neural", "data", "science", "blockchain",
                "solidity", "web3", "flutter", "swift", "ios", "react-native",
                "tensorflow", "pytorch", "scikit-learn", "pandas", "numpy",
            ) to 3.0f,
            setOf(
                "build", "develop", "design", "implement", "deploy", "optimize",
                "refactor", "debug", "test", "write", "create", "manage",
                "configure", "integrate", "migrate", "scale", "monitor",
                "automate", "ship", "launch",
            ) to 2.0f,
            setOf(
                "internship", "job", "career", "interview", "resume", "portfolio",
                "certification", "hackathon", "startup", "placement", "salary",
                "stipend", "offer", "role", "position", "opportunity",
                "application", "recruiter", "hiring", "fresher",
            ) to 1.8f,
            setOf(
                "algorithm", "data-structure", "array", "string", "graph", "tree",
                "dp", "dynamic-programming", "recursion", "sorting", "searching",
                "linked-list", "stack", "queue", "heap", "hash", "binary",
                "time-complexity", "space-complexity", "big-o",
            ) to 1.8f,
            setOf(
                "ui", "ux", "frontend", "backend", "fullstack", "mobile",
                "web", "cloud", "database", "server", "client", "sdk",
                "framework", "library", "module", "component", "service",
            ) to 1.5f
        )

        idfWeights = mapOf(
            "the" to 0.3f, "a" to 0.3f, "an" to 0.3f, "is" to 0.4f,
            "are" to 0.4f, "was" to 0.4f, "were" to 0.4f, "be" to 0.4f,
            "been" to 0.4f, "to" to 0.5f, "of" to 0.5f, "in" to 0.5f,
            "for" to 0.5f, "on" to 0.5f, "with" to 0.5f, "at" to 0.5f,
            "by" to 0.5f, "from" to 0.5f, "and" to 0.3f, "or" to 0.5f,
            "but" to 0.5f, "not" to 0.6f, "this" to 0.5f, "that" to 0.5f,
            "it" to 0.5f, "you" to 0.6f, "your" to 0.6f, "we" to 0.6f,
            "our" to 0.6f, "have" to 0.6f, "has" to 0.6f, "do" to 0.6f,
            "does" to 0.6f, "will" to 0.6f, "would" to 0.6f, "can" to 0.6f,
            "could" to 0.6f, "should" to 0.6f, "about" to 0.6f, "into" to 0.6f,
            "over" to 0.6f, "also" to 0.6f, "very" to 0.5f, "just" to 0.5f,
            "more" to 0.6f, "some" to 0.6f, "such" to 0.6f, "which" to 0.6f,
            "their" to 0.6f, "them" to 0.6f, "than" to 0.6f, "then" to 0.6f,
            "when" to 0.6f, "where" to 0.6f, "what" to 0.6f, "how" to 0.6f,
            "why" to 0.6f, "if" to 0.6f, "so" to 0.7f, "as" to 0.6f,
            "up" to 0.6f, "out" to 0.6f, "no" to 0.7f,
        )

        defaultIdf = 1.0f
    }

    private suspend fun loadRemoteConfig() {
        try {
            val response = api.getEmbeddingConfig()
            if (response.isSuccessful) {
                val config = response.body()
                if (config != null) {
                    if (config.embeddingDim != EMBEDDING_DIM || config.modelVersion != MODEL_VERSION) {
                        Timber.w(
                            "Ignoring incompatible embedding config (dim=%d, version=%s); using %d/%s",
                            config.embeddingDim,
                            config.modelVersion,
                            EMBEDDING_DIM,
                            MODEL_VERSION,
                        )
                        return
                    }
                    defaultIdf = config.defaultIdf

                    // Convert semantic groups from DTO format to internal format
                    val tempSemanticGroups = mutableMapOf<Set<String>, Float>()
                    for (groupDto in config.semanticGroups) {
                        val termSet = groupDto.terms.toSet()
                        tempSemanticGroups[termSet] = groupDto.weight
                    }
                    semanticGroups = tempSemanticGroups

                    // Convert IDF weights
                    idfWeights = config.idfWeights.toMap()

                    Timber.i("Embedding config loaded from API: dim=$EMBEDDING_DIM, version=$MODEL_VERSION")
                }
            } else {
                Timber.w("Failed to fetch embedding config from API, using defaults")
            }
        } catch (e: Exception) {
            Timber.w(e, "Error fetching embedding config, using defaults")
        }
    }

    suspend fun computeEmbedding(text: String): ByteArray = withContext(Dispatchers.Default) {
        ensureRemoteConfig()
        val tokens = tokenize(text)
        if (tokens.isEmpty()) return@withContext ByteArray(EMBEDDING_DIM * 4)

        val vec = FloatArray(EMBEDDING_DIM)
        val totalTokens = tokens.size

        for ((index, token) in tokens.withIndex()) {
            val positionWeight = 1.0f + 0.5f * (1.0f - index.toFloat() / totalTokens)
            val semanticWeight = semanticWeight(token)
            val idf = idf(token)
            val weight = positionWeight * semanticWeight * idf

            val hash = token.hashCode()
            val dim1 = (hash and Int.MAX_VALUE) % EMBEDDING_DIM
            val dim2 = ((hash ushr 7) and Int.MAX_VALUE) % EMBEDDING_DIM

            vec[dim1] += weight * 0.6f
            vec[dim2] += weight * 0.4f
        }

        normalize(vec)
        floatsToBytes(vec)
    }

    fun decodeEmbedding(bytes: ByteArray): FloatArray {
        require(bytes.size == EMBEDDING_DIM * 4) { "Invalid embedding size: ${bytes.size}" }
        return FloatArray(EMBEDDING_DIM) { i ->
            val bits = ((bytes[i * 4].toInt() and 0xFF) shl 24) or
                ((bytes[i * 4 + 1].toInt() and 0xFF) shl 16) or
                ((bytes[i * 4 + 2].toInt() and 0xFF) shl 8) or
                (bytes[i * 4 + 3].toInt() and 0xFF)
            Float.fromBits(bits)
        }
    }

    private fun tokenize(text: String): List<String> {
        return text.lowercase()
            .replace(Regex("[^a-z0-9\\- ]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() && it.length <= 40 }
    }

    private fun semanticWeight(token: String): Float {
        for ((group, weight) in semanticGroups) {
            if (token in group) return weight
        }
        return 1.0f
    }

    private fun idf(token: String): Float {
        return idfWeights[token] ?: defaultIdf
    }

    private fun floatsToBytes(floats: FloatArray): ByteArray {
        val bytes = ByteArray(floats.size * 4)
        for (i in floats.indices) {
            val bits = floats[i].toBits()
            bytes[i * 4] = (bits ushr 24).toByte()
            bytes[i * 4 + 1] = (bits ushr 16).toByte()
            bytes[i * 4 + 2] = (bits ushr 8).toByte()
            bytes[i * 4 + 3] = bits.toByte()
        }
        return bytes
    }

    private fun normalize(vec: FloatArray) {
        val sumSq = vec.sumOf { (it * it).toDouble() }
        val norm = sqrt(sumSq.toFloat())
        if (norm > 0.0001f) {
            for (i in vec.indices) {
                vec[i] /= norm
            }
        }
    }

    fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        require(a.size == b.size) { "Dimension mismatch: ${a.size} vs ${b.size}" }
        var dot = 0f
        var normA = 0f
        var normB = 0f
        for (i in a.indices) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        val denom = sqrt(normA) * sqrt(normB)
        return if (denom > 0.0001f) dot / denom else 0f
    }
}