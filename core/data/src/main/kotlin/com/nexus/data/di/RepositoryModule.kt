package com.nexus.data.di

import com.nexus.data.repository.*
import com.nexus.domain.repository.ICohortRepository
import com.nexus.domain.repository.IOpportunityRepository
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
    fun provideOpportunityRepository(impl: OpportunityRepositoryImpl): IOpportunityRepository = impl

    @Provides @Singleton
    fun provideOpportunityFeedRepository(
        api: NexusApiService,
        internshipDao: InternshipDao,
        hackathonDao: HackathonDao,
        gson: com.google.gson.Gson,
    ): OpportunityFeedRepository =
        OpportunityFeedRepository(api, internshipDao, hackathonDao, gson)

    @Provides @Singleton
    fun provideInternshipRepository(
        feed: OpportunityFeedRepository,
        dao: InternshipDao,
    ): InternshipRepository = InternshipRepository(feed, dao)

    @Provides @Singleton
    fun provideCertificationRepository(
        api: NexusApiService,
        dao: CertificationDao,
        progressDao: CertificationProgressDao,
    ): CertificationRepository = CertificationRepository(api, dao, progressDao)

    @Provides @Singleton
    fun provideHackathonRepository(
        feed: OpportunityFeedRepository,
        dao: HackathonDao,
    ): HackathonRepository = HackathonRepository(feed, dao)

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
