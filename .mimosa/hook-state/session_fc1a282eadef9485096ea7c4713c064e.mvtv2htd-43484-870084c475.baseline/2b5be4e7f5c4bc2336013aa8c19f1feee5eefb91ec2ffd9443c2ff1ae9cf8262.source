package com.nexus.common

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int = -1, val throwable: Throwable? = null) : ApiResult<Nothing>()
    data object Loading : ApiResult<Nothing>()

    val isSuccess get() = this is Success
    val isError get() = this is Error
    val dataOrNull get() = (this as? Success)?.data
    val errorMessage get() = (this as? Error)?.message

    fun <R> map(transform: (T) -> R): ApiResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(message, code)
        is Loading -> Loading
    }

    fun onSuccess(action: (T) -> Unit): ApiResult<T> {
        if (this is Success) action(data)
        return this
    }

    fun onError(action: (String) -> Unit): ApiResult<T> {
        if (this is Error) action(message)
        return this
    }
}
