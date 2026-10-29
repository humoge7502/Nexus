package com.nexus.domain.usecase

import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.ApplicationStatus
import com.nexus.domain.repository.IApplicationRepository
import com.nexus.domain.repository.IStreakRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlacementStateEngine @Inject constructor(
  private val userPreferences: UserPreferences,
  private val applicationRepository: IApplicationRepository,
  private val streakRepository: IStreakRepository,
) {

  sealed class PlacementState {
    data class EarlyPreparation(val daysToSeason: Int) : PlacementState()
    data class ActivePreparation(val daysToSeason: Int) : PlacementState()
    data class PlacementSeason(val daysToSeason: Int) : PlacementState()
    data class ActiveInterview(
      val interviewCount: Int,
      val companies: List<String>,
      val nextInterviewDate: Long?,
    ) : PlacementState()

    data class OfferReceived(val company: String, val role: String) : PlacementState()
    data object PostPlacement : PlacementState()
  }

  suspend fun getCurrentPlacementState(): PlacementState {
    val currentUser = userPreferences.userId.first()
    val now = LocalDate.now()
    val currentMonth = now.monthValue

    val interviewingApplications = applicationRepository
      .getByStatus(ApplicationStatus.INTERVIEWING)
      .let { flow ->
        var result: List<com.nexus.domain.model.Application> = emptyList()
        flow.collect { result = it }
        result
      }
    if (interviewingApplications.isNotEmpty()) {
      val nextDate = interviewingApplications.mapNotNull { it.interviewDate }.minOrNull()
      return PlacementState.ActiveInterview(
        interviewCount = interviewingApplications.size,
        companies = interviewingApplications.map { it.company }.distinct(),
        nextInterviewDate = nextDate,
      )
    }

    val offeredApplications = applicationRepository
      .getByStatus(ApplicationStatus.OFFERED)
      .let { flow ->
        var result: List<com.nexus.domain.model.Application> = emptyList()
        flow.collect { result = it }
        result
      }
    if (offeredApplications.isNotEmpty()) {
      return PlacementState.OfferReceived(
        company = offeredApplications.first().company,
        role = offeredApplications.first().role,
      )
    }

    val acceptedApplications = applicationRepository
      .getByStatus(ApplicationStatus.ACCEPTED)
      .let { flow ->
        var result: List<com.nexus.domain.model.Application> = emptyList()
        flow.collect { result = it }
        result
      }
    if (acceptedApplications.isNotEmpty()) {
      return PlacementState.PostPlacement
    }

    val yearFlow = userPreferences.userYear
    val collegeFlow = userPreferences.userCollege
    val collegeYear = yearFlow.first()
    val currentYear = now.year
    val yearsToFinalYear = when (collegeYear) {
      1 -> 3
      2 -> 2
      3 -> 1
      4 -> 0
      else -> 2
    }
    val daysToPlacement = estimateDaysToPlacement(currentMonth, yearsToFinalYear)

    return when {
      daysToPlacement > 180 -> PlacementState.EarlyPreparation(daysToSeason = daysToPlacement)
      daysToPlacement > 60 -> PlacementState.ActivePreparation(daysToSeason = daysToPlacement)
      daysToPlacement > 0 -> PlacementState.PlacementSeason(daysToSeason = daysToPlacement)
      else -> PlacementState.PostPlacement
    }
  }

  private fun estimateDaysToPlacement(currentMonth: Int, yearsToFinal: Int): Int {
    val targetMonth = 7
    val now = LocalDate.now()
    val targetYear = if (yearsToFinal <= 1) {
      now.year + if (currentMonth >= targetMonth) 1 else 0
    } else {
      now.year + yearsToFinal - 1
    }
    val targetDate = LocalDate.of(targetYear, targetMonth, 1)
    return ChronoUnit.DAYS.between(now, targetDate).coerceAtLeast(0).toInt()
  }
}
