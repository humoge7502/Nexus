package com.nexus.domain.repository

import com.nexus.domain.model.Skill
import kotlinx.coroutines.flow.Flow

interface ISkillGraphRepository {
    suspend fun initializeIfEmpty()

    /**
     * Makes the graph match the skills a student claims, so the radar shows the
     * same profile the matcher scores against. The given set is the whole truth:
     * skills dropped from it stop being shown as known.
     */
    suspend fun setDeclaredSkills(names: Collection<String>)
    suspend fun getRecommendedSkills(): List<Skill>
    suspend fun getUserSkills(): List<Skill>

    /** Emits again on every claim change, so a saved skill set reaches the radar without a restart. */
    fun observeUserSkills(): Flow<List<Skill>>
}
