package com.nexus.domain.repository

import com.nexus.common.ApiResult
import com.nexus.domain.model.Hackathon
import kotlinx.coroutines.flow.Flow

interface IHackathonRepository {
    fun getAllHackathons(): Flow<List<Hackathon>>
    fun getLiveHackathons(): Flow<List<Hackathon>>
    fun getSavedHackathons(): Flow<List<Hackathon>>
    suspend fun toggleSave(id: String, saved: Boolean)
    suspend fun refreshHackathons(): ApiResult<List<Hackathon>>
}
