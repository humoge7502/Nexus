# NEXUS / Ascent — Strategic Analysis & Implementation Report

**Date:** 2026-11-08 · **Scope:** full-project analysis across strategy, issues, enhancements, advanced features, additions, and testing · **Evidence:** every claim below was verified against the working tree this session (commit base `ff53ba5`, 8 commits ahead of origin, unpushed) or against the live PR #26 plan text. Historical claims from earlier documents are marked as such and were re-checked before inclusion.

---

## Executive summary

The product is **closed-beta ready** and materially further along than its own documentation: several items the draft PR #26 roadmap records as open have since **landed** (Paging 3 on the main feeds, trust-metadata/provenance bands on cards, doc-total sync, the weekly-plan section). The genuinely-open frontier is narrow and concrete:

1. **Accessibility sweep (TalkBack + 200% font)** — the one P0-adjacent item that had never been run. Executed this session; findings below.
2. **Console-gated security work** (App Check, Crashlytics enablement, API-key restrictions) — codeable parts are prepared; the rest is blocked on the Firebase/Play account owner.
3. ~~**The verification state machine**~~ — **LANDED same day** as `VerificationState`/`VerificationPolicy` in `core/domain` (§3.4): the single highest-leverage open product item, because provenance *is* the differentiation.
4. **Debt with measurable shape:** 28 lint baselines (not the 7 historically documented), 58 broad `catch (e: Exception)` sites, a stale detekt config with no gradle task attached.
5. **Three new motion primitives landed this session** from the web-canon sweep (commit microinteraction, path-draw frame reveal, readout state-morph), wired to real surfaces under the one-clock law.

Nothing in this report re-reports historical fixed defects as current bugs, per the reproduction-quality standard in PR #26 §2.2.

---

## 1. Project Analysis & Strategic Opportunities

### 1.1 What the project already has that competitors do not

| Asset | Evidence in tree | Why it is defensible |
|---|---|---|
| **Verified-opportunity provenance** | `verifiedAt` in Room entities + `OpportunityEditorialCard` VERIFIED signal tags, mono dates, source provenance rows; Explore offline band + source-check staleness bands (>48h Neutral, >7d Fault) | Aggregator apps show listings; this shows *why the listing is trustworthy*. Provenance is the trust moat PR #26 correctly names the core differentiator. |
| **On-device, deterministic readiness model** | `core/domain` calculators, Placement Forecast drivers + what-if simulator | Runs offline, explains itself, no server cost; competitors' "job match %" is a black box (the codebase deliberately renders "4 of 6 skills", never fake percentages). |
| **AI through an authenticated proxy** | Firebase callable + SSE bridge (`streamAI`), provider key never ships | Security posture competitors copy from tutorials doesn't have: quota control, no client-side keys. |
| **Instrument-voice design system** | 47+ components, `revealClock` single-timeline law, no-infinite-motion rule, no-fake-data law | The design system is now *documented law*, not taste — three new primitives joined it this session under the same constraints. |
| **Offline-first sync chain** | SyncWorker 6h → Room → immediate Firestore backup + NewMatchAlert chain (pushed `cd6c400`) | Fresh arrivals are announced within the same cycle — verified live against the real feed. |

### 1.2 Strategic opportunities, ranked by (differentiation × feasibility)

1. **Formalize the verification state machine (A1).** The staleness logic exists per-screen; lifting it into `core/domain` as `VerificationState` (UNVERIFIED → VERIFIED → STALE → EXPIRED, with REPORTED branch) makes provenance queryable, testable, and renderable everywhere consistently. Est. 2–4 days. *This is the top recommendation.*
2. **Adaptive weekly plan v2 (A3).** "Movable This Week" (Home section 01) currently takes the top 3 readiness action items. v2: schedule them against the user's real deadlines and DSA cadence, with completion feedback that recalibrates. Est. 1–2 weeks. Converts intelligence into habit.
3. **Career memory (A2).** Room-backed event timeline + retrieval with provenance for the AI coach — "why did Sagitta recommend this?" becomes answerable. Est. 2–4 weeks; sequence after A1/A3.
4. **Localization pilot (hi-IN first).** The audience is Indian CSE students; English-only copy limits reach. Est. 3–7 days for the first locale + string inventory. Medium effort, real market gain.
5. **Campus/TPO console (A8).** B2B distribution channel; big build (4–8 weeks), park until consumer loop is proven.
6. **Physical-device performance budget publication.** Emulator scroll P95 (816 ms, SwiftShader) is a signal, not an SLA. One real mid-tier device session produces the budgets PR #26 §7.3 proposes. User-side action.

### 1.3 Competitive position statement (what to say in the store listing)

"Every opportunity is verified, timestamped and re-checked. Every score explains itself. Every answer shows its sources." — all three sentences are already true in code; no competitor in the segment can say them honestly.

---

## 2. Issues & Required Fixes

Severity/priority follow PR #26's convention. **Statuses were re-verified this session** — several differ from the PR text.

| ID | Issue | Severity / Priority | Status (verified 2026-11-08) | Effort | Dependencies | Timeline |
|---|---|---|---|---|---|---|
| S-01 | App Check / Play Integrity not enforced for callable/proxy abuse | High / P1 | **OPEN** — dependency present, enforcement console-gated | 1–2 d eng + console setup | Firebase project owner | Before public beta |
| S-02 | Firebase API key package + SHA-256 restrictions | Medium / P1 | **OPEN** — console-gated | 2–4 h | Release cert fingerprints | Before public beta |
| S-03 | Crashlytics prepared but not enabled | High / P1 | **OPEN** — plugin/dependency present in `app/build.gradle.kts`; enablement console-gated | 0.5–1.5 d | Firebase owner, consent copy | Before public beta |
| S-04 | Scraper host/secret lifecycle verification | High / P1 | **OPEN** — needs deployment-inventory check against current docs | 0.5–1 d | Deployment owner | Immediate validation |
| Q-01 | Lint baselines across modules | Medium / P2 | **OPEN — worse than documented:** 28 `lint-baseline.xml` files (PR said 7) | 2–4 d | Lint reports | Next 2 sprints |
| Q-02 | Swallowed-exception cleanup partial | Medium / P2 | **OPEN — 58 `catch (e: Exception)` sites** in `core/data` + features (grep count this session) | 1–3 d | Error taxonomy | Next 2 sprints |
| Q-03 | Production TODO/FIXME residue | Low / P3 | **OPEN** — 25 raw `// TODO\|FIXME\|HACK` hits in prod Kotlin (doc claims 9; filters differ — recount with one filter, then triage) | 0.5–2 d | — | Ongoing |
| Q-04 | README/doc instrumentation-count inconsistencies | Medium / P1 | **LANDED** (`cd09816` syncs verified totals: JVM 428/64, device 46, backend 28) | done | — | done |
| P-01 | Physical-device startup/frame budgets unpublished | High / P1 | **OPEN — needs real device** (emulator numbers are SwiftShader, not SLA) | 1–2 d measure | Mid-tier device (user) | Before public launch |
| P-02 | Emulator scroll CPU P95 816 ms | Medium / P1 | **OPEN** — treat as profiling signal only | 1–2 d | Trace capture | Next perf pass |
| P-03 | Paging 3 coverage | Medium / P2 | **LARGELY LANDED** — `ExploreInternshipPagingSource`, `ExploreViewModel`, `InternshipPagingSource` verified in tree; confirm hackathons + saved lists next | 0–2 d residual | — | Next sprint |
| P-04 | AI first-token p95 < 3 s unmeasured on deployed key | Medium / P1 | **OPEN — Blaze-gated** | 1–2 d | Deployed backend | Before public beta |
| T-01 | Compose UI tests for older screens | Medium / P1 | **OPEN** — staged effort | 5–10 d | Fake repositories | Continuous |
| T-02 | TalkBack + 200% font sweep | Medium / P1 | **EXECUTED THIS SESSION** — static audit clean (charts carry semantics; container rows speak via child text); runtime pass below | 1–2 d | — | done → findings §3.7 |
| T-03 | Emulator-scale quota concurrency assertion | Medium / P1 | **OPEN** | 0.5–1.5 d | Firebase emulator | Before public beta |
| T-04 | Hilt component tests constrained by Firebase singletons | Low / P2 | **OPEN** | 2–4 d | Test bindings | Next 2 sprints |
| L-01 | Nexus vs Sagitta naming drift | Medium / P2 | **PARTIAL** — brand decision recorded (Sagitta; packages stay Nexus); README title + docs still mixed; sweep is a 1-day task | 1–2 d | none | Before store listing |
| L-02 | Localization | Medium / P2 | **OPEN** | 3–7 d first locale | String inventory | Pilot next |
| O-01 | Firebase console actions (App Check, Crashlytics, key restriction) | High / P1 | **BLOCKED — user-only** | — | Account owner | Release gate |

**Not re-reported:** scraper unauthenticated writes, blank titles, expired-token retry, migration fixtures, negative achievement padding — all historical fixed defects (PR #26 §2.2 standard).

**Dependencies chain:** S-01/S-02/S-03/O-01 all funnel through the Firebase/Play account owner (user) — none are code-blocked. Q-01/Q-02 share an error-taxonomy prerequisite. T-01 needs the fake-repository layer T-04 introduces.

---

## 3. Enhancements & Modifications

### 3.1 Landed this session (with implementation notes)

1. **Commit microinteraction** (`NexusButton` gains `settled`/`settledText`; wired to Skill Gap's real async): press → spinner → check, exits quicker than entrances (90 ms out / 220 ms in), haptic on settle, collapsed to zero under `LocalReducedMotion`. Trade-off honored: **never simulated** — `settled` must be driven by the real operation's result, not a timer. Roll-out path: auth, onboarding save, account deletion, roadmap/LinkedIn generators (all have honest `isLoading` + result states).
2. **Path-draw reveal** (`effect/PathInk.kt`: `inkedPath`, `inkedBrackets`, `plateFramePath`; `InstrumentPlate` gains `reveal`): the plate's hairline frame is *inked on* and the four registration brackets stamp in reading order (settled state pixel-identical to the standing plate: TL+BR lit). Wired to the Growth ScoreLedger — frame and signature score sweep now share **one clock** (`CareerProgressionReveal` accepts an external `clock`, preserving the single-timeline law). Draw cost: geometry cached against size, per-frame work bounded to the reveal window.
3. **Readout state-morph** (`DeadlineIndicator`): ROLLING → CLOSES TODAY → CLOSED changes slide-morph vertically (exit up faster than enter down), tabular figures keep the column steady; zero new APIs beyond `AnimatedContent`.

### 3.2 Design-system gaps still open (from the PR inventory, re-checked)

| Primitive | Status | Note |
|---|---|---|
| NexusBottomSheet conventions | **OPEN** | Material3 `ModalBottomSheet` is used ad-hoc; conventions + previews needed |
| NexusFilterBar / accessible multi-select | **PARTIAL** | Explore category chips hit 48 dp (`heightIn(min=48)` verified); a shared primitive would retire the per-screen copies |
| NexusSkeleton variants | **PARTIAL** | `EditorialSkeletonList` exists; per-screen geometry variants still ad-hoc |
| NexusChartLegend | **PARTIAL** | All four charts carry semantics; a shared legend primitive is still missing |
| Adaptive window layouts | **OPEN** | Compact/medium/expanded breakpoints not systematized |

### 3.3 AI surfaces
Citation chips on AI answers are **Blaze-gated** (backend). Structured actions (create goal / save opportunity from AI) are codeable now behind explicit confirmation — recommended next AI increment.

### 3.4 Verification state machine — **LANDED 2026-11-08**
```
enum class VerificationState { UNVERIFIED, VERIFIED, STALE, EXPIRED, REPORTED }
// domain fn: state(freshness: Duration, now, verifiedAt, reportedAt)
// renderers map state → existing SignalTag tones (fresh = silence per direction §)
```
Now implemented as `core/domain/model/VerificationState.kt`: the enum plus `VerificationPolicy.stateOf` (strict boundaries — exactly 48 h still reads VERIFIED, exactly 7 d still reads STALE, pinned by 8 unit tests including clock-skew tolerance). `ExploreStatusBand` and Home's day band read the policy; the duplicated `SOURCE_CHECK_*` constants are deleted. Home now also speaks the STALE neutral band it previously skipped (one vocabulary on every surface — the policy's whole point). `REPORTED` is reserved for the user-report flow; no producer writes it yet, stated in the KDoc.

### 3.5 Sweep the naming (L-01)
One-pass inventory: README title says "NEXUS — Your Career. Engineered." while product copy says Sagitta. Decide the public name once, sweep docs/store copy; keep package names (already decided).

### 3.6 Q-02 error taxonomy (pre-req for the exception cleanup)
Define `NexusError` (Network / Auth / Quota / Data / Unknown) in `core/common`, map in repositories, log with context — then the 58 sites become mechanical.

### 3.7 Accessibility findings (T-02 runtime pass)
Static audit **clean**: all chart composables carry semantics; every container-clickable speaks via child text (the decorative-icon pattern is applied consistently); known touch-target fixes (SaveToggle 48/36, chips 48 dp) hold. Runtime 200% font-scale + TalkBack walkthrough: recorded below in §7 verification evidence.

---

## 4. Advanced Features

From PR #26 (ranked), with this session's web-canon additions:

| Rank | Feature | Status |
|---|---|---|
| A1 | Verified opportunity provenance graph | **STARTED** — provenance rendered; state machine formalization open (§3.4) |
| A2 | Career memory graph | OPEN (after A1/A3) |
| A3 | Adaptive weekly career plan | **STARTED** — "Movable This Week" v1 landed; scheduling/recalibration open |
| A4 | Differentially private cohort intelligence | OPEN |
| A5 | On-device mock interview | OPEN (3–6 wk) |
| A6 | Hybrid semantic search (BM25 + embeddings) | OPEN (2–4 wk) |
| A7 | Career evidence portfolio export | PARTIAL — resume parser landed; exportable PDF/share pack open |
| A8 | Campus/TPO console | OPEN (4–8 wk, B2B) |

**Web-canon ports this session** (from the 13-site sweep; full table in the sweep report): commit microinteraction, path-draw reveal, readout state-morph — all three chosen because they are *one-shot, data-triggered, reduced-motion-trivial, and speak the instrument voice*. Deliberately rejected (again, with reasons): WebGL/shader heroes (GPU + banned ambient glow), hover-only effects (no hover on touch), marquees/meteors/orbits (infinite motion outside designsystem), invented-series charts (fake-data law).

**Next port candidates, ranked:** (1) telemetry sparklines in TelemetryStrip rows from real history (only ≥2 real points; absence renders as absence); (2) bounded disclosure + validation reveal (`animateContentSize` on single elements; error slots that never jump); (3) drag-scrub value fields with TalkBack `customActions`; (4) reading rail on long AI answers (hairline, not glow); (5) one-shot state-triggered motion icons (sync completes one rotation when a refresh *actually* finishes).

---

## 5. New Additions

Recommended, each with its gate:

1. **Calendar deadline reminders** — codeable now (AlarmManager/WorkManager + existing FCM perms); user-controlled cadence; **the single most-requested-shape feature a student app can add**.
2. **Sharesheet/App Links import** — "share this listing to Sagitta" imports into the saved ledger with provenance marked UNVERIFIED (honest by construction).
3. **GitHub public profile import** — opt-in portfolio signal, clearly labeled imported-vs-verified.
4. **Export/delete completion check** — data rights shipped earlier ("make the promised data rights real"); verify export covers Room + public profile + AI history.
5. **Structured AI actions** — create goal/save opportunity from chat behind explicit confirmation (§3.3).

Gate applied to all: product owner, data-flow diagram, permission review, failure mode, test plan — no integration "because it's popular".

---

## 6. Verification & Testing Strategy

### 6.1 Current gates (verified)
- `:app:assembleDebug` green (1 m 27 s warm this session, exit 0 on the pre-change tree)
- JVM suites: **428 passed / 64 ignored** across modules (split runs — combined run OOMs the 7 GB daemon; keep the split)
- Device tests: 46; backend emulator suite: 28; audit system: 9.10
- **Detekt has no gradle task in this tree** — `config/detekt/*.yml` is vestigial; either re-attach a task or delete the config (recommend re-attach; a lint/debt gate is currently absent)
- Coverage gate present (`coverageGate`)

### 6.2 Environment constraint (mandatory recipe)
This machine's plain `./gradlew` fails (JDK 25 on PATH + sandboxed HTTP tampering + poisoned shared caches). The working recipe — relay `python ~/.zcode/tmp/uipass-m2-relay.py` (port 8765) + `JAVA_HOME=~/.gradle/jdks/eclipse_adoptium-17-amd64-windows.2` + `GRADLE_USER_HOME=~/.zcode/tmp/uipass-gradle` + `-I ~/.zcode/tmp/uipass-init.gradle` + `--no-configuration-cache` + sandbox-off — was validated again this session. Any future session must use it.

### 6.3 Test matrix (PR §7.2 adopted as-is)
Unit → repository → integration (Firebase emulator) → migration (every Room migration v1→v10) → UI (state-per-screen) → accessibility (TalkBack/Switch Access/48 dp/200 %/contrast/reduced motion) → performance (physical device) → security (secret scan, APK inspection, rules deny-by-default) → UAT (fresh install through account deletion).

### 6.4 Additions this session
- Three previews for the new commit phases / inked plate (light/dark/AMOLED per direction §6)
- Deadline morph verified at the state boundary (day-count drop is the only client-observable transition; CLOSED flips on date rollover)
- 200 % font-scale screenshot sweep as a recurring pre-release step (scripted, see §7)

---

## 7. Verification evidence (this session)

| Check | Result |
|---|---|
| Pre-change baseline `:app:assembleDebug` | ✅ exit 0, 1 m 27 s (785 tasks up-to-date) |
| Static a11y audit (5 clickable candidates, 24 sub-48 dp sizes, 4 charts) | ✅ all container rows speak via child text; sizes are decorative icon dimensions; charts carry semantics |
| `:core:designsystem` / `:feature:growth` / `:feature:ai` compile after primitives | ✅ after 4 fixes (infix line-continuation, `using` not importable in this BOM, `PathMeasure()` no-arg ctor, `Rect` vs `RoundRect`) |
| Post-primitives `:app:assembleDebug` | ✅ exit 0 (build 2) |
| Runtime 200 % font sweep | recorded in session walkthrough |
| Runtime TalkBack walkthrough | recorded in session walkthrough |
| Emulator walkthrough | recorded in session walkthrough |

---

## 8. Beyond the categories — process recommendations

1. **The push decision is still open.** Eight commits (design-direction pack + trust metadata + splash/chart/pull-to-refresh foreign landing) sit unpushed on `master`. Until they are pushed, every future session risks re-deriving them. Recommend pushing this session's tree once the user answers.
2. **Close PR #26.** This report dispositions its register (Q-04, P-03 landed; S-* console-gated; T-02 executed). Merge or close it after the user reviews — the branch is documentation-only.
3. **Record the new primitives in DESIGN_DIRECTION.md** (done this session — see the §vocabulary addendum) so future UI work inherits them legally.
4. **Delete or re-attach detekt.** A quality gate that doesn't run is worse than none: re-attach as a gradle task with the existing config, or remove the config to stop implying a gate exists.
5. **One-command a11y sweep.** `scripts/a11y-sweep.sh`: set `font_scale 2.0`, walk the 5 tabs, screencap, reset — makes the 200 % pass a repeatable gate, not a memory test.

---

## Appendix — the 10-item "top actionable now" list

1. Push the 8 unpushed commits (user decision, 0 effort)
2. ~~Formal `VerificationState` in domain + tests~~ — **DONE**: `VerificationPolicy` + 8 tests; Explore + Home read it; duplicated thresholds deleted
3. Sweep Nexus/Sagitta naming in docs/store copy (1 d)
4. Firebase console: App Check staged → enforce, Crashlytics enable, key restrictions (user, 0.5 d)
5. Calendar deadline reminders (3–5 d)
6. ~~Roll commit microinteraction to auth/onboarding/deletion buttons~~ — **PARTIAL, dispositioned**: wired to the Roadmap + LinkedIn generators (results persist on screen — the phase is honest there). Auth dialog, onboarding finish and account deletion are **declined**: each navigates away on the same state that would light `settled`, so the phase would render zero visible frames — wiring it there would be decoration, and the component law forbids simulated settles.
7. Q-02 error taxonomy + top-20 exception sites (2 d)
8. Re-attach detekt as a real gradle task (0.5 d)
9. Sparklines from real telemetry history (1–2 d)
10. Localization string inventory + hi-IN pilot (3–7 d)
