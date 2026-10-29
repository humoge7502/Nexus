package com.nexus.app.di

import com.nexus.app.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Named("googleWebClientId")
    @Singleton
    fun provideGoogleWebClientId(): String = BuildConfig.GOOGLE_WEB_CLIENT_ID

    /**
     * GitHub PAT for the authenticated API tier. Empty string means the
     * repository falls back to unauthenticated requests (60 req/h rate
     * limit); the real token is stored per-user after GitHub OAuth login.
     */
    @Provides
    @Named("github_token")
    @Singleton
    fun provideGithubToken(): String = ""
}
