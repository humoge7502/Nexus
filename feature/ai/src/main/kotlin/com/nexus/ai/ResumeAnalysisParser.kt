package com.nexus.ai

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject
import kotlin.math.roundToInt

/** The model reads the whole resume only up to here; past that we score a prefix. */
internal const val MAX_READABLE_CHARS = 12_000

/**
 * Turns the analyzer's model reply into scores, and refuses to when the reply
 * does not actually carry them — a truncated or hallucinated shape must surface
 * as a fault on screen, not as a zero-filled report.
 */
class ResumeAnalysisParser @Inject constructor() {

    fun parse(raw: String, charactersGiven: Int): ResumeAnalysis? =
        runCatching { read(raw, charactersGiven) }.getOrNull()

    private fun read(raw: String, charactersGiven: Int): ResumeAnalysis? {
        val root = Json.parseToJsonElement(betweenBraces(raw)).jsonObject

        val ats = root.number("ats_score") ?: return null
        if (ats !in 0..100) return null

        val rows = root["sections"].asArray() ?: return null
        val sections = rows.mapNotNull { row -> row.asObject()?.let(::section) }
        if (sections.isEmpty()) return null

        return ResumeAnalysis(
            atsScore = ats,
            sections = sections,
            missingKeywords = strings(root["missing_keywords"]),
            recommendations = strings(root["recommendations"]),
            charactersRead = minOf(charactersGiven, MAX_READABLE_CHARS),
            charactersGiven = charactersGiven,
        )
    }

    private fun section(row: JsonObject): ResumeSectionScore? {
        val name = row["name"].asText()?.trim().orEmpty()
        val score = row.number("score")
        if (name.isEmpty() || score == null || score !in 0..100) return null
        return ResumeSectionScore(name, score, row["note"].asText()?.trim().orEmpty())
    }

    /** Models emit scores as both `78` and `"78"`, and occasionally `78.4`. */
    private fun JsonObject.number(key: String): Int? =
        this[key].asText()?.trim()?.toDoubleOrNull()?.roundToInt()

    private fun strings(element: JsonElement?): List<String> =
        element.asArray()?.mapNotNull { it.asText()?.trim() }
            ?.filter { it.isNotEmpty() } ?: emptyList()

    private fun JsonElement?.asArray(): JsonArray? = this as? JsonArray

    private fun JsonElement?.asObject(): JsonObject? = this as? JsonObject

    private fun JsonElement?.asText(): String? = (this as? JsonPrimitive)?.contentOrNull

    /** Prose around the payload is common enough that the braces are the only
     *  boundary worth trusting; anything outside them is not our data. */
    private fun betweenBraces(raw: String): String {
        val start = raw.indexOf('{')
        val end = raw.lastIndexOf('}')
        return if (start in 0 until end) raw.substring(start, end + 1) else raw
    }
}
