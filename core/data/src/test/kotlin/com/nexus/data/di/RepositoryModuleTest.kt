package com.nexus.data.di

import com.nexus.data.repository.*
import com.nexus.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

  @Binds
  @Singleton
  abstract fun bindOpportunityRepository(
    impl: OpportunityRepositoryImpl,
  ): IOpportunityRepository

  @Binds
  @Singleton
  abstract fun bindApplicationRepository(
    impl: ApplicationRepository,
  ): IApplicationRepository

  @Binds
  @Singleton
  abstract fun bindStreakRepository(
    impl: StreakRepository,
  ): IStreakRepository

  @Binds
  @Singleton
  abstract fun bindDSAProblemRepository(
    impl: DSAProblemRepository,
  ): IDSAProblemRepository

  @Binds
  @Singleton
  abstract fun bindUserXpRepository(
    impl: UserXpRepository,
  ): IUserXpRepository
}
