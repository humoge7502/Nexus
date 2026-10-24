package com.nexus.network.dto

import com.google.gson.annotations.SerializedName

data class CareerHitDto(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "CAREER_TIP",
    val xpReward: Int = 15,
)

data class EmbeddingConfigDto(
    @SerializedName("embedding_dim") val embeddingDim: Int = 128,
    @SerializedName("model_version") val modelVersion: String = "nexus-tfidf-v1",
    @SerializedName("default_idf") val defaultIdf: Float = 1.0f,
    @SerializedName("semantic_groups") val semanticGroups: List<SemanticGroupDto> = emptyList(),
    @SerializedName("idf_weights") val idfWeights: Map<String, Float> = emptyMap(),
)

data class SemanticGroupDto(
    val terms: List<String> = emptyList(),
    val weight: Float = 1.0f,
)
