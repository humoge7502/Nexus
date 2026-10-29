package com.nexus.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nexus.database.entity.InterviewReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InterviewReportDao {
  @Query("SELECT * FROM interview_reports WHERE company = :company ORDER BY reportedAt DESC LIMIT :limit")
  suspend fun getReportsForCompany(company: String, limit: Int = 20): List<InterviewReportEntity>

  @Query("SELECT DISTINCT company FROM interview_reports ORDER BY reportedAt DESC")
  suspend fun getCompaniesWithReports(): List<String>

  @Query("SELECT topics FROM interview_reports WHERE company = :company AND outcome = 'PASSED' ORDER BY reportedAt DESC LIMIT 10")
  suspend fun getSuccessfulTopicsForCompany(company: String): List<String>

  @Query("SELECT COUNT(*) FROM interview_reports WHERE company = :company")
  suspend fun getReportCountForCompany(company: String): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(report: InterviewReportEntity)

  @Query("SELECT * FROM interview_reports WHERE applicationId = :applicationId")
  suspend fun getReportsForApplication(applicationId: String): List<InterviewReportEntity>

  @Query("SELECT * FROM interview_reports ORDER BY reportedAt DESC")
  fun getAllReports(): Flow<List<InterviewReportEntity>>

  @Query("SELECT * FROM interview_reports")
  suspend fun getAllEntities(): List<InterviewReportEntity>

  @Query("SELECT COUNT(*) FROM interview_reports")
  suspend fun getTotalReportCount(): Int
}
