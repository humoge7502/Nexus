package com.nexus.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nexus.database.entity.CertificationEntity
import com.nexus.database.entity.CertificationProgressEntity
import com.nexus.database.entity.StudySessionEntity
import com.nexus.database.entity.AchievementEntity
import com.nexus.database.entity.ApplicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CertificationDao {
    @Query("SELECT * FROM certifications")
    fun getAll(): Flow<List<CertificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(certs: List<CertificationEntity>)

    @Query("SELECT COUNT(*) FROM certifications")
    suspend fun countAllCertifications(): Int
}

@Dao
interface CertificationProgressDao {
    @Query("SELECT * FROM certification_progress")
    fun getAll(): Flow<List<CertificationProgressEntity>>

    @Query("SELECT * FROM certification_progress WHERE certification_id = :certificationId")
    suspend fun getFor(certificationId: String): CertificationProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: CertificationProgressEntity)

    @Query("SELECT COUNT(*) FROM certification_progress WHERE completed_at IS NOT NULL")
    fun getCompletedCount(): Flow<Int>

    @Query("DELETE FROM certification_progress WHERE certification_id = :certificationId")
    suspend fun delete(certificationId: String)
}

@Dao
interface StudySessionDao {
    @androidx.room.Query("SELECT * FROM study_sessions")
    suspend fun getAllEntities(): List<StudySessionEntity>

    @Query("SELECT * FROM study_sessions ORDER BY start_time DESC LIMIT 30")
    fun getRecentSessions(): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: StudySessionEntity)

    @Query("SELECT SUM(duration_minutes) FROM study_sessions WHERE start_time >= :since")
    suspend fun getTotalMinutesSince(since: Long): Int
}

@Dao
interface ApplicationDao {
    @androidx.room.Query("SELECT * FROM applications")
    suspend fun getAllEntities(): List<ApplicationEntity>

    @Query("SELECT * FROM applications ORDER BY updated_at DESC")
    fun getAll(): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications ORDER BY updated_at DESC")
    fun getAllApplications(): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications WHERE status = :status ORDER BY updated_at DESC")
    fun getByStatus(status: String): Flow<List<ApplicationEntity>>

    @Query("SELECT COUNT(*) FROM applications WHERE status = :status")
    fun getCountByStatus(status: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM applications")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT * FROM applications WHERE opportunity_id = :opportunityId LIMIT 1")
    suspend fun getByOpportunityId(opportunityId: String): ApplicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(application: ApplicationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(application: ApplicationEntity)

    @Query("DELETE FROM applications WHERE id = :id")
    suspend fun deleteById(id: String)

    @androidx.room.Delete
    suspend fun delete(application: ApplicationEntity)

    @Query("UPDATE applications SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query(
        "SELECT company, COUNT(*) as totalApplications, " +
            "AVG(CASE WHEN status IN ('OFFERED', 'ACCEPTED') THEN 1.0 ELSE 0.0 END) as successRate " +
            "FROM applications GROUP BY company ORDER BY totalApplications DESC"
    )
    fun getCompanyStats(): Flow<List<CompanyApplicationStats>>
}

@Dao
interface AchievementDao {
    @androidx.room.Query("SELECT * FROM achievements")
    suspend fun getAllEntities(): List<AchievementEntity>

    @Query("SELECT * FROM achievements")
    fun getAll(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE is_unlocked = 1")
    fun getUnlocked(): Flow<List<AchievementEntity>>

    @Query("UPDATE achievements SET is_unlocked = 1, unlocked_at = :timestamp WHERE id = :id")
    suspend fun unlock(id: String, timestamp: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<AchievementEntity>)
}
