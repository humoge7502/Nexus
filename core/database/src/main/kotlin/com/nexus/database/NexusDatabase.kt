package com.nexus.database

import androidx.room.Database
import androidx.room.RoomDatabase
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
import com.nexus.database.entity.AIConversationEntity
import com.nexus.database.entity.AchievementEntity
import com.nexus.database.entity.ApplicationEntity
import com.nexus.database.entity.CareerMemoryEntity
import com.nexus.database.entity.CareerMirrorEntity
import com.nexus.database.entity.CertificationEntity
import com.nexus.database.entity.CohortIntelligenceEntity
import com.nexus.database.entity.ConfidenceSelfAssessmentEntity
import com.nexus.database.entity.DSAProblemEntity
import com.nexus.database.entity.GoalEntity
import com.nexus.database.entity.HackathonEntity
import com.nexus.database.entity.InternshipEntity
import com.nexus.database.entity.InterviewOutcomeEntity
import com.nexus.database.entity.InterviewReportEntity
import com.nexus.database.entity.NotificationEntity
import com.nexus.database.entity.OpportunityEmbeddingEntity
import com.nexus.database.entity.PlacementForecastEntity
import com.nexus.database.entity.ProjectEntity
import com.nexus.database.entity.RetentionEntity
import com.nexus.database.entity.ScoreSnapshotEntity
import com.nexus.database.entity.ScholarshipEntity
import com.nexus.database.entity.SkillEdgeEntity
import com.nexus.database.entity.SkillNodeEntity
import com.nexus.database.entity.SkillVelocitySnapshotEntity
import com.nexus.database.entity.StudySessionEntity
import com.nexus.database.entity.UserSurveyEntity

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
        CareerMemoryEntity::class,
        SkillNodeEntity::class,
        SkillEdgeEntity::class,
        OpportunityEmbeddingEntity::class,
        RetentionEntity::class,
        UserSurveyEntity::class,
        InterviewOutcomeEntity::class,
        ConfidenceSelfAssessmentEntity::class,
        PlacementForecastEntity::class,
        CareerMirrorEntity::class,
        ScoreSnapshotEntity::class,
        CohortIntelligenceEntity::class,
        SkillVelocitySnapshotEntity::class,
        InterviewReportEntity::class,
    ],
    version = 8,
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
    abstract fun careerMemoryDao(): CareerMemoryDao
    abstract fun skillGraphDao(): SkillGraphDao
    abstract fun retentionDao(): RetentionDao
    abstract fun userSurveyDao(): UserSurveyDao
    abstract fun interviewOutcomeDao(): InterviewOutcomeDao
    abstract fun confidenceAssessmentDao(): ConfidenceAssessmentDao
    abstract fun placementForecastDao(): PlacementForecastDao
    abstract fun careerMirrorDao(): CareerMirrorDao
    abstract fun scoreSnapshotDao(): ScoreSnapshotDao
    abstract fun cohortIntelligenceDao(): CohortIntelligenceDao
    abstract fun skillVelocitySnapshotDao(): SkillVelocitySnapshotDao
    abstract fun interviewReportDao(): InterviewReportDao
    abstract fun opportunityEmbeddingDao(): OpportunityEmbeddingDao
}
