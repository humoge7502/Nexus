package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.network.service.LeetCodeFunctionsService
import com.nexus.network.service.LeetCodeStatsPayload
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

  private lateinit var repository: LeetCodeRepository

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    repository = LeetCodeRepository(functions)
  }

  @Test
  fun `getStats maps the callable payload`() = runTest {
    coEvery { functions.getStats("testuser") } returns Result.success(statsPayload(150))

    val result = repository.getStats("testuser")
    assertTrue(result is ApiResult.Success)
    assertEquals(150, (result as ApiResult.Success).data.totalSolved)
    assertEquals(80, result.data.easySolved)
    assertEquals(50_000, result.data.ranking)
  }

  @Test
  fun `getStats returns error on exception`() = runTest {
    coEvery { functions.getStats("testuser") } returns Result.failure(Exception("API down"))

    val result = repository.getStats("testuser")
    assertTrue(result is ApiResult.Error)
    assertEquals("API down", (result as ApiResult.Error).message)
  }

  @Test
  fun `repeated lookups are served from the ttl cache`() = runTest {
    var now = 1_000L
    repository.nowMillis = { now }
    coEvery { functions.getStats("testuser") } returns Result.success(statsPayload(150))

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
}
