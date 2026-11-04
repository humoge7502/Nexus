package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.network.service.GitHubEventDto
import com.nexus.network.service.GitHubEventPayloadDto
import com.nexus.network.service.GitHubRepoDto
import com.nexus.network.service.GitHubRestService
import com.nexus.network.service.GitHubUserDto
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.time.LocalDate
import java.time.ZoneOffset

class GitHubRepositoryTest {
  @MockK
  private lateinit var rest: GitHubRestService

  private lateinit var repository: GitHubRepository

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    repository = GitHubRepository(rest, OkHttpClient(), githubToken = "")
  }

  @Test
  fun `getProfile maps the rest payload`() = runTest {
    coEvery { rest.getUser("octo") } returns GitHubUserDto(
      login = "octo",
      name = "Octo Cat",
      publicRepos = 12,
      followers = 100,
      following = 7,
    )

    val result = repository.getProfile("@octo ")
    assertTrue(result is ApiResult.Success)
    val profile = (result as ApiResult.Success).data
    assertEquals("octo", profile.login)
    assertEquals(12, profile.publicRepos)
    assertEquals(100, profile.followers)
  }

  @Test
  fun `a blank handle errors without touching the network`() = runTest {
    val result = repository.getProfile("   ")
    assertTrue(result is ApiResult.Error)
    coVerify(exactly = 0) { rest.getUser(any()) }
  }

  @Test
  fun `http 404 maps to a friendly message and code`() = runTest {
    coEvery { rest.getUser("ghost") } throws HttpException(
      Response.error<Any>(404, ByteArray(0).toResponseBody(null)),
    )

    val result = repository.getProfile("ghost")
    assertTrue(result is ApiResult.Error)
    val error = result as ApiResult.Error
    assertEquals(404, error.code)
    assertEquals("GitHub handle not found", error.message)
  }

  @Test
  fun `repositories skip entries without a name and map stars`() = runTest {
    coEvery { rest.getRepos("octo", any(), any()) } returns listOf(
      GitHubRepoDto(name = "a", stargazersCount = 5, fork = false),
      GitHubRepoDto(name = null),
    )

    val result = repository.getRepositories("octo")
    assertTrue(result is ApiResult.Success)
    val repos = (result as ApiResult.Success).data
    assertEquals(1, repos.size)
    assertEquals(5, repos[0].stars)
  }

  @Test
  fun `contributions error without a token guard and never touch the network`() = runTest {
    val result = repository.getContributions("octo")
    assertTrue(result is ApiResult.Error)
    coVerify(exactly = 0) { rest.getUser(any()) }
  }

  @Test
  fun `recent activity builds days and a streak from public events`() = runTest {
    val utc = ZoneOffset.UTC
    val today = LocalDate.now(utc)
    fun at(day: LocalDate) = day.atStartOfDay(utc).plusHours(10).toInstant().toString()
    coEvery { rest.getPublicEvents("octo", any()) } returns listOf(
      GitHubEventDto(type = "PushEvent", createdAt = at(today), payload = GitHubEventPayloadDto(size = 3)),
      GitHubEventDto(type = "PushEvent", createdAt = at(today), payload = GitHubEventPayloadDto(size = 2)),
      GitHubEventDto(type = "CreateEvent", createdAt = at(today.minusDays(1)), payload = null),
      GitHubEventDto(type = "PushEvent", createdAt = at(today.minusDays(3)), payload = GitHubEventPayloadDto(size = 1)),
    )

    val result = repository.getRecentActivity("octo")
    assertTrue(result is ApiResult.Success)
    val activity = (result as ApiResult.Success).data
    assertEquals(4, activity.totalEvents)
    assertEquals(6, activity.totalCommits)
    assertEquals(2, activity.currentStreakDays)
    assertEquals(3, activity.days.size)
    assertEquals(1, activity.days[0].events)
    assertEquals(2, activity.days[2].events)
    assertEquals(5, activity.days[2].commits)
  }
}

