# Contributing to NEXUS

## Setup

1. JDK 17, Android SDK 36, and a device/emulator (API 35 recommended).
2. Copy `local.properties.template` → `local.properties` and fill in your own
   values. **Never commit real keys.**
3. `cp app/google-services.json.template app/google-services.json` for local
   builds.
4. `./gradlew :app:assembleDebug`

## Module map

| Layer | Modules |
|---|---|
| App shell | `:app` |
| Core | `:core:common`, `:core:database`, `:core:datastore`, `:core:network`, `:core:domain`, `:core:designsystem`, `:core:ui` |
| Features | `:feature:home`, `:feature:explore`, `:feature:ai`, `:feature:growth`, `:feature:profile`, `:feature:careermirror`, `:feature:placementforecast`, `:feature:cohortintelligence`, `:feature:interviewlog`, `:feature:tribe`, `:feature:paywall`, `:feature:achievements`, … |
| Tooling | `:audit-core`, `:audit-scanner`, `:audit-rules`, `:audit-reporting`, `:phase-analyzers`, `:audit-cli` |

**Layering rule:** features may depend on `:core` only. A feature→feature
dependency is an audit failure (`ARCH-001`). Put shared behaviour in a core
module.

## Before you open a PR

```bash
./scripts/verify.sh             # tests + detekt + audit + debug build
```

Or run the steps individually:

```bash
./gradlew testDebugUnitTest      # unit tests
./scripts/detekt.sh              # static analysis (blocking)
./gradlew runAudit               # 8-phase production audit
./gradlew :app:assembleDebug     # build
```

All four must pass. For UI changes, attach a screenshot or short recording.

## Coding standards

- Kotlin official style, 4-space indent, 140-column limit (`.editorconfig`).
- No wildcard imports in new code (`WildcardImport` is active).
- Prefer `suspend` over `runBlocking`; never block the main thread.
- No hardcoded user-facing strings in composables — use string resources.
- Every `Image` needs a `contentDescription` (or explicit `null` if decorative).
- New business logic gets a unit test; bug fixes get a regression test.
- Do not add a dependency without checking `gradle/libs.versions.toml` first and
  justifying the size/perf cost in the PR.

## Commit & PR conventions

- Conventional commits: `feat:`, `fix:`, `perf:`, `refactor:`, `test:`, `docs:`,
  `chore:`, `build:`, `ci:`.
- One logical change per PR; keep diffs reviewable.
- PR description must state: problem, approach, risk, verification (commands run).
- Security-sensitive changes (auth, crypto, billing, Firestore rules) require a
  second reviewer.

## Architecture decisions

Significant decisions are recorded as ADRs in [`docs/adr/`](docs/adr/README.md).
Add a new ADR for any change that alters module boundaries, persistence,
networking, or the AI cost model.
