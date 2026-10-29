package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.domain.repository.DayData
import com.nexus.domain.repository.GitHubContributions
import com.nexus.domain.repository.GitHubRepo
import com.nexus.domain.repository.IGitHubRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class GitHubRepository @Inject constructor(
    private val client: OkHttpClient,
    @Named("github_token") private val githubToken: String,
) : IGitHubRepository {
    private val jsonMediaType = "application/json".toMediaType()
    private val graphqlUrl = "https://api.github.com/graphql"

override fun getContributions(username: String): Flow<ApiResult<GitHubContributions>> = flow {
    emit(ApiResult.Loading)
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

            val jsonBody = JSONObject().apply {
                put("query", query)
            }

            val request = Request.Builder()
                .url(graphqlUrl)
                .addHeader("Authorization", "Bearer $githubToken")
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
  val bodyString = try {
    if (!response.isSuccessful) {
      emit(ApiResult.Error("GitHub API error: ${response.code}", code = response.code))
      return@flow
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

  emit(
    ApiResult.Success(
      GitHubContributions(
        totalContributions = total,
        weeks = weeks,
      )
    )
  )
} catch (e: Exception) {
  emit(ApiResult.Error(e.message ?: "Failed to fetch contributions", code = -1, throwable = e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getRepositories(username: String): Flow<ApiResult<List<GitHubRepo>>> = flow {
        emit(ApiResult.Loading)
        try {
            val safeUsername = username.replace("\"", "")
            val query = """
                query {
                  user(login: "$safeUsername") {
                    repositories(first: 50, orderBy: {field: UPDATED_AT, direction: DESC}) {
                      nodes {
                        name
                        description
                        url
                        stargazerCount
                        forkCount
                        primaryLanguage { name }
                        updatedAt
                        isFork
                      }
                    }
                  }
                }
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                put("query", query)
            }

            val request = Request.Builder()
                .url(graphqlUrl)
                .addHeader("Authorization", "Bearer $githubToken")
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
  val bodyString = try {
    if (!response.isSuccessful) {
      emit(ApiResult.Error("GitHub API error: ${response.code}", code = response.code))
      return@flow
    }
    response.body?.string() ?: ""
  } finally {
    response.close()
  }

  val body = JSONObject(bodyString)
  val data = body.getJSONObject("data")
  val user = data.getJSONObject("user")
  val repos = user.getJSONObject("repositories")
  val nodes = repos.getJSONArray("nodes")

  val repoList = mutableListOf<GitHubRepo>()
            for (i in 0 until nodes.length()) {
                val node = nodes.getJSONObject(i)
                val language = if (node.has("primaryLanguage") && !node.isNull("primaryLanguage")) {
                    node.getJSONObject("primaryLanguage").optString("name", "").ifEmpty { null }
                } else null

                repoList.add(
                    GitHubRepo(
                        name = node.getString("name"),
                        description = node.optString("description", ""),
                        url = node.getString("url"),
                        stars = node.getInt("stargazerCount"),
                        forks = node.getInt("forkCount"),
                        primaryLanguage = language,
                        updatedAt = node.getString("updatedAt"),
                        isFork = node.getBoolean("isFork"),
                    )
                )
            }

            emit(ApiResult.Success(repoList))
        } catch (e: Exception) {
            emit(ApiResult.Error(e.message ?: "Failed to fetch repositories", code = -1, throwable = e))
        }
    }.flowOn(Dispatchers.IO)
}
