package com.nexus.database.di

import android.content.Context
import androidx.room.Room
import com.nexus.database.NexusDatabase
import com.nexus.database.dao.AchievementDao
import com.nexus.database.dao.AIDao
import com.nexus.database.dao.ApplicationDao
import com.nexus.database.dao.CareerMemoryDao
import com.nexus.database.dao.CareerMirrorDao
import com.nexus.database.dao.CertificationDao
import com.nexus.database.dao.CohortIntelligenceDao
import com.nexus.database.dao.ConfidenceAssessmentDao
import com.nexus.database.dao.DSAProblemDao
import com.nexus.database.dao.GoalDao
import com.nexus.database.dao.HackathonDao
import com.nexus.database.dao.InternshipDao
import com.nexus.database.dao.InterviewOutcomeDao
import com.nexus.database.dao.InterviewReportDao
import com.nexus.database.dao.NotificationDao
import com.nexus.database.dao.OpportunityEmbeddingDao
import com.nexus.database.dao.PlacementForecastDao
import com.nexus.database.dao.ProjectDao
import com.nexus.database.dao.RetentionDao
import com.nexus.database.dao.ScoreSnapshotDao
import com.nexus.database.dao.ScholarshipDao
import com.nexus.database.dao.SkillGraphDao
import com.nexus.database.dao.SkillVelocitySnapshotDao
import com.nexus.database.dao.StudySessionDao
import com.nexus.database.dao.UserSurveyDao
import com.nexus.database.migration.MigrationRegistry
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NexusDatabase {
        return Room.databaseBuilder(
            context,
            NexusDatabase::class.java,
            "nexus.db"
        )
            .addMigrations(*MigrationRegistry.ALL_MIGRATIONS)
            .build()
    }

    @Provides fun provideInternshipDao(db: NexusDatabase): InternshipDao = db.internshipDao()
    @Provides fun provideCertificationDao(db: NexusDatabase): CertificationDao = db.certificationDao()
    @Provides fun provideApplicationDao(db: NexusDatabase): ApplicationDao = db.applicationDao()
    @Provides fun provideStudySessionDao(db: NexusDatabase): StudySessionDao = db.studySessionDao()
    @Provides fun provideAchievementDao(db: NexusDatabase): AchievementDao = db.achievementDao()
    @Provides fun provideHackathonDao(db: NexusDatabase): HackathonDao = db.hackathonDao()
    @Provides fun provideScholarshipDao(db: NexusDatabase): ScholarshipDao = db.scholarshipDao()
    @Provides fun provideProjectDao(db: NexusDatabase): ProjectDao = db.projectDao()
    @Provides fun provideGoalDao(db: NexusDatabase): GoalDao = db.goalDao()
    @Provides fun provideDSAProblemDao(db: NexusDatabase): DSAProblemDao = db.dsaProblemDao()
    @Provides fun provideNotificationDao(db: NexusDatabase): NotificationDao = db.notificationDao()
    @Provides fun provideAIDao(db: NexusDatabase): AIDao = db.aiDao()
    @Provides fun provideCareerMemoryDao(db: NexusDatabase): CareerMemoryDao = db.careerMemoryDao()
    @Provides fun provideSkillGraphDao(db: NexusDatabase): SkillGraphDao = db.skillGraphDao()
    @Provides fun provideRetentionDao(db: NexusDatabase): RetentionDao = db.retentionDao()
    @Provides fun provideUserSurveyDao(db: NexusDatabase): UserSurveyDao = db.userSurveyDao()
    @Provides fun provideInterviewOutcomeDao(db: NexusDatabase): InterviewOutcomeDao = db.interviewOutcomeDao()
    @Provides fun provideConfidenceAssessmentDao(db: NexusDatabase): ConfidenceAssessmentDao = db.confidenceAssessmentDao()
    @Provides fun providePlacementForecastDao(db: NexusDatabase): PlacementForecastDao = db.placementForecastDao()
    @Provides fun provideCareerMirrorDao(db: NexusDatabase): CareerMirrorDao = db.careerMirrorDao()
    @Provides fun provideScoreSnapshotDao(db: NexusDatabase): ScoreSnapshotDao = db.scoreSnapshotDao()
    @Provides fun provideCohortIntelligenceDao(db: NexusDatabase): CohortIntelligenceDao = db.cohortIntelligenceDao()
    @Provides fun provideSkillVelocityDao(db: NexusDatabase): SkillVelocitySnapshotDao = db.skillVelocitySnapshotDao()
    @Provides fun provideInterviewReportDao(db: NexusDatabase): InterviewReportDao = db.interviewReportDao()
    @Provides fun provideOpportunityEmbeddingDao(db: NexusDatabase): OpportunityEmbeddingDao = db.opportunityEmbeddingDao()
}
