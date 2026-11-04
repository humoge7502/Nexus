package com.nexus.network.di

import javax.inject.Qualifier

/**
 * Marks the OkHttpClient that talks to third-party developer platforms
 * (GitHub, Codeforces, LeetCode). It deliberately carries no auth
 * interceptor: the app's own credentials must never leave for another
 * host, and these endpoints either need no key or take platform tokens
 * added per-request by the repository.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PlatformApi
