package com.nexus.domain.repository

import com.nexus.domain.model.PortfolioProject
import kotlinx.coroutines.flow.Flow

interface IPortfolioRepository {
    val projects: Flow<List<PortfolioProject>>
    fun getProjectCount(): Flow<Int>
    suspend fun addProject(project: PortfolioProject)
    suspend fun removeProject(id: String)
}
