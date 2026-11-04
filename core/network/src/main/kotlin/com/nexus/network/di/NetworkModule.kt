package com.nexus.network.di

import com.google.gson.Gson
import com.nexus.network.BuildConfig
import com.nexus.network.api.AuthInterceptor
import com.nexus.network.api.NexusApiService
import com.nexus.network.service.CodeforcesService
import com.nexus.network.service.GitHubRestService
import com.nexus.network.service.LeetCodeDirectService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.NEXUS_API_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideNexusApiService(retrofit: Retrofit): NexusApiService {
        return retrofit.create(NexusApiService::class.java)
    }

    // Third-party developer platforms. A separate client without the app's
    // AuthInterceptor: our credentials must never ride along to another host.

    @Provides
    @Singleton
    @PlatformApi
    fun providePlatformOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            // A recognizable UA: bare OkHttp defaults get throttled harder by
            // github.com and leetcode.com edge layers.
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", "Ascent/1.0 (Android; career tracker)")
                        .build(),
                )
            }
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    private fun platformRetrofit(baseUrl: String, client: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideGitHubRestService(
        @PlatformApi client: OkHttpClient,
        gson: Gson,
    ): GitHubRestService = platformRetrofit(BASE_GITHUB, client, gson)
        .create(GitHubRestService::class.java)

    @Provides
    @Singleton
    fun provideCodeforcesService(
        @PlatformApi client: OkHttpClient,
        gson: Gson,
    ): CodeforcesService = platformRetrofit(BASE_CODEFORCES, client, gson)
        .create(CodeforcesService::class.java)

    @Provides
    @Singleton
    fun provideLeetCodeDirectService(
        @PlatformApi client: OkHttpClient,
        gson: Gson,
    ): LeetCodeDirectService = platformRetrofit(BASE_LEETCODE, client, gson)
        .create(LeetCodeDirectService::class.java)

    private const val BASE_GITHUB = "https://api.github.com/"
    private const val BASE_CODEFORCES = "https://codeforces.com/api/"
    private const val BASE_LEETCODE = "https://leetcode.com/"
}
