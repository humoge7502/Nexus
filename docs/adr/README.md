# Architecture Decision Records

Short, immutable records of the decisions that shape NEXUS. Format: context →
decision → consequences. Superseding a decision means adding a new ADR, not
editing the old one.

| ADR | Decision | Status |
|---|---|---|
| [001](#adr-001-modular-multi-module-architecture) | Modular multi-module architecture with `:core` / `:feature` layering | Accepted |
| [002](#adr-002-jetpack-compose--material-3) | Jetpack Compose + Material 3 for all UI | Accepted |
| [003](#adr-003-hilt-for-dependency-injection) | Hilt for dependency injection | Accepted |
| [004](#adr-004-room-as-the-offline-source-of-truth) | Room as the offline source of truth, schema-versioned | Accepted |
| [005](#adr-005-ai-calls-go-through-a-backend-proxy) | AI calls go through a backend proxy; keys never ship | Accepted |
| [006](#adr-006-on-device-placement-model) | On-device logistic placement model instead of per-call LLM inference | Accepted |
| [007](#adr-007-server-authoritative-entitlements) | Server-authoritative Pro entitlements | Accepted |
| [008](#adr-008-dynamic-color-with-brand-fallback) | Material You dynamic color with a brand fallback palette | Accepted |
| [009](#adr-009-first-class-production-audit) | Production-readiness audit as a first-class, CI-gating tool | Accepted |
| [010](#adr-010-reproducible-synthetic-ml-dataset) | Reproducible, documented synthetic ML dataset with Python↔Kotlin parity | Accepted |

---

## ADR-001: Modular multi-module architecture

**Context.** A single-module Android app becomes a merge-conflict and build-time
bottleneck as the feature surface grows.

**Decision.** Split into `:app`, `:core:*`, and `:feature:*`. Features depend only
on core modules; the audit enforces the rule.

**Consequences.** Faster incremental builds and clear ownership. Cost: more
Gradle configuration and the need for convention plugins.

## ADR-002: Jetpack Compose + Material 3

**Context.** The UI must support dynamic color, motion, and a distinctive design
language across many screens.

**Decision.** Compose-only UI with Material 3 theming; no XML layouts for new
screens.

**Consequences.** Concise, testable UI and a single design system. Cost: Compose
tooling and recomposition discipline are required.

## ADR-003: Hilt for dependency injection

**Decision.** Hilt (KSP) provides graph construction and scoping.

**Consequences.** Testable ViewModels and clear lifetimes. Cost: annotation
processing and a learning curve for contributors.

## ADR-004: Room as the offline source of truth

**Decision.** Persist user data in Room, with exported schemas and explicit
migrations (currently v10).

**Consequences.** Offline-first UX and migration tests. Cost: every schema change
needs a migration and a test.

## ADR-005: AI calls go through a backend proxy

**Context.** Shipping an upstream AI key in an APK is unrecoverable once leaked.

**Decision.** Release builds send AI traffic to a Firebase callable proxy that
enforces per-user quotas and holds the upstream key. The key is empty in release
`BuildConfig`; CI asserts the APK contains no `sk-or-*` material.

**Consequences.** Revocable access and cost control. Cost: a network hop and a
backend dependency.

## ADR-006: On-device placement model

**Context.** Per-user placement prediction via an LLM would be slow, costly, and
non-deterministic.

**Decision.** Train a 14-feature logistic model offline, export it to Kotlin, and
run inference on-device; verify parity with golden vectors.

**Consequences.** Zero inference cost, instant and explainable predictions. Cost:
retraining and export discipline.

## ADR-007: Server-authoritative entitlements

**Decision.** Pro state is written and validated server-side (Firestore rules +
callable), never trusted from the client.

**Consequences.** Refunds and abuse are controllable. Cost: entitlement changes
require a backend deploy.

## ADR-008: Dynamic color with brand fallback

**Decision.** Use Material You dynamic color on Android 12+ by default, with an
explicit brand palette (and AMOLED opt-in) when the user picks an accent or the
OS is older.

**Consequences.** Personal, native-feeling UI without losing brand identity.

## ADR-009: First-class production audit

**Context.** Manual release checklists drift and miss regressions.

**Decision.** Ship an 8-phase static audit (`:audit-cli`) that emits
Markdown/JSON/HTML, exits non-zero on `NOT_READY`, and runs in CI.

**Consequences.** Repeatable, reviewable release gates. Cost: rules need
maintenance as the codebase evolves.

## ADR-010: Reproducible synthetic ML dataset

**Context.** Real placement data is sensitive and unavailable at bootstrap.

**Decision.** Generate a documented synthetic cohort with a fixed seed and
manifest (`rows`, `positive_rate`, `sha256`); CI verifies byte-for-byte
reproducibility and a holdout AUC floor. Replace with consented, anonymized real
data when available.

**Consequences.** Honest, reproducible baselines. Cost: synthetic distributions
may not match the real population — retraining on real data is a tracked
follow-up.
