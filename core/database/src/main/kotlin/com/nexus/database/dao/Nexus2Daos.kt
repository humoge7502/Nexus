package com.nexus.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nexus.database.entity.CareerMemoryEntity
import com.nexus.database.entity.CareerMirrorEntity
import com.nexus.database.entity.CohortIntelligenceEntity
import com.nexus.database.entity.ConfidenceSelfAssessmentEntity
import com.nexus.database.entity.InterviewOutcomeEntity
import com.nexus.database.entity.PlacementForecastEntity
import com.nexus.database.entity.RetentionEntity
import com.nexus.database.entity.ScoreSnapshotEntity
import com.nexus.database.entity.SkillEdgeEntity
import com.nexus.database.entity.SkillNodeEntity
import com.nexus.database.entity.SkillVelocitySnapshotEntity
import com.nexus.database.entity.UserSurveyEntity
import kotlinx.coroutines.flow.Flow

/** Query-result projections (no table). */
data class CompanyApplicationStats(
    val company: String,
    val totalApplications: Int,
    val successRate: Float,
)

data class CompanyInterviewStats(
    val company: String,
    val totalInterviews: Int,
)

data class CollegeTierBreakdown(
    val companyTier: String,
    val avgScore: Float,
    val totalPlaced: Int,
    val totalStudents: Int,
)

@Dao
interface CareerMemoryDao {
    @androidx.room.Query("SELECT * FROM career_memory")
    suspend fun getAllEntities(): List<CareerMemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memory: CareerMemoryEntity)

    @Query("SELECT * FROM career_memory WHERE isActive = 1 ORDER BY relevanceScore DESC LIMIT :limit")
    suspend fun getTopMemories(limit: Int): List<CareerMemoryEntity>

    @Query(
        "UPDATE career_memory SET referenceCount = referenceCount + 1, " +
            "lastReferencedAt = :now WHERE id = :id"
    )
    suspend fun recordReference(id: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE career_memory SET relevanceScore = relevanceScore * :decayFactor")
    suspend fun applyDecay(decayFactor: Float = 0.95f)

    @Query("UPDATE career_memory SET isActive = 0 WHERE relevanceScore < :threshold")
    suspend fun archiveIrrelevant(threshold: Float = 0.2f)
}

@Dao
interface SkillGraphDao {
    @Query("SELECT COUNT(*) FROM skill_nodes")
    suspend fun getTotalSkillCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNodes(nodes: List<SkillNodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEdges(edges: List<SkillEdgeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEdge(edge: SkillEdgeEntity)

    @Query("SELECT * FROM skill_nodes WHERE skillId = :skillId LIMIT 1")
    suspend fun getSkill(skillId: String): SkillNodeEntity?

    @Query(
        "UPDATE skill_nodes SET isUserKnown = :known, userProficiency = :proficiency, " +
            "lastUpdated = :now WHERE skillId = :skillId"
    )
    suspend fun updateUserSkill(
        skillId: String,
        known: Boolean,
        proficiency: Int,
        now: Long = System.currentTimeMillis(),
    )

    @Query("SELECT * FROM skill_nodes WHERE isUserKnown = 1 ORDER BY displayName ASC")
    fun getUserSkills(): Flow<List<SkillNodeEntity>>

    @Query("SELECT COUNT(*) FROM skill_nodes WHERE isUserKnown = 1")
    fun getUserSkillCount(): Flow<Int>

    @Query(
        "SELECT * FROM skill_nodes WHERE isUserKnown = 0 AND skillId NOT IN (:excludeIds) " +
            "ORDER BY popularityScore DESC LIMIT :limit"
    )
    suspend fun getRecommendedSkills(excludeIds: List<String>, limit: Int): List<SkillNodeEntity>
}

@Dao
interface RetentionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: RetentionEntity)

    @Query("SELECT MIN(installWeek) FROM retention_events")
    suspend fun getInstallWeek(): String?

    @Query("SELECT * FROM retention_events WHERE installWeek = :installWeek ORDER BY weeksSinceInstall ASC")
    fun getCohortData(installWeek: String): Flow<List<RetentionEntity>>
}

@Dao
interface UserSurveyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(survey: UserSurveyEntity)

    @Query("SELECT COUNT(*) FROM user_surveys")
    suspend fun getCount(): Int

    @Query("SELECT * FROM user_surveys ORDER BY completedAt DESC")
    fun getAll(): Flow<List<UserSurveyEntity>>
}

@Dao
interface InterviewOutcomeDao {
    @androidx.room.Query("SELECT * FROM interview_outcomes")
    suspend fun getAllEntities(): List<InterviewOutcomeEntity>

    @Query("SELECT * FROM interview_outcomes ORDER BY interviewDate DESC")
    fun getAll(): Flow<List<InterviewOutcomeEntity>>

    @Query("SELECT * FROM interview_outcomes WHERE applicationId = :applicationId ORDER BY interviewDate DESC")
    fun getByApplicationId(applicationId: String): Flow<List<InterviewOutcomeEntity>>

    @Query("SELECT * FROM interview_outcomes WHERE company = :company ORDER BY interviewDate DESC")
    fun getByCompany(company: String): Flow<List<InterviewOutcomeEntity>>

    @Query(
        "SELECT company, COUNT(*) as totalInterviews FROM interview_outcomes " +
            "GROUP BY company ORDER BY totalInterviews DESC LIMIT :limit"
    )
    fun getTopCompanies(limit: Int): Flow<List<CompanyInterviewStats>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(outcome: InterviewOutcomeEntity)

    @Query("UPDATE interview_outcomes SET result = :result, notes = :notes, updatedAt = :now WHERE id = :id")
    suspend fun updateResult(id: String, result: String, notes: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE interview_outcomes SET questionsAsked = :questions, updatedAt = :now WHERE id = :id")
    suspend fun updateQuestions(id: String, questions: String, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM interview_outcomes WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COUNT(*) FROM interview_outcomes")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM interview_outcomes WHERE result = 'SELECTED'")
    suspend fun getSelectedCount(): Int

    @Query("SELECT AVG(confidenceRating) FROM interview_outcomes")
    suspend fun getAvgConfidenceRating(): Float?
}

@Dao
interface ConfidenceAssessmentDao {
    @androidx.room.Query("SELECT * FROM confidence_assessments")
    suspend fun getAllEntities(): List<ConfidenceSelfAssessmentEntity>

    @Query("SELECT * FROM confidence_assessments ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ConfidenceSelfAssessmentEntity>>

    @Query("SELECT * FROM confidence_assessments WHERE assessmentType = :type ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestByType(type: String): ConfidenceSelfAssessmentEntity?

    @Query("SELECT COALESCE(AVG(ABS(confidenceScore - actualScore)), 0) FROM confidence_assessments WHERE actualScore IS NOT NULL")
    suspend fun getAvgCalibrationError(): Float

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assessment: ConfidenceSelfAssessmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(assessments: List<ConfidenceSelfAssessmentEntity>)
}

@Dao
interface PlacementForecastDao {
    @androidx.room.Query("SELECT * FROM placement_forecasts")
    suspend fun getAllEntities(): List<PlacementForecastEntity>

    @Query("SELECT * FROM placement_forecasts WHERE userId = :userId ORDER BY calculatedAt DESC")
    fun getById(userId: String): Flow<List<PlacementForecastEntity>>

    @Query(
        "SELECT * FROM placement_forecasts WHERE userId = :userId AND companyTier = :tier " +
            "ORDER BY calculatedAt DESC LIMIT 1"
    )
    suspend fun getLatestForTier(userId: String, tier: String): PlacementForecastEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(forecast: PlacementForecastEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(forecasts: List<PlacementForecastEntity>)

    @Query("DELETE FROM placement_forecasts WHERE userId = :userId AND calculatedAt < :before")
    suspend fun deleteOlderThan(userId: String, before: Long)
}

@Dao
interface CareerMirrorDao {
    @androidx.room.Query("SELECT * FROM career_mirrors")
    suspend fun getAllEntities(): List<CareerMirrorEntity>

    @Query("SELECT * FROM career_mirrors WHERE userId = :userId ORDER BY periodStart DESC")
    fun getByUserId(userId: String): Flow<List<CareerMirrorEntity>>

    @Query("SELECT * FROM career_mirrors WHERE userId = :userId ORDER BY periodStart DESC LIMIT 1")
    suspend fun getLatestMirror(userId: String): CareerMirrorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mirror: CareerMirrorEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mirrors: List<CareerMirrorEntity>)

    @Query("DELETE FROM career_mirrors WHERE userId = :userId")
    suspend fun clearForUser(userId: String)
}

@Dao
interface ScoreSnapshotDao {
    @androidx.room.Query("SELECT * FROM score_snapshots")
    suspend fun getAllEntities(): List<ScoreSnapshotEntity>

    @Query("SELECT * FROM score_snapshots WHERE userId = :userId ORDER BY recordedAt ASC")
    fun getHistoryForUser(userId: String): Flow<List<ScoreSnapshotEntity>>

    @Query("SELECT * FROM score_snapshots WHERE userId = :userId ORDER BY recordedAt DESC LIMIT :limit")
    fun getRecentSnapshots(userId: String, limit: Int): Flow<List<ScoreSnapshotEntity>>

    @Query("SELECT * FROM score_snapshots WHERE userId = :userId ORDER BY recordedAt DESC LIMIT 1")
    suspend fun getLatestSnapshot(userId: String): ScoreSnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: ScoreSnapshotEntity)

    @Query("DELETE FROM score_snapshots WHERE userId = :userId AND recordedAt < :before")
    suspend fun deleteOlderThan(userId: String, before: Long)
}

@Dao
interface CohortIntelligenceDao {
    @Query("SELECT * FROM cohort_intelligence WHERE collegeId = :collegeId ORDER BY updatedAt DESC")
    fun getByCollege(collegeId: String): Flow<List<CohortIntelligenceEntity>>

    @Query(
        "SELECT * FROM cohort_intelligence WHERE collegeId = :collegeId AND companyTier = :tier " +
            "ORDER BY updatedAt DESC LIMIT 1"
    )
    suspend fun getLatestForCollegeAndTier(collegeId: String, tier: String): CohortIntelligenceEntity?

    @Query(
        "SELECT AVG(placedCount * 100.0 / MAX(totalStudents, 1)) FROM cohort_intelligence " +
            "WHERE collegeId = :collegeId"
    )
    suspend fun getCollegePlacementRate(collegeId: String): Float?

    @Query(
        "SELECT companyTier, AVG(avgScore) as avgScore, SUM(placedCount) as totalPlaced, " +
            "SUM(totalStudents) as totalStudents FROM cohort_intelligence WHERE collegeId = :collegeId " +
            "GROUP BY companyTier ORDER BY totalPlaced DESC"
    )
    suspend fun getCollegeBreakdown(collegeId: String): List<CollegeTierBreakdown>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: CohortIntelligenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<CohortIntelligenceEntity>)

    @Query(
        "UPDATE cohort_intelligence SET placedCount = placedCount + 1, " +
            "updatedAt = CAST(strftime('%s', 'now') AS INTEGER) * 1000 " +
            "WHERE id = (SELECT id FROM cohort_intelligence WHERE collegeId = :collegeId " +
            "AND companyTier = :tier AND semester = :semester ORDER BY updatedAt DESC LIMIT 1)"
    )
    suspend fun recordPlacement(collegeId: String, tier: String, semester: String)
}

@Dao
interface SkillVelocitySnapshotDao {
    @androidx.room.Query("SELECT * FROM skill_velocity")
    suspend fun getAllEntities(): List<SkillVelocitySnapshotEntity>

    @Query("SELECT * FROM skill_velocity WHERE userId = :userId ORDER BY recordedAt DESC")
    fun getByUserId(userId: String): Flow<List<SkillVelocitySnapshotEntity>>

    @Query(
        "SELECT * FROM skill_velocity WHERE userId = :userId AND skillId = :skillId " +
            "ORDER BY recordedAt DESC"
    )
    fun getByUserAndSkill(userId: String, skillId: String): Flow<List<SkillVelocitySnapshotEntity>>

    @Query(
        "SELECT * FROM skill_velocity WHERE userId = :userId " +
            "ORDER BY ABS(velocityScore) DESC LIMIT :limit"
    )
    fun getTopVelocitySkills(userId: String, limit: Int): Flow<List<SkillVelocitySnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: SkillVelocitySnapshotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(snapshots: List<SkillVelocitySnapshotEntity>)

    @Query("DELETE FROM skill_velocity WHERE userId = :userId")
    suspend fun clearForUser(userId: String)
}
