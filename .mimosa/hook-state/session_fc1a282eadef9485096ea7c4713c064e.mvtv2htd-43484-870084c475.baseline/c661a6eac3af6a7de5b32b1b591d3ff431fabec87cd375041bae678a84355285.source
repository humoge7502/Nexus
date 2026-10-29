package com.nexus.data.usecase

import com.nexus.database.entity.CareerMemoryEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CareerMemoryManagerTest {
  private lateinit var mockDao: com.nexus.database.dao.CareerMemoryDao
  private lateinit var manager: CareerMemoryManager

  @Before
  fun setup() {
    mockDao = mockk(relaxed = true)
    manager = CareerMemoryManager(mockDao, mockk(relaxed = true))
  }

  @Test
  fun `extracts GOAL memory from want to phrase`() = runTest {
    manager.extractAndStoreMemories(
      aiResponse = "Great!",
      userMessage = "I want to work at Google as an SDE",
      conversationId = "c1", tool = "chat",
    )
    coVerify { mockDao.insert(match { it.memoryType == "GOAL" }) }
  }

  @Test
  fun `extracts COMPANY_INTEREST from Flipkart mention`() = runTest {
    manager.extractAndStoreMemories(
      aiResponse = "Nice!",
      userMessage = "I am targeting Flipkart and Swiggy",
      conversationId = "c1", tool = "chat",
    )
    coVerify { mockDao.insert(match { it.memoryType == "COMPANY_INTEREST" }) }
  }

  @Test
  fun `extracts WEAKNESS from struggling with phrase`() = runTest {
    manager.extractAndStoreMemories(
      aiResponse = "Let me help.",
      userMessage = "I am struggling with dynamic programming",
      conversationId = "c1", tool = "chat",
    )
    coVerify { mockDao.insert(match { it.memoryType == "WEAKNESS" }) }
  }

  @Test
  fun `buildLongitudinalContext returns empty for no memories`() = runTest {
    coEvery { mockDao.getTopMemories(any()) } returns emptyList()
    val ctx = manager.buildLongitudinalContext()
    assertTrue(ctx.isEmpty())
  }

  @Test
  fun `buildLongitudinalContext contains STUDENT CAREER HISTORY`() = runTest {
    coEvery { mockDao.getTopMemories(any()) } returns listOf(
      CareerMemoryEntity(memoryType = "GOAL", content = "User wants Google", relevanceScore = 0.9f),
    )
    val ctx = manager.buildLongitudinalContext()
    assertTrue(ctx.contains("STUDENT CAREER HISTORY"))
  }

  @Test
  fun `runMaintenance invokes decay and archive`() = runTest {
    manager.runMaintenance()
    coVerify { mockDao.applyDecay(any()) }
    coVerify { mockDao.archiveIrrelevant() }
  }

  @Test
  fun `no matching patterns inserts no memories`() = runTest {
    manager.extractAndStoreMemories(
      aiResponse = "OK.", userMessage = "Hello",
      conversationId = "c1", tool = "chat",
    )
    coVerify(exactly = 0) { mockDao.insert(any()) }
  }

  @Test
  fun `buildLongitudinalContext records reference for each memory`() = runTest {
    val mem = CareerMemoryEntity(id = "m1", memoryType = "GOAL", content = "Google", relevanceScore = 0.9f)
    coEvery { mockDao.getTopMemories(any()) } returns listOf(mem)
    manager.buildLongitudinalContext()
    coVerify { mockDao.recordReference("m1", any()) }
  }
}
