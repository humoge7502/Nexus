package com.nexus.common

import android.net.Uri

object NexusDeepLinks {
  private const val SCHEME = "nexus"
  private const val HOST = "career"

  val HOME        = "$SCHEME://$HOST/home"
  val GROWTH      = "$SCHEME://$HOST/growth"
  val EXPLORE     = "$SCHEME://$HOST/explore"
  val PROFILE     = "$SCHEME://$HOST/profile"
  val APPLICATIONS = "$SCHEME://$HOST/applications"
  val ACHIEVEMENTS = "$SCHEME://$HOST/achievements"

  val AI_CHAT     = "$SCHEME://ai/chat"
  val AI_INTERVIEW = "$SCHEME://ai/interview_coach"
  val AI_SKILL_GAP = "$SCHEME://ai/skill_gap"
  val AI_RESUME   = "$SCHEME://ai/resume"
  val AI_ROADMAP  = "$SCHEME://ai/roadmap"

  fun opportunity(id: String) = "$SCHEME://opportunity/$id"
  fun aiChatWithPrompt(prompt: String) = "$SCHEME://ai/chat?prompt=${Uri.encode(prompt)}"
  fun searchQuery(query: String) = "$SCHEME://$HOST/search?q=${Uri.encode(query)}"
}
