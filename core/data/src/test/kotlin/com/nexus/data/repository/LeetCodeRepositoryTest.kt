package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.network.service.LeetCodeAcCountDto
import com.nexus.network.service.LeetCodeDirectService
import com.nexus.network.service.LeetCodeFunctionsService
import com.nexus.network.service.LeetCodeGraphQLData
import com.nexus.network.service.LeetCodeGraphQLResponse
import com.nexus.network.service.LeetCodeMatchedUserDto
import com.nexus.network.service.LeetCodeProfileDto
import com.nexus.network.service.LeetCodeStatsPayload
import com.nexus.network.service.LeetCodeSubmitStatsDto
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LeetCodeRepositoryTest {
  @MockK
  private lateinit var functions: LeetCodeFunctionsService

  @MockK
  private lateinit var direct: LeetCodeDirectService

  private lateinit var repository: LeetCodeRepository

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    repository = LeetCodeRepository(functions, direct)
  }

  @Test
  fun `getStats maps the callable payload`() = runTest {
    coEvery { functions.getStats("testuser") } returns Result.success(statsPayload(150))
    coEvery { direct.query(any()) } returns graphqlResponse()

    val result = repository.getStats("testuser")
    assertTrue(result is ApiResult.Success)
    assertEquals(150, (result as ApiResult.Success).data.totalSolved)
    assertEquals(80, result.data.easySolved)
    assertEquals(50_000, result.data.ranking)
    coVerify(exactly = 0) { direct.query(any()) }
  }

  @Test
  fun `falls back to public graphql when the function is down`() = runTest {
    coEvery { functions.getStats("testuser") } returns Result.failure(Exception("Function not deployed"))
    coEvery { direct.query(any()) } returns
      graphqlResponse(total = 150, easy = 80, medium = 50, hard = 20, ranking = 50_000)

    val result = repository.getStats("testuser")
    assertTrue(result is ApiResult.Success)
    assertEquals(150, (result as ApiResult.Success).data.totalSolved)
    assertEquals(80, result.data.easySolved)
    assertEquals(20, result.data.hardSolved)
    coVerify(exactly = 1) { direct.query(any()) }
  }

  @Test
  fun `getStats returns error when both paths fail`() = runTest {
    coEvery { functions.getStats("testuser") } returns Result.failure(Exception("API down"))
    coEvery { direct.query(any()) } throws Exception("Network unreachable")

    val result = repository.getStats("testuser")
    assertTrue(result is ApiResult.Error)
    assertEquals("Network unreachable", (result as ApiResult.Error).message)
  }

  @Test
  fun `repeated lookups are served from the ttl cache`() = runTest {
    var now = 1_000L
    repository.nowMillis = { now }
    coEvery { functions.getStats("testuser") } returns Result.success(statsPayload(150))
    coEvery { direct.query(any()) } returns graphqlResponse()

    repository.getStats("testuser")
    now += 60_000L
    val second = repository.getStats("TestUser")

    assertTrue(second is ApiResult.Success)
    assertEquals(150, (second as ApiResult.Success).data.totalSolved)
    coVerify(exactly = 1) { functions.getStats("testuser") }
  }

  @Test
  fun `expired entries are refetched from the backend`() = runTest {
    var now = 1_000L
    repository.nowMillis = { now }
    coEvery { functions.getStats("testuser") } returnsMany listOf(
      Result.success(statsPayload(150)),
      Result.success(statsPayload(160)),
    )
    coEvery { direct.query(any()) } returns graphqlResponse()

    repository.getStats("testuser")
    now += 10 * 60 * 1000L + 1
    val refreshed = repository.getStats("testuser")

    assertTrue(refreshed is ApiResult.Success)
    assertEquals(160, (refreshed as ApiResult.Success).data.totalSolved)
    coVerify(exactly = 2) { functions.getStats("testuser") }
  }

  @Test
  fun `failures are not cached`() = runTest {
    repository.nowMillis = { 1_000L }
    coEvery { functions.getStats("testuser") } returns Result.failure(Exception("API down"))
    coEvery { direct.query(any()) } throws Exception("Network unreachable")
    assertTrue(repository.getStats("testuser") is ApiResult.Error)

    coEvery { functions.getStats("testuser") } returns Result.success(statsPayload(120))
    val recovered = repository.getStats("testuser")

    assertTrue(recovered is ApiResult.Success)
    assertEquals(120, (recovered as ApiResult.Success).data.totalSolved)
    coVerify(exactly = 2) { functions.getStats("testuser") }
  }

  private fun statsPayload(totalSolved: Int) = LeetCodeStatsPayload(
    totalSolved = totalSolved,
    easySolved = 80,
    mediumSolved = 50,
    hardSolved = 20,
    ranking = 50_000,
  )

  private fun graphqlResponse(
    total: Int = 150,
    easy: Int = 80,
    medium: Int = 50,
    hard: Int = 20,
    ranking: Int = 50_000,
  ) = LeetCodeGraphQLResponse(
    data = LeetCodeGraphQLData(
      matchedUser = LeetCodeMatchedUserDto(
        username = "testuser",
        profile = LeetCodeProfileDto(ranking = ranking, reputation = 10),
        submitStats = LeetCodeSubmitStatsDto(
          acSubmissionNum = listOf(
            LeetCodeAcCountDto(difficulty = "All", count = total),
            LeetCodeAcCountDto(difficulty = "Easy", count = easy),
            LeetCodeAcCountDto(difficulty = "Medium", count = medium),
            LeetCodeAcCountDto(difficulty = "Hard", count = hard),
          ),
        ),
      ),
    ),
  )
}
