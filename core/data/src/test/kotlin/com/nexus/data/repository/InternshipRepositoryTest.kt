package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.database.dao.InternshipDao
import com.nexus.database.entity.InternshipEntity
import com.nexus.network.api.NexusApiService
import com.nexus.network.dto.InternshipDto
import com.nexus.network.dto.PaginatedResponse
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class InternshipRepositoryTest {

  @MockK
  private lateinit var api: NexusApiService

  @MockK
  private lateinit var dao: InternshipDao

  private lateinit var repository: InternshipRepository

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    repository = InternshipRepository(api, dao)
  }

  @Test
  fun `getAllInternships returns mapped domain models`() = runTest {
    val entities = listOf(
      InternshipEntity(
        id = "1", title = "Test Intern", companyName = "TestCo",
        location = "Remote", isRemote = true, durationMonths = 3,
        applyUrl = "https://test.com/apply", companyLogoUrl = null,
      ),
    )
    every { dao.getAllInternships() } returns flowOf(entities)

    val result = repository.getAllInternships().first()
    assert(result.size == 1)
    assert(result[0].title == "Test Intern")
    assert(result[0].company.name == "TestCo")
  }

  @Test
  fun `refreshInternships success inserts and returns success`() = runTest {
    val dtos = listOf(
      InternshipDto(
        id = "1", title = "Test", company = "TestCo",
        location = "Remote", isRemote = true,
        duration = "3 months", applyUrl = "https://test.com",
        skills = listOf("Kotlin", "Android"),
      ),
    )
    val response = PaginatedResponse(data = dtos, page = 0, totalPages = 1, total = 1)
    coEvery { api.getInternships(any(), any(), any(), any(), any()) } returns Response.success(response)
    coEvery { dao.deleteAll() } just runs
    coEvery { dao.insertAll(any()) } just runs

    val result = repository.refreshInternships()
    assert(result.isSuccess)
    coVerify { dao.deleteAll() }
    coVerify { dao.insertAll(any()) }
  }

  @Test
  fun `refreshInternships error returns error result`() = runTest {
    coEvery { api.getInternships(any(), any(), any(), any(), any()) } throws Exception("Network error")

    val result = repository.refreshInternships()
    assert(result.isError)
    assert(result.errorMessage == "Network error")
  }
}
