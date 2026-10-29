package com.nexus.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nexus.database.NexusDatabase
import com.nexus.database.entity.CohortIntelligenceEntity
import com.nexus.database.entity.PlacementForecastEntity
import com.nexus.database.entity.ScoreSnapshotEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Nexus2DaoTest {

  private lateinit var db: NexusDatabase

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, NexusDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun forecastsAreScopedToUserAndPrunedByTime() = runTest {
    val dao = db.placementForecastDao()
    dao.insertAll(
      listOf(
        PlacementForecastEntity(
          id = "old", userId = "u1", companyTier = "TIER_1",
          probabilityScore = 0.2f, calculatedAt = 1_000L,
        ),
        PlacementForecastEntity(
          id = "new", userId = "u1", companyTier = "TIER_1",
          probabilityScore = 0.8f, calculatedAt = 2_000L,
        ),
        PlacementForecastEntity(
          id = "other", userId = "u2", companyTier = "TIER_2",
          probabilityScore = 0.5f, calculatedAt = 2_000L,
        ),
      )
    )

    val forUser = dao.getById("u1").first()
    assertEquals(2, forUser.size)
    assertEquals("new", forUser[0].id)

    dao.deleteOlderThan("u1", before = 1_500L)
    assertEquals(listOf("new"), dao.getById("u1").first().map { it.id })
    assertEquals(1, dao.getById("u2").first().size)
  }

  @Test
  fun cohortAggregatesAndPlacementRecording() = runTest {
    val dao = db.cohortIntelligenceDao()
    dao.insert(
      CohortIntelligenceEntity(
        id = "c1", collegeId = "college_a", semester = "YEAR_3",
        companyTier = "TIER_1", totalStudents = 100, placedCount = 40,
        avgScore = 720f, avgStreak = 9f, skills = "kotlin,dsa",
        updatedAt = 1_000L,
      )
    )
    dao.insert(
      CohortIntelligenceEntity(
        id = "c2", collegeId = "college_a", semester = "YEAR_3",
        companyTier = "TIER_2", totalStudents = 100, placedCount = 20,
        avgScore = 620f, avgStreak = 5f, updatedAt = 1_000L,
      )
    )

    val entries = dao.getByCollege("college_a").first()
    assertEquals(2, entries.size)

    val breakdown = dao.getCollegeBreakdown("college_a")
    assertEquals(2, breakdown.size)

    dao.recordPlacement("college_a", "TIER_1", "YEAR_3")
    val tierOne = dao.getLatestForCollegeAndTier("college_a", "TIER_1")
    assertEquals(41, tierOne?.placedCount)

    val rate = dao.getCollegePlacementRate("college_a")
    assertTrue(rate != null && rate > 0f)
  }

  @Test
  fun scoreSnapshotsReturnNewestFirstAndLatest() = runTest {
    val dao = db.scoreSnapshotDao()
    dao.insert(ScoreSnapshotEntity(id = "s1", userId = "u1", score = 500, recordedAt = 100L))
    dao.insert(ScoreSnapshotEntity(id = "s2", userId = "u1", score = 650, recordedAt = 300L))
    dao.insert(ScoreSnapshotEntity(id = "s3", userId = "u1", score = 600, recordedAt = 200L))

    val recent = dao.getRecentSnapshots("u1", limit = 2).first()
    assertEquals(listOf(650, 600), recent.map { it.score })
    assertEquals(650, dao.getLatestSnapshot("u1")?.score)
    assertNull(dao.getLatestSnapshot("missing"))
  }
}
