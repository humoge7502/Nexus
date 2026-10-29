# Performance & Security Audit Checklist

> Status column added 2026-10-25 after a device + emulator verification pass;
> refreshed 2026-10-26 for schema v10 and the final UI/data-integrity pass.
> "Verified" means executed against real output; "static" means confirmed by
> reading code with no runtime proof. Nothing is marked done on assumption.

## Memory & Leak Detection

| Item | Status | Evidence |
|---|---|---|
| LeakCanary | **NOT DONE** | No `leakcanary` dependency anywhere (`gradle/libs.versions.toml` has no entry); nothing is installed in `NexusApplication`. |
| Timber release hygiene | **Verified** | `NexusApplication.onCreate` plants `Timber.DebugTree` only under `BuildConfig.DEBUG` ([NexusApplication.kt:32](../app/src/main/kotlin/com/nexus/app/NexusApplication.kt#L32)). Release plants no tree, so all `Timber.*` calls are no-ops and nothing is logged. Trade-off: release builds also lose their own diagnostics until Crashlytics is enabled. |
| WorkManager log level | **Static** | `workManagerConfiguration` uses `Log.DEBUG` for debug, `Log.WARN` otherwise. |
| ViewModel coroutine scope | **Static** | `viewModelScope` used across the ViewModels reviewed; no `GlobalScope` in production sources (only referenced by `audit-rules` and scripts/docs). |

## Compose Stability

| Item | Status | Evidence |
|---|---|---|
| `@Stable` / `@Immutable` on state classes | **Partial** | Applied in `core:domain` models; not systematically audited. Compose compiler stability metrics have never been generated — no `-P compose:metrics=true` output is checked in. |
| `LazyColumn` keys | **Partial** | Keyed in the opportunity and home lists; not verified module-by-module across all 26 feature modules. |
| `LazyColumn` `contentType` | **NOT DONE** | No `contentType(` call in feature sources. |
| Compose compiler metrics review | **NOT DONE** | Requires a build flag plus a manual review pass. |

## Database (Room)

| Item | Status | Evidence |
|---|---|---|
| Query indices | **Verified for the hot path** | `MIGRATION_8_9` adds `index_internships_posted_at`, and `migrate8to9PreservesInternships` asserts its presence on a real device. Full `EXPLAIN QUERY PLAN` review across all DAOs still open. |
| Transaction batching | **Static** | Bulk paths use `@Transaction` + `suspend`. |
| **Migration testing** | **DONE** | `MigrationTestHelper` runs 1→10 with `runMigrationsAndValidate`, plus per-step data-preservation assertions and DAO contracts — **12/12 on AVD `NexusApi35`** after adding v9→v10 (`certification_progress`) coverage. This item was unchecked for the whole first pass; executing it immediately exposed six missing `NOT NULL` columns in the test fixtures. |

## Network & Firestore

| Item | Status | Evidence |
|---|---|---|
| Firestore read limits | **Verified for the feed** | `getOpportunityFeed` clamps `limit=999, offset=-5` to a bounded page, asserted on the emulator (`test:emulator`). |
| API keys never hard-coded / never in VCS | **Verified** | Secrets are read from gitignored `local.properties`; `google-services.json` is gitignored and CI uses the committed template. Gitleaks runs over full history in CI. |
| **Keys restricted by package name / SHA in console** | **NOT DONE — launch requirement** | The Firebase client API key is present in `resources.arsc` as designed. Restrict it to `com.nexus.app` + the signing SHA-256. Current release signer: `02a7c3695f1f7745136b6995599ab4488e3b884c4cb8c15dfc4b7183686ceab9` (local keystore only — regenerate before Play App Signing). |
| **Upstream AI key cannot ship** | **Fixed 2026-10-25** | `core:network` used to resolve `OPENROUTER_API_KEY` for *every* build type while the CI release job injected the real secret — the key was compiled into the distributable APK. Release now hard-codes the field to `""`, CI stopped supplying it, and a CI step fails if `sk-or-*` appears in the artifact. Proven by building release with a sentinel key in `local.properties`: debug BuildConfig receives it, release stays empty and the APK scan finds no key material. |

## Build & Obfuscation

| Item | Status | Evidence |
|---|---|---|
| R8 / shrinking | **Verified** | `release { isMinifyEnabled = true; isShrinkResources = true }`. Release APK is **7.1 MB** vs **28.7 MB** debug. |
| Proguard rule coverage | **Verified at runtime** | Rules cover kotlinx.serialization, Retrofit, Room, Balloon, Lottie. The signed minified APK was installed on `NexusApi35` and driven through auth → onboarding → all five tabs → internship detail with an empty crash buffer. That pass caught a real R8-only bug: tab selection compared `::class.qualifiedName` against a fixed route string, which obfuscation broke, hiding the bottom bar in release only. Fixed with `NavDestination.hasRoute(KClass)` + index-based bar selection, then re-verified on a rebuilt APK. |
| **Baseline profiles** | **DONE** | `:app:generateBaselineProfile` drives the app on-device through `:macrobenchmark`; 26,737 rules committed at `app/src/release/generated/baselineProfiles/baseline-prof.txt` and packaged as `assets/dexopt/baseline.prof` + `baseline.profm` inside the signed release APK. |
| **Startup benchmark** | **DONE (emulator baseline)** | `macrobenchmark/.../StartupBenchmark.kt` — `StartupTimingMetric`, `StartupMode.COLD`, 5 iterations + 1 warmup, `CompilationMode.Partial` with the packaged profile. Run on `NexusApi35` via `:macrobenchmark:connectedBenchmarkReleaseAndroidTest` with `-Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR`. **TTID median 1,345.6 ms** (min 542.5, max 1,767.2 in the combined 3-test run) with the profile packaged; signed release measured with `am start -W` at a ≈1.25 s median. Emulator data is not device-representative — the harness, not the number, is the deliverable. |
| **Frame-timing benchmark** | **DONE (emulator baseline)** | `macrobenchmark/.../FrameTimingBenchmark.kt` — `FrameTimingMetric` over five Explore-feed swipes, same compilation mode; 5 iterations green. Emulator result under swiftshader: `frameDurationCpuMs` P50 97.8 / P90 695.3 / P95 816.5 / P99 1,218.5 ms, `frameOverrunMs` P95 1,082.2 ms. Emulator frame data is noisy and does not represent a physical device. |
| Signature verification | **Verified** | `apksigner verify --print-certs` exits 0 on the local release build. |

## Transport Security

| Item | Status | Evidence |
|---|---|---|
| Certificate pinning | **NOT DONE** | No `CertificatePinner` in `core/network`. Tooling exists but is unwired: `scripts/extract_cert_pins.sh`, `scripts/verify_cert_pins.sh`. Pinning a host whose leaf rotates (Render, OpenRouter) needs a documented expiry/update strategy before it is safe to enable. |
| Firebase App Check | **NOT DONE** | Not enforced on Firestore, Storage or Functions. Play Integrity enrolment needs a real Firebase project. |

## Coroutine Safety

| Item | Status | Evidence |
|---|---|---|
| No `runBlocking` in production | **Verified** | Grep-clean across Android production Kotlin (I-03). The only `runBlocking` is the `audit-cli` command-line entry point, which is tooling, not app code. |
| Scope cancellation | **Static** | `NexusFCMService` cancels its injected `CoroutineScope` in `onDestroy`. |
| `Dispatchers.IO` for network/disk | **Static** | Repositories and workers dispatch to IO/Default. |

## Highest-value next steps, in order

1. Restrict the Firebase client API key in Cloud Console (S, closes the only
   secret that legitimately ships).
2. Enable App Check with Play Integrity (M, stops scripted proxy abuse).
3. Run `StartupBenchmark` + `FrameTimingBenchmark` on a physical device (or
   FTL) and set a regression threshold — the harness exists, but emulator
   numbers must not become the budget (M).
4. ~~Run the existing instrumentation suite on CI~~ — **DONE**: the
   `instrumented` job runs the Room suite plus five Compose UI suites on an
   API 35 emulator with KVM enabled.
5. Install LeakCanary in debug and take the first real leak snapshot (S).
6. ~~Compose UI tests for the five NEXUS 2.0 screens~~ — **DONE**: 22/22 on
   `NexusApi35`; the follow-up contrast / TalkBack / reduced-motion audit
   remains open (L).
