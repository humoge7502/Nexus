package com.nexus.domain.model

data class Application(
  val id: String,
  val opportunityId: String,
  val company: String,
  val role: String,
  val opportunityType: String,
  val status: ApplicationStatus,
  val matchScore: Float,
  val appliedDate: Long?,
  val interviewDate: Long?,
  val interviewRounds: Int,
  val outcomeDate: Long?,
  val outcomeNotes: String?,
  val salaryOffered: String?,
  val preparationNotes: String?,
  val createdAt: Long,
  val updatedAt: Long,
)
