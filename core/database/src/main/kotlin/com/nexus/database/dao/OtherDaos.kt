package com.nexus.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nexus.database.entity.CertificationEntity
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
}

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY start_time DESC LIMIT 30")
    fun getRecentSessions(): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: StudySessionEntity)

    @Query("SELECT SUM(duration_minutes) FROM study_sessions WHERE start_time >= :since")
    suspend fun getTotalMinutesSince(since: Long): Int
}

@Dao
interface ApplicationDao {
    @Query("SELECT * FROM applications ORDER BY last_updated DESC")
    fun getAll(): Flow<List<ApplicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(application: ApplicationEntity)

    @Query("UPDATE applications SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAll(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE is_unlocked = 1")
    fun getUnlocked(): Flow<List<AchievementEntity>>

    @Query("UPDATE achievements SET is_unlocked = 1, unlocked_at = :timestamp WHERE id = :id")
    suspend fun unlock(id: String, timestamp: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<AchievementEntity>)
}
