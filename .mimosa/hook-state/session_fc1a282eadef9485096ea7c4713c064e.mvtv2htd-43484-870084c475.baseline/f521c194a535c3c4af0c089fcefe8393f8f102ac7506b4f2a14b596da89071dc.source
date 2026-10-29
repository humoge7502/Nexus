package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.database.dao.AIDao
import com.nexus.database.entity.AIConversationEntity
import com.nexus.network.api.NexusApiService
import com.nexus.network.dto.ChatResponseDto
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

class AIConversationRepositoryTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var api: NexusApiService

    @MockK
    private lateinit var dao: AIDao

    private lateinit var repository: AIConversationRepository

    @Before
    fun setup() {
        repository = AIConversationRepository(api, dao)
    }

    @Test
    fun `blank assistant reply is an error and is never persisted`() = runTest {
        coEvery { api.sendChatMessage(any()) } returns Response.success(ChatResponseDto(reply = "   "))

        val result = repository.sendMessage("conv_1", "How do I prep for interviews?")

        assertTrue("a blank reply must not be reported as success", result is ApiResult.Error)
        coVerify(exactly = 0) { dao.updateMessages(any(), any(), any()) }
    }

    @Test
    fun `real assistant reply is persisted and returned`() = runTest {
        coEvery { api.sendChatMessage(any()) } returns
            Response.success(ChatResponseDto(reply = "Start with arrays and two pointers."))
        coEvery { dao.getConversationById("conv_1") } returns
            AIConversationEntity(id = "conv_1", title = "Chat", messages = "")
        coEvery { dao.updateMessages(any(), any(), any()) } just Runs

        val result = repository.sendMessage("conv_1", "Where do I start?")

        assertEquals("sendMessage failed: ${result.errorMessage}", "Start with arrays and two pointers.", result.dataOrNull)
        coVerify(exactly = 2) { dao.updateMessages("conv_1", any(), any()) }
    }
}
