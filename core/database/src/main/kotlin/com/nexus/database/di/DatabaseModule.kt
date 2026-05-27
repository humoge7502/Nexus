package com.nexus.database.di

import android.content.Context
import androidx.room.Room
import com.nexus.database.NexusDatabase
import com.nexus.database.dao.AchievementDao
import com.nexus.database.dao.AIDao
import com.nexus.database.dao.ApplicationDao
import com.nexus.database.dao.CertificationDao
import com.nexus.database.dao.DSAProblemDao
import com.nexus.database.dao.GoalDao
import com.nexus.database.dao.HackathonDao
import com.nexus.database.dao.InternshipDao
import com.nexus.database.dao.NotificationDao
import com.nexus.database.dao.ProjectDao
import com.nexus.database.dao.ScholarshipDao
import com.nexus.database.dao.StudySessionDao
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
        ).fallbackToDestructiveMigration().build()
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
}
