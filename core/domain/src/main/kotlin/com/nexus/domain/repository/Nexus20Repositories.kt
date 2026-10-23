package com.nexus.domain.repository

import com.nexus.domain.model.InterviewOutcome
import com.nexus.domain.model.PlacementForecast
import com.nexus.domain.model.CareerMirror
import com.nexus.domain.model.ScoreSnapshot
import com.nexus.domain.model.CohortEntry
import com.nexus.domain.model.SkillVelocity
import com.nexus.domain.model.ConfidenceAssessment
import kotlinx.coroutines.flow.Flow

interface IInterviewRepository {
  fun getAllOutcomes(): Flow<List<InterviewOutcome>>
  fun getByApplicationId(applicationId: String): Flow<List<InterviewOutcome>>
  fun getByCompany(company: String): Flow<List<InterviewOutcome>>
  fun getTopCompanies(limit: Int = 20): Flow<List<Pair<String, Int>>>
  suspend fun insert(outcome: InterviewOutcome)
  suspend fun updateResult(id: String, result: String, notes: String)
  suspend fun updateQuestions(id: String, questions: String)
  suspend fun delete(id: String)
  suspend fun getTotalCount(): Int
  suspend fun getSelectedCount(): Int
  suspend fun getAvgConfidence(): Float?
}

interface IConfidenceRepository {
  fun getAllAssessments(): Flow<List<ConfidenceAssessment>>
  suspend fun getLatestByType(type: String): ConfidenceAssessment?
  suspend fun getAvgCalibrationError(): Float
  suspend fun insert(assessment: ConfidenceAssessment)
  suspend fun insertAll(assessments: List<ConfidenceAssessment>)
}

interface IScoreSnapshotRepository {
  fun getHistory(userId: String): Flow<List<ScoreSnapshot>>
  fun getRecentSnapshots(userId: String, limit: Int = 30): Flow<List<ScoreSnapshot>>
  suspend fun getLatest(userId: String): ScoreSnapshot?
  suspend fun insert(snapshot: ScoreSnapshot)
  suspend fun deleteOlderThan(userId: String, before: Long)
}

interface IPlacementForecastRepository {
  fun getForecasts(userId: String): Flow<List<PlacementForecast>>
  suspend fun getLatestForTier(userId: String, tier: String): PlacementForecast?
  suspend fun insert(forecast: PlacementForecast)
  suspend fun insertAll(forecasts: List<PlacementForecast>)
  suspend fun deleteOlderThan(userId: String, before: Long)
}

interface ICareerMirrorRepository {
  fun getByUserId(userId: String): Flow<List<CareerMirror>>
  suspend fun getLatest(userId: String): CareerMirror?
  suspend fun insert(mirror: CareerMirror)
  suspend fun insertAll(mirrors: List<CareerMirror>)
  suspend fun clearForUser(userId: String)
}

interface ICohortRepository {
  fun getByCollege(collegeId: String): Flow<List<CohortEntry>>
  suspend fun getLatestForCollegeAndTier(collegeId: String, tier: String): CohortEntry?
  suspend fun getCollegePlacementRate(collegeId: String): Float?
  suspend fun getCollegeBreakdown(collegeId: String): List<TierBreakdown>
  suspend fun insert(entry: CohortEntry)
  suspend fun insertAll(entries: List<CohortEntry>)
  suspend fun recordPlacement(collegeId: String, tier: String, semester: String)
}

data class TierBreakdown(
  val companyTier: String,
  val avgScore: Float,
  val totalPlaced: Int,
  val totalStudents: Int,
)

interface ISkillVelocityRepository {
  fun getByUserId(userId: String): Flow<List<SkillVelocity>>
  fun getByUserAndSkill(userId: String, skillId: String): Flow<List<SkillVelocity>>
  fun getTopVelocitySkills(userId: String, limit: Int = 20): Flow<List<SkillVelocity>>
  suspend fun insert(snapshot: SkillVelocity)
  suspend fun insertAll(snapshots: List<SkillVelocity>)
  suspend fun clearForUser(userId: String)
}
