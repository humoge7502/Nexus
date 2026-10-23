package com.nexus.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `internships` (
                `id` TEXT NOT NULL, `title` TEXT NOT NULL, `company_name` TEXT NOT NULL,
                `company_logo_url` TEXT, `location` TEXT NOT NULL, `is_remote` INTEGER NOT NULL,
                `stipend_amount` INTEGER, `stipend_currency` TEXT NOT NULL DEFAULT 'INR',
                `duration_months` INTEGER NOT NULL, `skills` TEXT NOT NULL DEFAULT '',
                `deadline` INTEGER NOT NULL DEFAULT 0, `apply_url` TEXT NOT NULL,
                `description` TEXT NOT NULL DEFAULT '', `posted_at` INTEGER NOT NULL DEFAULT 0,
                `is_saved` INTEGER NOT NULL DEFAULT 0, `cached_at` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `certifications` (
                `id` TEXT NOT NULL, `title` TEXT NOT NULL, `provider` TEXT NOT NULL,
                `estimated_hours` INTEGER NOT NULL, `skills` TEXT NOT NULL DEFAULT '',
                `rating` REAL NOT NULL DEFAULT 0, `enrollment_url` TEXT NOT NULL DEFAULT '',
                `is_trending` INTEGER NOT NULL DEFAULT 0,
                `employer_recognition` INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(`id`)
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `applications` (
                `id` TEXT NOT NULL, `internship_id` TEXT NOT NULL,
                `status` TEXT NOT NULL DEFAULT 'APPLIED', `applied_at` INTEGER NOT NULL DEFAULT 0,
                `last_updated` INTEGER NOT NULL DEFAULT 0, `notes` TEXT NOT NULL DEFAULT '',
                PRIMARY KEY(`id`)
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `study_sessions` (
                `id` TEXT NOT NULL, `start_time` INTEGER NOT NULL,
                `duration_minutes` INTEGER NOT NULL, `topic` TEXT NOT NULL DEFAULT '',
                `focus_score` REAL NOT NULL DEFAULT 1.0, PRIMARY KEY(`id`)
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `achievements` (
                `id` TEXT NOT NULL, `type` TEXT NOT NULL, `title` TEXT NOT NULL,
                `description` TEXT NOT NULL, `is_unlocked` INTEGER NOT NULL DEFAULT 0,
                `unlocked_at` INTEGER, `progress` REAL NOT NULL DEFAULT 0.0,
                `max_progress` REAL NOT NULL DEFAULT 1.0, `xp_reward` INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(`id`)
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `hackathons` (
                `id` TEXT NOT NULL, `title` TEXT NOT NULL, `organizer` TEXT NOT NULL,
                `description` TEXT NOT NULL DEFAULT '', `mode` TEXT NOT NULL DEFAULT 'ONLINE',
                `location` TEXT, `startDate` INTEGER NOT NULL DEFAULT 0,
                `endDate` INTEGER NOT NULL DEFAULT 0,
                `registrationDeadline` INTEGER NOT NULL DEFAULT 0,
                `prizePool` TEXT NOT NULL DEFAULT '', `registrationUrl` TEXT NOT NULL DEFAULT '',
                `isLive` INTEGER NOT NULL DEFAULT 0, `isSaved` INTEGER NOT NULL DEFAULT 0,
                `tags` TEXT NOT NULL DEFAULT '', `cached_at` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `scholarships` (
                `id` TEXT NOT NULL, `title` TEXT NOT NULL, `provider` TEXT NOT NULL,
                `amount` TEXT NOT NULL, `description` TEXT NOT NULL DEFAULT '',
                `eligibility` TEXT NOT NULL DEFAULT '', `deadline` INTEGER NOT NULL DEFAULT 0,
                `applicationUrl` TEXT NOT NULL DEFAULT '', `isOpen` INTEGER NOT NULL DEFAULT 1,
                `tags` TEXT NOT NULL DEFAULT '', PRIMARY KEY(`id`)
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `projects` (
                `id` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL DEFAULT '',
                `techStack` TEXT NOT NULL DEFAULT '', `githubUrl` TEXT, `liveUrl` TEXT,
                `imageUrl` TEXT, `starCount` INTEGER NOT NULL DEFAULT 0,
                `isFeatured` INTEGER NOT NULL DEFAULT 0, `completedAt` INTEGER,
                `category` TEXT NOT NULL DEFAULT 'GENERAL', PRIMARY KEY(`id`)
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `goals` (
                `id` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL DEFAULT '',
                `category` TEXT NOT NULL, `progress` REAL NOT NULL DEFAULT 0.0,
                `targetDate` INTEGER, `milestones` TEXT NOT NULL DEFAULT '',
                `isCompleted` INTEGER NOT NULL DEFAULT 0,
                `createdAt` INTEGER NOT NULL, `completedAt` INTEGER,
                PRIMARY KEY(`id`)
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `dsa_problems` (
                `id` TEXT NOT NULL, `title` TEXT NOT NULL, `difficulty` TEXT NOT NULL,
                `category` TEXT NOT NULL DEFAULT '', `platform` TEXT NOT NULL DEFAULT 'LeetCode',
                `link` TEXT NOT NULL DEFAULT '', `topics` TEXT NOT NULL DEFAULT '',
                `frequency` REAL NOT NULL DEFAULT 0.0,
                `status` TEXT NOT NULL DEFAULT 'NOT_ATTEMPTED', `solvedAt` INTEGER,
                `userNotes` TEXT NOT NULL DEFAULT '', PRIMARY KEY(`id`)
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `notifications` (
                `id` TEXT NOT NULL, `type` TEXT NOT NULL, `title` TEXT NOT NULL,
                `body` TEXT NOT NULL, `isRead` INTEGER NOT NULL DEFAULT 0,
                `actionUrl` TEXT, `createdAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `ai_conversations` (
                `id` TEXT NOT NULL, `title` TEXT NOT NULL DEFAULT '',
                `messages` TEXT NOT NULL DEFAULT '',
                `createdAt` INTEGER NOT NULL, `lastMessageAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """)
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `applications_new` (
                `id` TEXT NOT NULL PRIMARY KEY,
                `opportunity_id` TEXT NOT NULL,
                `company` TEXT NOT NULL,
                `role` TEXT NOT NULL,
                `opportunity_type` TEXT NOT NULL,
                `status` TEXT NOT NULL DEFAULT 'SAVED',
                `match_score` REAL NOT NULL DEFAULT 0.0,
                `applied_date` INTEGER,
                `interview_date` INTEGER,
                `interview_rounds` INTEGER NOT NULL DEFAULT 0,
                `outcome_date` INTEGER,
                `outcome_notes` TEXT,
                `salary_offered` TEXT,
                `preparation_notes` TEXT,
                `created_at` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL
            )
        """)
        db.execSQL("""
            INSERT INTO `applications_new` (
                `id`, `opportunity_id`, `company`, `role`, `opportunity_type`,
                `status`, `match_score`, `applied_date`, `interview_date`,
                `interview_rounds`, `outcome_date`, `outcome_notes`,
                `salary_offered`, `preparation_notes`, `created_at`, `updated_at`
            )
            SELECT
                `id`, `internship_id`, '' as `company`, `notes` as `role`,
                'INTERNSHIP' as `opportunity_type`,
                CASE WHEN `status` = 'APPLIED' THEN 'APPLIED' ELSE 'SAVED' END as `status`,
                0.0 as `match_score`, `applied_at` as `applied_date`,
                NULL as `interview_date`, 0 as `interview_rounds`,
                NULL as `outcome_date`, NULL as `outcome_notes`,
                NULL as `salary_offered`, '' as `preparation_notes`,
                `last_updated` as `created_at`, `last_updated` as `updated_at`
            FROM `applications`
        """)
        db.execSQL("DROP TABLE IF EXISTS `applications`")
        db.execSQL("ALTER TABLE `applications_new` RENAME TO `applications`")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_applications_status ON applications(status)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_applications_company ON applications(company)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_applications_opportunity_id ON applications(opportunity_id)")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
override fun migrate(db: SupportSQLiteDatabase) {
db.execSQL(
""" CREATE TABLE IF NOT EXISTS `career_memory` (
`id` TEXT NOT NULL PRIMARY KEY,
`memoryType` TEXT NOT NULL,
`content` TEXT NOT NULL,
`sourceConversationId` TEXT,
`sourceTool` TEXT,
`relevanceScore` REAL NOT NULL DEFAULT 1.0,
`isActive` INTEGER NOT NULL DEFAULT 1,
`tags` TEXT NOT NULL DEFAULT '',
`createdAt` INTEGER NOT NULL,
`lastReferencedAt` INTEGER NOT NULL,
`referenceCount` INTEGER NOT NULL DEFAULT 0
) """)
db.execSQL("CREATE INDEX IF NOT EXISTS index_career_memory_memoryType ON career_memory(memoryType)")
db.execSQL("CREATE INDEX IF NOT EXISTS index_career_memory_createdAt ON career_memory(createdAt)")
db.execSQL("CREATE INDEX IF NOT EXISTS index_career_memory_relevanceScore ON career_memory(relevanceScore)")
db.execSQL(
""" CREATE TABLE IF NOT EXISTS `skill_nodes` (
`skillId` TEXT NOT NULL PRIMARY KEY,
`displayName` TEXT NOT NULL,
`category` TEXT NOT NULL,
`difficultyLevel` INTEGER NOT NULL,
`popularityScore` REAL NOT NULL,
`isUserKnown` INTEGER NOT NULL DEFAULT 0,
`userProficiency` INTEGER NOT NULL DEFAULT 0,
`lastUpdated` INTEGER NOT NULL
) """)
db.execSQL(
""" CREATE TABLE IF NOT EXISTS `skill_edges` (
`fromSkillId` TEXT NOT NULL,
`toSkillId` TEXT NOT NULL,
`edgeType` TEXT NOT NULL,
`weight` REAL NOT NULL,
`transferability` REAL NOT NULL DEFAULT 0.5,
PRIMARY KEY(`fromSkillId`, `toSkillId`)
) """)
db.execSQL("CREATE INDEX IF NOT EXISTS index_skill_nodes_isUserKnown ON skill_nodes(isUserKnown)")
db.execSQL("CREATE INDEX IF NOT EXISTS index_skill_nodes_category ON skill_nodes(category)")
db.execSQL("CREATE INDEX IF NOT EXISTS index_skill_edges_fromSkillId ON skill_edges(fromSkillId)")
db.execSQL("CREATE INDEX IF NOT EXISTS index_skill_edges_toSkillId ON skill_edges(toSkillId)")
  }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL(
      """ CREATE TABLE IF NOT EXISTS `opportunity_embeddings` (
        `id` TEXT NOT NULL PRIMARY KEY,
        `opportunityId` TEXT NOT NULL,
        `opportunityType` TEXT NOT NULL,
        `embedding` BLOB NOT NULL,
        `modelVersion` TEXT NOT NULL,
        `cachedAt` INTEGER NOT NULL DEFAULT 0
      ) """.trimIndent()
    )
    db.execSQL(
      "CREATE UNIQUE INDEX IF NOT EXISTS `index_opportunity_embeddings_opportunityId_opportunityType` ON `opportunity_embeddings` (`opportunityId`, `opportunityType`)"
    )
  }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("""
      CREATE TABLE IF NOT EXISTS `retention_events` (
        `id` TEXT NOT NULL PRIMARY KEY,
        `installWeek` TEXT NOT NULL,
        `activityWeek` TEXT NOT NULL,
        `weeksSinceInstall` INTEGER NOT NULL,
        `sessionCount` INTEGER NOT NULL,
        `xpEarned` INTEGER NOT NULL,
        `featuresUsed` TEXT NOT NULL,
        `recordedAt` INTEGER NOT NULL
      )
    """.trimIndent())
    db.execSQL("CREATE INDEX IF NOT EXISTS index_retention_events_installWeek ON retention_events(installWeek)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_retention_events_activityWeek ON retention_events(activityWeek)")
    db.execSQL("""
      CREATE TABLE IF NOT EXISTS `user_surveys` (
        `id` TEXT NOT NULL PRIMARY KEY,
        `surveyType` TEXT NOT NULL,
        `score` INTEGER,
        `responseText` TEXT,
        `triggerEvent` TEXT NOT NULL,
        `daysActive` INTEGER NOT NULL,
        `completedAt` INTEGER NOT NULL
      )
    """.trimIndent())
    db.execSQL("CREATE INDEX IF NOT EXISTS index_user_surveys_surveyType ON user_surveys(surveyType)")
  }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("""
      CREATE TABLE IF NOT EXISTS `interview_outcomes` (
        `id` TEXT NOT NULL PRIMARY KEY,
        `applicationId` TEXT NOT NULL,
        `company` TEXT NOT NULL,
        `role` TEXT NOT NULL,
        `interviewRound` TEXT NOT NULL,
        `interviewDate` INTEGER NOT NULL,
        `result` TEXT NOT NULL,
        `questionsAsked` TEXT NOT NULL DEFAULT '',
        `topics` TEXT NOT NULL DEFAULT '',
        `confidenceRating` REAL,
        `perceivedPerformance` REAL,
        `notes` TEXT NOT NULL DEFAULT '',
        `createdAt` INTEGER NOT NULL,
        `updatedAt` INTEGER NOT NULL
      )
    """.trimIndent())
    db.execSQL("CREATE INDEX IF NOT EXISTS index_interview_outcomes_company ON interview_outcomes(company)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_interview_outcomes_interviewDate ON interview_outcomes(interviewDate)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_interview_outcomes_result ON interview_outcomes(result)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_interview_outcomes_applicationId ON interview_outcomes(applicationId)")

    db.execSQL("""
      CREATE TABLE IF NOT EXISTS `confidence_assessments` (
        `id` TEXT NOT NULL PRIMARY KEY,
        `assessmentType` TEXT NOT NULL,
        `confidenceScore` REAL NOT NULL,
        `actualScore` REAL,
        `notes` TEXT NOT NULL DEFAULT '',
        `createdAt` INTEGER NOT NULL
      )
    """.trimIndent())
    db.execSQL("CREATE INDEX IF NOT EXISTS index_confidence_assessments_assessmentType ON confidence_assessments(assessmentType)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_confidence_assessments_createdAt ON confidence_assessments(createdAt)")

    db.execSQL("""
      CREATE TABLE IF NOT EXISTS `placement_forecasts` (
        `id` TEXT NOT NULL PRIMARY KEY,
        `userId` TEXT NOT NULL,
        `companyTier` TEXT NOT NULL,
        `probabilityScore` REAL NOT NULL,
        `determiningFactors` TEXT NOT NULL DEFAULT '',
        `calculatedAt` INTEGER NOT NULL
      )
    """.trimIndent())
    db.execSQL("CREATE INDEX IF NOT EXISTS index_placement_forecasts_userId ON placement_forecasts(userId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_placement_forecasts_companyTier ON placement_forecasts(companyTier)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_placement_forecasts_calculatedAt ON placement_forecasts(calculatedAt)")

    db.execSQL("""
      CREATE TABLE IF NOT EXISTS `career_mirrors` (
        `id` TEXT NOT NULL PRIMARY KEY,
        `userId` TEXT NOT NULL,
        `periodStart` INTEGER NOT NULL,
        `periodEnd` INTEGER NOT NULL,
        `periodLabel` TEXT NOT NULL,
        `narrative` TEXT NOT NULL DEFAULT '',
        `keyInsights` TEXT NOT NULL DEFAULT '',
        `actionItems` TEXT NOT NULL DEFAULT '',
        `createdAt` INTEGER NOT NULL
      )
    """.trimIndent())
    db.execSQL("CREATE INDEX IF NOT EXISTS index_career_mirrors_userId ON career_mirrors(userId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_career_mirrors_periodStart ON career_mirrors(periodStart)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_career_mirrors_createdAt ON career_mirrors(createdAt)")

    db.execSQL("""
      CREATE TABLE IF NOT EXISTS `score_snapshots` (
        `id` TEXT NOT NULL PRIMARY KEY,
        `userId` TEXT NOT NULL,
        `score` INTEGER NOT NULL,
        `components` TEXT NOT NULL DEFAULT '',
        `delta` INTEGER NOT NULL DEFAULT 0,
        `recordedAt` INTEGER NOT NULL
      )
    """.trimIndent())
    db.execSQL("CREATE INDEX IF NOT EXISTS index_score_snapshots_userId ON score_snapshots(userId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_score_snapshots_recordedAt ON score_snapshots(recordedAt)")

    db.execSQL("""
      CREATE TABLE IF NOT EXISTS `cohort_intelligence` (
        `id` TEXT NOT NULL PRIMARY KEY,
        `collegeId` TEXT NOT NULL,
        `semester` TEXT NOT NULL,
        `companyTier` TEXT NOT NULL,
        `totalStudents` INTEGER NOT NULL DEFAULT 0,
        `placedCount` INTEGER NOT NULL DEFAULT 0,
        `avgScore` REAL NOT NULL DEFAULT 0,
        `avgStreak` REAL NOT NULL DEFAULT 0,
        `skills` TEXT NOT NULL DEFAULT '',
        `updatedAt` INTEGER NOT NULL
      )
    """.trimIndent())
    db.execSQL("CREATE INDEX IF NOT EXISTS index_cohort_intelligence_collegeId ON cohort_intelligence(collegeId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_cohort_intelligence_collegeId_companyTier ON cohort_intelligence(collegeId, companyTier)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_cohort_intelligence_semester ON cohort_intelligence(semester)")

    db.execSQL("""
      CREATE TABLE IF NOT EXISTS `skill_velocity` (
        `userId` TEXT NOT NULL,
        `skillId` TEXT NOT NULL,
        `skillName` TEXT NOT NULL,
        `proficiency` INTEGER NOT NULL,
        `previousProficiency` INTEGER NOT NULL DEFAULT 0,
        `velocityScore` REAL NOT NULL DEFAULT 0,
        `daysSinceStart` INTEGER NOT NULL DEFAULT 0,
        `onTrackForTarget` INTEGER NOT NULL DEFAULT 1,
        `recordedAt` INTEGER NOT NULL,
        PRIMARY KEY(`userId`, `skillId`)
      )
    """.trimIndent())
    db.execSQL("CREATE INDEX IF NOT EXISTS index_skill_velocity_userId ON skill_velocity(userId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_skill_velocity_recordedAt ON skill_velocity(recordedAt)")
  }
}


val MIGRATION_7_8 = object : Migration(7, 8) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("""
      CREATE TABLE IF NOT EXISTS `interview_reports` (
        `id` TEXT NOT NULL PRIMARY KEY,
        `applicationId` TEXT NOT NULL,
        `company` TEXT NOT NULL,
        `role` TEXT NOT NULL,
        `roundType` TEXT NOT NULL,
        `roundNumber` INTEGER NOT NULL,
        `questions` TEXT NOT NULL DEFAULT '',
        `difficulty` TEXT NOT NULL,
        `topics` TEXT NOT NULL DEFAULT '',
        `outcome` TEXT NOT NULL,
        `tips` TEXT,
        `isAnonymized` INTEGER NOT NULL DEFAULT 1,
        `reportedAt` INTEGER NOT NULL
      )
    """.trimIndent())
    db.execSQL("CREATE INDEX IF NOT EXISTS index_interview_reports_company ON interview_reports(company)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_interview_reports_role ON interview_reports(role)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_interview_reports_reportedAt ON interview_reports(reportedAt)")

    // Backfill indices declared by the v8 entities on base tables created in
    // MIGRATION_1_2. IF NOT EXISTS makes this idempotent on fresh installs.
    db.execSQL("CREATE INDEX IF NOT EXISTS index_internships_is_saved ON internships(is_saved)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_internships_deadline ON internships(deadline)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_internships_company_name ON internships(company_name)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_internships_cached_at ON internships(cached_at)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_certifications_provider ON certifications(provider)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_certifications_is_trending ON certifications(is_trending)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_study_sessions_start_time ON study_sessions(start_time)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_achievements_is_unlocked ON achievements(is_unlocked)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_achievements_type ON achievements(type)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_hackathons_isLive ON hackathons(isLive)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_hackathons_isSaved ON hackathons(isSaved)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_hackathons_startDate ON hackathons(startDate)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_hackathons_registrationDeadline ON hackathons(registrationDeadline)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_scholarships_isOpen ON scholarships(isOpen)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_scholarships_deadline ON scholarships(deadline)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_projects_isFeatured ON projects(isFeatured)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_projects_category ON projects(category)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_goals_isCompleted ON goals(isCompleted)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_goals_category ON goals(category)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_goals_targetDate ON goals(targetDate)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_dsa_problems_difficulty ON dsa_problems(difficulty)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_dsa_problems_status ON dsa_problems(status)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_dsa_problems_platform ON dsa_problems(platform)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_isRead ON notifications(isRead)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_createdAt ON notifications(createdAt)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_type ON notifications(type)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_ai_conversations_lastMessageAt ON ai_conversations(lastMessageAt)")
  }
}
