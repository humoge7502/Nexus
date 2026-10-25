# NEXUS 2.0 Feature Implementation Status

> Last verified: 2026-10-25, on JDK 17 + Android SDK 35 + AVD `NexusApi35` and the
> Firebase emulator. See `docs/NEXUS_COMPREHENSIVE_ANALYSIS.md` for the full
> verification report and remaining backlog.

## Intelligence layer — COMPLETE

- [x] Room entities, DAOs and migrations through **schema v9**
- [x] Domain models and repository interfaces/implementations
- [x] Hilt bindings for score snapshots, cohort, forecasts and mirrors
- [x] `NexusAnalytics` events for mirror/forecast/interview flows
- [x] `PlacementPredictionEngine` (rule + ML factors)
- [x] `PlacementReadinessModel` (trained, exported Kotlin, golden-vector tested)
- [x] `PlacementReadinessFeatures` (raw app signals → model features)
- [x] `InterviewIntelligenceEngine` (company round maps + weak-spot flags)
- [x] `CareerMirrorGenerator`, `ConfidenceCalibrationEngine`
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
- [x] Navigation destinations wired from `MainActivity`

## Verified build & tests

- [x] `:app:assembleDebug` exits 0 (28.7 MB)
- [x] `:app:assembleRelease` — R8 + resource shrinking, signed, 6.69 MB, `apksigner verify` exit 0
- [x] `testDebugUnitTest` green — **128 tests / 24 suites, 0 failures, 0 skipped**
- [x] Backend `npm test` green (10 tests)
- [x] ML dataset reproducible byte-for-byte; Python↔Kotlin parity green
- [x] `core:database` androidTest sources compile (migrations 1→9)
- [x] `connectedDebugAndroidTest` — **11/11 executed on AVD `NexusApi35` (API 35)**:
      `migrateAll` 1→9 validated against the Room schema, per-step data
      preservation, `InternshipDaoTest`, `Nexus2DaoTest`
- [x] Firebase emulator suites — callables **5/5** (`npm run test:emulator`),
      Firestore rules **11/11** (`npm run test:rules`); both wired into CI
- [x] Baseline profile generated on-device (`:app:generateBaselineProfile`) and
      packaged in the release APK as `assets/dexopt/baseline.prof` + `.profm`
- [x] Release APK scanned for `sk-or-*` key material — clean

Two real defects were found only by running on a device, and are fixed: the
`internships` seed in `MigrationTest` omitted six `NOT NULL` columns, and
`feature:notifications` reused `core:notifications`' manifest namespace.

## Remaining

Console/account-gated — cannot be closed from this repo:

- Firebase project login and `firebase deploy` (nothing has ever been deployed)
- App Check with Play Integrity, so the AI proxy can't be scripted
- Crashlytics enablement + consent copy
- Restricting the Firebase client API key by package name + SHA-256 in Cloud
  Console (this key legitimately ships inside `resources.arsc`)

Engineering backlog:

- Startup/frame-time benchmarks against the shipped profile — the profile is in
  the APK but cold start is still **unmeasured**
- Instrumentation suite on CI (locally green, CI has no emulator)
- Compose UI tests for the five NEXUS 2.0 screens; unit tests for `feature:tribe`,
  `feature:paywall`, `feature:community` (only 7 of 37 modules are tested)
- Detekt blocking job; regenerate the 28 stale Lint baselines
- Paging 3 and SSE streaming
- i18n and accessibility audit
- Real (consented, anonymized) placement cohort for retraining
