package com.nexus.common

import kotlinx.coroutines.delay
import timber.log.Timber
import kotlin.math.min
import kotlin.math.pow

object SafeApiCall {
    private const val MAX_RETRIES = 3
    private const val BASE_DELAY_MS = 1000L
    private const val MAX_DELAY_MS = 10000L

    suspend fun <T> run(
        operation: String = "api_call",
        maxRetries: Int = MAX_RETRIES,
        block: suspend () -> T,
    ): ApiResult<T> {
        var lastException: Exception? = null
        for (attempt in 0..maxRetries) {
            try {
                val result = block()
                return ApiResult.Success(result)
            } catch (e: Exception) {
                lastException = e
                val isRetryable = isRetryable(e)
                if (attempt < maxRetries && isRetryable) {
                    val delayMs = calculateBackoff(attempt)
                    Timber.w("$operation failed (attempt ${attempt + 1}/$maxRetries): ${e.message}. Retrying in ${delayMs}ms")
                    delay(delayMs)
                } else {
                    Timber.e(e, "$operation failed after ${attempt + 1} attempts")
                    break
                }
            }
        }
        val message = lastException?.message ?: "Unknown error in $operation"
        return ApiResult.Error(message, throwable = lastException)
    }

    private fun isRetryable(e: Exception): Boolean {
        return when {
            e is java.net.UnknownHostException -> true
            e is java.net.SocketTimeoutException -> true
            e is java.net.ConnectException -> true
            e is java.io.IOException -> true
            e.message?.contains("timeout", ignoreCase = true) == true -> true
            e.message?.contains("network", ignoreCase = true) == true -> true
            else -> false
        }
    }

    private fun calculateBackoff(attempt: Int): Long {
        val exponential = BASE_DELAY_MS * 2.0.pow(attempt.toDouble())
        val jitter = (0..500).random().toLong()
        return min(exponential.toLong() + jitter, MAX_DELAY_MS)
    }
}
