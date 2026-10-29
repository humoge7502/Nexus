package com.nexus.common

typealias NexusResult<T> = ApiResult<T>

val <T> NexusResult<T>.isSuccess: Boolean
  get() = this is ApiResult.Success

val <T> NexusResult<T>.isError: Boolean
  get() = this is ApiResult.Error

val <T> NexusResult<T>.isLoading: Boolean
  get() = this is ApiResult.Loading

val <T> NexusResult<T>.dataOrNull: T?
  get() = (this as? ApiResult.Success)?.data

val <T> NexusResult<T>.errorMessage: String?
  get() = (this as? ApiResult.Error)?.message
