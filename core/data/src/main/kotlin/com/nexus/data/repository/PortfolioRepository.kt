package com.nexus.data.repository

import com.nexus.database.dao.ProjectDao
import com.nexus.database.entity.ProjectEntity
import com.nexus.domain.model.PortfolioProject
import com.nexus.domain.model.SkillCategory
import com.nexus.domain.repository.IPortfolioRepository
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PortfolioRepository @Inject constructor(
    private val projectDao: ProjectDao
) : IPortfolioRepository {
    override val projects: Flow<List<PortfolioProject>> = projectDao.getAll().map { entities ->
        entities.map { entity ->
            PortfolioProject(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                techStack = entity.techStack
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .toImmutableList(),
                githubUrl = entity.githubUrl,
                liveUrl = entity.liveUrl,
                imageUrl = entity.imageUrl,
                starCount = entity.starCount,
                isFeatured = entity.isFeatured,
                completedAt = entity.completedAt?.let { LocalDate.ofEpochDay(it) },
                category = runCatching { SkillCategory.valueOf(entity.category) }
                    .getOrDefault(SkillCategory.GENERAL)
            )
        }
    }

    override fun getProjectCount(): Flow<Int> = projectDao.getAll().map { it.size }

    override suspend fun addProject(project: PortfolioProject) {
        val entity = ProjectEntity(
            id = project.id,
            title = project.title,
            description = project.description,
            techStack = project.techStack.joinToString(","),
            githubUrl = project.githubUrl,
            liveUrl = project.liveUrl,
            imageUrl = project.imageUrl,
            starCount = project.starCount,
            isFeatured = project.isFeatured,
            completedAt = project.completedAt?.toEpochDay(),
            category = project.category.name
        )
        projectDao.insert(entity)
    }

    override suspend fun removeProject(id: String) {
        projectDao.delete(id)
    }
}