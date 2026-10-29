package com.nexus.network.dto

import com.google.gson.annotations.SerializedName

data class CertificationDto(
    val id: String,
    val title: String,
    val provider: String,
    @SerializedName("estimated_hours") val estimatedHours: Int,
    val skills: List<String> = emptyList(),
    val rating: Float = 0f,
    val imageUrl: String? = null,
    val enrollmentUrl: String = "",
    val difficulty: String = "INTERMEDIATE",
    val price: String = "FREE",
)
