package com.nexus.data.repository

import com.nexus.database.dao.InterviewOutcomeDao
import com.nexus.database.entity.InterviewOutcomeEntity
import com.nexus.domain.model.InterviewOutcome
import com.nexus.domain.repository.IInterviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterviewOutcomeRepository @Inject constructor(
  private val interviewOutcomeDao: InterviewOutcomeDao,
) : IInterviewRepository {

  override fun getAllOutcomes(): Flow<List<InterviewOutcome>> =
    interviewOutcomeDao.getAll().map { it.map { e -> e.toDomain() } }

  override fun getByApplicationId(applicationId: String): Flow<List<InterviewOutcome>> =
    interviewOutcomeDao.getByApplicationId(applicationId).map { it.map { e -> e.toDomain() } }

  override fun getByCompany(company: String): Flow<List<InterviewOutcome>> =
    interviewOutcomeDao.getByCompany(company).map { it.map { e -> e.toDomain() } }

  override fun getTopCompanies(limit: Int): Flow<List<Pair<String, Int>>> =
    interviewOutcomeDao.getTopCompanies(limit).map { list ->
      list.map { it.company to it.totalInterviews }
    }

  override suspend fun insert(outcome: InterviewOutcome) {
    interviewOutcomeDao.insert(outcome.toEntity())
  }

  override suspend fun updateResult(id: String, result: String, notes: String) {
    interviewOutcomeDao.updateResult(id, result, notes)
  }

  override suspend fun updateQuestions(id: String, questions: String) {
    interviewOutcomeDao.updateQuestions(id, questions)
  }

  override suspend fun delete(id: String) {
    interviewOutcomeDao.delete(id)
  }

  override suspend fun getTotalCount(): Int = interviewOutcomeDao.getTotalCount()
  override suspend fun getSelectedCount(): Int = interviewOutcomeDao.getSelectedCount()
  override suspend fun getAvgConfidence(): Float? = interviewOutcomeDao.getAvgConfidenceRating()
}

private fun InterviewOutcomeEntity.toDomain(): InterviewOutcome = InterviewOutcome(
  id = id,
  applicationId = applicationId,
  company = company,
  role = role,
  interviewRound = interviewRound,
  interviewDate = interviewDate,
  result = result,
  questionsAsked = questionsAsked,
  topics = topics,
  confidenceRating = confidenceRating,
  perceivedPerformance = perceivedPerformance,
  notes = notes,
  createdAt = createdAt,
)

private fun InterviewOutcome.toEntity() = InterviewOutcomeEntity(
  id = id.ifBlank { UUID.randomUUID().toString() },
  applicationId = applicationId,
  company = company,
  role = role,
  interviewRound = interviewRound,
  interviewDate = interviewDate,
  result = result,
  questionsAsked = questionsAsked,
  topics = topics,
  confidenceRating = confidenceRating,
  perceivedPerformance = perceivedPerformance,
  notes = notes,
  createdAt = createdAt,
)
