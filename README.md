# NEXUS — Your Career. Engineered.

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.1.21-purple?logo=kotlin" />
  <img src="https://img.shields.io/badge/Android-16%20(API%2036)-green?logo=android" />
  <img src="https://img.shields.io/badge/Compose-BOM%202025.06-blue?logo=jetpackcompose" />
  <img src="https://img.shields.io/badge/Material-You%20%2B%20AMOLED-6750A4" />
  <img src="https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean-important" />
  <img src="https://img.shields.io/badge/Dagger-Hilt%202.56-orange" />
  <img src="https://img.shields.io/badge/minSdk-26-brightgreen" />
</p>

**NEXUS** is a production-grade Android career platform for CSE students. It
brings internship discovery, certification tracking, DSA practice, applications,
interview intelligence and an on-device placement-readiness model into one
premium, Material You experience — online or offline.

The project is **closed-beta ready**: signed release builds pass, 322 JVM tests
and 40 on-device instrumentation tests are green, the Firebase backend runs
against the emulator suite, and startup plus scroll macrobenchmarks run against
a real ART baseline profile shipped in the APK.

## Features

### Career intelligence
- **Placement readiness** — a trained, on-device model converts real app signals
  into a 0–100 score, readiness level, ordered action items and an estimated
  timeline.
- **Placement Forecast** — drivers, confidence, and a what-if simulator for
  skills, projects, DSA and applications.
- **Career Mirror** — strengths, gaps and a concrete next move.
- **Cohort Intelligence** — anonymized benchmark context.
- **Interview Intelligence** — company-specific round maps, weak-spot flags and
  an interview log.

### AI
- **NEXUS AI coach** — quota-controlled Firebase proxy; replies stream over an
  authenticated SSE bridge (`streamAI`) so tokens render as they arrive. The
  upstream provider key never ships in the app.
- **Deep Research** — agent-search-pattern research flow with citations.
- **Resume Analyzer** — structured feedback on resume content.

### Opportunities & learning
- Internship and hackathon discovery with search, filters, saved items and
  detail pages.
- Applications tracker with statuses from saved through offer.
- Certifications with per-module progress (Room schema v10, persisted).
- DSA tracker, goals, portfolio and an opt-in public leaderboard
  (`publicProfiles`: display name, college, XP, streak — nothing private).

### Community & motivation
- Campus **Tribe** create/join/leave, community feed, XP and levels,
  achievements with seeded badge catalog, and a single-source streak.

### Design
- **Material You** dynamic color by default, optional true-black **AMOLED**.
- Distinctive heading/body type pairing; app-wide navigation motion.
- Premium surfaces: press-scale feedback, sheen sweep, hairline strokes, arc
  glow, glass cards, animated backgrounds.

## Architecture

```text
Nexus/
├── app/                    # Application shell, Hilt entry points, navigation
├── core/
│   ├── designsystem/       # Tokens, components, motion, premium surfaces
│   ├── domain/             # Models, use cases, readiness model + calculators
│   ├── data/               # Repositories, data sources, DataStore/streak
│   ├── network/            # Retrofit/OkHttp, AI router, interceptors
│   ├── database/           # Room entities, DAOs, migrations (schema v10)
│   ├── datastore/          # User preferences
│   ├── analytics/          # Event tracking
│   ├── notifications/      # FCM handling
│   ├── common/             # Shared utilities (rate limiting, results)
│   ├── sync/               # WorkManager workers
│   └── ui/                 # Shared UI utilities
├── feature/                # 24 feature modules (home, explore, ai, growth,
│                           # certifications, achievements, tribe, forecast,
│                           # interviewlog, portfolio, paywall, …)
├── macrobenchmark/         # Baseline-profile generator
├── audit-core/ … audit-cli # 6-module production-readiness audit toolkit
├── backend/functions/      # Firebase Cloud Functions (AI proxy, feeds, cohort)
├── ml/                     # Dataset generator, training, exported Kotlin model
└── scripts/                # Scraper, LeetCode sync, verification scripts
```

**Pattern:** MVVM + Clean Architecture, StateFlow-driven UI, type-safe Compose
Navigation, Hilt DI, unidirectional data flow.

## Tech stack

| Category | Choice |
|---|---|
| Language | Kotlin 2.1.21 |
| Build | AGP 8.12.3, Gradle 8.14.3, KSP |
| SDK | compile/target 36 (Android 16), min 26 |
| UI | Jetpack Compose, Material 3, Material You dynamic color |
| Navigation | Navigation Compose 2.9.0 (type-safe routes) |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt 2.56.2 |
| Async | Coroutines 1.10.2, Flow, WorkManager 2.10.0 |
| Local DB | Room 2.7.2 (schema v10, migration-tested) |
| Preferences | DataStore 1.1.1 |
| Networking | Retrofit 2.11, OkHttp 4.12 |
| Images | Coil 2.7 |
| Firebase | BOM 33.16 (Auth, Firestore, Functions, Messaging) |
| Paging / Billing | Paging 3.3.4, Play Billing 7.1.1 |
| Performance | Baseline Profile 1.3.3, macrobenchmark |
| Backend | TypeScript Cloud Functions, Firebase emulator suite |

## Design system

- **Color:** Material You dynamic palette; optional AMOLED true-black base.
- **Type:** paired display/body faces for hierarchy and personality.
- **Motion:** spring-based navigation transitions, press-scale, sheen sweep.
- **Surfaces:** hairline strokes, arc glow, glass cards, mesh gradients.
- **Components:** 15+ production composables (`NexusCard`, buttons, chips,
  bottom bar, stats, gauges, illustrations).

Full token reference, motion specs, accessibility rules and the
"not vibe-coded" review checklist: [`docs/DESIGN_SYSTEM.md`](docs/DESIGN_SYSTEM.md).

## Getting started

### Prerequisites
- JDK 17
- Android Studio (current stable)
- Android SDK 36 and an API 35/36 emulator or device

### Setup

```bash
git clone <your-repo-url> nexus
cd nexus
cp local.properties.template local.properties   # fill in optional values
```

1. Open the project in Android Studio and sync Gradle.
2. Place your Firebase `google-services.json` in `app/` for a real project.
3. Run on an emulator or device.

`local.properties`, `app/google-services.json` and `*.jks` are gitignored.
`OPENROUTER_API_KEY` is only compiled into debug builds; release builds never
contain it. `AI_PROXY_URL` is deploy-time configuration and may be blank until
the backend is deployed.

### Build

```bash
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease   # R8 + resource shrinking + signing
```

## Verification

| Check | Command | Result |
|---|---|---|
| JVM tests | `./gradlew test` | **322 unique tests / 51 suites, 0 failures** |
| Instrumentation | `./gradlew :core:database:connectedDebugAndroidTest` | **12/12 on AVD `NexusApi35`** |
| Token cipher | `./gradlew :core:datastore:connectedDebugAndroidTest` | **4/4 on AVD `NexusApi35`** (Keystore AES-GCM roundtrip, fresh IV per call, legacy plaintext, tamper fail-closed) |
| Compose UI tests | six `:feature:*:connectedDebugAndroidTest` suites | **25/25 on AVD `NexusApi35`** (Career Mirror 5, Cohort 3, Interview Log 3, Forecast 5, Tribe 7, Community 2) |
| App boot smoke test | `./gradlew :app:connectedDebugAndroidTest` | **1/1 on AVD `NexusApi35`** — real Hilt/Firebase/Room graph, cold start to `RESUMED` |
| Macrobenchmark | `./gradlew :macrobenchmark:connectedBenchmarkReleaseAndroidTest` | startup + scroll harnesses green; emulator TTID median **1.35 s**, scroll P95 **816 ms** CPU |
| Backend tests | `cd backend/functions && npm test` | **21/21** (validation + LeetCode stats + stream bridge), `tsc` clean |
| Firebase emulator | `npm run test:emulator` / `test:rules` | **10/10 emulator checks** (callables + `streamAI` auth/validation/fail-closed), **13/13 rules** (owner-only profiles, server-guarded XP/Pro, opt-in leaderboard schema) |
| Release build | `./gradlew :app:assembleRelease` | **signed, minified, verified with `apksigner`** |
| Baseline profile | `./gradlew :app:generateBaselineProfile` | embedded as `assets/dexopt/baseline.prof` |
| Secret scan | zip/string scan of the release APK | no upstream AI key material |
| Production audit | `./gradlew runAudit` | **READY, 10.0/10** — all 8 phases 10.0, no CRITICAL/HIGH/MEDIUM/LOW findings; UI copy externalized to string resources |
| Static analysis | `./scripts/detekt.sh` | **blocking, exit 0** against a reviewed baseline (`config/detekt/baseline.xml`) |

Full reports: [`docs/NEXUS_COMPREHENSIVE_ANALYSIS.md`](docs/NEXUS_COMPREHENSIVE_ANALYSIS.md),
[`docs/NEXUS20_IMPLEMENTATION_STATUS.md`](docs/NEXUS20_IMPLEMENTATION_STATUS.md),
[`docs/performance-security-audit.md`](docs/performance-security-audit.md),
[`docs/BUSINESS_MODEL.md`](docs/BUSINESS_MODEL.md) and
[`docs/COMPETITIVE_LANDSCAPE.md`](docs/COMPETITIVE_LANDSCAPE.md).

Engineering guides: [`SECURITY.md`](SECURITY.md), [`TESTING.md`](TESTING.md),
[`CONTRIBUTING.md`](CONTRIBUTING.md), [`AUDIT_SYSTEM_README.md`](AUDIT_SYSTEM_README.md),
[architecture decisions](docs/adr/README.md) and the
[ML data dictionary](ml/placement_readiness/DATA_DICTIONARY.md).

## License

MIT License — see [LICENSE](LICENSE).
