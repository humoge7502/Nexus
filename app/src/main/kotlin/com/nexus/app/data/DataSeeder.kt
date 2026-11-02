package com.nexus.app.data

import android.content.Context
import com.nexus.data.repository.OpportunityFeedRepository
import com.nexus.database.NexusDatabase
import com.nexus.database.entity.AchievementEntity
import com.nexus.database.entity.CertificationEntity
import com.nexus.database.entity.DSAProblemEntity
import com.nexus.datastore.UserPreferences
import com.nexus.domain.repository.ISkillGraphRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

/**
 * First-launch seeding from bundled datasets. Opportunities are a real,
 * verified capture of the published feed (assets/opportunities_seed.json),
 * so the very first run has genuine listings even with no connection; the
 * next refresh replaces them. Certifications, DSA problems and badge
 * definitions also seed from assets.
 *
 * Runs only when the corresponding table is empty, so user progress is never
 * overwritten.
 */
@Singleton
class DataSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: NexusDatabase,
    private val feed: OpportunityFeedRepository,
    private val skillGraphRepository: ISkillGraphRepository,
    private val userPreferences: UserPreferences,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun seedIfEmpty() {
        scope.launch { seedOpportunities() }
        scope.launch { seedCertifications() }
        scope.launch { seedDsaProblems() }
        scope.launch { seedAchievements() }
        scope.launch { seedSkillGraph() }
    }

    /**
     * The graph backs the skill radar, the "recommended next skill" list and the
     * context AI answers are grounded in, and nothing had been asking for it —
     * so it stayed empty and every profile looked like its student knew nothing.
     *
     * Students who already finished onboarding are repaired in the same pass:
     * their claims live in preferences, and the graph adopts them. Sequencing
     * matters — the catalog is seeded first so a claim matches a curated node
     * instead of inventing a duplicate for it.
     */
    private suspend fun seedSkillGraph() = withContext(Dispatchers.IO) {
        runCatching { skillGraphRepository.initializeIfEmpty() }
            .onFailure { android.util.Log.e("DataSeeder", "skill graph seed failed", it) }
        runCatching {
            val declared = userPreferences.userSkills.first()
            if (declared.isNotEmpty() && skillGraphRepository.getUserSkills().isEmpty()) {
                skillGraphRepository.setDeclaredSkills(declared)
            }
        }.onFailure { android.util.Log.e("DataSeeder", "skill claim backfill failed", it) }
    }

    /**
     * Badge definitions live in the DB so unlock state survives restarts.
     * IDs are the lowercase enum names — BadgeUnlockEvaluator unlocks by that id.
     */
    private suspend fun seedAchievements() = withContext(Dispatchers.IO) {
        val dao = database.achievementDao()
        if (dao.getAllEntities().isNotEmpty()) return@withContext
        dao.insertAll(
            listOf(
                AchievementEntity(
                    id = "first_internship", type = "FIRST_INTERNSHIP",
                    title = "First Application", description = "Submit your first internship application",
                    xpReward = 50,
                ),
                AchievementEntity(
                    id = "first_certification", type = "FIRST_CERTIFICATION",
                    title = "First Certification", description = "Complete your first certification",
                    xpReward = 100,
                ),
                AchievementEntity(
                    id = "streak_7", type = "STREAK_7",
                    title = "Week Warrior", description = "Keep a 7-day learning streak",
                    xpReward = 75,
                ),
                AchievementEntity(
                    id = "streak_30", type = "STREAK_30",
                    title = "Unstoppable", description = "Keep a 30-day learning streak",
                    xpReward = 300,
                ),
                AchievementEntity(
                    id = "ten_applications", type = "TEN_APPLICATIONS",
                    title = "Application Machine", description = "Submit 10 internship applications",
                    xpReward = 150,
                ),
                AchievementEntity(
                    id = "five_certs", type = "FIVE_CERTS",
                    title = "Certified Pro", description = "Complete 5 certifications",
                    xpReward = 250,
                ),
                AchievementEntity(
                    id = "master_coder", type = "MASTER_CODER",
                    title = "Master Coder", description = "Solve 50 DSA problems",
                    xpReward = 200,
                ),
            )
        )
    }

    private suspend fun seedOpportunities() = withContext(Dispatchers.IO) {
        if (database.internshipDao().countAll() > 0 || database.hackathonDao().countAll() > 0) {
            return@withContext
        }
        runCatching {
            val json = context.assets.open("opportunities_seed.json").bufferedReader().use { it.readText() }
            feed.importSnapshot(json)
        }.onFailure { android.util.Log.e("DataSeeder", "opportunity seed failed", it) }
    }

    private suspend fun seedCertifications() = withContext(Dispatchers.IO) {
        val dao = database.certificationDao()
        if (dao.countAllCertifications() > 0) return@withContext
        runCatching {
            val json = context.assets.open("certifications_seed.json").bufferedReader().use { it.readText() }
            val arr = JSONArray(json)
            val entities = mutableListOf<CertificationEntity>()
            for (i in 0 until arr.length()) {
                val c = arr.getJSONObject(i)
                val skills = c.optJSONArray("skills") ?: JSONArray()
                val skillStr = (0 until skills.length()).joinToString(",") { skills.getString(it) }
                entities += CertificationEntity(
                    id = c.optString("id", "cert_$i"),
                    title = c.optString("title", ""),
                    provider = c.optString("provider", ""),
                    estimatedHours = c.optInt("estimatedHours", 0),
                    skills = skillStr,
                    rating = c.optDouble("rating", 0.0).toFloat(),
                    enrollmentUrl = c.optString("enrollmentUrl", ""),
                    isTrending = i < 6,
                    employerRecognition = 0,
                )
            }
            if (entities.isNotEmpty()) dao.insertAll(entities)
        }.onFailure { android.util.Log.e("DataSeeder", "certification seed failed", it) }
    }

    private suspend fun seedDsaProblems() = withContext(Dispatchers.IO) {
        val dao = database.dsaProblemDao()
        if (dao.countAllProblems() > 0) return@withContext
        runCatching {
            val json = context.assets.open("dsa_problems_seed.json").bufferedReader().use { it.readText() }
            val arr = JSONArray(json)
            val entities = mutableListOf<DSAProblemEntity>()
            for (i in 0 until arr.length()) {
                val p = arr.getJSONObject(i)
                val topics = p.optJSONArray("topics") ?: JSONArray()
                val topicStr = (0 until topics.length()).joinToString(",") { topics.getString(it) }
                entities += DSAProblemEntity(
                    id = p.optString("id", "dsa_$i"),
                    title = p.optString("title", ""),
                    difficulty = p.optString("difficulty", "EASY"),
                    category = topicStr.substringBefore(','),
                    platform = "LeetCode",
                    link = p.optString("url", ""),
                    topics = topicStr,
                    frequency = 0f,
                    status = "NOT_ATTEMPTED",
                    solvedAt = null,
                    userNotes = "",
                )
            }
            if (entities.isNotEmpty()) dao.insertAll(entities)
        }.onFailure { android.util.Log.e("DataSeeder", "dsa seed failed", it) }
    }
}
