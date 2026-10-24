package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.database.dao.DSAProblemDao
import com.nexus.network.api.LeetCodeApiService
import com.nexus.network.dto.LeetCodeUserDto
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class LeetCodeRepositoryTest {
  @MockK
  private lateinit var api: LeetCodeApiService

  @MockK
  private lateinit var dao: DSAProblemDao

  private lateinit var repository: LeetCodeRepository

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    repository = LeetCodeRepository(api, dao)
  }

  @Test
  fun `getStats returns parsed stats on success`() = runTest {
    val dto = LeetCodeUserDto(
      username = "testuser",
      totalSolved = 150,
      easySolved = 80,
      mediumSolved = 50,
      hardSolved = 20,
      ranking = 50000,
    )
    coEvery { api.getUserStats("testuser") } returns Response.success(dto)

    val result = repository.getStats("testuser")
    assertTrue(result is ApiResult.Success)
    assertEquals(150, (result as ApiResult.Success).data.totalSolved)
  }

  @Test
  fun `getStats returns error on exception`() = runTest {
    coEvery { api.getUserStats("testuser") } throws Exception("API down")

    val result = repository.getStats("testuser")
    assertTrue(result is ApiResult.Error)
  }
}
