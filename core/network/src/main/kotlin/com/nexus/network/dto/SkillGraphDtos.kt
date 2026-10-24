package com.nexus.network.dto

data class SkillNodeDto(
    val id: String,
    val displayName: String,
    val category: String = "GENERAL",
    val level: Int = 2,
    val proficiency: Int = 0,
)

data class SkillEdgeDto(
    val fromId: String,
    val toId: String,
    val relationship: String = "LEADS_TO",
    val weight: Float = 0.5f,
    val confidence: Float = 0.5f,
)
