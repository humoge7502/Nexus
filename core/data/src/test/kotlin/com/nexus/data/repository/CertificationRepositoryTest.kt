package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.database.dao.CertificationDao
import com.nexus.database.entity.CertificationEntity
import com.nexus.network.api.NexusApiService
import com.nexus.network.dto.CertificationDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.junit4.MockKRule
import io.mockk.runs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

class CertificationRepositoryTest {
    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var api: NexusApiService

    @MockK
    private lateinit var dao: CertificationDao

    private lateinit var repository: CertificationRepository

    @Before
    fun setup() {
        repository = CertificationRepository(api, dao)
    }

    @Test
    fun `getAllCertifications returns mapped domain models`() = runTest {
        val entities = listOf(
            CertificationEntity(
                id = "1", title = "AWS Certified", provider = "Amazon",
                estimatedHours = 40, skills = "Cloud,AWS", rating = 4.5f,
                enrollmentUrl = "https://aws.amazon.com/certification",
                isTrending = true, employerRecognition = 90,
            )
        )
        every { dao.getAll() } returns flowOf(entities)

        val result = repository.getAllCertifications().first()
        assertEquals(1, result.size)
        assertEquals("AWS Certified", result[0].title)
    }

    @Test
    fun `getCertificationCount returns dao count`() = runTest {
        every { dao.getAll() } returns flowOf(
            listOf(
                CertificationEntity(id = "1", title = "C1", provider = "P1",
                    estimatedHours = 10, skills = "S1", rating = 4f,
                    enrollmentUrl = "https://url", isTrending = false, employerRecognition = 80),
                CertificationEntity(id = "2", title = "C2", provider = "P2",
                    estimatedHours = 20, skills = "S2", rating = 3f,
                    enrollmentUrl = "https://url2", isTrending = false, employerRecognition = 60),
            )
        )

        val count = repository.getCertificationCount()
        assertEquals(2, count)
    }

    @Test
    fun `refreshCertifications success inserts data`() = runTest {
        val dtos = listOf(
            CertificationDto(id = "1", title = "AWS", provider = "Amazon",
                estimatedHours = 40, skills = listOf("Cloud"), rating = 4.5f,
                enrollmentUrl = "https://aws.com"),
        )
        coEvery { api.getRecommendedCertifications(any()) } returns Response.success(dtos)
        coEvery { dao.insertAll(any()) } just runs

        val result = repository.refreshCertifications()
        assertTrue(result is ApiResult.Success)
        val data = (result as ApiResult.Success).data
        assertEquals(1, data.size)
        assertEquals("AWS", data[0].title)
        coVerify { dao.insertAll(any()) }
    }

    @Test
    fun `refreshCertifications error on API failure`() = runTest {
        coEvery { api.getRecommendedCertifications(any()) } throws Exception("API down")

        val result = repository.refreshCertifications()
        assertTrue(result is ApiResult.Error)
    }
}
