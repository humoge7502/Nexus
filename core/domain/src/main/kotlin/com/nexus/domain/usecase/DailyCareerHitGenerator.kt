package com.nexus.domain.usecase

import com.nexus.common.Constants
import com.nexus.domain.model.CareerHitCategory
import com.nexus.domain.model.DailyCareerHit
import com.nexus.network.api.NexusApiService
import com.nexus.network.dto.CareerHitDto
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.collections.arrayListOf

@Singleton
class DailyCareerHitGenerator @Inject constructor(
    private val api: NexusApiService,
) {

    private val hits = CopyOnWriteArrayList<DailyCareerHit>()
    private var initialized = false

    init {
        // Initialize lazily on first call to avoid blocking startup
    }

    private suspend fun initialize() {
        if (initialized) return
        try {
            val response = api.getDailyCareerHits()
            val dtoList = if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
            val hitList = dtoList.map { dto ->
                DailyCareerHit(
                    id = dto.id,
                    title = dto.title,
                    tip = dto.description,
                    category = when (dto.category) {
                        "CAREER_TIP" -> CareerHitCategory.CAREER_TIP
                        "INTERVIEW_TIP" -> CareerHitCategory.INTERVIEW_TIP
                        "PRODUCTIVITY_TIP" -> CareerHitCategory.PRODUCTIVITY_TIP
                        "DSA_TIP" -> CareerHitCategory.DSA_TIP
                        "RESUME_TIP" -> CareerHitCategory.RESUME_TIP
                        "NETWORKING_TIP" -> CareerHitCategory.NETWORKING_TIP
                        "SKILL_TIP" -> CareerHitCategory.SKILL_TIP
                        else -> CareerHitCategory.CAREER_TIP
                    },
                    xpReward = dto.xpReward
                )
            }
            if (hitList.isNotEmpty()) {
                hits.clear()
                hits.addAll(hitList)
            } else {
                loadHardcodedHits()
            }
        } catch (e: Exception) {
            loadHardcodedHits()
        }
        initialized = true
    }

    private fun loadHardcodedHits() {
        hits.clear()
        hits.addAll(arrayListOf(
            DailyCareerHit(
                "hit_1", "Build Projects That Solve Real Problems",
                "Instead of TODO apps, build something people actually need. A project that helps 10 real users is worth more than 10 projects on GitHub.",
                CareerHitCategory.CAREER_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_2", "Master the STAR Method",
                "Situation, Task, Action, Result. Structure every interview answer this way. It makes your responses clear, concise, and memorable.",
                CareerHitCategory.INTERVIEW_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_3", "The Pomodoro Technique for Coders",
                "25 min focus, 5 min break. After 4 cycles, take a 15-30 min break. This boosts deep work and prevents burnout during long coding sessions.",
                CareerHitCategory.PRODUCTIVITY_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_4", "LeetCode Smart Practice",
                "Don't grind randomly. Focus on one pattern per week (Sliding Window, DP, Graphs). Master 10 patterns to solve 80% of interview problems.",
                CareerHitCategory.DSA_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_5", "Your LinkedIn Headline Matters",
                "Don't just put 'Student'. Use: 'CSE @ University | Building [project] | Passionate about [field]'. Recruiters search by keywords.",
                CareerHitCategory.RESUME_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_6", "Networking Over Applications",
                "Referrals are 5x more effective than cold applying. Connect with alumni on LinkedI engage with their posts, and ask for 15-min coffee chats.",
                CareerHitCategory.NETWORKING_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_7", "Learn by Teaching",
                "Write blog posts or make videos about what you learn. Teaching solidifies concepts and builds your personal brand. Start with one post this week.",
                CareerHitCategory.SKILL_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_8", "The 80/20 Rule for Certifications",
                "Focus on certifications that employers actually recognize: AWS, Google Cloud, Meta. One high-value cert > five low-value ones.",
                CareerHitCategory.CAREER_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_9", "Read Code, Not Just Write It",
                "Read open-source projects on GitHub. Pick a library you use, read 1 file per day. You'll learn patterns that take years to discover alone.",
                CareerHitCategory.SKILL_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_10", "Optimize Your Resume for ATS",
                "Use standard section headers, avoid tables/columns, include keywords from the job description. Most resumes are filtered by bots before humans see them.",
                CareerHitCategory.RESUME_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_11", "Consistency > Intensity",
                "Coding for 30 minutes daily beats 5 hours once a week. Use the streak system to build momentum. Small daily wins compound massively.",
                CareerHitCategory.PRODUCTIVITY_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_12", "Two-Pointer Technique",
                "Perfect for sorted arrays and linked list problems. One pointer starts at the beginning, one at the end. Move them based on conditions. O(n) time, O(1) space.",
                CareerHitCategory.DSA_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_13", "Ask Better Questions in Interviews",
                "Instead of 'What's the culture like?', ask 'What does a typical day look like for this role?' or 'What challenges is the team facing right now?'",
                CareerHitCategory.INTERVIEW_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_14", "Build Your Digital Garden",
                "Document everything you learn on GitHub or a personal wiki. Future employers will see your growth. Plus, you'll have a reference for your own use.",
                CareerHitCategory.CAREER_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_15", "Contribute to Open Source",
                "Start with 'good first issue' labels. Fix documentation or small bugs. It teaches collaboration, code review, and looks great on your resume.",
                CareerHitCategory.NETWORKING_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_16", "Sliding Window Pattern",
                "For subarray/substring problems, think sliding window. Expand the right pointer, shrink the left. Maintain a window of valid state. Key for array problems.",
                CareerHitCategory.DSA_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_17", "Quantify Your Resume Bullets",
                "Bad: 'Improved app performance'. Good: 'Reduced app launch time by 40% by implementing lazy loading and image caching.' Numbers grab attention.",
                CareerHitCategory.RESUME_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_18", "The Feynman Technique",
                "Take a concept, explain it in plain English to a 12-year-old. If you can't simplify it, you don't understand it well enough. Great for system design prep.",
                CareerHitCategory.SKILL_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_19", "Track Your Job Applications",
                "Use a spreadsheet to track: company, role, date applied, status, follow-up date. Follow up after 7 days of no response. Persistence pays off.",
                CareerHitCategory.CAREER_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_20", "Deep Work > Shallow Work",
                "Block 2-hour slots for focused coding. Turn off notifications. Shallow work (emails, Slack) can wait. Deep work is what actually moves your career forward.",
                CareerHitCategory.PRODUCTIVITY_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_21", "System Design: Start with Requirements",
                "Before drawing boxes, clarify: functional requirements, non-functional requirements, scale estimates. A good design without requirements is just art.",
                CareerHitCategory.INTERVIEW_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_22", "Learn Git Beyond Commit-Push-Pull",
                "Learn branching strategies, rebase vs merge, interactive rebase, cherry-pick, and bisect. These skills separate juniors from seniors in real projects.",
                CareerHitCategory.SKILL_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_23", "Cold Outreach Template",
                "Hi [Name], I'm a [year] student at [college] and I admire your work at [company]. Would you be open to a 15-minute chat about your career journey?",
                CareerHitCategory.NETWORKING_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_24", "DFS vs BFS: When to Use What",
                "DFS: when you need to explore all paths (maze, permutations). BFS: shortest path in unweighted graph, level-order traversal. Know the trade-offs.",
                CareerHitCategory.DSA_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_25", "Side Projects > GPA",
                "A strong GitHub profile with real projects often matters more than a perfect GPA. Employers want to see what you can build, not just what you studied.",
                CareerHitCategory.CAREER_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_26", "The 5-Minute Resume Rule",
                "Recruiters spend 5-7 seconds scanning your resume. Make sure your most impressive achievement is at the top. Use bold for key numbers and roles.",
                CareerHitCategory.RESUME_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_27", "Mock Interviews Are Non-Negotiable",
                "Practice with friends or platforms like Pramp. The first mock interview is always terrible. By the 5th one, you'll be confident. Start before you feel ready.",
                CareerHitCategory.INTERVIEW_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_28", "Build a Personal Website",
                "Your own domain > LinkedIn. Showcase projects, blog posts, resume, and contact. It takes 2 hours to set up with GitHub Pages or Vercel. Do it this week.",
                CareerHitCategory.NETWORKING_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_29", "Dynamic Programming: The Framework",
                "1. Define state (dp[i] = ...). 2. Find recurrence relation. 3. Identify base cases. 4. Decide top-down vs bottom-up. 5. Optimize space if possible.",
                CareerHitCategory.DSA_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_30", "Join a Community",
                "Join Discord servers, Reddit communities, or local tech meetups. Surround yourself with motivated people. Your network is your net worth in tech.",
                CareerHitCategory.NETWORKING_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_31", "Start a Tech Blog",
                "Write about what you learn on Hashnode or Dev.to. One article per week. It documents your journey, helps others, and impresses recruiters.",
                CareerHitCategory.SKILL_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_32", "The Power of Daily Standups",
                "Even for personal projects, write 3 things daily: what you did yesterday, what you'll do today, blockers. This builds accountability and momentum.",
                CareerHitCategory.PRODUCTIVITY_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_33", "Graph Algorithms Cheat Sheet",
                "Know by heart: Dijkstra (shortest path), Union-Find (connectivity), Topological Sort (dependency resolution), Kruskal's (MST). These appear constantly.",
                CareerHitCategory.DSA_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_34", "Negotiate Your Offer",
                "Always negotiate. Research salary ranges on Glassdoor. Say 'I'm excited about this role, but I have another offer. Can you match this?' Companies expect it.",
                CareerHitCategory.CAREER_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_35", "Learn One Framework Deeply",
                "Instead of jumping between React, Angular, Vue, and Svelte, master one framework deeply. Deep knowledge of one is better than surface knowledge of many.",
                CareerHitCategory.SKILL_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            ),
            DailyCareerHit(
                "hit_36", "Prepare Your 'Tell Me About Yourself'",
                "Structure: Current (year/college) + Past (key project/experience) + Future (what you're looking for). Keep it under 60 seconds. Practice until natural.",
                CareerHitCategory.INTERVIEW_TIP, xpReward = Constants.XP_PER_DAILY_HIT,
            )
        ))
    }

    fun getHitForDate(date: LocalDate = LocalDate.now()): DailyCareerHit {
        // Initialize lazily
        if (!initialized) {
            // Note: This is blocking, but it's okay for now as it's called from the UI thread only once.
            // In a real app, we might want to do this asynchronously and show a loading state.
            runBlocking { initialize() }
        }
        if (hits.isEmpty()) {
            // If still empty after initialization, load hardcoded as fallback
            loadHardcodedHits()
        }
        val dayOfYear = date.dayOfYear
        val index = (dayOfYear - 1) % hits.size
        return hits[index].copy(date = date)
    }

    // For testing purposes
    fun clearCache() {
        initialized = false
        hits.clear()
    }
}