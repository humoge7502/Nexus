# NEXUS 2.0 Feature Implementation Status

> Last verified: **2026-10-26**, on JDK 17 + Android SDK 36 + AVD `NexusApi35`
> (Android 15) and the Firebase emulator. See
> `docs/NEXUS_COMPREHENSIVE_ANALYSIS.md` for the full verification report and
> remaining backlog.

## Intelligence layer — COMPLETE

- [x] Room entities, DAOs and migrations through **schema v10**
      (v9 → v10 adds `certification_progress` with per-module completion)
- [x] Domain models and repository interfaces/implementations
- [x] Hilt bindings for score snapshots, cohort, forecasts and mirrors
- [x] `NexusAnalytics` events for mirror/forecast/interview flows
- [x] `PlacementPredictionEngine` (rule + ML factors)
- [x] `PlacementReadinessModel` (trained, exported Kotlin, golden-vector tested)
- [x] `PlacementReadinessFeatures` (raw app signals → model features)
- [x] `InterviewIntelligenceEngine` (company round maps + weak-spot flags)
- [x] `CareerMirrorGenerator`, `ConfidenceCalibrationEngine`
- [x] `CareerScoreCalculator` — NEXUS Score from skill progress, goals,
      streak, solved problems and unlocked badges; feeds Achievements
- [x] `PlacementReadinessCalculator` — real 0–100 score, level, ordered action
      items and estimated timeline; drives Home and Growth
- [x] Single source of truth for streaks: `StreakRepository` (DataStore
      `streak_prefs`). Home records daily activity, `StreakWorker` keeps it warm,
      and the dead `UserPreferences.studyStreak` field was removed
- [x] Onboarding personalization persisted atomically (name, college, year,
      skills, interests, career goal) with a skip path
- [x] AMOLED toggle separated from dark mode

## UI layer — COMPLETE for NEXUS 2.0 screens

- [x] `CareerMirrorScreen` + ViewModel
- [x] `PlacementForecastScreen` + ViewModel — readiness card, drivers,
      what-if simulator, refresh, empty/error states
- [x] `CohortIntelligenceScreen` + ViewModel
- [x] `InterviewLogScreen` + ViewModel
- [x] `TribeSetupScreen` + ViewModel — campus tribe creation/join/leave,
      persisted in DataStore, wired into navigation
- [x] Interview coach with target-company round intelligence
- [x] Deep Research (`feature:explore`) — agent-search pattern
- [x] Achievement gallery — real XP/badges/streak stats, computed NEXUS Score,
      back navigation, seeded badge catalog, empty state
- [x] Navigation destinations wired from `MainActivity`

## UI/UX & data-integrity pass — 2026-10-26

Design:
- [x] Distinctive typography pairing and app-wide navigation motion
- [x] Premium surface effects: press-scale feedback, sheen sweep, hairline
      strokes, arc glow
- [x] Material You (dynamic color) is the default, with AMOLED opt-in
- [x] Certifications detail: removed the dimming scrim, progress is legible,
      compact single-line CTAs, sticky action row
- [x] Home: career-readiness card now shows the real calculator score and a
      dynamic next action; QuickStats use real cert/interview/streak counts
- [x] Achievements: removed the nested-scroll grid, fixed the negative-padding
      crash, added back navigation, real stats and top-percentile display

Correctness:
- [x] Home and Growth now agree on streak (single `StreakRepository`)
- [x] Certification progress persists (schema v10) and updates on-device
- [x] Badge catalog seeded; `unlock` ids match `AchievementType` names
- [x] NEXUS Score no longer hard-coded to zero
- [x] Catalog sizes are never presented as the user's own completed counts
- [x] Audit tooling compiles and runs (`RegexOption.IGNORE_CASE`,
      `ScanUtil.relativePath`, unterminated regex, `runBlocking` in CLI)
- [x] Production-readiness audit is trustworthy and gated: tests + the audit
      tool's own modules are excluded from scans, Kotlin-DSL minify and real
      Application subclasses are detected, the only feature→feature edge
      (`growth → ai`) is gone via `core:network.ai`, and the verdict is
      `READY` at **10.0/10** (all 8 phases 10.0, no CRITICAL/HIGH/MEDIUM/LOW
      findings). The new CI `audit` job fails on `NOT_READY` and uploads
      md + json reports.
- [x] Tribe and Paywall ViewModels covered by 16 JVM tests — join validation,
      slug/truncation, cohort degradation, billing product mapping, all three
      purchase tiers, restore, and Pro persistence

## Verified build & tests (2026-10-26)

- [x] `:app:assembleDebug` exits 0
- [x] `:app:assembleRelease` — R8 + resource shrinking, signed,
      `apksigner verify` exit 0
- [x] Full JVM suite `./gradlew test` green —
      **223 unique tests / 35 suites, 0 failures, 0 skipped**
      (185 Android debug unit tests + 38 pure-JVM audit tests; the release
      variant runs the 185 again with release `BuildConfig` and passes)
- [x] ViewModel JVM suites — **15/15**: Placement Forecast (7: signal mapping
      through the ML model, deterministic persisted forecasts, what-if clamp,
      blank-user and refresh-failure paths) and Onboarding (8: profile fields,
      five-skill cap, save/skip payloads, XP award, datastore completion)
- [x] Backend `npm test` green (10 tests) and `tsc --noEmit` clean
- [x] ML dataset reproducible byte-for-byte; Python↔Kotlin parity green
- [x] `connectedDebugAndroidTest` — **12/12 executed on AVD `NexusApi35`
      (API 35)**: `migrateAll` 1→10 validated against the Room schema, v9→v10
      `certification_progress` coverage, per-step data preservation,
      `InternshipDaoTest`, `Nexus2DaoTest`
- [x] TokenCipher device suite — **4/4 on AVD `NexusApi35`**: Keystore
      AES-256-GCM roundtrip, unique IV per encryption, legacy plaintext
      passthrough, and tamper/corrupt ciphertext fails closed
- [x] Compose UI suites — **22/22 on AVD `NexusApi35`**: Career Mirror (5),
      Cohort Intelligence (3), Interview Log (3), Placement Forecast (5),
      Tribe (7); loading/empty/error states, back navigation, refresh, setup-form
      submission, cohort benchmarks, and leave-tribe callback. Screens now expose
      stateless `*Content` composables so these tests run without a Hilt graph,
      and CI's `instrumented` job runs all six device suites
- [x] Firebase emulator suites — callables **5/5** (`npm run test:emulator`),
      Firestore rules **11/11** (`npm run test:rules`); both wired into CI
- [x] Baseline profile generated on-device (`:app:generateBaselineProfile`) and
      packaged in the release APK as `assets/dexopt/baseline.prof` + `.profm`
- [x] Startup (`StartupTimingMetric`) and scroll (`FrameTimingMetric`)
      macrobenchmarks wired in `:macrobenchmark`; the combined suite runs green
      on `NexusApi35` (3/3, profile generation skipped as already present) with
      **TTID median 1,345.6 ms** (min 542.5 / max 1,767.2) and scroll
      `frameDurationCpuMs` P50 97.8 / P95 816.5 ms (5 iterations each).
      Emulator numbers are a harness baseline, not a device-representative budget
- [x] Release APK scanned for `sk-or-*` key material — clean
- [x] Minified release installed and driven on `NexusApi35`: auth → onboarding →
      Home → Explore → AI Hub → Growth → Profile, plus an internship detail,
      crash buffer empty. This pass caught and fixed an R8-only regression where
      tab selection compared obfuscated `qualifiedName` strings, hiding the
      bottom bar in release; matching now uses `NavDestination.hasRoute(KClass)`

Device walkthrough verified this pass: Home (1-day streak, 2% readiness with a
real "50 more problems" action), Growth (1 Day · Best: 1), certification detail
(12% → 25% after "Mark done", persisted across restart), Profile (1 Day streak),
Achievements (7 seeded badges, 130 XP, 0 unlocked, Top 5% band, crash-free).
The rebuilt signed APK repeated the five-tab walkthrough with the bar visible
and the selection pill following each tab.

## Remaining

Console/account-gated — cannot be closed from this repo:

- Firebase project login and `firebase deploy` (nothing has ever been deployed)
- App Check with Play Integrity, so the AI proxy can't be scripted
- Crashlytics enablement + consent copy
- Restricting the Firebase client API key by package name + SHA-256 in Cloud
  Console (this key legitimately ships inside `resources.arsc`)

Engineering backlog:

- Device-representative startup/frame-time budgets — emulator harness baseline
  exists, physical-device thresholds still to be recorded
- Pro paywall loading/billing-unavailable polish on devices without Play Billing
- Paging 3 for catalogs beyond Explore/Internships
- Accessibility audit (i18n strings are externalized; copy itself is English-only)
- Real (consented, anonymized) placement cohort for retraining

## 2026-10-27 — Audit, static analysis & documentation pass

- [x] 8-phase audit toolkit implemented end-to-end (`phase-analyzers`,
      `audit-rules`, `audit-reporting`, `audit-cli`) and verified:
      `./gradlew runAudit` → **READY, 10.0/10**, md/json/html written
- [x] 6 new JVM tests for the analyzers + reporter (registry, secret/HTTP/
      allowBackup detection, `audit-allow` suppression, all report formats) — green
- [x] `audit-allow: <RULE-ID>` suppression and audit-toolkit self-exclusion, so
      the audit never flags its own regexes
- [x] Detekt blocking gate with a reviewed 297-finding baseline; CI `detekt` job
- [x] Dependabot for Gradle, npm, pip and GitHub Actions
- [x] New docs: `SECURITY.md`, `TESTING.md`, `CONTRIBUTING.md`, `docs/adr/`
      (10 ADRs), `docs/DESIGN_SYSTEM.md`, `docs/COMPETITIVE_LANDSCAPE.md`,
      `ml/placement_readiness/DATA_DICTIONARY.md`
- [x] CI audit job now uses `:audit-cli:runProductionAudit` and emits
      md + json + html
- [x] Every `Text("...")` literal externalized to per-module `strings.xml`
      (182 literals across 25 modules, incl. `%1$s`-templated copy); audit
      regex residual is 0 and the LOW i18n finding is cleared → **10.0/10**

## 2026-10-27 — Verification after string externalization

- [x] `./gradlew testDebugUnitTest coverageGate` green; coverage floors hold at
      domain **43.42%**, data **26.72%**, network **29.45%**, aggregate **33.69%**
- [x] `./gradlew runAudit` → **READY, 10.0/10**, startup quality **10.0/10**;
      all 8 phases 10.0 and only 6 INFO notes (no CRITICAL/HIGH/MEDIUM/LOW)
- [x] Backend callable/unit suites green after the LeetCode path moved in-house:
      npm **16/16**, callable emulator **7/7**, Firestore rules **12/12**
- [x] Signed release rebuilt after the resource sweep and re-verified

## 2026-10-27 — SSE streaming, Community live data & Hilt binding

- [x] `streamAI` onRequest bridge (`backend/functions/src/ai/stream.ts`):
      Firebase ID-token auth, the same `validateRequest` caps and transactional
      daily quota as `proxyAI`, OpenRouter `stream:true` with usage reporting,
      90 s upstream timeout, abort on client disconnect, server-only error
      bodies, and `success`/`cancelled`/`timeout` logging to `ai_proxy_logs`
- [x] `AIStreamClient` (`core:network`): derives the `streamAI` URL from
      `AI_PROXY_URL` or the Firebase project, sends the ID token as a Bearer
      header, streams deltas through `callbackFlow` with an unlimited buffer,
      parses `data:` deltas, `usage.total_tokens`, `[DONE]` and in-band errors,
      and maps 401/429/503 to user-facing messages
- [x] `NexusAIService` proxy path now emits SSE deltas end-to-end; the chat UI
      needed no changes because it already consumed `Flow<String>`
- [x] Backend tests **21/21** (`stream.test.js` adds bearer/status/usage
      parsing), emulator checks **10/10** (new: stream non-POST 405, anonymous
      401, validation 400 + fail-closed 503), rules **12/12**
- [x] `AIStreamClientTest` — 12 tests covering delta ordering, usage, auth and
      payload shape, quota/401 mapping, malformed chunks, in-band errors,
      zero-token completion, sign-in gating and URL derivation
- [x] `./gradlew testDebugUnitTest coverageGate` green: **311 unique tests /
      49 suites**, coverage floors hold at domain **43.42%**, data **26.69%**,
      network **38.30%** (was 29.45%), aggregate **35.14%** (was 33.69%)
- [x] Community now renders live Room-backed hackathons and the DataStore
      college with pull-to-refresh and navigation into Cohort/Tribe/Hackathon;
      4 ViewModel tests + 2 Compose UI tests (25/25 UI suite on `NexusApi35`)
- [x] Added the missing `IOpportunityRepository` Hilt binding in
      `RepositoryModule` — caught by app-wide Hilt aggregation during the full
      test run
- [x] Docs refreshed: README, TESTING, comprehensive analysis and intelligence
      report now describe the SSE path and current test/coverage figures
- [x] Signed release rebuilt after the SSE change: **7,557,039 B**, `apksigner`
      verify passes, APK key scan clean (no `sk-or-*` material)
- [x] `./gradlew runAudit` → **READY, 10.0/10**, startup quality **10.0/10**;
      all 8 phases 10.0 and only 6 INFO notes (no CRITICAL/HIGH/MEDIUM/LOW)

## 2026-10-27 — Leaderboard integrity, i18n completion & Arena real data

- [x] Profile screen fully externalized (26 resources) and the skill-radar
      `Canvas` now has a generated TalkBack description listing every axis score
- [x] i18n sweep across 14 feature modules (home, achievements, growth,
      settings, onboarding, paywall, placement forecast, career mirror, cohort
      intelligence, interview log, certifications, internships, explore) —
      every screen renders through `stringResource`
- [x] Fixed five machine-generated resources that kept Kotlin `$name` templates
      and rendered literal placeholders: growth solved/total, home company—role,
      notifications unread count, productivity pomodoro count, and interview-log
      per-company counts now use `%1$s`/`%1$d` with call-site arguments
- [x] Leaderboard data chain rebuilt: reads/writes moved to the opt-in
      `publicProfiles` collection (world-readable, owner-writable), publish and
      leave APIs added, XP sync no longer attempts to write the rules-guarded
      `level`, and two composite indexes added for college/domain ranking
- [x] `publicProfiles` rules allowlist enforces the ranking schema and numeric
      `totalXP`/`streak` on both create and update; cross-user writes, injected
      fields and non-numeric scores are denied — rules suite **13/13**
- [x] NEXUS Arena rewritten: no fabricated competitors, scores, participants or
      deadlines; it now shows the signed-in user's real XP/streak, the live
      global board, backend-computed rank/percentile, and explicit opt-in
      join/leave controls with error states
- [x] `ArenaRankingTest` — 5 tests for rank→top-percent conversion (unranked,
      every decile boundary, rounding, coercion); `feature:growth` JVM green
- [x] Backend `npm test` **21/21**; docs refreshed for the new leaderboard
      architecture and test counts

## 2026-10-27 — Chart accessibility, real score history & copy sweep

- [x] Every custom `Canvas` chart now exposes TalkBack semantics: line, bar and
      donut charts (`core:ui`), weekly-XP bars, DSA progress, skill radar,
      career-score trend, the GitHub contribution heatmap, and the
      `CareerScoreRing`; descriptions are generated from live values through
      localized templates
- [x] Score/timer rings expose `ProgressBarRangeInfo` (Growth NEXUS ring,
      Achievements score arc, Pomodoro timer); the decorative readiness ring is
      hidden from TalkBack so the adjacent percentage is not read twice
- [x] Weekly score delta is now real: `recordScoreSnapshot` persists one
      `ScoreSnapshot` per calendar day from the Growth and Achievements
      collectors, and `weeklyChangeOf` compares the current score against the
      earliest snapshot in a 7-day window; 6 `WeeklyScoreChangeTest` cases
      cover empty/partial/positive/negative/expired/zero states
- [x] Percentile is derived from the live public leaderboard via the shared
      `topPercentOf` rule instead of score buckets; unranked users see a
      join-Arena prompt rather than a fabricated rank
- [x] Remaining chrome copy externalized: Home greetings, View All / Track /
      Try again / Browse all, day streak moved to a real plural resource,
      readiness caption, Growth live-score copy, and Pomodoro
      Start/Pause/Focus labels
- [x] Full JVM suite green: **322 unique tests / 51 suites** (274 Android debug
      + 48 pure-JVM audit); `:app:compileDebugKotlin` green
- [x] Docs refreshed with the new test counts, the 13-test rules suite, the
      40-test device suite, and the chart-accessibility status

## 2026-10-27 — Quality gate closure & signed release re-verification

- [x] Detekt gate now passes at `DETEKT_MAX_ISSUES=0` (exit 0). All 60 new
      findings from the refactor pass were resolved: over-length lines wrapped,
      `throw IllegalStateException` → `error()`, dead locale properties removed,
      the SSE read loop restructured, and `LongMethod`/`LongParameterList`
      hotspots split into focused composables or explicitly suppressed with a
      justification
- [x] Full JVM suite re-run after the cleanup: **322 tests / 51 suites**, zero
      failures, zero errors, zero skipped (274 Android debug + 48 pure-JVM
      audit modules)
- [x] Signed release rebuilt from the cleaned tree: `:app:assembleRelease`
      green, `app-release.apk` at 7.3 MB, `apksigner verify --print-certs`
      confirms the release certificate (SHA-256 `02a7c369…6ceab9`)
- [x] Production-readiness audit (`./gradlew runAudit`) exits 0 with
      recommendation **READY**; HTML report at `build/audit/audit-report.html`

## 2026-10-27 — Exact global percentile & published-score sync

- [x] Percentile is no longer computed against a fetched slice. The new
      `getGlobalRanking()` uses Firestore count aggregations (total published
      profiles + profiles strictly above the user's published XP) to return an
      exact rank; `AchievementsViewModel` and `NexusArenaViewModel` both use it,
      with the fetched-board calculation only as an offline/aggregation
      fallback
- [x] Published profiles stay current: `NexusArenaViewModel.refresh()` now
      re-syncs the public profile (name, college, XP, streak) before loading
      the board, so rank, percentile and the visible leaderboard agree after
      XP gains; sync failures are logged without blocking the board
- [x] Re-verified after the change: detekt gate exit 0, feature/achievements +
      feature/growth + core/data compile and unit tests green, signed release
      rebuilt (7.3 MB) and `apksigner verify` passes with the same certificate

## 2026-10-28 — Lint gate made real, device suite re-verified, copy bug fixed

- [x] Replaced the 28 stale per-module `lint-baseline.xml` files (1,824
      suppressed findings, 1,356 of them `UnknownIssueId`) with one regenerated
      `app/lint-baseline.xml` (62 current findings). `:app:lintDebug` exits 0
      with "Lint found no new issues"
- [x] Fixed all 8 errors the cleanup surfaced: escaped Windows paths in
      `local.properties` (PropertyEscape), removed `autoVerify` from the custom
      `nexus://` intent filter (AppLinkUrlError), and deleted genuinely dead
      resources — five unreferenced Lottie raw files, `file_provider_paths.xml`,
      `nexus_widget_info.xml` and the `widget_description` string
- [x] Re-ran the instrumented suite on `NexusApi35`: **42/42 green** across 9
      modules — 12 Room/DAO + 4 TokenCipher + 25 Compose UI + 1 `:app`
      MainActivity cold-start boot test (the previous headline of 40/40
      counted 24 Compose tests)
- [x] The rerun caught a device-only copy bug: Tribe's empty-state sentence was
      split across a string resource ending in a space and a hardcoded suffix;
      AAPT trims the trailing space, so the device rendered "collegeare on
      NEXUS…". The sentence now lives in one formatted resource, and the test
      scrolls before asserting; `:feature:tribe` 7/7 green
- [x] Signed release rebuilt from the final tree: `:app:assembleRelease` green,
      7,652,845 bytes (7.65 MB / 7.30 MiB), `apksigner verify --print-certs`
      exit 0 with SHA-256 `02a7c369…6ceab9`, no `sk-or-*` material in dex or
      `resources.arsc`, baseline profile still packaged as
      `assets/dexopt/baseline.prof` (+`.profm`)
- [x] Final gates on that same tree: `test` **322/51, 0 failures**, detekt exit
      0 at `DETEKT_MAX_ISSUES=0`, `:app:lintDebug` exit 0 (62 baselined
      warnings, no errors), `runAudit` → **READY**, debug APK 31.3 MB
