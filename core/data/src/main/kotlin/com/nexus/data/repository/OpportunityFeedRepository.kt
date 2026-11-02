package com.nexus.data.repository

import com.google.gson.Gson
import com.nexus.common.ApiResult
import com.nexus.database.dao.HackathonDao
import com.nexus.database.dao.InternshipDao
import com.nexus.database.entity.HackathonEntity
import com.nexus.database.entity.InternshipEntity
import com.nexus.domain.model.OpportunityKind
import com.nexus.network.api.NexusApiService
import com.nexus.network.dto.FeedHackathonDto
import com.nexus.network.dto.FeedInternshipDto
import com.nexus.network.dto.FeedResponseDto
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class FeedSnapshot(
    val internshipCount: Int,
    val hackathonCount: Int,
    val generatedAt: String,
)

/**
 * Single writer for the verified opportunity feed. The pipeline publishes
 * feed/opportunities.json every six hours; this repository downloads it,
 * verifies its shape, and upserts both tables while preserving bookmarks.
 *
 * A short in-memory cache collapses the burst of refreshes that Home,
 * Internships, Explore and the sync worker request on cold start.
 */
@Singleton
class OpportunityFeedRepository @Inject constructor(
    private val api: NexusApiService,
    private val internshipDao: InternshipDao,
    private val hackathonDao: HackathonDao,
    private val gson: Gson,
) {
    private val mutex = Mutex()
    private var cachedAt = 0L
    private var cachedResult: ApiResult<FeedSnapshot>? = null

    suspend fun refresh(force: Boolean = false): ApiResult<FeedSnapshot> = mutex.withLock {
        val cached = cachedResult
        if (!force && cached != null && System.currentTimeMillis() - cachedAt < CACHE_TTL_MS) {
            return cached
        }
        val result = try {
            val response = api.getOpportunityFeed()
            val feed = response.body()
            when {
                !response.isSuccessful -> ApiResult.Error("Feed unavailable (${response.code()})")
                feed == null -> ApiResult.Error("Feed response was empty")
                feed.internships.isEmpty() && feed.hackathons.isEmpty() ->
                    ApiResult.Error("Feed contained no opportunities")
                else -> {
                    importFeed(feed)
                    ApiResult.Success(
                        FeedSnapshot(feed.internships.size, feed.hackathons.size, feed.generatedAt)
                    )
                }
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
        cachedAt = System.currentTimeMillis()
        cachedResult = result
        result
    }

    /** Seeds from the bundled asset when a device has never reached the feed. */
    suspend fun importSnapshot(json: String): ApiResult<FeedSnapshot> = mutex.withLock {
        try {
            val feed = gson.fromJson(json, FeedResponseDto::class.java)
            if (feed == null || (feed.internships.isEmpty() && feed.hackathons.isEmpty())) {
                ApiResult.Error("Snapshot contained no opportunities")
            } else {
                importFeed(feed)
                ApiResult.Success(
                    FeedSnapshot(feed.internships.size, feed.hackathons.size, feed.generatedAt)
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Snapshot parse error")
        }
    }

    private suspend fun importFeed(feed: FeedResponseDto) {
        if (feed.internships.isNotEmpty()) {
            val savedIds = internshipDao.getSavedIds().toSet()
            internshipDao.deleteUnsaved()
            internshipDao.insertAll(feed.internships.map { it.toEntity(savedIds) })
        }
        if (feed.hackathons.isNotEmpty()) {
            val savedIds = hackathonDao.getSavedIds().toSet()
            hackathonDao.deleteUnsaved()
            hackathonDao.insertAll(feed.hackathons.map { it.toEntity(savedIds) })
        }
    }

    private fun FeedInternshipDto.toEntity(savedIds: Set<String>) = InternshipEntity(
        id = id,
        title = title,
        companyName = company,
        companyLogoUrl = null,
        location = location,
        isRemote = isRemote || location.contains("remote", ignoreCase = true),
        stipendAmount = null,
        stipendCurrency = "INR",
        durationMonths = durationMonths(duration),
        skills = skills.distinct().joinToString(","),
        deadline = parseCalendarDay(deadline) ?: 0L,
        applyUrl = applyUrl,
        description = plainText(description),
        postedAt = parseInstantMillis(postedAt),
        isSaved = id in savedIds,
        cachedAt = System.currentTimeMillis(),
        kind = OpportunityKind.from(kind).name,
        sourceName = sourceName,
        verifiedAt = parseInstantMillis(verifiedAt).takeIf { it > 0L },
        stipendLabel = stipendLabel,
        durationLabel = duration,
        tags = tags.distinct().joinToString(","),
    )

    private fun FeedHackathonDto.toEntity(savedIds: Set<String>): HackathonEntity {
        val deadlineDay = parseCalendarDay(registrationDeadline) ?: parseCalendarDay(endDate) ?: 0L
        val startDay = parseCalendarDay(startDate) ?: deadlineDay
        val endDay = parseCalendarDay(endDate) ?: startDay
        return HackathonEntity(
            id = id,
            title = title,
            organizer = organizer,
            description = description,
            mode = mode.uppercase(Locale.ROOT).takeIf { it in EVENT_MODES } ?: "ONLINE",
            location = location?.takeIf { it.isNotBlank() },
            startDate = startDay,
            endDate = endDay,
            registrationDeadline = deadlineDay,
            prizePool = prizePool,
            registrationUrl = registrationUrl,
            isLive = isLive,
            isSaved = id in savedIds,
            tags = tags.distinct().joinToString(","),
            cachedAt = System.currentTimeMillis(),
            sourceName = sourceName,
            verifiedAt = parseInstantMillis(verifiedAt).takeIf { it > 0L },
        )
    }
    private fun durationMonths(label: String): Int {
        val match = DURATION_PATTERN.find(label) ?: return 0
        val value = match.groupValues[1].toIntOrNull() ?: return 0
        return if (match.groupValues[2].startsWith("week", ignoreCase = true)) (value + 3) / 4 else value
    }

    private companion object {
        const val CACHE_TTL_MS = 5 * 60 * 1000L
        val EVENT_MODES = setOf("ONLINE", "OFFLINE", "HYBRID")
        val DURATION_PATTERN = Regex("(\\d{1,2})\\s*(month|week)", RegexOption.IGNORE_CASE)
    }
}

internal fun parseCalendarDay(raw: String?): Long? =
    raw?.trim()?.takeIf { it.isNotEmpty() }?.let {
        runCatching { LocalDate.parse(it).toEpochDay() }.getOrNull()
    }

internal fun parseInstantMillis(raw: String?): Long =
    raw?.trim()?.takeIf { it.isNotEmpty() }?.let {
        runCatching { Instant.parse(it).toEpochMilli() }.getOrNull()
    } ?: 0L

private val HTML_TAG = Regex("<[^>]*>")
private val WHITESPACE_RUN = Regex("\\s+")

/** Feed descriptions arrive as HTML; the app renders plain text. */
internal fun plainText(raw: String): String =
    raw.replace(HTML_TAG, " ")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&apos;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace(WHITESPACE_RUN, " ")
        .trim()
