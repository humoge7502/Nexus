package com.nexus.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nexus.database.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HackathonDao {
    @Query("SELECT * FROM hackathons ORDER BY startDate ASC")
    fun getAll(): Flow<List<HackathonEntity>>
    @Query("SELECT * FROM hackathons WHERE isLive = 1")
    fun getLive(): Flow<List<HackathonEntity>>
    @Query("SELECT * FROM hackathons WHERE isSaved = 1")
    fun getSaved(): Flow<List<HackathonEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(hackathons: List<HackathonEntity>)
    @Query("UPDATE hackathons SET isSaved = :saved WHERE id = :id")
    suspend fun updateSaveStatus(id: String, saved: Boolean)
}

@Dao
interface ScholarshipDao {
    @Query("SELECT * FROM scholarships ORDER BY deadline ASC")
    fun getAll(): Flow<List<ScholarshipEntity>>
    @Query("SELECT * FROM scholarships WHERE isOpen = 1")
    fun getOpen(): Flow<List<ScholarshipEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(scholarships: List<ScholarshipEntity>)
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY isFeatured DESC, completedAt DESC")
    fun getAll(): Flow<List<ProjectEntity>>
    @Query("SELECT * FROM projects WHERE isFeatured = 1")
    fun getFeatured(): Flow<List<ProjectEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(projects: List<ProjectEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: ProjectEntity)
    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface GoalDao {
    @androidx.room.Query("SELECT * FROM goals")
    suspend fun getAllEntities(): List<GoalEntity>

    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    fun getAll(): Flow<List<GoalEntity>>
    @Query("SELECT * FROM goals WHERE isCompleted = 0 ORDER BY targetDate ASC")
    fun getActive(): Flow<List<GoalEntity>>

    @Query("SELECT COUNT(*) FROM goals WHERE isCompleted = 1")
    fun getCompletedCount(): Flow<Int>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: GoalEntity)
    @Query("UPDATE goals SET progress = :progress WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Float)
    @Query("UPDATE goals SET isCompleted = 1, completedAt = :now WHERE id = :id")
    suspend fun complete(id: String, now: Long = System.currentTimeMillis())
    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface DSAProblemDao {
    @androidx.room.Query("SELECT * FROM dsa_problems")
    suspend fun getAllEntities(): List<DSAProblemEntity>

    @Query("SELECT * FROM dsa_problems ORDER BY frequency DESC")
    fun getAll(): Flow<List<DSAProblemEntity>>
    @Query("SELECT * FROM dsa_problems WHERE difficulty = :difficulty")
    fun getByDifficulty(difficulty: String): Flow<List<DSAProblemEntity>>
    @Query("SELECT * FROM dsa_problems WHERE status = 'NOT_ATTEMPTED' LIMIT 1")
    suspend fun getDailyChallenge(): DSAProblemEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(problems: List<DSAProblemEntity>)
    @Query("UPDATE dsa_problems SET status = :status, solvedAt = :now WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, now: Long = System.currentTimeMillis())
    @Query("SELECT COUNT(*) FROM dsa_problems WHERE status = 'SOLVED'")
    suspend fun getSolvedCount(): Int

    @Query("SELECT COUNT(*) FROM dsa_problems WHERE status = 'SOLVED'")
    fun getSolvedCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM dsa_problems")
    fun getTotalCount(): Flow<Int>
}

@Dao
interface NotificationDao {
    @androidx.room.Query("SELECT * FROM notifications")
    suspend fun getAllEntities(): List<NotificationEntity>

    @Query("SELECT * FROM notifications ORDER BY createdAt DESC LIMIT 50")
    fun getAll(): Flow<List<NotificationEntity>>
    @Query("SELECT * FROM notifications WHERE isRead = 0 ORDER BY createdAt DESC")
    fun getUnread(): Flow<List<NotificationEntity>>
    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    suspend fun getUnreadCount(): Int
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<NotificationEntity>)
    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)
    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()
    @Query("DELETE FROM notifications WHERE createdAt < :before")
    suspend fun deleteOld(before: Long)
}
