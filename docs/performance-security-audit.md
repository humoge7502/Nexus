# Performance & Security Audit Checklist

> Status column added 2026-10-25 after a device + emulator verification pass.
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
| **Migration testing** | **DONE** | `MigrationTestHelper` runs 1→9 with `runMigrationsAndValidate`, plus per-step data-preservation assertions and DAO contracts — **11/11 on AVD `NexusApi35`**. This item was unchecked for the whole first pass; executing it immediately exposed six missing `NOT NULL` columns in the test fixtures. |

## Network & Firestore

| Item | Status | Evidence |
|---|---|---|
| Firestore read limits | **Verified for the feed** | `getOpportunityFeed` clamps `limit=999, offset=-5` to a bounded page, asserted on the emulator (`test:emulator`). |
| API keys never hard-coded / never in VCS | **Verified** | Secrets are read from gitignored `local.properties`; `google-services.json` is gitignored and CI uses the committed template. Gitleaks runs over full history in CI. |
| **Keys restricted by package name / SHA in console** | **NOT DONE — launch requirement** | The Firebase client API key is present in `resources.arsc` as designed. Restrict it to `com.nexus.app` + the signing SHA-256. Current release signer: `32004595dd7e9c5386065cec4a4c0c012ac1f94ac3f94c429431ec7e2f8fa25c` (local keystore only — regenerate before Play App Signing). |
| **Upstream AI key cannot ship** | **Fixed 2026-10-25** | `core:network` used to resolve `OPENROUTER_API_KEY` for *every* build type while the CI release job injected the real secret — the key was compiled into the distributable APK. Release now hard-codes the field to `""`, CI stopped supplying it, and a CI step fails if `sk-or-*` appears in the artifact. Proven by building release with a sentinel key in `local.properties`: debug BuildConfig receives it, release stays empty and the APK scan finds no key material. |

## Build & Obfuscation

| Item | Status | Evidence |
|---|---|---|
| R8 / shrinking | **Verified** | `release { isMinifyEnabled = true; isShrinkResources = true }`. Release APK is **6.69 MB** vs **28.7 MB** debug. |
| Proguard rule coverage | **Static** | Rules cover kotlinx.serialization, Retrofit, Room, Balloon, Lottie. Not yet proven by a *runtime* pass on a minified build — the release APK has only been launched implicitly via the non-minified `benchmark` variant. |
| **Baseline profiles** | **DONE** | `:app:generateBaselineProfile` drives the app on-device through `:macrobenchmark`; 26,737 rules committed at `app/src/release/generated/baselineProfiles/baseline-prof.txt` and packaged as `assets/dexopt/baseline.prof` + `baseline.profm` inside the signed release APK. |
| Startup verified with Macrobenchmark | **NOT DONE** | The profile ships but no `StartupTimingMetric` benchmark exists — cold start is still unmeasured, so there is nothing to regress-test against. |
| Signature verification | **Verified** | `apksigner verify --print-certs` exits 0 on the local release build. |

## Transport Security

| Item | Status | Evidence |
|---|---|---|
| Certificate pinning | **NOT DONE** | No `CertificatePinner` in `core/network`. Tooling exists but is unwired: `scripts/extract_cert_pins.sh`, `scripts/verify_cert_pins.sh`. Pinning a host whose leaf rotates (Render, OpenRouter) needs a documented expiry/update strategy before it is safe to enable. |
| Firebase App Check | **NOT DONE** | Not enforced on Firestore, Storage or Functions. Play Integrity enrolment needs a real Firebase project. |

## Coroutine Safety

| Item | Status | Evidence |
|---|---|---|
| No `runBlocking` in production | **Verified** | Grep-clean across production Kotlin (I-03). |
| Scope cancellation | **Static** | `NexusFCMService` cancels its injected `CoroutineScope` in `onDestroy`. |
| `Dispatchers.IO` for network/disk | **Static** | Repositories and workers dispatch to IO/Default. |

## Highest-value next steps, in order

1. Restrict the Firebase client API key in Cloud Console (S, closes the only
   secret that legitimately ships).
2. Enable App Check with Play Integrity (M, stops scripted proxy abuse).
3. Add a `BaselineProfileStartupBenchmark` with `StartupTimingMetric` so the
   shipped profile is actually measured and protected from regression (M).
4. Run the existing instrumentation suite on CI (M) — it is green locally and
   invisible to CI today.
5. Install LeakCanary in debug and take the first real leak snapshot (S).
6. Compose UI tests for the five NEXUS 2.0 screens, then a contrast /
   TalkBack / reduced-motion audit (L).
