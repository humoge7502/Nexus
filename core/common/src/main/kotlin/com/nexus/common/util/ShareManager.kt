package com.nexus.common.util

import android.content.Context
import android.content.Intent
import com.nexus.common.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun shareAchievement(
        title: String,
        description: String,
        score: Int? = null,
        streak: Int? = null,
    ) {
        val text = buildString {
            append("\uD83C\uDFC6 $title\n")
            append("$description\n\n")
            if (score != null) append("\uD83D\uDCCA Career Score: $score/1000\n")
            if (streak != null) append("\uD83D\uDD25 Streak: $streak days\n")
            append("\nTrack your career growth on Ascent!")
            append("\n${Constants.PLAY_STORE_URL}")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, "My Ascent Achievement: $title")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(
            Intent.createChooser(shareIntent, "Share your achievement")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    fun shareLevelUp(levelName: String, totalXP: Int, streak: Int) {
        shareAchievement(
            title = "Reached $levelName Level!",
            description = "I just leveled up to $levelName on Ascent with $totalXP XP!",
            streak = streak,
        )
    }

    fun shareBadge(badgeName: String, badgeDescription: String, score: Int) {
        shareAchievement(
            title = "Unlocked: $badgeName",
            description = badgeDescription,
            score = score,
        )
    }

    fun shareStreakMilestone(days: Int, score: Int) {
        shareAchievement(
            title = "$days-Day Career Streak! \uD83D\uDD25",
            description = "I've maintained a $days-day career growth streak on Ascent!",
            score = score,
            streak = days,
        )
    }

    fun shareScore(score: Int, level: String) {
        shareAchievement(
            title = "Career Score: $score/1000",
            description = "My career readiness score on Ascent as a $level.",
            score = score,
        )
    }
}
