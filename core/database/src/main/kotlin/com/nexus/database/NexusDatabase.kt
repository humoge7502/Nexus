package com.nexus.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nexus.database.dao.*
import com.nexus.database.entity.*

@Database(
    entities = [
        InternshipEntity::class,
        CertificationEntity::class,
        ApplicationEntity::class,
        StudySessionEntity::class,
        AchievementEntity::class,
        HackathonEntity::class,
        ScholarshipEntity::class,
        ProjectEntity::class,
        GoalEntity::class,
        DSAProblemEntity::class,
        NotificationEntity::class,
        AIConversationEntity::class,
    ],
    version = 2,
    exportSchema = true
)
abstract class NexusDatabase : RoomDatabase() {
    abstract fun internshipDao(): InternshipDao
    abstract fun certificationDao(): CertificationDao
    abstract fun applicationDao(): ApplicationDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun achievementDao(): AchievementDao
    abstract fun hackathonDao(): HackathonDao
    abstract fun scholarshipDao(): ScholarshipDao
    abstract fun projectDao(): ProjectDao
    abstract fun goalDao(): GoalDao
    abstract fun dsaProblemDao(): DSAProblemDao
    abstract fun notificationDao(): NotificationDao
    abstract fun aiDao(): AIDao
}
