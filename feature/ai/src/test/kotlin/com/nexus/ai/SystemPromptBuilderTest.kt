package com.nexus.ai

import com.nexus.data.usecase.CareerMemoryManager
import com.nexus.domain.model.UserProfile
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class SystemPromptBuilderTest {
    private lateinit var careerMemoryManager: CareerMemoryManager
    private lateinit var builder: SystemPromptBuilder

    @Before
    fun setup() {
        careerMemoryManager = mock(CareerMemoryManager::class.java)
        builder = SystemPromptBuilder(careerMemoryManager)

        `when`(careerMemoryManager.buildLongitudinalContext()).thenReturn("")
    }

    @Test
    fun `buildSystemPrompt includes user name`() = runTest {
        val profile = UserProfile(
            id = "1",
            firstName = "John",
            email = "john@test.com",
            skills = persistentListOf(),
            interests = persistentListOf(),
        )
        val prompt = builder.buildSystemPrompt(profile)
        assertTrue(prompt.contains("John"))
    }

    @Test
    fun `buildSystemPrompt includes skills when present`() = runTest {
        val profile = UserProfile(
            id = "1",
            firstName = "Jane",
            email = "jane@test.com",
            skills = persistentListOf(
                com.nexus.domain.model.Skill("Kotlin"),
                com.nexus.domain.model.Skill("Android"),
            ),
            interests = persistentListOf(),
        )
        val prompt = builder.buildSystemPrompt(profile)
        assertTrue(prompt.contains("Kotlin"))
        assertTrue(prompt.contains("Android"))
    }

    @Test
    fun `buildSystemPrompt handles empty profile gracefully`() = runTest {
        val profile = UserProfile(
            id = "",
            firstName = "",
            email = "",
            skills = persistentListOf(),
            interests = persistentListOf(),
        )
        val prompt = builder.buildSystemPrompt(profile)
        assertTrue(prompt.isNotBlank())
    }
}
