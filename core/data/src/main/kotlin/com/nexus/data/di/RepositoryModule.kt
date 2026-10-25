package com.nexus.data.di

import com.nexus.data.repository.*
import com.nexus.domain.repository.ICohortRepository
import com.nexus.domain.repository.IScoreSnapshotRepository
import com.nexus.domain.repository.ISkillGraphRepository
import com.nexus.database.dao.*
import com.nexus.network.api.NexusApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides @Singleton
    fun provideSkillGraphRepository(impl: SkillGraphRepository): ISkillGraphRepository = impl

    @Provides @Singleton
    fun provideScoreSnapshotRepository(impl: ScoreSnapshotRepository): IScoreSnapshotRepository = impl

    @Provides @Singleton
    fun provideCohortRepository(impl: CohortIntelligenceRepository): ICohortRepository = impl

    @Provides @Singleton
    fun provideInternshipRepository(api: NexusApiService, dao: InternshipDao): InternshipRepository =
        InternshipRepository(api, dao)

    @Provides @Singleton
    fun provideCertificationRepository(api: NexusApiService, dao: CertificationDao): CertificationRepository =
        CertificationRepository(api, dao)

    @Provides @Singleton
    fun provideHackathonRepository(api: NexusApiService, dao: HackathonDao): HackathonRepository =
        HackathonRepository(api, dao)

    @Provides @Singleton
    fun provideGoalRepository(api: NexusApiService, dao: GoalDao): GoalRepository =
        GoalRepository(api, dao)

    @Provides @Singleton
    fun provideDSAProblemRepository(api: NexusApiService, dao: DSAProblemDao): DSAProblemRepository =
        DSAProblemRepository(api, dao)

    @Provides @Singleton
    fun provideAchievementRepository(api: NexusApiService, dao: AchievementDao): AchievementRepository =
        AchievementRepository(api, dao)

    @Provides @Singleton
    fun provideAIConversationRepository(api: NexusApiService, dao: AIDao): AIConversationRepository =
        AIConversationRepository(api, dao)
}
