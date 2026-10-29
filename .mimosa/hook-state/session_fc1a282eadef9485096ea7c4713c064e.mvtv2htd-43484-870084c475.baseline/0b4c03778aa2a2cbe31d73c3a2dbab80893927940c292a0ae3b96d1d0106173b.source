package com.nexus.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.ln

class PlacementReadinessFeaturesTest {

  @Test
  fun `maps raw counters into the trained feature space`() {
    val features = PlacementReadinessFeatures.fromRaw(
      PlacementReadinessFeatures.RawSignals(
        nexusScore = 750,
        dsaSolved = 100,
        dsaHardSolved = 25,
        dsaMediumSolved = 50,
        dsaAttempted = 10,
        applications = 20,
        interviews = 5,
        offers = 2,
        certifications = 3,
        projects = 4,
        streakDays = 30,
        skillCount = 10,
        mockInterviews = 6,
        collegeTier = "TIER_1",
      )
    )

    assertEquals(ln(101f), features.dsaSolvedLog, 1e-5f)
    assertEquals(0.25f, features.dsaHardRatio, 1e-6f)
    assertEquals(0.5f, features.dsaMediumRatio, 1e-6f)
    assertEquals(ln(21f), features.applicationsLog, 1e-5f)
    assertEquals(0.25f, features.interviewRate, 1e-6f)
    assertEquals(0.10f, features.offerRate, 1e-6f)
    assertEquals(ln(4f), features.certsLog, 1e-5f)
    assertEquals(ln(5f), features.projectsLog, 1e-5f)
    assertEquals(ln(31f), features.streakLog, 1e-5f)
    assertEquals(0.5f, features.skillsNorm, 1e-6f)
    assertEquals(ln(7f), features.mockInterviewsLog, 1e-5f)
    assertEquals(0.75f, features.scoreNorm, 1e-6f)
    assertEquals(1f, features.collegeTierNorm, 1e-6f)
    assertEquals(ln(111f), features.attemptsLog, 1e-5f)
  }

  @Test
  fun `empty profile produces finite neutral features`() {
    val features = PlacementReadinessFeatures.fromRaw(PlacementReadinessFeatures.RawSignals())
    features.toFloatArray().forEach { value ->
      assertTrue("feature must be finite", value.isFinite())
    }
    assertEquals(0.5f, features.scoreNorm, 1e-6f)
    assertEquals(0f, features.interviewRate, 1e-6f)
    assertEquals(0f, features.offerRate, 1e-6f)
    assertEquals(0.5f, features.collegeTierNorm, 1e-6f)
  }

  @Test
  fun `college tier mapping mirrors the training column`() {
    assertEquals(1f, PlacementReadinessFeatures.collegeTierNorm("TIER_1"), 1e-6f)
    assertEquals(0.75f, PlacementReadinessFeatures.collegeTierNorm("tier_2"), 1e-6f)
    assertEquals(0.5f, PlacementReadinessFeatures.collegeTierNorm("TIER_3"), 1e-6f)
    assertEquals(0.25f, PlacementReadinessFeatures.collegeTierNorm("TIER_4"), 1e-6f)
    assertEquals(0.5f, PlacementReadinessFeatures.collegeTierNorm(null), 1e-6f)
    assertEquals(0.5f, PlacementReadinessFeatures.collegeTierNorm("unknown"), 1e-6f)
  }

  @Test
  fun `more evidence never lowers the readiness prediction`() {
    val weak = PlacementReadinessModel.predict(
      PlacementReadinessFeatures.fromRaw(
        PlacementReadinessFeatures.RawSignals(
          nexusScore = 300,
          dsaSolved = 5,
          applications = 1,
          streakDays = 1,
        )
      )
    )
    val strong = PlacementReadinessModel.predict(
      PlacementReadinessFeatures.fromRaw(
        PlacementReadinessFeatures.RawSignals(
          nexusScore = 900,
          dsaSolved = 300,
          dsaHardSolved = 120,
          dsaMediumSolved = 120,
          applications = 40,
          interviews = 20,
          offers = 10,
          certifications = 8,
          projects = 10,
          streakDays = 200,
          skillCount = 18,
          mockInterviews = 25,
          collegeTier = "TIER_1",
        )
      )
    )
    assertTrue("strong profile $strong should beat weak profile $weak", strong > weak)
    assertTrue(strong in 0f..1f)
  }
}
