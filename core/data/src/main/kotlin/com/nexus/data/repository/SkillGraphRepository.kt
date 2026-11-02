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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SkillGraphRepository @Inject constructor(
    private val skillGraphDao: SkillGraphDao,
    private val analyticsTracker: AnalyticsTracker,
    private val api: NexusApiService,
) : ISkillGraphRepository {

    /** Chip wording → curated node id, for claims whose text differs from the display name. */
    private val claimToCuratedNode = mapOf("android" to "android")

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

    override suspend fun setDeclaredSkills(names: Collection<String>) {
        val declared = names.map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        val declaredLower = declared.map { it.lowercase() }.toSet()

        skillGraphDao.getUserSkills().first().forEach { node ->
            if (node.displayName.lowercase() !in declaredLower) {
                skillGraphDao.updateUserSkill(node.skillId, known = false, proficiency = 0)
            }
        }

        declared.forEach { name ->
            val existing = skillGraphDao.getSkillByName(name)
                ?: name.lowercase().let { claimToCuratedNode[it] }?.let { skillGraphDao.getSkill(it) }
            if (existing != null) {
                skillGraphDao.updateUserSkill(existing.skillId, known = true, proficiency = 3)
            } else {
                // A claim the catalog has no node for — a term saved before the graph
                // covered it, or one added to the picker without curating a node. The
                // radar should still show what the student actually claimed, so the
                // claim becomes its own node rather than silently vanishing.
                skillGraphDao.insertNodes(
                    listOf(
                        SkillNodeEntity(
                            skillId = declaredSkillId(name),
                            displayName = name,
                            category = SkillCategory.GENERAL.name,
                            popularityScore = 0f,
                            isUserKnown = true,
                            userProficiency = 3,
                        ),
                    ),
                )
            }
        }
        analyticsTracker.track("skills_declared", mapOf("count" to declared.size))
    }

    private fun declaredSkillId(name: String) =
        "declared_" + name.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')

    override suspend fun getRecommendedSkills(): List<Skill> {
        val userSkills = skillGraphDao.getUserSkills().first()
        val userSkillIds = userSkills.map { node -> node.skillId }
        val recommended = skillGraphDao.getRecommendedSkills(userSkillIds, 5)
        return recommended.map { it.toDomain() }
    }

    override suspend fun getUserSkills(): List<Skill> {
        return skillGraphDao.getUserSkills().first().map { it.toDomain() }
    }

    override fun observeUserSkills(): Flow<List<Skill>> =
        skillGraphDao.getUserSkills().map { nodes -> nodes.map { it.toDomain() } }

    fun getUserSkillCount(): kotlinx.coroutines.flow.Flow<Int> = skillGraphDao.getUserSkillCount()

    private fun SkillNodeEntity.toDomain() = Skill(
        id = skillId,
        name = displayName,
        category = try { SkillCategory.valueOf(category) } catch (e: Exception) { SkillCategory.GENERAL },
        proficiency = userProficiency.coerceIn(0, 3),
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