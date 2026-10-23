package com.nexus.ai

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject

class RoadmapParser @Inject constructor() {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun parseRoadmap(jsonString: String): Roadmap? {
        return try {
            val element = json.parseToJsonElement(jsonString)
            val root = element.jsonObject

            if (root.containsKey("phases")) {
                json.decodeFromJsonElement<Roadmap>(element)
            } else if (root.containsKey("roadmap")) {
                val nested = root["roadmap"]?.jsonObject ?: return null
                json.decodeFromJsonElement<Roadmap>(nested)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun parseRoadmapFromMessage(message: String): Roadmap? {
        val jsonBlock = extractJsonBlock(message) ?: return null
        return parseRoadmap(jsonBlock)
    }

    private fun extractJsonBlock(text: String): String? {
        val start = text.indexOf("```json")
        val end = text.lastIndexOf("```")
        if (start != -1 && end > start) {
            return text.substring(start + 7, end).trim()
        }
        val braceStart = text.indexOf('{')
        val braceEnd = text.lastIndexOf('}')
        if (braceStart != -1 && braceEnd > braceStart) {
            return text.substring(braceStart, braceEnd + 1)
        }
        return null
    }
}

@Serializable
data class Roadmap(
    val phases: List<Phase>,
)

@Serializable
data class Phase(
    val title: String,
    val duration: String = "",
    val skills: List<String> = emptyList(),
    val resources: List<String> = emptyList(),
    val project: String = "",
)
