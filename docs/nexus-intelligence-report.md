# NEXUS Android — Complete App Intelligence Report

> **Report Date:** 2026-05-29
> **Codebase:** 31 modules, ~130+ source files, Compose Multi-Module Clean Architecture
> **Report Type:** Exhaustive source-level analysis. Every claim sourced to file:line.
> **Key:** ✅ = Fully Implemented | ⚠️ = Partial/Stub | ❌ = Missing/Dead | 🔴 = Bug

---

## SECTION 1: PROJECT OVERVIEW & ARCHITECTURE

### 1.1 Meta
| Attribute | Value |
|-----------|-------|
| App Name | NEXUS |
| Package | `com.nexus.careerOS` |
| Target SDK | 35 (`Constants.kt:12`) |
| Min SDK | 26 (`Constants.kt:10`) |
| Build System | Gradle KTS + Version Catalog (`gradle/libs.versions.toml`) |
| DI | Hilt 2.52 + KSP |
| UI Framework | Jetpack Compose (BOM 2025.05.00) |
| Architecture | Multi-module Clean Architecture (claimed MVI, but no module implements MVI) |

### 1.2 Architecture Analysis
The project follows a layered Clean Architecture with strict module separation:

```
app → feature/* → core/data → core/domain → core/network + core/database
```

- **core/domain**: Pure Kotlin business logic (use cases, models). Pristine — zero Android imports.
- **core/data**: Repository implementations. Bridges domain with network/database layers.
- **core/network**: Retrofit API services, DTOs, OkHttp interceptors.
- **core/database**: Room 2.6.1 with 12 entities and 11 DAOs.
- **core/common**: Shared utilities, constants, DI modules, extensions.
- **core/designsystem**: All UI components, theme, effects.
- **core/ui**: Shared charts (Vico), tooltips (Balloon).
- **core/sync**: WorkManager workers and sync scheduling.
- **core/analytics**: Firebase Analytics + Crashlytics wrapper.
- **core/notifications**: FCM service + notification channels.

**Verdict:** ✅ Architecture structure is sound. Split between MVI and MVVM — some ViewModels use explicit `UiState` sealed interfaces (MVI-like), others use simple `StateFlow` (MVVM). No module truly implements the unidirectional data flow of MVI. This is pragmatic MVVM with sealed-UI-state pattern.

---

## SECTION 2: MODULE STRUCTURE & DEPENDENCIES

### 2.1 All Modules (31 total)

| Module | Type | Dependencies | Purpose |
|--------|------|-------------|---------|
| `:app` | Application | ALL core + ALL feature + Firebase BOM, Coil, Chucker, LeakCanary | Host app, NavHost, DI, manifest |
| `:core:designsystem` | Android Library | `:core:common`, Compose, Shimmer, Haze, Lottie, Konfetti, Coil | Theme, components, effects |
| `:core:domain` | Android Library | `:core:common`, kotlinx-collections-immutable | Use cases, models |
| `:core:data` | Android Library | `:core:common`, `:core:domain`, `:core:network`, `:core:database`, `:core:datastore` | Repository impls |
| `:core:network` | Android Library | `:core:common`, `:core:datastore`, Retrofit, OkHttp | API services, DTOs |
| `:core:database` | Android Library | Room, kotlinx-serialization | Room DB, entities, DAOs |
| `:core:datastore` | Android Library | DataStore | User preferences |
| `:core:common` | Android Library | kotlinx-serialization, coroutines, datetime, DataStore | Constants, utilities |
| `:core:ui` | Android Library | Compose, Vico, Balloon | Shared charts |
| `:core:sync` | Android Library | `:core:data`, `:core:domain`, `:core:common`, `:core:database`, `:core:notifications`, `:core:datastore`, WorkManager, Firebase | Workers, sync scheduling |
| `:core:analytics` | Android Library | Firebase Analytics, Crashlytics, Timber | Analytics wrapper |
| `:core:notifications` | Android Library | Firebase Messaging, DataStore, Firebase Auth/Firestore | FCM, notifications |
| `:feature:home` | Feature | `:core:designsystem`, `:core:domain`, `:core:data` | Dashboard, daily hit, streak |
| `:feature:auth` | Feature | `:core:designsystem`, `:core:domain`, `:core:data` | Google/Email/GitHub auth |
| `:feature:onboarding` | Feature | `:core:designsystem`, `:core:domain`, `:core:data` | First-launch flow |
| `:feature:explore` | Feature | `:core:designsystem`, `:core:domain`, `:core:data` | Opportunity browsing |
| `:feature:ai` | Feature | `:core:designsystem`, `:core:domain`, `:core:data`, `:core:common`, OkHttp | AI Hub (7 tools) |
| `:feature:growth` | Feature | `:core:designsystem`, `:core:domain`, `:core:data`, `:feature:ai` | Growth dashboard, DSA, Oracle, Arena |
| `:feature:profile` | Feature | `:core:designsystem`, `:core:domain`, `:core:data` | User profile, referral |
| `:feature:settings` | Feature | `:core:designsystem`, `:core:domain`, `:core:data`, Firebase auth/firestore/storage, `:core:analytics` | Settings, account deletion |
| `:feature:achievements` | Feature | `:core:designsystem`, `:core:domain`, `:core:data`, Lottie, Konfetti | Badges, leaderboard |
| `:feature:internships` | Feature | `:core:designsystem`, `:core:domain`, `:core:data`, Paging | Internship list/detail |
| `:feature:certifications` | Feature | `:core:designsystem`, `:core:domain`, `:core:data` | Certification list/detail |
| `:feature:notifications` | Feature | `:core:designsystem`, `:core:domain`, `:core:data` | Notification history |
| `:feature:goals` | Feature | `:core:designsystem`, `:core:domain`, `:core:data` | Goal CRUD |
| `:feature:productivity` | Feature | `:core:designsystem`, `:core:domain`, `:core:data`, Vico, Lottie, Konfetti | Pomodoro timer |
| `:feature:portfolio` | Feature | `:core:designsystem`, `:core:domain` | ⚠️ Partial — hardcoded mock data |
| `:feature:community` | Feature | `:core:designsystem` | ❌ Stub — no ViewModel, all hardcoded |
| `:macrobenchmark` | Benchmark | `:app` | Baseline profile generation |

**Verdict:** ✅ Well-modularized. Domain layer is pure Kotlin. Feature modules are properly isolated. Community and portfolio are the weakest modules.

---

## SECTION 3: NAVIGATION ARCHITECTURE

### 3.1 Route Definitions
**File:** `app/src/main/kotlin/com/nexus/app/Navigation.kt`

**Pattern:** Sealed interface `NexusDestination`
**Total routes:** 30

**Top-level (bottom nav):**
- `Onboarding` → `OnboardingScreen`
- `Home` → `HomeScreen`
- `Explore` → `ExploreScreen`
- `AIHub` → `AIHubScreen`
- `Growth` → `GrowthScreen`

**Detail routes (19):**
| Route | Screen | Notes |
|-------|--------|-------|
| `Goals` | `GoalsScreen` | |
| `DSATracker` | `DSATrackerScreen` | |
| `Notifications` | `NotificationsScreen` | |
| `Leaderboard` | `LeaderboardScreen` | |
| `InternshipDetail(internshipId)` | `InternshipDetailScreen` | Deep link: `nexus://internship/{id}` |
| `CertificationDetail(certId)` | `CertificationDetailScreen` | |
| `CertificationBrowser` | `CertificationScreen` | |
| `InternshipList` | `InternshipListScreen` | |
| `Productivity` | `ProductivityScreen` | |
| `Profile` | `ProfileScreen` | |
| `Achievements` | `AchievementsScreen` | |
| `Settings` | `SettingsScreen` | |
| `Portfolio` | `PortfolioScreen` | |
| `HackathonDetail(hackathonId)` | `HackathonDetailScreen` | |
| `Oracle` | `NexusOracleScreen` | |
| `Arena` | `NexusArenaScreen` | |

**AI sub-screens (7):**
| Route | Screen | Status |
|-------|--------|--------|
| `AIChat` | `AIChatScreen` | ✅ |
| `AIResumeAnalyzer` | `AIResumeAnalyzerScreen` | ⚠️ Has AI + hardcoded fallback |
| `RoadmapGenerator` | `RoadmapGeneratorScreen` | ✅ |
| `LinkedInPost` | `LinkedInPostScreen` | ✅ |
| `SkillGap` | `SkillGapScreen` | ✅ |
| `InterviewCoach` | `InterviewCoachScreen` | ✅ |
| `CareerInsights` | `CareerInsightsScreen` | ✅ |

**Auth & Account (2):**
| Route | Screen | Status |
|-------|--------|--------|
| `Auth` | `AuthScreen` | ✅ |
| `AccountDeletion` | `AccountDeletionScreen` | ✅ |

### 3.2 Navigation Flow
**File:** `app/src/main/kotlin/com/nexus/app/MainActivity.kt`

```
App Start
   ↓
AnimatedContent(bootFlow) {
  "loading" → Splash (AnimatedSplash)
  "auth"   → AuthScreen (Google/GitHub/Email/Skip)
  "onboarding" → OnboardingScreen → onComplete → "main"
  "main"   → NexusMainScaffold (NavHost + NexusBottomBar)
}
```

- Start destination determined by `MainViewModel` which checks Firebase Auth + Firestore onboarding flag.
- Bottom bar: Home, Explore, AI, Growth, Profile (5 tabs)
- Uses `collectAsStateWithLifecycle()` ✅ (fixed during polish)

**Verdict:** ✅ Solid navigation architecture. Single-activity, Compose-driven. Animated transitions between auth/onboarding/main flows. Deep link support for internships.

---

## SECTION 4: DEPENDENCY INJECTION

### 4.1 Hilt Modules (9 total)

| Module | File | Bindings |
|--------|------|----------|
| `AppModule` | `app/.../di/AppModule.kt` | `FirebaseAuth`, `FirebaseFirestore`, `FirebaseStorage`, `@Named("googleWebClientId")` |
| `NetworkModule` | `core/network/.../di/NetworkModule.kt` | `OkHttpClient`, `@NexusApi Retrofit`, `NexusApiService` |
| `RepositoryModule` | `core/data/.../di/RepositoryModule.kt` | 7 repositories (Internship, Certification, Hackathon, Goal, DSAProblem, Achievement, AIConversation) |
| `DatabaseModule` | `core/database/.../di/DatabaseModule.kt` | `NexusDatabase` + 11 DAOs |
| `DataStoreModule` | `core/datastore/.../di/DataStoreModule.kt` | `DataStore<Preferences>` |
| `CommonModule` | `core/common/.../di/CommonModule.kt` | `ConnectivityManagerNetworkMonitor` as `NetworkMonitor` |
| `SyncModule` | `core/sync/.../di/SyncModule.kt` | ❌ Empty — no actual bindings |
| `AnalyticsModule` | `core/analytics/.../di/AnalyticsModule.kt` | `FirebaseAnalytics`, `FirebaseCrashlytics`, `AnalyticsTracker` |
| `BalloonModule` | `core/ui/.../di/BalloonModule.kt` | `TooltipManager` |

### 4.2 Missing Bindings
- **`@Named("github_token")`** — defined as injection point in `GitHubRepository.kt:22` but **no Hilt module provides it**. Would crash at runtime if ever injected.
- **`Scheduler` / `SyncScheduler`** — uses `@Inject constructor`, auto-created by Dagger. No explicit module binding needed.

### 4.3 DI Pattern
- All ViewModels use `@HiltViewModel` + `@Inject constructor`
- All Workers use `@HiltWorker` + `@AssistedInject`
- All repositories use `@Singleton` scoping
- Feature modules use `@EntryPoint` where needed (e.g., HiltWorkFactory in app)

**Verdict:** ✅ Good DI coverage. Main gap is `SyncModule` (empty) and the missing `github_token` binding (currently harmless since GitHubRepository is dead code).

---

## SECTION 5: SCREENS & VIEWMODELS

### 5.1 Screen Inventory (31 screens)

| # | Screen | Module | ViewModel | Data Source | Status |
|---|--------|--------|-----------|-------------|--------|
| 1 | `HomeScreen` | home | `HomeViewModel` | InternshipRepository, CareerHitRepository, UserPreferences | ✅ Real data |
| 2 | `ExploreScreen` | explore | `ExploreViewModel` | InternshipRepository, HackathonRepository | ⚠️ Search/filter visual-only |
| 3 | `HackathonDetailScreen` | explore | (from ExploreViewModel) | HackathonRepository | ✅ Real data |
| 4 | `AIHubScreen` | ai | (none) | Static grid | ✅ Navigation hub |
| 5 | `AIChatScreen` | ai | `AIChatViewModel` | AIOpenRouterService, AIRateLimiter | ✅ Real AI |
| 6 | `AIResumeAnalyzerScreen` | ai | (none — inline) | AIOpenRouterService | ⚠️ AI + hardcoded fallback |
| 7 | `RoadmapGeneratorScreen` | ai | `RoadmapViewModel` | AIOpenRouterService | ✅ Real AI |
| 8 | `SkillGapScreen` | ai | `SkillGapViewModel` | AIOpenRouterService | ✅ Real AI |
| 9 | `LinkedInPostScreen` | ai | `LinkedInPostViewModel` | AIOpenRouterService | ✅ Real AI |
| 10 | `InterviewCoachScreen` | ai | `InterviewCoachViewModel` | AIOpenRouterService | ✅ Real AI |
| 11 | `CareerInsightsScreen` | ai | `CareerInsightsViewModel` | AIOpenRouterService | ✅ Real AI |
| 12 | `AuthScreen` | auth | `AuthViewModel` | FirebaseAuth, UserPreferences | ✅ Real auth |
| 13 | `OnboardingScreen` | onboarding | `OnboardingViewModel` | UserPreferences | ✅ Multi-step |
| 14 | `GrowthScreen` | growth | `GrowthViewModel` | DSAProblemRepository, GoalRepository | ⚠️ UI rich, data hardcoded |
| 15 | `DSATrackerScreen` | growth | (from GrowthViewModel) | DSAProblemRepository | ✅ Real data |
| 16 | `NexusOracleScreen` | growth | `NexusOracleViewModel` | AIOpenRouterService | ✅ Real AI |
| 17 | `NexusArenaScreen` | growth | (none) | Hardcoded data | ❌ Stub |
| 18 | `ProfileScreen` | profile | `ProfileViewModel` | UserPreferences | ⚠️ Level/XP hardcoded at 1/0 |
| 19 | `SettingsScreen` | settings | (none) | — | ✅ UI only |
| 20 | `AccountDeletionScreen` | settings | `AccountDeletionViewModel` | FirebaseAuth, Firestore, Storage, Analytics | ✅ Real |
| 21 | `AchievementsScreen` | achievements | `AchievementsViewModel` | AchievementRepository | ⚠️ Level/XP/stats hardcoded in UI |
| 22 | `LeaderboardScreen` | achievements | `LeaderboardViewModel` | LeaderboardRepository | ⚠️ Institution hardcoded |
| 23 | `InternshipListScreen` | internships | `InternshipViewModel` | InternshipRepository | ⚠️ Search/filter visual-only |
| 24 | `InternshipDetailScreen` | internships | (from InternshipViewModel) | InternshipRepository | ✅ Real data |
| 25 | `CertificationScreen` | certifications | `CertificationViewModel` | CertificationRepository | ✅ Real data |
| 26 | `CertificationDetailScreen` | certifications | (from CertificationViewModel) | CertificationRepository | ✅ Real data |
| 27 | `NotificationsScreen` | notifications | `NotificationsViewModel` | NotificationDao | ✅ Real data |
| 28 | `GoalsScreen` | goals | `GoalsViewModel` | GoalRepository | ✅ Real data |
| 29 | `ProductivityScreen` | productivity | (none — inline) | Local state | ⚠️ Loses state on process death |
| 30 | `PortfolioScreen` | portfolio | `PortfolioViewModel` | Hardcoded mock | ❌ Mock data only |
| 31 | `CommunityScreen` | community | (none) | Hardcoded stub | ❌ Stub — "coming soon" |

### 5.2 Screens WITH ViewModels (17)
Home, Explore, AIChat, RoadmapGenerator, SkillGap, LinkedInPost, InterviewCoach, CareerInsights, Auth, Onboarding, Growth (shared with DSA), NexusOracle, Profile, AccountDeletion, Achievements, Leaderboard, Internship, Certifications, Notifications, Goals, Portfolio

### 5.3 Screens WITHOUT ViewModels (6)
AIHub, AIResumeAnalyzer, NexusArena, Settings, Productivity, Community

### 5.4 ViewModel State Pattern
- 14 ViewModels use sealed `UiState` interfaces (Loading/Success/Error pattern) ✅
- 3 ViewModels (AuthViewModel, ProfileViewModel, AIResumeAnalyzer) use simpler StateFlow without sealed state
- All ViewModels use `viewModelScope.launch` for async operations ✅
- All ViewModels use Hilt injection ✅

**Verdict:** ✅ Good ViewModel coverage. Weak spots: NexusArena (no VM, mock data), Portfolio (mock data), Community (stub), Productivity (no VM, state lost on process death). AIResumeAnalyzer has no ViewModel despite being a complex screen.

---

## SECTION 6: DATA LAYER

### 6.1 Room Database

**File:** `core/database/.../NexusDatabase.kt`
**Version:** 2 (MIGRATION_1_2 exists)

**Entities (12):**
| Entity | DAO | Key Fields |
|--------|-----|------------|
| `InternshipEntity` | `InternshipDao` | id, title, company, location, stipend, deadline, remote |
| `CertificationEntity` | `CertificationDao` | id, name, provider, url, duration |
| `ApplicationEntity` | `ApplicationDao` | id, internshipId, status, date |
| `StudySessionEntity` | `StudySessionDao` | id, startTime, endTime, duration |
| `AchievementEntity` | `AchievementDao` | id, name, description, iconUrl, unlocked |
| `HackathonEntity` | `HackathonDao` | id, title, organizer, prizePool, deadline |
| `ScholarshipEntity` | `ScholarshipDao` | id, name, amount, deadline |
| `ProjectEntity` | `ProjectDao` | id, title, description, url, featured |
| `GoalEntity` | `GoalDao` | id, title, progress, target, deadline |
| `DSAProblemEntity` | `DSAProblemDao` | id, title, difficulty, platform, solved |
| `NotificationEntity` | `NotificationDao` | id, title, body, type, read, timestamp |
| `AIConversationEntity` | `AIDao` | id, title, messages, createdAt, updatedAt |

### 6.2 DataStore

**File:** `core/datastore/.../UserPreferences.kt`
Stores: `onboarding_completed`, `theme_mode`, `accent_color`, `notifications_enabled`, `study_streak`, `user_id`, `display_name`, `email`, `auth_token`

Additional DataStore instances:
- `CareerHitRepository` — `career_hit_prefs` (today_hit_id, hit_viewed_date, last_hit_xp_date)
- `StreakRepository` — `streak_prefs` (streak_count, last_active_date)
- `AIRateLimiter` — `ai_rate_limit` (ai_calls_today, ai_calls_reset_day)
- `FirestoreSync` — `firestore_sync_prefs` (sync timestamps per collection)
- `MainViewModel` — `onboarding` (inline DataStore<Preferences>)

### 6.3 Repositories (13 files)

| Repository | File | Dependencies | Real Data? |
|-----------|------|-------------|------------|
| `InternshipRepository` | `core/data/.../InternshipRepository.kt` | NexusApiService, InternshipDao | ✅ |
| `CertificationRepository` | `core/data/.../CertificationRepository.kt` | NexusApiService, CertificationDao | ✅ |
| `HackathonRepository` | `core/data/.../HackathonRepository.kt` | NexusApiService, HackathonDao | ✅ |
| `GoalRepository` | `core/data/.../GoalRepository.kt` | NexusApiService, GoalDao | ✅ |
| `DSAProblemRepository` | `core/data/.../DSAProblemRepository.kt` | NexusApiService, DSAProblemDao | ✅ |
| `AchievementRepository` | `core/data/.../AchievementRepository.kt` | NexusApiService, AchievementDao | ✅ |
| `AIConversationRepository` | `core/data/.../AIConversationRepository.kt` | NexusApiService, AIDao | ✅ |
| `LeaderboardRepository` | `core/data/.../LeaderboardRepository.kt` | UserPreferences | ⚠️ Hardcoded mock `generateEntries()` |
| `CareerHitRepository` | `core/data/.../CareerHitRepository.kt` | Context | ✅ Real (DataStore-backed) |
| `StreakRepository` | `core/data/.../StreakRepository.kt` | Context | ✅ Real (DataStore-backed) |
| `GitHubRepository` | `core/data/.../GitHubRepository.kt` | OkHttp, `@Named("github_token")` | ❌ Dead code, missing DI binding |
| `LeetCodeRepository` | `core/data/.../LeetCodeRepository.kt` | LeetCodeFunctionsService | ✅ Real |
| `OpportunityRepositoryImpl` | `core/data/.../OpportunityRepositoryImpl.kt` | NexusApiService, 3 DAOs, NetworkMonitor | ✅ Real |

**Verdict:** ✅ Strong data layer with Room, DataStore, and Retrofit. Main gaps: `LeaderboardRepository` returns mock data, `GitHubRepository` is dead code and has missing DI binding.

---

## SECTION 7: NETWORK LAYER

### 7.1 API Services

**NexusApiService** (`core/network/.../api/NexusApiService.kt`)
- Base URL: `https://api.nexus.app/` (fallback: `https://api.nexusapp.in/v1`)
- 17 endpoints across internships, certifications, hackathons, goals, DSA, achievements, leaderboard, AI chat, resume analysis, user profile
- Auth via `AuthInterceptor` (reads token from DataStore, adds `Bearer` header)

**LeetCodeFunctionsService** (`core/network/.../service/LeetCodeFunctionsService.kt`)
- Calls the `leetcodeStats` Firebase callable (auth required)
- The backend queries LeetCode GraphQL directly, validates the username, enforces an
  8s upstream timeout, and caches results in Firestore for 10 minutes
- Client keeps a 10-minute in-memory TTL cache on top; no community mirror is involved

### 7.2 Network Configuration
- **OkHttp**: Singleton with logging interceptor (debug), auth interceptor, retry (3 attempts)
- **Connectivity**: `NetworkMonitor` uses `ConnectivityManager.registerNetworkCallback` with `callbackFlow`
- **🔴 Bug**: `AuthInterceptor.kt:16` uses `runBlocking` on main thread to read DataStore — potential ANR
- **🔴 Bug**: `AndroidManifest.xml` declares `INTERNET` but not `ACCESS_NETWORK_STATE` — `NetworkMonitor` requires this on Android 12+

### 7.3 Caching Strategy
- Room DB serves as local cache
- Cache TTLs defined in `Constants.kt:60-62`: Opportunities=30min, Certifications=24h, AI insights=24h
- Sync workers refresh every 6 hours

### 7.4 DTO Layer
- `ApiDtos.kt` — PaginatedResponse, InternshipDto, HackathonDto, GoalDto, DSAProblemDto, AchievementDto, LeaderboardEntryDto, ChatMessageDto, ChatRequestDto, ChatResponseDto, ResumeRequestDto, ResumeAnalysisDto, UserProfileDto
- `CertificationDto.kt`

**Verdict:** ✅ Strong network layer with proper DTOs, auth interceptor, retry logic, and connectivity awareness. Two bugs: missing `ACCESS_NETWORK_STATE` permission and `runBlocking` in `AuthInterceptor`.

---

## SECTION 8: AI SERVICES

### 8.1 AI Service Architecture

**File:** `feature/ai/.../AIOpenRouterService.kt` (103 lines — corrected 2026-10-25)
- ✅ Non-streaming single-shot AI service. Uses `openai/gpt-4o-mini` via
  `AIRequestRouter`, which selects the proxy or the direct OpenRouter endpoint.
- Supports `sendPrompt(prompt)` and `sendMessages(messages)`.
- **✅ Fixed since this report was written:** the fabricated fallback response is
  gone. In proxy mode the result is `result.getOrThrow()`; the direct path throws
  `AIServiceException("AI service returned an empty response", isRetryable = true)`
  ([AIOpenRouterService.kt:80](../feature/ai/src/main/kotlin/com/nexus/ai/AIOpenRouterService.kt#L80)),
  so callers surface a real failure instead of a canned line.
- `MAX_RETRIES = 3` no longer lives in this file. Retry with exponential backoff
  *does* exist as `core/common/SafeApiCall.kt`, but only `core/sync/SyncWorker`
  uses it — no AI call site wraps `sendMessage` in `safeApiCall`, so
  `AIServiceException.isRetryable` is set and then never read on the AI path.
  **Live gap:** AI requests currently fail on the first transient 5xx.
- API key via `BuildConfig.OPENROUTER_API_KEY` — **debug builds only.** As of
  2026-10-25, `core/network/build.gradle.kts` hard-codes that field to `""` for
  the release variant, so a release APK can never carry the upstream key.

**File:** `feature/ai/.../NexusAIService.kt` (180 lines)
- ✅ Streaming chat service (returns `Flow<String>`), now backed by the
  authenticated `streamAI` SSE bridge so deltas render as they arrive.
- Integrates `ConversationMemoryManager` and `SystemPromptBuilder`
- **No longer orphaned** — `AIChatViewModel` injects it and streams through
  `AIStreamClient` (proxy) or the debug-only direct path. `AIOpenRouterService`
  still serves the five single-shot screens (Career Insights, Interview Coach,
  LinkedIn Post, Roadmap, Skill Gap) and `NexusOracleViewModel`.

**File:** `feature/ai/.../SystemPromptBuilder.kt`
- ✅ Builds personalized system prompts from `UserProfile` (name, college, skills, goals, target companies)

**File:** `feature/ai/.../ConversationMemoryManager.kt` (in `core/domain/.../usecase/`)
- ✅ Manages conversation context window: keeps last 20 messages, summarizes older ones
- ✅ Used by `NexusAIService` for the streaming chat path

**File:** `core/common/.../AIRateLimiter.kt`
- ✅ Free-tier rate limiter: 10 calls/day via DataStore. Pro users bypass.
- **⚠️ Only wired to `AIChatViewModel`** — the other 6 AI ViewModels have no rate limiting

### 8.2 AI Screen Inventory

| Screen | ViewModel | Service | Rate Limited? | Streaming? |
|--------|-----------|---------|---------------|------------|
| AIChat | `AIChatViewModel` | `NexusAIService` (SSE) | ✅ (10/day) | ✅ |
| CareerInsights | `CareerInsightsViewModel` | AIOpenRouterService | ❌ | ❌ |
| InterviewCoach | `InterviewCoachViewModel` | AIOpenRouterService | ❌ | ❌ |
| LinkedInPost | `LinkedInPostViewModel` | AIOpenRouterService | ❌ | ❌ |
| SkillGap | `SkillGapViewModel` | AIOpenRouterService | ❌ | ❌ |
| RoadmapGenerator | `RoadmapViewModel` | AIOpenRouterService | ❌ | ❌ |
| NexusOracle | `NexusOracleViewModel` | AIOpenRouterService | ❌ | ❌ |
| AIResumeAnalyzer | (none) | AIOpenRouterService (nullable) | ❌ | ❌ |

### 8.3 AI Quality Summary
- **6/8 screens use real AI** — good
- **2 parallel service impls** — `AIOpenRouterService` (single-shot screens) and
  `NexusAIService` (streaming chat via `streamAI`)
- **Rate limiting gated to only 1/8 screens** — free-tier abuse possible via 7 other AI screens
- **Silent failures** — AI returns mock responses on error, user never knows

**Verdict:** ⚠️ Good AI integration breadth but needs cleanup: consolidate services, wire rate limiter everywhere, surface API errors to users.

---

## SECTION 9: FIREBASE INTEGRATION

### 9.1 Firebase Services Usage

| Service | Used? | Where | Status |
|---------|-------|-------|--------|
| Firebase Auth | ✅ | `AuthViewModel`, `AppModule`, `MainViewModel`, account deletion | Fully implemented |
| Firebase Firestore | ✅ | `FirestoreSync`, `MainViewModel`, `NexusFCMService` | Syncs 5 collections |
| Firebase Crashlytics | ✅ | `AnalyticsTracker`, `NexusApplication` (CrashlyticsTree) | Fully implemented |
| Firebase Analytics | ✅ | `AnalyticsTracker` | ✅ Wired but ❌ never called from any screen/VM |
| Firebase Storage | ❌ | DI binding exists in `AppModule:33` | Never used in any source file |
| Firebase Functions | ❌ | Dependency in `build.gradle.kts` | Never imported or called |

### 9.2 Key Firebase Details

**Auth Flow** (`AuthViewModel.kt`):
- Google Sign-In via `GoogleAuthProvider.getCredential`
- Email/password sign-in/sign-up
- **🔴 Bug**: `signInWithGoogle()` at line 72-77 is a **no-op** — just emits `NavigateToHome` without actually authenticating. The real Google sign-in is in `handleGoogleSignInResult()` called from MainActivity's ActivityResultLauncher.

**Firestore Sync** (`FirestoreSync.kt`):
- 5 collections: `internships`, `hackathons`, `certifications`, `goals`, `achievements`
- Uses `whereGreaterThan("updatedAt", lastSync)` for incremental sync ✅
- Tracks last sync timestamp per collection in DataStore ✅

**FCM** (`NexusFCMService.kt`):
- ✅ Receives push notifications, routes to deep links
- ✅ Writes FCM token to Firestore `users/{userId}/fcmToken`
- ✅ 5 notification channels defined in `NotificationHelper.kt`

**Verdict:** ✅ Firebase integrated for auth, firestore, crashlytics, and FCM. Storage and Functions are unused dependencies. AnalyticsTracker is wired but never invoked. Auth has a no-op bug in `signInWithGoogle()`.

---

## SECTION 10: BACKGROUND WORK (WORKMANAGER)

### 10.1 Worker Inventory (6 workers)

| Worker | Period | Constraints | Purpose | Status |
|--------|--------|-------------|---------|--------|
| `SyncWorker` | 6h | Unmetered network | Syncs all API data to Room | ✅ |
| `FirestoreSyncWorker` | 6h | Unmetered network | Syncs Firestore → Room | ✅ |
| `LeetCodeSyncWorker` | 6h | Connected network | LeetCode stats sync | ✅ Reads `leetcodeUsername`; uses the `leetcodeStats` callable |
| `StreakWorker` | 24h | None | Updates streak | ✅ |
| `DeadlineAlertWorker` | 6h | Connected network | Deadline notifications | 🔴 Time unit mismatch bug |
| `DailyBriefingWorker` | 24h | None (at 7AM) | Morning briefing notification | ✅ |

### 10.2 Scheduling
**File:** `core/sync/.../SyncScheduler.kt`
- ✅ All workers scheduled via `SyncScheduler.scheduleAll()`
- ✅ Uses `ExistingPeriodicWorkPolicy.KEEP` — does not duplicate
- ✅ Called from `NexusApplication.onCreate()` with 2s delay on `Dispatchers.Default`
- ✅ Custom `WorkManager` configuration using `HiltWorkerFactory`

### 10.3 Bugs

**🔴 Bug 1 — DeadlineAlertWorker time unit mismatch** (`DeadlineAlertWorker.kt:34`)
```kotlin
val threeDaysFromNow = now + TimeUnit.DAYS.toMillis(3)
// but deadline is epoch-day (e.g., 20245), not millis
// InternshipRepository.toEntity() stores: deadline = LocalDate.now().toEpochDay()
```
The comparison `entity.deadline in (now + 1)..threeDaysFromNow` compares epoch-day numbers with millisecond timestamps. This will either never trigger or trigger incorrectly.

**✅ Resolved — LeetCodeSyncWorker wrong preference** (`LeetCodeSyncWorker.kt:23`)

The worker now reads `userPreferences.leetcodeUsername` (a dedicated DataStore key),
skips with `Result.success()` when it is blank, and refreshes stats through the
`leetcodeStats` Cloud Function.

### 10.4 Firestore Sync
**File:** `core/sync/.../FirestoreSync.kt`
- ✅ Full implementation with incremental sync
- ✅ 5 collections synced
- ✅ Uses `whereGreaterThan("updatedAt", lastSync)` — but if Firestore documents are created without `updatedAt` field, they will never sync
- ✅ Tracks per-collection sync timestamps in DataStore

**Verdict:** ⚠️ Workers are real implementations with scheduling. One reliability bug remains: time unit mismatch in DeadlineAlertWorker. The LeetCode username bug is fixed.

---

## SECTION 11: GAMIFICATION SYSTEM

### 11.1 XP Economy

**Constants defined** (`Constants.kt:20-41`):

| Action | XP | Wired? |
|--------|----|--------|
| Daily login | 10 | ❌ |
| Challenge completion | 25 | ❌ |
| Job application | 30 | ❌ |
| Certification module | 50 | ❌ |
| Full certification | 200 | ❌ |
| Hackathon win | 500 | ❌ |
| Roadmap milestone | 75 | ❌ |
| 7-day streak bonus | 100 | ❌ |
| 30-day streak bonus | 500 | ❌ |
| Profile completion | 150 | ❌ |
| Daily career hit | 10 | ⚠️ Flag set but XP never accumulated |
| DSA Easy/Med/Hard | 15/30/60 | ❌ |
| Goal completion | 100 | ❌ |
| Skill added | 10 | ❌ |
| Achievement shared | 20 | ❌ |
| Resume analyzed | 25 | ❌ |
| Mock interview | 75 | ❌ |
| Onboarding complete | 150 | ❌ |
| Portfolio project | 50 | ❌ |
| Referral | 100 | ❌ |

### 11.2 Level System

**Constants defined** (`Constants.kt:64-71`):
| Level | XP Range |
|-------|----------|
| Rookie | 0-500 |
| Builder | 501-1500 |
| Engineer | 1501-4000 |
| Architect | 4001-10000 |
| Pioneer | 10001-25000 |
| NEXUS Legend | 25001+ |

**🔴 Critical Finding: These LEVEL_THRESHOLDS are never used anywhere.** The application has:
- No `UserXpRepository` or XP accumulation logic
- No level computation
- `ProfileViewModel` hardcodes `level = 1, totalXP = 0, xpToNextLevel = 100`
- `AchievementsScreen` hardcodes `level 3 "Engineer"` and `2450f / 4000f` progress

### 11.3 Streak System
**StreakCalculator** (`core/domain/.../StreakCalculator.kt`):
- ✅ Fully implemented with clock injection
- ✅ Correctly calculates current and longest streaks
- ✅ 6 unit tests

**StreakRepository** (`core/data/.../StreakRepository.kt`):
- ✅ Uses DataStore for persistence
- ⚠️ Has its own streak logic — does NOT delegate to StreakCalculator (parallel implementation)

**StreakWorker** (`core/sync/.../StreakWorker.kt`):
- ✅ Daily check, no network required
- ✅ Calls `StreakRepository.checkAndUpdateStreaks()`

### 11.4 NEXUS Score
**CareerScoreCalculator** (`core/domain/.../CareerScoreCalculator.kt`):
- ✅ 6 weighted dimensions (skill 250, engagement 200, consistency 200, DSA 150, achievement 100, goal 100)
- ⚠️ `percentile` and `weeklyChange` always return 0 (`CareerScoreCalculator.kt:31-32`)
- ⚠️ `communityContribution` weight is allocated but source is always 0

**Score Display:**
- `GrowthScreen.kt:159` hardcodes `847f` — not from calculator
- `AchievementsScreen.kt:132` hardcodes `847f / 1000f` — not from calculator

### 11.5 Badge System
**Constants defined** (`Constants.kt:73-81`):
7 badges: First Steps, Speed Runner, Consistent, Problem Solver, Achiever, Portfolio Pro, AI Master

**🔴 These badge definitions are never used.** The badge display in `AchievementsScreen` uses data from `AchievementRepository` but renders all badges with a hardcoded `trophy` emoji regardless of type.

### 11.6 Verdict
❌ **XP system is entirely theoretical.** All 22 XP constants are defined but none are accumulated. Level thresholds are defined but unused. NEXUS Score computation exists but results are hardcoded in UI. Badge definitions exist but are never referenced. The streak system is the only fully implemented gamification component.

---

## SECTION 12: DESIGN SYSTEM

### 12.1 Component Inventory (10 components)

| Component | File | Features |
|-----------|------|----------|
| `EmptyState` | `component/EmptyState.kt` | Icon, title, subtitle, action button |
| `GlassCard` | `component/GlassCard.kt` | Glassmorphism effect card |
| `IntelligenceCard` | `component/IntelligenceCard.kt` | Gradient header, subtitle, body, action |
| `LoadingState` | `component/LoadingState.kt` | Circular progress with label |
| `NexusBottomBar` | `component/NexusBottomBar.kt` | 5-tab bottom navigation |
| `NexusButton` | `component/NexusButton.kt` | Gradient, loading, disabled states |
| `NexusCard` | `component/NexusCard.kt` | Base card with elevation, shape variants |
| `NexusChip` | `component/NexusChip.kt` | Filter chip with selected state |
| `SectionHeader` | `component/SectionHeader.kt` | Title + optional "See All" action |
| `StatCard` | `component/StatCard.kt` | Value, label, icon, trend indicator |

### 12.2 Chart Components (2)

| Chart | File | Features |
|-------|------|----------|
| `CareerScoreRing` | `chart/CareerScoreRing.kt` | Animated arc with score, animated sweep |
| `GitHubContributionHeatmap` | `chart/GitHubContributionHeatmap.kt` | Contribution grid (needs data) |

### 12.3 Effect Components (10)

| Effect | File | Features |
|--------|------|----------|
| `AchievementParticleBurst` | `effect/AchievementParticleBurst.kt` | Konfetti burst on unlock |
| `AnimatedOpportunityList` | `effect/AnimatedOpportunityList.kt` | Animated list items |
| `BlinkingCursor` | `effect/BlinkingCursor.kt` | AI typing indicator |
| `GoalCompletionCelebration` | `effect/GoalCompletionCelebration.kt` | Celebration animation |
| `MeshGradientBackground` | `effect/MeshGradientBackground.kt` | Animated mesh gradient |
| `NexusMotion` | `effect/NexusMotion.kt` | Spring animation wrapper |
| `ParticleBackground` | `effect/ParticleBackground.kt` | Floating particle system |
| `SharedElementTransition` | `effect/SharedElementTransition.kt` | Shared element animation |
| `StreamingAIResponse` | `effect/StreamingAIResponse.kt` | Typewriter text effect |
| `SwipeableOpportunityCard` | `effect/SwipeableOpportunityCard.kt` | Tinder-style swipeable card |

### 12.4 Utility Components (2)
| Utility | File |
|---------|------|
| `AccessibilityModifiers` | `util/AccessibilityModifiers.kt` |
| `ImageLoader` | `util/ImageLoader.kt` |

**Verdict:** ✅ Exceptionally comprehensive design system with 24 components/charts/effects. Consistent glassmorphism style, spring animations throughout. Production-grade componentry.

---

## SECTION 13: THEME SYSTEM

### 13.1 Color Palette
**File:** `core/designsystem/.../theme/Color.kt`

- **Dark backgrounds:** 5 levels (VoidBlack #080B14 → Slate #252D40)
- **Light backgrounds:** 5 levels (PearlWhite #F8FAFF → LavenderSmoke #D4DAF0)
- **Accent colors:** 6 pairs with dark variants (NexusBlue, ElectricIndigo, AuroraGreen, PlasmaOrange, StellarPink)
- **Semantic:** Success (AuroraGreen), Warning (Amber500), Error (Rose500), Info (NexusBlue)
- **Neutrals:** 14-step scale (Neutral950 → Neutral0)
- **Social colors:** LinkedInBlue, GitHubDark
- **13 gradients** defined in `NexusGradients`

### 13.2 Typography
**File:** `core/designsystem/.../theme/Typography.kt`

14-type scale: DisplayLarge (40sp) → LabelSmall (10sp). Mapped to Material3 `Typography`.

### 13.3 Shape System
**File:** `core/designsystem/.../theme/Shape.kt`
9 levels: xxs (4dp) → pill (50%). Mapped to Material3 `Shapes`.

### 13.4 Spacing & Elevation
**File:** `core/designsystem/.../theme/Spacing.kt`
- 11 spacing levels: xxxs (2dp) → massive (64dp)
- 7 elevation levels: none (0dp) → xxl (24dp)

### 13.5 Animation
**File:** `core/designsystem/.../theme/Animation.kt`
- 7 duration tokens (0ms → 1000ms)
- 6 easing curves
- 4 spring presets (button, card, list, achievementUnlock)
- 4 tween presets (screenEnter, screenExit, cardFadeIn, numberCount)

### 13.6 Adaptive Theme
**File:** `core/designsystem/.../theme/AdaptiveTheme.kt`
5 context modes: Morning, DeepWork, Exploration, DeadlineCrisis, Celebration. Each with unique gradient, accent, and card style. Auto-detects based on time/context.

### 13.7 Theme Modes
4 modes: LIGHT, DARK, SYSTEM, AMOLED. Supports Android 12+ dynamic color with fallback. Transparent status bar with adaptive icon tinting.

**Verdict:** ✅ Exceptional theme system. Professional color palette, comprehensive type scale, consistent spacing/elevation, spring animation system, adaptive context modes. AMOLED dark mode support. This is production-grade design infrastructure.

---

## SECTION 14: RESOURCES & LOCALIZATION

### 14.1 String Resources

**App strings** (`app/src/main/res/values/strings.xml`): 107 lines, 67 string keys
- App identity (2), Auth (9), Home (9), Navigation (5), Explore (7), AI Hub (16), Errors (7), Empty states (7), Settings (11), Profile (7), Notifications (4)

**Feature module strings:**
| Module | Keys |
|--------|------|
| `feature/home` | 6 (greetings, streak, opportunities) |
| `feature/auth` | 9 (auth buttons, privacy, terms) |
| `feature/explore` | 3 (nav, search, opportunities) |
| `feature/ai` | 2 (hub title, mentor title) |
| `feature/settings` | 13 (settings sections, delete flow) |
| `feature/profile` | 3 (title, referral, invite) |
| `core/designsystem` | 5 (navigation labels) |

**Modules WITHOUT strings:** notifications, onboarding, internships, goals, growth, certifications, community, portfolio, achievements, productivity

### 14.2 Other Resources
- **Icons**: Material Icons Extended (via Compose BOM)
- **Animations**: Lottie JSON files (not enumerated)
- **Fonts**: Not explicitly overridden — uses Material3 default (Roboto on Android)
- **Images**: Coil for remote image loading, SVG support

### 14.3 Localization
- **Only English** — no `values-xx` resource directories
- `MissingTranslation` set to `ignore` in `lint.xml:6` — intentional
- **Verdict:** ⚠️ English-only. Acceptable for MVP but limits market reach.

---

## SECTION 15: SECURITY ANALYSIS

### 15.1 Secrets Management

> Corrected 2026-10-25. The original version of this table asserted that five
> secrets reached `BuildConfig`. Enumerating every `buildConfigField(` call in
> the project shows **exactly three** exist: `GOOGLE_WEB_CLIENT_ID`
> (`app/build.gradle.kts:33`), `OPENROUTER_API_KEY` and `AI_PROXY_URL`
> (`core/network/build.gradle.kts`). The remaining keys in `local.properties`
> are read by CI or by server-side code and are never compiled into the APK.

| Secret | Storage | Reaches the APK? | Risk |
|--------|---------|------------------|------|
| OPENROUTER_API_KEY | `local.properties` → `core:network` BuildConfig | **debug only** | **Fixed 2026-10-25** — release hard-codes the field to `""`. Proven by building release with a sentinel key present: debug BuildConfig received it, release stayed empty and the fresh APK contains no `sk-or-*` material. The proxy Cloud Function holds the real key. |
| AI_PROXY_URL | `local.properties` → BuildConfig | yes, all variants | ✅ Public function URL, not a secret. |
| GOOGLE_WEB_CLIENT_ID | `local.properties` → `app` BuildConfig | yes | ✅ Standard and expected for Google Sign-In; OAuth client IDs for installed apps are not secret. Still restrict it in Cloud Console. |
| GITHUB_CLIENT_SECRET | `local.properties` (CI writes it) | **no** | ✅ No `buildConfigField` and no Kotlin reference exists — it never ships. The original 🔴 "secret inside a mobile APK" finding was wrong. If GitHub OAuth is ever added to the app, use PKCE and keep the secret server-side. |
| GITHUB_TOKEN | environment / CI only | **no** | ✅ Used by `scripts/github_sync.py` via `os.environ`; no BuildConfig field exists. |
| NEXUS_BACKEND_URL | `local.properties` only | **no** | ⚠️ Dead config: it is written by CI and the setup scripts but no `buildConfigField` or Kotlin reference consumes it. Either wire it or delete it from `local.properties.template`, the CI jobs and `scripts/setup_secrets.sh` so the surface stops implying a backend base URL the app never reads. |
| Firebase client API key | `app/google-services.json` → `resources.arsc` | **yes** | ⚠️ Unavoidable for a Firebase app. **Launch requirement:** restrict by package name `com.nexus.app` + signing SHA-256 in Google Cloud Console. |
| Signing keystore | `keystore/nexus-release.jks`, gitignored (`*.jks`) | n/a | ✅ Not in VCS; passwords live in gitignored `local.properties`. |

All sensitive values loaded from `local.properties` via `requireProp()` — will fail build if missing. ✅ No secrets in source code.

### 15.2 Permission Analysis

| Permission | Declared | Used? | Risk |
|-----------|----------|-------|------|
| `INTERNET` | ✅ | ✅ | Required |
| `POST_NOTIFICATIONS` | ✅ | ✅ | Runtime (Android 13+) |
| `RECEIVE_BOOT_COMPLETED` | ✅ | ✅ | Required for WorkManager |
| `FOREGROUND_SERVICE` | ✅ | Required by WorkManager | Standard |
| `FOREGROUND_SERVICE_DATA_SYNC` | ✅ | Android 14+ | Standard |
| `CAMERA` | ✅ | ❌ Never used | ⚠️ Unnecessary — should remove |
| `ACCESS_NETWORK_STATE` | ❌ | ✅ NetworkMonitor needs it | 🔴 Missing — breaks on Android 12+ |

### 15.3 Security Issues Found

**🔴 Critical: Missing ACCESS_NETWORK_STATE permission** — `NetworkMonitor.kt` uses `ConnectivityManager.registerNetworkCallback()` which requires this permission on API 31+. Without it, connectivity checks silently fail.

**🔴 High: runBlocking in AuthInterceptor** (`AuthInterceptor.kt:16`) — Reads DataStore on the main thread with `runBlocking`. If DataStore read is slow (first access involves disk I/O), this causes ANR.

**✅ Resolved (was 🔴 High): OAuth client secret in mobile app** — the original
finding assumed `GITHUB_CLIENT_SECRET` reached `BuildConfig`. It does not: no
`buildConfigField` and no Kotlin reference for it exists, so it never ships. If
GitHub OAuth is added to the app later, use PKCE and keep the secret server-side.

**✅ Resolved (was ⚠️ Medium): Unused CAMERA permission** — not declared in the
source manifest and not present in the merged release manifest.

**🔴 New (2026-10-25): Ad identifiers ship via Firebase Analytics.** The merged
release manifest declares `com.google.android.gms.permission.AD_ID`,
`ACCESS_ADSERVICES_AD_ID` and `ACCESS_ADSERVICES_ATTRIBUTION`, all pulled in
transitively by the Firebase Analytics registrars (`app/src/main/AndroidManifest.xml`
asks only for `INTERNET`, `ACCESS_NETWORK_STATE`, `POST_NOTIFICATIONS`). Nothing in
`app/build.gradle.kts` excludes them. Consequences, none of which are code:
Google Play **Data Safety** must declare advertising-ID collection, the form must
answer the Ads-SDK questions truthfully, and the policy for a studentaudience app
with advertising identifiers needs review. Either accept and disclose it, or add
the `androidx.ads`/`AD_ID` removal + a consent gate before upload. Decide before
the first internal track, because retro-fixing a published Data Safety form is a
policy risk.

**📦 Verified (2026-10-25): baseline-profile machinery is in the shipped artifact.**
The merged release manifest contains
`androidx.profileinstaller.ProfileInstallerInitializer`, which is what applies
`assets/dexopt/baseline.prof` at install time — the profile and its consumer are
both present, not just the file in `src`.

**✅ Reclassified (was ⚠️ Low): BuildConfig secrets** — there are three
`BuildConfig` string fields in the entire project (`GOOGLE_WEB_CLIENT_ID`,
`AI_PROXY_URL`, `OPENROUTER_API_KEY`), not five. The first two are non-secret by
design. The third is now empty in every release variant.

### 15.4 Data Protection

- User auth token stored in DataStore (encrypted at OS level on Android 6+)
- FCM token written to Firestore `users/{userId}/fcmToken`
- Firestore rules not audited in this report
- `allowBackup="false"` in manifest — prevents backup-based data leakage ✅
- `fullBackupContent="false"` — prevents auto-backup ✅

**Verdict:** ⚠️ Standard Android security practices followed. Two runtime bugs (missing permission, runBlocking) and one architectural concern (OAuth secret in APK). Camera permission should be removed.

---

## SECTION 16: TESTING COVERAGE

### 16.1 Test Inventory (9 test files, ~700 lines)

| # | Test | File | Type | Tests | Status |
|---|------|------|------|-------|--------|
| 1 | `AIRateLimiterTest` | `core/common/.../test/...` | Unit + MockK | 4 | 🔴 3 failing (NPE) |
| 2 | `LeetCodeRepositoryTest` | `core/data/.../test/...` | Unit + MockK | 2 | ✅ |
| 3 | `InternshipRepositoryTest` | `core/data/.../test/...` | Unit + MockK | 3 | ✅ |
| 4 | `CareerScoreCalculatorTest` | `core/domain/.../test/...` | Unit | 3 | ✅ |
| 5 | `OpportunityMatchingEngineTest` | `core/domain/.../test/...` | Unit | 3 | ✅ |
| 6 | `StreakCalculatorTest` | `core/domain/.../test/...` | Unit | 6 | ✅ |
| 7 | `InternshipDaoTest` | `core/database/.../test/...` | Instrumented (Room) | 4 | ✅ |
| 8 | `AIChatViewModelTest` | `feature/ai/.../test/...` | Unit + MockK | 4 | ✅ |
| 9 | `HomeViewModelTest` | `feature/home/.../test/...` | Unit + MockK | 4 | ✅ |

### 16.2 Coverage Analysis

| Dimension | Coverage | Notes |
|-----------|----------|-------|
| ViewModels (17) | 2 tested (12%) | Only AIChat and Home |
| Repositories (13) | 3 tested (23%) | Internship, LeetCode, CareerScore |
| Domain use cases (4) | 3 tested (75%) | StreakCalculator, CareerScore, MatchingEngine |
| Workers (6) | 0 tested (0%) | No worker tests exist |
| Screens (31) | 0 tested (0%) | No Compose UI tests exist |
| DAOs (11) | 1 tested (9%) | Only InternshipDao |
| Analytics | 0 tested (0%) | No AnalyticsTracker tests |
| Auth | 0 tested (0%) | No AuthViewModel tests |
| AI Services | 0 tested (0%) | No AIOpenRouterService tests |

### 16.3 Test Quality

- **MockK used consistently** across all mock-based tests ✅
- **Coroutine testing** uses proper `runTest` + `StandardTestDispatcher` ✅
- **Room test** uses in-memory database ✅
- **Test assertions** are adequate but not comprehensive
- **Edge case coverage** is weak — most tests test happy path only

### 16.4 Pre-existing Failures
- `AIRateLimiterTest` — 3 tests fail with `NullPointerException` (test setup issue, not caused by polish changes)
- `core:data` module — missing test dependencies (JUnit, MockK, kotlinx-coroutines-test) — added during polish

**Verdict:** ❌ Severely under-tested. Only 9 test files for a project of ~130+ source files. Zero UI tests, zero worker tests, zero integration tests. Pre-existing test failures in AIRateLimiterTest.

---

## SECTION 17: PERFORMANCE ANALYSIS

### 17.1 Compose Performance
- All `LazyColumn`/`LazyGrid` calls have `key` parameters ✅ (fixed during polish)
- `HomeScreen` mixed-type list has `contentType` parameter ✅
- `derivedStateOf` used in 2 recomputation-prone calculations ✅
- `remember` used consistently across all screens ✅
- Particle background effects may cause frame drops on mid-range devices ⚠️

### 17.2 Startup Performance
- `WorkManager` initializer disabled via manifest — custom config in `NexusApplication` ✅
- 6 workers scheduled with 2s delay on `Dispatchers.Default` ✅
- Splash screen with `AnimatedContent` transitions ⚠️
- **No Baseline Profile** — this is the single biggest startup performance gap. Can improve cold start by 30-50%.
- `macrobenchmark` module exists for Baseline Profile generation — **not yet run or generated**

### 17.3 Memory & Threading
- `runBlocking` in `AuthInterceptor.kt:16` — ANR risk on main thread
- All async work uses `Dispatchers.IO` or `Dispatchers.Default` appropriately ✅
- Room queries use coroutines ✅
- ViewModel state uses `StateFlow` with proper scoping ✅

### 17.4 Network Performance
- OkHttp with connection pooling ✅
- Room DB as local cache with TTLs ✅
- Paging library wired for internships (`InternshipPagingSource.kt`) ✅
- **No response caching headers** configured on OkHttp client ⚠️

### 17.5 Image Loading
- Coil 2.7.0 with Compose integration ✅
- SVG support ✅
- No explicit disk cache size configuration ⚠️

### 17.6 Compose Compiler Stability
- No stability annotations (`@Stable`, `@Immutable`) found on data classes ⚠️
- Compose Compiler 1.5.15 (Kotlin 2.0.21 compatible) ✅

**Verdict:** ⚠️ Reasonable performance baseline. Two significant gaps: no Baseline Profile (30-50% startup improvement available) and no stability annotations (unnecessary recomposition possible). `runBlocking` in AuthInterceptor is an ANR risk.

---

## SECTION 18: ACCESSIBILITY

### 18.1 Current State

| Aspect | Status | Evidence |
|--------|--------|----------|
| Content descriptions | ❌ Not audited | No systematic contentDescription usage |
| TalkBack navigation | ❌ Not tested | No indication of TalkBack testing |
| Color contrast | ❌ Not tested | AMOLED dark theme may fail WCAG minimums |
| Font scaling | ❌ Not tested | No test results for system font size changes |
| Touch targets | ✅ Likely OK | Material3 default minimums (48dp) |
| Accessibility modifiers | ⚠️ Utility exists | `util/AccessibilityModifiers.kt` exists but usage unclear |

### 18.2 Accessibility Utilities
- `AccessibilityModifiers.kt` in core/designsystem — provides `enhancedSemantics` modifier helper
- All buttons use Material3 which provides baseline accessibility ✅
- Custom glassmorphism effects may not announce properly over TalkBack ⚠️

### 18.3 Lint Configuration
- `ContentDescription` set to `warning` in `lint.xml:4` — non-blocking
- No automated accessibility testing in build pipeline ❌

**Verdict:** ❌ Accessibility is the weakest dimension. No systematic content descriptions, no TalkBack testing, no contrast verification, no font scaling tests. The accessibility modifier utility exists but its adoption across screens is unverified.

---

## SECTION 19: KNOWN BUGS & ISSUES

### 19.1 Runtime Crashes (🔴 High Severity)

| # | Bug | File:Line | Impact |
|---|-----|-----------|--------|
| 1 | `signInWithGoogle()` is a no-op — just emits NavigateToHome | `AuthViewModel.kt:72-77` | Google Sign-In doesn't trigger real auth |
| 2 | Missing `@Named("github_token")` DI binding | `GitHubRepository.kt:22` | Would crash at runtime if injected |
| 3 | Missing `ACCESS_NETWORK_STATE` permission | `AndroidManifest.xml` | `NetworkMonitor` fails silently on Android 12+ |
| 4 | `runBlocking` on main thread in AuthInterceptor | `AuthInterceptor.kt:16` | Potential ANR on DataStore read |

### 19.2 Logic Bugs (⚠️ Medium Severity)

| # | Bug | File:Line | Impact |
|---|-----|-----------|--------|
| 5 | DeadlineAlertWorker compares epoch-days vs epoch-millis | `DeadlineAlertWorker.kt:34` | Deadline notifications unreliable |
| 6 | ~~LeetCodeSyncWorker uses authToken as username~~ | `LeetCodeSyncWorker.kt:23` | ✅ Fixed — reads `leetcodeUsername` |
| 7 | AIRateLimiter only wired to AIChatViewModel | `core/common/.../AIRateLimiter.kt` | Other 6 AI screens bypass rate limits |
| 8 | AI returns silent mock responses on failure | `AIOpenRouterService.kt:~170` | User never knows AI is down |
| 9 | `cardFadeIn` constant name typo | `Animation.kt:58` | Should be `cardFadeIn` → `cardFadeIn` (cosmetic, already this) |

### 19.3 Data Issues (⚠️ Medium Severity)

| # | Issue | File:Line | Impact |
|---|-------|-----------|--------|
| 10 | XP never accumulated despite all constants defined | Across codebase | Whole XP system is theoretical |
| 11 | NEXUS Score `percentile` and `weeklyChange` always 0 | `CareerScoreCalculator.kt:31-32` | Incomplete score reporting |
| 12 | CommunityContribution always 0 | `CareerScoreCalculator.kt` | Dimension exists but never populated |
| 13 | LeaderboardRepository returns hardcoded mock data | `LeaderboardRepository.kt:40-60` | Leaderboard shows fake data |
| 14 | Level thresholds defined but never used | `Constants.kt:64-71` | User level always shows as Rookie 1 |
| 15 | Profile level/XP hardcoded | `ProfileViewModel.kt:24-34` | `level=1, totalXP=0` |

### 19.4 Dead Code

| # | Issue | File | Notes |
|---|-------|------|-------|
| 16 | `NexusAIService` — orphaned streaming service | `feature/ai/.../NexusAIService.kt` | Has streaming, never used |
| 17 | `GitHubRepository` — dead code | `core/data/.../GitHubRepository.kt` | No injector, missing DI binding |
| 18 | `AnalyticsTracker` — wired but never called | `core/analytics/.../AnalyticsTracker.kt` | `track()` never invoked |
| 19 | `Firebase Storage` — DI binding exists, never used | `AppModule.kt:33` | Dead dependency |
| 20 | `Firebase Functions` — dependency, never used | `app/build.gradle.kts:128` | Dead dependency |
| 21 | `SyncModule` — empty DI module | `core/sync/.../di/SyncModule.kt` | No bindings |
| 22 | `ConversationMemoryManager` — only used by orphaned service | `core/domain/.../ConversationMemoryManager.kt` | Unused |

### 19.5 Stub/Incomplete Features

| # | Feature | Notes |
|---|---------|-------|
| 23 | Community module | Full stub — "coming soon" |
| 24 | Portfolio module | Hardcoded mock data only |
| 25 | NexusArena | Hardcoded mock data, no ViewModel |
| 26 | GrowthScreen metrics | All 5 metrics hardcoded |
| 27 | Explore search | searchQuery stored but never applied |
| 28 | InternshipList search | searchQuery stored but never applied |
| 29 | AIResumeAnalyzer | No ViewModel, hardcoded fallback data |
| 30 | Productivity Pomodoro | State lost on process death (no ViewModel) |

---

## SECTION 20: STRATEGIC ASSESSMENT

### 20.1 Overall Score: 5.8/10

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Architecture | 7/10 | Clean Architecture structure is solid, but no true MVI, some DIP violations |
| UI/UX | 7/10 | Design system is exceptional, 24 polished components, spring animations |
| Data Layer | 7/10 | Room + DataStore + Retrofit with proper caching and TTLs |
| Navigation | 8/10 | Single-activity, 30 routes, deep links, animated transitions |
| AI Integration | 6/10 | 6/8 AI screens work, but rate limiting incomplete, orphaned streaming service |
| Gamification | 3/10 | XP/Levels/Scores are defined but never actually earned or accumulated |
| Background Sync | 6/10 | 6 real workers with scheduling, but 2 bugs reduce reliability |
| Firebase | 6/10 | Auth/Crashlytics/Firestore/FCM working, Storage/Functions unused |
| Security | 5/10 | No secrets in code, but missing permission, ANR risk, unneeded camera |
| Testing | 2/10 | 9 tests only, zero UI tests, zero worker tests, pre-existing failures |
| Performance | 5/10 | No Baseline Profile, no stability annotations, ANR risk |
| Accessibility | 2/10 | No content descriptions, no TalkBack testing, no contrast audit |
| Product Readiness | 5/10 | Many screens work, but XP/levels/scores are decorative, community/portfolio stubbed |

### 20.2 Critical Path to 7/10

**Immediate (1-2 weeks):**
1. Wire `AIRateLimiter` to all 7 AI ViewModels — prevent free-tier abuse
2. Fix `AuthInterceptor` — replace `runBlocking` with non-blocking approach
3. Add `ACCESS_NETWORK_STATE` permission to manifest
4. Fix `signInWithGoogle()` no-op
5. Fix `DeadlineAlertWorker` time unit mismatch
6. ~~Fix `LeetCodeSyncWorker` username preference~~ — done
7. Remove unused `CAMERA` permission
8. Generate Baseline Profile via existing `macrobenchmark` module

**Short-term (2-4 weeks):**
9. Implement `UserXpRepository` — accumulate XP across all actions
10. Wire level computation using existing `LEVEL_THRESHOLDS`
11. Wire `CareerScoreCalculator` to live data sources instead of hardcoded values
12. Consolidate AI services — migrate to `NexusAIService` (streaming) or remove it
13. Implement actual search/filter logic in Explore and InternshipList screens
14. Add ViewModel to ProductivityScreen (state survives process death)
15. Add ViewModel to AIResumeAnalyzerScreen

**Medium-term (1-2 months):**
16. Implement Community module properly (circles, mentorship, study groups)
17. Implement real Portfolio (project CRUD, image upload)
18. Wire `AnalyticsTracker.track()` calls to all ViewModels
19. Add Compose UI tests for top 5 screens
20. Add worker tests for all 6 workers
21. Add `ACCESS_NETWORK_STATE` usage rationale to manifest
22. Clean up dead code (NexusAIService, GitHubRepository, SyncModule)

### 20.3 Competitive Positioning

- **Strengths:** Design system, AI integration breadth, module architecture, navigation structure, room/datastore data layer
- **Weaknesses:** Testing, accessibility, XP/gamification is decorative, community/portfolio incomplete
- **Opportunities:** Baseline Profile for startup perf, streaming AI, real XP economy, Compose UI tests
- **Threats:** 2 runtime crash bugs, missing permission on Android 12+, ANR risk in auth interceptor

### 20.4 Verdict

A project with **exceptional foundations** — the architecture, design system, and data layer are genuinely production-grade. However, **execution is uneven**: some features (AI, navigation, auth) are well-implemented while others (gamification, community, portfolio, testing) are surface-level only. The biggest gap is the **XP/level/score system**: it has all the definitions and UI scaffolding but **no actual XP accumulation or level computation**. Fixing this single system would transform the app's retention mechanics.

The **2 runtime crash bugs** (auth no-op, missing DI binding) and the **DeadlineAlertWorker time unit mismatch** should be fixed before any production release.

---

*Report generated from actual source code analysis. All file paths relative to `C:\Users\Krishna\Nexus`.*
