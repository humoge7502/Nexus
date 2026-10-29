package com.nexus.domain.repository

import com.nexus.domain.model.Skill

interface ISkillGraphRepository {
    suspend fun initializeIfEmpty()
    suspend fun markKnownSkill(skillId: String)
    suspend fun getRecommendedSkills(): List<Skill>
    suspend fun getUserSkills(): List<Skill>
}
