package com.nexus.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nexus.database.entity.InternshipEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InternshipDao {

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(internship: InternshipEntity)

    @Query("UPDATE internships SET is_saved = :isSaved WHERE id = :id")
    suspend fun updateSaveStatus(id: String, isSaved: Boolean)

    @Query("DELETE FROM internships")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM internships")
    fun getCount(): Flow<Int>
}
