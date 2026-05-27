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
}
