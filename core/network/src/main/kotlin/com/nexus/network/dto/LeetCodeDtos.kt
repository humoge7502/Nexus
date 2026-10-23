package com.nexus.network.dto

import com.google.gson.annotations.SerializedName

data class LeetCodeUserDto(
    val username: String,
    @SerializedName("totalSolved") val totalSolved: Int = 0,
    @SerializedName("easySolved") val easySolved: Int = 0,
    @SerializedName("mediumSolved") val mediumSolved: Int = 0,
    @SerializedName("hardSolved") val hardSolved: Int = 0,
    val ranking: Int = 0,
    val reputation: Int = 0,
)

data class LeetCodeProblemDto(
    val id: String = "",
    val title: String,
    val difficulty: String = "MEDIUM",
    val platform: String = "LeetCode",
    val url: String = "",
    val topics: List<String> = emptyList(),
    @SerializedName("is_solved") val isSolved: Boolean = false,
)

data class LeetCodeDailyDto(
    val id: String = "",
    val title: String,
    val difficulty: String = "MEDIUM",
    val url: String = "",
    val topics: List<String> = emptyList(),
)
