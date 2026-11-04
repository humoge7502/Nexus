package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.domain.repository.ActivityDay
import com.nexus.domain.repository.DayData
import com.nexus.domain.repository.GitHubActivity
import com.nexus.domain.repository.GitHubContributions
import com.nexus.domain.repository.GitHubProfile
import com.nexus.domain.repository.GitHubRepo
import com.nexus.domain.repository.IGitHubRepository
import com.nexus.network.service.GitHubEventDto
import com.nexus.network.service.GitHubRestService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.HttpException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class GitHubRepository @Inject constructor(
    private val rest: GitHubRestService,
    private val client: OkHttpClient,
    @Named("github_token") private val githubToken: String,
) : IGitHubRepository {

    private data class CacheEntry<T>(val value: T, val expiresAtMs: Long)

    private val profileCache = ConcurrentHashMap<String, CacheEntry<GitHubProfile>>()
    private val reposCache = ConcurrentHashMap<String, CacheEntry<List<GitHubRepo>>>()
    private val activityCache = ConcurrentHashMap<String, CacheEntry<GitHubActivity>>()

    internal var nowMillis: () -> Long = System::currentTimeMillis

    override suspend fun getProfile(username: String): ApiResult<GitHubProfile> {
        val key = normalize(username)
        if (key.isBlank()) return ApiResult.Error("GitHub handle is blank")
        profileCache[key]?.takeIf { it.expiresAtMs > nowMillis() }?.let {
            return ApiResult.Success(it.value)
        }
        return runCatching {
            val dto = rest.getUser(key)
            GitHubProfile(
                login = dto.login ?: key,
                name = dto.name,
                avatarUrl = dto.avatarUrl,
                profileUrl = dto.htmlUrl,
                publicRepos = dto.publicRepos,
                followers = dto.followers,
                following = dto.following,
            )
        }.fold(
            onSuccess = {
                profileCache[key] = CacheEntry(it, nowMillis() + CACHE_TTL_MS)
                ApiResult.Success(it)
            },
            onFailure = { e -> ApiResult.Error(httpMessage(e), httpCode(e), e) },
        )
    }

    override suspend fun getRepositories(username: String): ApiResult<List<GitHubRepo>> {
        val key = normalize(username)
        if (key.isBlank()) return ApiResult.Error("GitHub handle is blank")
        reposCache[key]?.takeIf { it.expiresAtMs > nowMillis() }?.let {
            return ApiResult.Success(it.value)
        }
        return runCatching {
            rest.getRepos(key).mapNotNull { dto ->
                val name = dto.name ?: return@mapNotNull null
                GitHubRepo(
                    name = name,
                    description = dto.description.orEmpty(),
                    url = dto.htmlUrl.orEmpty(),
                    stars = dto.stargazersCount,
                    forks = dto.forksCount,
                    primaryLanguage = dto.language?.ifBlank { null },
                    updatedAt = dto.updatedAt.orEmpty(),
                    isFork = dto.fork,
                )
            }
        }.fold(
            onSuccess = {
                reposCache[key] = CacheEntry(it, nowMillis() + CACHE_TTL_MS)
                ApiResult.Success(it)
            },
            onFailure = { e -> ApiResult.Error(httpMessage(e), httpCode(e), e) },
        )
    }

    override suspend fun getRecentActivity(username: String): ApiResult<GitHubActivity> {
        val key = normalize(username)
        if (key.isBlank()) return ApiResult.Error("GitHub handle is blank")
        activityCache[key]?.takeIf { it.expiresAtMs > nowMillis() }?.let {
            return ApiResult.Success(it.value)
        }
        return runCatching {
            val events = rest.getPublicEvents(key)
            buildActivity(events)
        }.fold(
            onSuccess = {
                activityCache[key] = CacheEntry(it, nowMillis() + CACHE_TTL_MS)
                ApiResult.Success(it)
            },
            onFailure = { e -> ApiResult.Error(httpMessage(e), httpCode(e), e) },
        )
    }

    override suspend fun getContributions(username: String): ApiResult<GitHubContributions> {
        if (githubToken.isBlank()) {
            // Anonymous GitHub has no calendar; the interface documents that
            // the tokenless path is getRecentActivity.
            return ApiResult.Error("GitHub token not connected")
        }
        return withContext(Dispatchers.IO) {
            try {
                val safeUsername = username.replace("\"", "")
                val query = """
                    query {
                      user(login: "$safeUsername") {
                        contributionsCollection {
                          contributionCalendar {
                            totalContributions
                            weeks {
                              contributionDays {
                                contributionCount
                                date
                                color
                              }
                            }
                          }
                        }
                      }
                    }
                """.trimIndent()

                val jsonBody = JSONObject().apply { put("query", query) }

                val request = Request.Builder()
                    .url(graphqlUrl)
                    .addHeader("Authorization", "Bearer $githubToken")
                    .addHeader("Content-Type", "application/json")
                    .post(jsonBody.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                val bodyString = try {
                    if (!response.isSuccessful) {
                        return@withContext ApiResult.Error(
                            "GitHub API error: ${response.code}",
                            code = response.code,
                        )
                    }
                    response.body?.string() ?: ""
                } finally {
                    response.close()
                }

                val body = JSONObject(bodyString)
                val data = body.getJSONObject("data")
                val user = data.getJSONObject("user")
                val contributions = user.getJSONObject("contributionsCollection")
                val calendar = contributions.getJSONObject("contributionCalendar")

                val total = calendar.getInt("totalContributions")
                val weeksArray = calendar.getJSONArray("weeks")
                val weeks = mutableListOf<List<DayData>>()

                for (i in 0 until weeksArray.length()) {
                    val week = weeksArray.getJSONObject(i)
                    val daysArray = week.getJSONArray("contributionDays")
                    val days = mutableListOf<DayData>()
                    for (j in 0 until daysArray.length()) {
                        val day = daysArray.getJSONObject(j)
                        days.add(
                            DayData(
                                count = day.getInt("contributionCount"),
                                date = day.getString("date"),
                                color = day.getString("color"),
                            )
                        )
                    }
                    weeks.add(days)
                }

                ApiResult.Success(
                    GitHubContributions(
                        totalContributions = total,
                        weeks = weeks,
                    )
                )
            } catch (e: Exception) {
                ApiResult.Error(e.message ?: "Failed to fetch contributions", code = -1, throwable = e)
            }
        }
    }

    private fun buildActivity(events: List<GitHubEventDto>): GitHubActivity {
        // Public events are the only tokenless activity record GitHub offers.
        // Counting exactly what the feed reports — never treating silence
        // inside the window as zero activity — keeps this honest.
        val perDay = LinkedHashMap<LocalDate, ActivityDay>()
        var totalCommits = 0
        for (event in events) {
            val day = event.createdAt
                ?.let { runCatching { Instant.parse(it).atZone(ZoneOffset.UTC).toLocalDate() }.getOrNull() }
                ?: continue
            val commits = if (event.type == "PushEvent") event.payload?.size ?: 0 else 0
            totalCommits += commits
            val existing = perDay[day] ?: ActivityDay(date = day.toString(), events = 0, commits = 0)
            perDay[day] = existing.copy(events = existing.events + 1, commits = existing.commits + commits)
        }

        val days = perDay.entries.sortedBy { it.key }.map { it.value }
        val streak = computeStreak(perDay.keys.toHashSet(), todayUtc())
        return GitHubActivity(
            totalEvents = events.size,
            totalCommits = totalCommits,
            currentStreakDays = streak,
            days = days,
        )
    }

    private fun computeStreak(activeDays: Set<LocalDate>, today: LocalDate): Int {
        var cursor = if (today in activeDays) today else today.minusDays(1)
        if (cursor !in activeDays) return 0
        var streak = 0
        while (cursor in activeDays) {
            streak++
            cursor = cursor.minusDays(1)
        }
        return streak
    }

    internal fun todayUtc(): LocalDate = LocalDate.now(ZoneOffset.UTC)

    private fun normalize(username: String): String = username.trim().trimStart('@')

    private fun httpCode(e: Throwable): Int = (e as? HttpException)?.code() ?: -1

    private fun httpMessage(e: Throwable): String = when (e) {
        is HttpException -> when (e.code()) {
            404 -> "GitHub handle not found"
            403 -> "GitHub rate limit reached; try again later"
            else -> "GitHub request failed (${e.code()})"
        }
        else -> e.message ?: "GitHub request failed"
    }

    private companion object {
        const val CACHE_TTL_MS = 10 * 60 * 1000L
        const val graphqlUrl = "https://api.github.com/graphql"
        val jsonMediaType = "application/json".toMediaType()
    }
}
