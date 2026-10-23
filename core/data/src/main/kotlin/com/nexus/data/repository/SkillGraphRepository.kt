package com.nexus.data.repository

import com.nexus.analytics.AnalyticsTracker
import com.nexus.database.dao.SkillGraphDao
import com.nexus.database.entity.SkillEdgeEntity
import com.nexus.database.entity.SkillNodeEntity
import com.nexus.data.skillgraph.SkillGraphSeedData
import com.nexus.domain.model.Skill
import com.nexus.domain.model.SkillCategory
import com.nexus.domain.repository.ISkillGraphRepository
import com.nexus.network.api.NexusApiService
import com.nexus.network.dto.SkillEdgeDto
import com.nexus.network.dto.SkillNodeDto
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SkillGraphRepository @Inject constructor(
    private val skillGraphDao: SkillGraphDao,
    private val analyticsTracker: AnalyticsTracker,
    private val api: NexusApiService,
) : ISkillGraphRepository {

    override suspend fun initializeIfEmpty() {
        if (skillGraphDao.getTotalSkillCount() == 0) {
            try {
                val nodesResponse = api.getSkillGraphNodes()
                val edgesResponse = api.getSkillGraphEdges()
                if (nodesResponse.isSuccessful && edgesResponse.isSuccessful) {
                    val nodeDtos = nodesResponse.body() ?: emptyList()
                    val edgeDtos = edgesResponse.body() ?: emptyList()
                    val nodes = nodeDtos.map { it.toEntity() }
                    val edges = edgeDtos.map { it.toEntity() }
                    skillGraphDao.insertNodes(nodes)
                    skillGraphDao.insertEdges(edges)
                    Timber.d("SkillGraph initialized with ${nodes.size} skills, ${edges.size} edges from API")
                } else {
                    // Fallback to seed data if API fails
                    skillGraphDao.insertNodes(SkillGraphSeedData.nodes)
                    SkillGraphSeedData.edges.forEach { skillGraphDao.insertEdge(it) }
                    Timber.w("Failed to fetch skill graph from API, using seed data. Nodes: ${SkillGraphSeedData.nodes.size}, Edges: ${SkillGraphSeedData.edges.size}")
                }
            } catch (e: Exception) {
                // Fallback to seed data on exception
                skillGraphDao.insertNodes(SkillGraphSeedData.nodes)
                SkillGraphSeedData.edges.forEach { skillGraphDao.insertEdge(it) }
                Timber.w(e, "Exception while fetching skill graph from API, using seed data")
            }
        }
    }

    override suspend fun markKnownSkill(skillId: String) {
        val node = skillGraphDao.getSkill(skillId)
        if (node != null) {
            skillGraphDao.updateUserSkill(skillId, known = true, proficiency = 3)
            Timber.d("Marked skill as known: $skillId")
        }
    }

    override suspend fun getRecommendedSkills(): List<Skill> {
        val userSkills = skillGraphDao.getUserSkills().first()
        val userSkillIds = userSkills.map { node -> node.skillId }
        val recommended = skillGraphDao.getRecommendedSkills(userSkillIds, 5)
        return recommended.map { it.toDomain() }
    }

    override suspend fun getUserSkills(): List<Skill> {
        return skillGraphDao.getUserSkills().first().map { it.toDomain() }
    }

    fun getUserSkillCount(): kotlinx.coroutines.flow.Flow<Int> = skillGraphDao.getUserSkillCount()

    private fun SkillNodeEntity.toDomain() = Skill(
        name = displayName,
        category = try { SkillCategory.valueOf(category) } catch (e: Exception) { SkillCategory.GENERAL }
    )

    private fun SkillNodeDto.toEntity() = SkillNodeEntity(
        skillId = id,
        displayName = displayName,
        category = category,
        difficultyLevel = level,
        userProficiency = proficiency
    )

    private fun SkillEdgeDto.toEntity() = SkillEdgeEntity(
        fromSkillId = fromId,
        toSkillId = toId,
        edgeType = relationship,
        weight = weight,
        transferability = confidence
    )
}