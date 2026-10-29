package com.nexus.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nexus.database.entity.InternshipEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InternshipDao {
    @Query("SELECT * FROM internships ORDER BY cached_at DESC LIMIT :limit OFFSET :offset")
    suspend fun getPage(limit: Int, offset: Int): List<com.nexus.database.entity.InternshipEntity>

    @Query(
        "SELECT * FROM internships WHERE " +
        "(deadline > 10000000000 AND deadline <= :cutoffMillis) OR " +
        "(deadline > 0 AND deadline <= 10000000000 AND deadline <= :cutoffEpochDay) " +
        "ORDER BY deadline ASC"
    )
    suspend fun getInternshipsWithDeadlineBefore(cutoffMillis: Long, cutoffEpochDay: Long): List<com.nexus.database.entity.InternshipEntity>


    @Query("SELECT * FROM internships ORDER BY posted_at DESC")
    fun getAllInternships(): Flow<List<InternshipEntity>>

    @Query("SELECT * FROM internships WHERE is_saved = 1")
    fun getSavedInternships(): Flow<List<InternshipEntity>>

    @Query("SELECT * FROM internships WHERE id = :id")
    suspend fun getInternshipById(id: String): InternshipEntity?

    @Query("""
        SELECT * FROM internships 
        WHERE (:isRemote IS NULL OR is_remote = :isRemote)
        AND (:minStipend IS NULL OR stipend_amount >= :minStipend)
        ORDER BY posted_at DESC
    """)
    fun getFilteredInternships(
        isRemote: Boolean? = null,
        minStipend: Int? = null,
    ): Flow<List<InternshipEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(internships: List<InternshipEntity>)

    @Query("SELECT COUNT(*) FROM internships")
    suspend fun countAll(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(internship: InternshipEntity)

    @Query("UPDATE internships SET is_saved = :isSaved WHERE id = :id")
    suspend fun updateSaveStatus(id: String, isSaved: Boolean)

    @Query("DELETE FROM internships")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM internships")
    fun getCount(): Flow<Int>
}
