package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.database.dao.CertificationDao
import com.nexus.database.dao.CertificationProgressDao
import com.nexus.database.entity.CertificationEntity
import com.nexus.database.entity.CertificationProgressEntity
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

    @MockK
    private lateinit var progressDao: CertificationProgressDao

    private lateinit var repository: CertificationRepository

    @Before
    fun setup() {
        repository = CertificationRepository(api, dao, progressDao)
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
        every { progressDao.getAll() } returns flowOf(emptyList())

        val result = repository.getAllCertifications().first()
        assertEquals(1, result.size)
        assertEquals("AWS Certified", result[0].title)
        assertEquals(2, result[0].skills.size)
    }

    @Test
    fun `getAllCertifications attaches persisted progress`() = runTest {
        every { dao.getAll() } returns flowOf(
            listOf(
                CertificationEntity(
                    id = "1", title = "C1", provider = "Coursera",
                    estimatedHours = 10, skills = "S1", rating = 4f,
                    enrollmentUrl = "https://url", isTrending = false, employerRecognition = 80,
                ),
            ),
        )
        every { progressDao.getAll() } returns flowOf(
            listOf(
                CertificationProgressEntity(
                    certificationId = "1",
                    enrolledAt = 1_700_000_000_000,
                    completedModules = 2,
                    totalModules = 5,
                ),
            ),
        )

        val result = repository.getAllCertifications().first().single()
        assertNotNull(result.userProgress)
        assertEquals(2, result.userProgress?.completedModules)
        assertEquals(0.4f, result.userProgress?.progressPercent ?: 0f, 0.001f)
    }

    @Test
    fun `getCompletedCertificationCount returns progress flow count`() = runTest {
        every { progressDao.getCompletedCount() } returns flowOf(3)

        assertEquals(3, repository.getCompletedCertificationCount().first())
    }

    @Test
    fun `startCertification creates progress with module budget`() = runTest {
        coEvery { progressDao.getFor("1") } returns null
        coEvery { progressDao.upsert(any()) } just runs

        repository.startCertification("1", estimatedHours = 45)

        coVerify {
            progressDao.upsert(
                match {
                    it.certificationId == "1" &&
                        it.totalModules == 4 &&
                        it.completedModules == 0 &&
                        it.enrolledAt != null
                },
            )
        }
    }

    @Test
    fun `startCertification is idempotent when already tracked`() = runTest {
        coEvery {
            progressDao.getFor("1")
        } returns CertificationProgressEntity("1", totalModules = 4)

        repository.startCertification("1", estimatedHours = 40)

        coVerify(exactly = 0) { progressDao.upsert(any()) }
    }

    @Test
    fun `completeModule increments progress and marks completion`() = runTest {
        coEvery {
            progressDao.getFor("1")
        } returns CertificationProgressEntity("1", completedModules = 1, totalModules = 2)
        coEvery { progressDao.upsert(any()) } just runs

        val progress = repository.completeModule("1")

        assertNotNull(progress)
        assertEquals(2, progress?.completedModules)
        assertTrue(progress?.isCompleted == true)
        assertNotNull(progress?.completedAt)
    }

    @Test
    fun `completeModule returns null when certification already complete`() = runTest {
        coEvery {
            progressDao.getFor("1")
        } returns CertificationProgressEntity(
            "1", completedModules = 2, totalModules = 2, completedAt = 1_700_000_000_000,
        )

        assertEquals(null, repository.completeModule("1"))
        coVerify(exactly = 0) { progressDao.upsert(any()) }
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
        every { progressDao.getAll() } returns flowOf(emptyList())

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
