package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.network.service.CodeforcesProblemDto
import com.nexus.network.service.CodeforcesResponse
import com.nexus.network.service.CodeforcesService
import com.nexus.network.service.CodeforcesRatingChangeDto
import com.nexus.network.service.CodeforcesSubmissionDto
import com.nexus.network.service.CodeforcesUserDto
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CodeforcesRepositoryTest {
  @MockK
  private lateinit var api: CodeforcesService

  private lateinit var repository: CodeforcesRepository

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    repository = CodeforcesRepository(api)
  }

  @Test
  fun `getProfile maps the ok envelope`() = runTest {
    coEvery { api.getUserInfo("tourist") } returns okUsers(
      CodeforcesUserDto(
        handle = "tourist",
        rating = 3_900,
        maxRating = 3_979,
        rank = "legendary grandmaster",
        maxRank = "legendary grandmaster",
        contribution = 180,
        organization = "IN PN NS NWU",
      ),
    )

    val result = repository.getProfile("tourist")
    assertTrue(result is ApiResult.Success)
    val profile = (result as ApiResult.Success).data
    assertEquals("tourist", profile.handle)
    assertEquals(3_900, profile.rating)
    assertEquals(3_979, profile.maxRating)
    assertEquals("legendary grandmaster", profile.rank)
  }

  @Test
  fun `an unrated handle keeps null ratings`() = runTest {
    coEvery { api.getUserInfo("newbie") } returns okUsers(CodeforcesUserDto(handle = "newbie"))

    val result = repository.getProfile("newbie")
    assertTrue(result is ApiResult.Success)
    val profile = (result as ApiResult.Success).data
    assertNull(profile.rating)
    assertNull(profile.rank)
  }

  @Test
  fun `a failed envelope carries the api comment`() = runTest {
    coEvery { api.getUserInfo("ghost") } returns CodeforcesResponse(
      status = "FAILED",
      comment = "handles: No handle with handle ghost found",
    )

    val result = repository.getProfile("ghost")
    assertTrue(result is ApiResult.Error)
    assertTrue((result as ApiResult.Error).message.contains("No handle"))
  }

  @Test
  fun `rating history maps to points with deltas in feed order`() = runTest {
    coEvery { api.getRatingHistory("tourist") } returns okRatings(
      CodeforcesRatingChangeDto(contestName = "Round A", oldRating = 1_400, newRating = 1_420, ratingUpdateTimeSeconds = 1_000L),
      CodeforcesRatingChangeDto(contestName = "Round B", oldRating = 1_420, newRating = 1_500, ratingUpdateTimeSeconds = 2_000L),
    )

    val result = repository.getRatingHistory("tourist")
    assertTrue(result is ApiResult.Success)
    val points = (result as ApiResult.Success).data
    assertEquals(2, points.size)
    assertEquals(20, points[0].delta)
    assertEquals(80, points[1].delta)
    assertEquals(1_500, points[1].newRating)
  }

  @Test
  fun `repeated lookups are served from the ttl cache`() = runTest {
    var now = 1_000L
    repository.nowMillis = { now }
    coEvery { api.getUserInfo("tourist") } returns okUsers(CodeforcesUserDto(handle = "tourist", rating = 3_900))

    repository.getProfile("tourist")
    now += 60_000L
    repository.getProfile("Tourist")

    coVerify(exactly = 1) { api.getUserInfo(any()) }
  }

  @Test
  fun `recent form counts unique accepted problems only`() = runTest {
    coEvery { api.getRecentSubmissions("tourist", any(), any()) } returns okSubmissions(
      submission(problem = "Two Sum", verdict = "OK"),
      submission(problem = "Two Sum", verdict = "OK"),
      submission(problem = "Three Sum", verdict = "WRONG_ANSWER"),
      submission(problem = "Watermelon", verdict = "OK"),
    )

    val result = repository.getRecentForm("tourist")
    assertTrue(result is ApiResult.Success)
    val form = (result as ApiResult.Success).data
    assertEquals(2, form.solvedCount)
    assertEquals(4, form.attemptsCount)
    assertEquals(4, form.windowSize)
  }

  private fun okUsers(vararg items: CodeforcesUserDto) =
    CodeforcesResponse(status = "OK", result = items.toList())

  private fun okRatings(vararg items: CodeforcesRatingChangeDto) =
    CodeforcesResponse(status = "OK", result = items.toList())

  private fun okSubmissions(vararg items: CodeforcesSubmissionDto) =
    CodeforcesResponse(status = "OK", result = items.toList())

  private fun submission(problem: String, verdict: String) = CodeforcesSubmissionDto(
    id = 1L,
    creationTimeSeconds = 1L,
    problem = CodeforcesProblemDto(name = problem, rating = 1_200),
    verdict = verdict,
  )
}
