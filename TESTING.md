# Testing Strategy

NEXUS uses a five-layer test pyramid. Every layer is runnable locally and the
fast layers gate CI on every pull request.

## 1. Unit tests (JVM, fastest)

```bash
./gradlew testDebugUnitTest
```

- **322 unique tests / 51 suites** — 274 Android debug tests plus 48 pure-JVM
  audit tests (registry, secret/HTTP/allowBackup detection, suppression, and all
  three report formats) — all green.
- Covers ViewModels (Placement Forecast, Onboarding, Tribe, Paywall),
  calculators (NEXUS Score, readiness), the ML feature mapper, streak logic,
  billing product mapping, and the audit engine itself.
- Rule: new business logic ships with a unit test; bug fixes ship with a
  regression test that fails before the fix.

## 2. Static analysis & production-readiness audit

```bash
./scripts/detekt.sh          # detekt (blocking; max-issues 0)
./gradlew runAudit           # 8-phase audit -> build/audit/{md,json,html}
```

Or run the whole gate at once: `./scripts/verify.sh`.

The audit is a first-class test: it scans the real tree for mock data, missing
timeouts, layering violations, oversized files, `runBlocking`, secrets, TLS
bypass, plaintext HTTP, signing/R8 gaps, and startup hazards. CI fails the
build on `NOT_READY`.

## 3. Integration tests

```bash
cd backend/functions && npm test          # callable + rules logic (21 tests)
npm run test:rules                        # Firestore rules (emulator, 13 tests)
npm run test:emulator                     # callable functions (emulator)
```

The rules suite covers owner-only `users` docs, server-guarded XP/Pro fields,
verified/anonymized interview outcomes, server-only AI logs and quota counters,
and the opt-in `publicProfiles` leaderboard (world-readable, owner-writable,
numeric ranking fields enforced).

## 4. Instrumented tests (Android, emulator/device)

```bash
./gradlew connectedDebugAndroidTest
```

- Room migrations 1→10 validated against the exported schema.
- Compose UI suites: Career Mirror (5), Cohort Intelligence (3),
  Interview Log (3), Placement Forecast (5), Tribe (7), Community (2) —
  **25/25**.
- `TokenCipher` Keystore suite (4/4): roundtrip, unique IV, legacy passthrough,
  tamper fails closed.
- App boot smoke test (`:app`, 1/1): launches the real `MainActivity` through
  the production Hilt/Firebase/Room graph and asserts it reaches `RESUMED` —
  the one suite that does not use fakes.
- **42/42** instrumented tests total, run per-module by CI.

Screens expose stateless `*Content` composables so UI tests run without a Hilt
graph.

## 5. Performance & release verification

```bash
./gradlew :app:generateBaselineProfile
./gradlew :macrobenchmark:connectedBenchmarkAndroidTest
./gradlew assembleRelease
```

- Macrobenchmarks: `StartupTimingMetric` (TTID) and `FrameTimingMetric` (scroll).
- Release job asserts: R8 + resource shrinking, signed APK verifies, no
  `sk-or-*` key material, APK under the 9 MB budget.

## ML reproducibility & parity

```bash
cd ml/placement_readiness
python generate_dataset.py     # must be byte-for-byte reproducible
python train.py                # trains + exports Kotlin model
```

CI asserts the dataset manifest (`rows`, `positive_rate`, `sha256`) and a
holdout ROC-AUC > 0.80, then fails if `datasets/` changed. The exported Kotlin
model is golden-vector tested against the Python model.

## Manual UAT checklist (release candidate)

1. Fresh install → auth → onboarding → Home.
2. Home shows real readiness score, streak, and next action.
3. Add an application; confirm it appears and survives restart.
4. Solve a DSA problem; streak increments exactly once per day.
5. Open a certification, mark a module done, restart → progress persists.
6. AI chat streams tokens incrementally; quota message appears after the free
   limit, and an expired session prompts sign-in rather than hanging.
7. Deep Research returns cited sources and follow-up questions.
8. Growth → Arena shows real XP/streak; “Join leaderboard” publishes an opt-in
   public profile, ranks the user, and “Leave” removes it.
9. Paywall: purchase, restore, and cancel paths on a test account.
10. Offline launch: cached content renders, errors are graceful.
11. Release (minified) build: bottom bar visible, navigation works, no crash.
