package com.nexus.app.data

import android.content.Context
import com.nexus.database.NexusDatabase
import com.nexus.database.entity.AchievementEntity
import com.nexus.database.entity.CertificationEntity
import com.nexus.database.entity.DSAProblemEntity
import com.nexus.database.entity.HackathonEntity
import com.nexus.database.entity.InternshipEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * First-launch seeding of the Room database from bundled real datasets
 * (assets/opportunities_seed.json — 17 internships and 4 hackathons,
 * certifications_seed.json — 57 certs, dsa_problems_seed.json — 147 problems,
 * roadmaps_seed.json — 10 roadmaps). This is what makes every screen show
 * real content on the very first run, offline, with zero backend needed.
 *
 * Runs only when the corresponding table is empty, so user progress
 * (saved internships, solved problems, statuses) is never overwritten.
 */
@Singleton
class DataSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: NexusDatabase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun seedIfEmpty() {
        scope.launch { seedInternships() }
        scope.launch { seedCertifications() }
        scope.launch { seedDsaProblems() }
        scope.launch { seedHackathons() }
        scope.launch { seedAchievements() }
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

    private suspend fun seedInternships() = withContext(Dispatchers.IO) {
        val dao = database.internshipDao()
        if (dao.countAll() > 0) return@withContext
        runCatching {
            val json = context.assets.open("opportunities_seed.json").bufferedReader().use { it.readText() }
            val arr = org.json.JSONObject(json).getJSONArray("opportunities")
            val entities = mutableListOf<InternshipEntity>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                if (o.optString("type") != "INTERNSHIP") continue
                val skills = o.optJSONArray("requiredSkills") ?: JSONArray()
                val skillStr = (0 until skills.length()).joinToString(",") { skills.getString(it) }
                val stipendStr = o.optString("stipend", "")
                val stipendAmount = Regex("\\d{2,7}").find(stipendStr.replace(",", ""))?.value?.toIntOrNull()
                val duration = o.optString("duration", "")
                val months = Regex("\\d+").find(duration)?.value?.toIntOrNull() ?: 2
                entities += InternshipEntity(
                    id = o.optString("id", "opp_$i"),
                    title = o.optString("role", "Internship"),
                    companyName = o.optString("company", ""),
                    companyLogoUrl = null,
                    location = o.optString("location", ""),
                    isRemote = o.optString("locationType", "") == "REMOTE",
                    stipendAmount = stipendAmount,
                    stipendCurrency = "INR",
                    durationMonths = months,
                    skills = skillStr,
                    deadline = o.optLong("deadline", 0L),
                    applyUrl = o.optString("applicationUrl", ""),
                    description = o.optString("description", ""),
                    postedAt = o.optLong("postedAt", 0L),
                    isSaved = false,
                    cachedAt = System.currentTimeMillis(),
                )
            }
            if (entities.isNotEmpty()) dao.insertAll(entities)
        }.onFailure { android.util.Log.e("DataSeeder", "internship seed failed", it) }
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

    private suspend fun seedHackathons() = withContext(Dispatchers.IO) {
        val dao = database.hackathonDao()
        if (dao.countAll() > 0) return@withContext
        runCatching {
            val json = context.assets.open("opportunities_seed.json").bufferedReader().use { it.readText() }
            val arr = org.json.JSONObject(json).getJSONArray("opportunities")
            val entities = mutableListOf<HackathonEntity>()
            for (i in 0 until arr.length()) {
                val h = arr.getJSONObject(i)
                if (h.optString("type") != "HACKATHON") continue
                val registrationDay = h.optLong("deadline", 0L).toEpochDayUtc()
                val tags = h.optJSONArray("tags") ?: JSONArray()
                entities += HackathonEntity(
                    id = h.optString("id", "hackathon_$i"),
                    title = h.optString("role", "Hackathon"),
                    organizer = h.optString("company", ""),
                    description = h.optString("description", ""),
                    mode = when (h.optString("locationType").uppercase()) {
                        "ONLINE", "REMOTE" -> "ONLINE"
                        "HYBRID" -> "HYBRID"
                        else -> "OFFLINE"
                    },
                    location = h.optString("location", "").ifBlank { null },
                    startDate = registrationDay,
                    endDate = registrationDay + 2,
                    registrationDeadline = registrationDay,
                    prizePool = h.optString("prizePool", ""),
                    registrationUrl = h.optString("applicationUrl", ""),
                    isLive = false,
                    isSaved = false,
                    tags = (0 until tags.length()).joinToString(",") { tags.getString(it) },
                )
            }
            if (entities.isNotEmpty()) dao.insertAll(entities)
        }.onFailure { android.util.Log.e("DataSeeder", "hackathon seed failed", it) }
    }

    private fun Long.toEpochDayUtc(): Long =
        if (this > 0) Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()
        else LocalDate.now().toEpochDay()
}
