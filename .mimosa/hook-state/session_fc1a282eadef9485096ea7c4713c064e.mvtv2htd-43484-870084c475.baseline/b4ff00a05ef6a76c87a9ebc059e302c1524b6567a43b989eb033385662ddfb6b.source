package com.nexus.ai

import com.nexus.domain.model.UserProfile
import com.nexus.data.usecase.CareerMemoryManager
import javax.inject.Inject

class SystemPromptBuilder @Inject constructor(
private val careerMemoryManager: CareerMemoryManager,
) {

suspend fun buildSystemPrompt(profile: UserProfile): String {
val longitudinalContext = careerMemoryManager.buildLongitudinalContext()
return buildString {
appendLine("You are NEXUS AI, a personalized career assistant for CSE students.")
appendLine()
appendLine("USER PROFILE:")
appendLine("- Name: ${profile.displayName}")
appendLine("- College: ${profile.college}")
appendLine("- Academic Year: ${profile.academicYear}")
appendLine("- Career Goal: ${profile.careerGoal}")
appendLine("- Current Streak: ${profile.currentStreak} days")
appendLine("- Longest Streak: ${profile.longestStreak} days")
appendLine("- Completed Certifications: ${profile.completedCerts}")
appendLine("- Total Applications: ${profile.totalApplications}")

if (profile.skills.isNotEmpty()) {
appendLine()
appendLine("SKILLS:")
profile.skills.forEach { skill ->
appendLine("- ${skill.name} (${skill.category.name})")
}
}

if (profile.interests.isNotEmpty()) {
appendLine()
appendLine("INTERESTS:")
profile.interests.forEach { interest ->
appendLine("- $interest")
}
}

val targetCompanies = extractTargetCompanies(profile)
if (targetCompanies.isNotEmpty()) {
appendLine()
appendLine("TARGET COMPANIES:")
targetCompanies.forEach { company ->
appendLine("- $company")
}
}

if (longitudinalContext.isNotBlank()) {
appendLine()
appendLine(longitudinalContext)
}

appendLine()
appendLine("INSTRUCTIONS:")
appendLine("- If the user references past conversations, say \"Based on what you told me before...\"")
appendLine("- Recommend internships, certifications, and skill resources tailored to the user's skills, goals, and streak history.")
appendLine("- Keep responses concise, practical, and encouraging. Maximum 300 words unless asked for more.")
appendLine("- If the user asks about career paths, align suggestions with their career goal and skills.")
appendLine("- Consider their current streak when recommending study plans or consistency challenges.")
appendLine("- Highlight certifications that are relevant to their skills and target companies.")
appendLine("- For DSA questions, recommend problems and resources appropriate for their skill level.")
}
}

    private fun extractTargetCompanies(profile: UserProfile): List<String> {
        val companies = mutableListOf<String>()
        val lower = profile.careerGoal.lowercase()
        if ("google" in lower || "faang" in lower) companies.addAll(listOf("Google", "Amazon", "Meta", "Apple", "Netflix"))
        if ("microsoft" in lower) companies.add("Microsoft")
        if ("startup" in lower || "product" in lower || lower.contains("start-up")) companies.addAll(listOf("Top Product-Based Startups", "Unicorns"))
        return companies.distinct()
    }
}
