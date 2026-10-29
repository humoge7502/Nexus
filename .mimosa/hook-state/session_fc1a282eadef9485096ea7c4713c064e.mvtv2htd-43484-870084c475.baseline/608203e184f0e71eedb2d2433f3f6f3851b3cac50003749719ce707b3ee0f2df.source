package com.nexus.data.usecase

import com.nexus.analytics.AnalyticsTracker
import com.nexus.database.dao.CareerMemoryDao
import com.nexus.database.entity.CareerMemoryEntity
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CareerMemoryManager @Inject constructor(
private val careerMemoryDao: CareerMemoryDao,
private val analyticsTracker: AnalyticsTracker,
) {

suspend fun extractAndStoreMemories(
aiResponse: String,
userMessage: String,
conversationId: String,
tool: String,
) {
val memories = mutableListOf<CareerMemoryEntity>()

goalPatterns.forEach { pattern ->
pattern.find(userMessage)?.let { match ->
memories.add(
CareerMemoryEntity(
memoryType = "GOAL",
content = "User expressed: ${match.value.take(200)}",
sourceConversationId = conversationId,
sourceTool = tool,
relevanceScore = 0.9f,
tags = extractTags(match.value),
)
)
}
}

val companyPattern = Regex(
"(?i)\\b(Google|Microsoft|Amazon|Meta|Apple|Netflix|Flipkart|Swiggy|Zomato|Razorpay|CRED|Zepto|Meesho|PhonePe|Paytm|Ola|Uber|Atlassian|Adobe|Oracle|SAP|Salesforce|Infosys|TCS|Wipro|HCL|Cognizant)\\b"
)
val mentionedCompanies = companyPattern.findAll(userMessage)
.map { it.value }
.distinct()
.toList()
if (mentionedCompanies.isNotEmpty()) {
memories.add(
CareerMemoryEntity(
memoryType = "COMPANY_INTEREST",
content = "User mentioned companies: ${mentionedCompanies.joinToString(", ")}",
sourceConversationId = conversationId,
sourceTool = tool,
relevanceScore = 0.85f,
tags = mentionedCompanies.joinToString(",").lowercase(),
)
)
}

skillPattern.findAll(userMessage).forEach { match ->
memories.add(
CareerMemoryEntity(
memoryType = "SKILL_PROGRESS",
content = "User reported: ${match.value.take(150)}",
sourceConversationId = conversationId,
sourceTool = tool,
relevanceScore = 0.8f,
tags = extractTags(match.value),
)
)
}

weaknessPattern.find(userMessage)?.let { match ->
memories.add(
CareerMemoryEntity(
memoryType = "WEAKNESS",
content = "User expressed difficulty: ${match.value.take(200)}",
sourceConversationId = conversationId,
sourceTool = tool,
relevanceScore = 0.9f,
tags = extractTags(match.value),
)
)
}

achievementPattern.find(userMessage)?.let { match ->
memories.add(
CareerMemoryEntity(
memoryType = "ACHIEVEMENT",
content = "User achievement: ${match.value.take(200)}",
sourceConversationId = conversationId,
sourceTool = tool,
relevanceScore = 0.85f,
tags = extractTags(match.value),
)
)
}

memories.forEach { memory ->
careerMemoryDao.insert(memory)
Timber.d("CareerMemory stored: ${memory.memoryType} — ${memory.content.take(50)}")
}
if (memories.isNotEmpty()) {
analyticsTracker.track(
"career_memory_extracted",
mapOf("count" to memories.size, "tool" to tool),
)
}
}

suspend fun buildLongitudinalContext(): String {
val topMemories = careerMemoryDao.getTopMemories(limit = 15)
if (topMemories.isEmpty()) return ""
val grouped = topMemories.groupBy { it.memoryType }
val sb = StringBuilder()
sb.appendLine("=== STUDENT CAREER HISTORY (from past conversations) ===")
grouped["GOAL"]?.take(3)?.let { goals ->
sb.appendLine("Known Goals:")
goals.forEach { sb.appendLine(" • ${it.content}") }
}
grouped["COMPANY_INTEREST"]?.take(2)?.let { companies ->
sb.appendLine("Target Companies:")
companies.forEach { sb.appendLine(" • ${it.content}") }
}
grouped["WEAKNESS"]?.take(3)?.let { weaknesses ->
sb.appendLine("Known Challenges:")
weaknesses.forEach { sb.appendLine(" • ${it.content}") }
}
grouped["SKILL_PROGRESS"]?.take(3)?.let { skills ->
sb.appendLine("Recent Skill Progress:")
skills.forEach { sb.appendLine(" • ${it.content}") }
}
grouped["ACHIEVEMENT"]?.take(2)?.let { achievements ->
sb.appendLine("Recent Achievements:")
achievements.forEach { sb.appendLine(" • ${it.content}") }
}
sb.appendLine("=== END HISTORY ===")
topMemories.forEach { careerMemoryDao.recordReference(it.id) }
return sb.toString()
}

suspend fun runMaintenance() {
careerMemoryDao.applyDecay()
careerMemoryDao.archiveIrrelevant()
Timber.d("CareerMemory maintenance complete")
}

suspend fun storeManualMemory(type: String, content: String, tags: String = "") {
careerMemoryDao.insert(
CareerMemoryEntity(
memoryType = type,
content = content,
relevanceScore = 1.0f,
tags = tags,
referenceCount = 1,
)
)
}

private fun extractTags(text: String): String {
val techTags = listOf(
"kotlin", "java", "python", "ml", "ai", "android", "web", "backend",
"frontend", "dsa", "system_design", "aws", "cloud", "data_science",
"flutter", "react", "node", "sql", "git", "docker",
)
return techTags.filter { text.lowercase().contains(it) }.joinToString(",")
}

companion object {
val goalPatterns = listOf(
Regex("""(?i)(want to|planning to|goal is to|aiming for|targeting)\s+(.{10,80})"""),
Regex("""(?i)(interested in|passionate about|excited about)\s+(.{10,60})"""),
Regex("""(?i)(dream (?:company|role|job) is|want to work at|targeting)\s+(.{5,50})"""),
)
val skillPattern = Regex(
"(?i)(learned|finished|completed|practicing|studying|mastering)\\s+" +
"(([A-Z][a-zA-Z+#.\\s]*?)(?:course|tutorial|project|framework|language|library|tool|platform|certification)?)",
)
val weaknessPattern = Regex("""(?i)(struggling with|weak in|don't understand|confused about|need help with|bad at|failing)\s+(.{5,60})""")
val achievementPattern = Regex("""(?i)(cleared|passed|got|completed|won|selected|shortlisted|placed at)\s+(.{5,80})""")
}
}
