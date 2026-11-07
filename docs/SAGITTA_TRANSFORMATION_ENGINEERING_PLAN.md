# Sagitta / Nexus — Transformation Engineering Plan

**Prepared:** 2026-11-07  
**Repository:** humoge7502/Nexus  
**Baseline branch:** master  
**Purpose:** Evidence-based product, engineering, security, performance, and native UI/UX transformation plan.

> This document is a plan and audit of repository evidence. It does not claim that proposed work has been implemented, deployed, or device-verified. Estimates are planning ranges, not measured execution times. Revalidate the repository and all gates before each release.

## 0. Executive direction

The strongest route is not a wholesale rewrite and not a pile of copied web components. The repository already contains a mature Kotlin/Jetpack Compose product foundation and a distinctive design system. Preserve it, close the documented launch gaps, then deepen the product's moat around verified opportunities, career intelligence, longitudinal coaching, and trusted outcomes.

The repository's newer business-model and design-direction documents use **Sagitta** as the product name, while the repository, Gradle project, packages, and many technical identifiers still use **Nexus**. Treat Sagitta as the proposed public product brand and Nexus as the current code/repository identity until a separately planned brand migration is approved. Do not mass-rename Kotlin packages, Firebase collections, application IDs, or Gradle modules as part of a visual refresh.

### Evidence snapshot from repository documents

| Area | Documented baseline | Interpretation |
|---|---:|---|
| Android architecture | Kotlin 2.1.21, Compose, Material 3, MVVM/Clean Architecture, Hilt | Preserve existing architecture |
| Modules | 11 core + 24 feature modules (README) | Modular product with meaningful feature surface |
| JVM tests | 322 unique tests / 51 suites | Strong reported unit-test base |
| Android instrumentation | 42 reported in comprehensive report | README summary says 40; reconcile documentation |
| Backend tests | 21 reported tests | Re-run against current checkout |
| Firestore rules | 13 reported emulator checks | Re-run current rules |
| Scraper tests | 9 reported tests | Re-run current Python suite |
| Coverage gate | Domain 43.42%, data 26.72%, network 29.45%, aggregate 33.69% (historical report) | Floors are modest; raise gradually |
| Cold-start TTID | Emulator median 1,345.6 ms | Good harness baseline; not a physical-device SLA |
| Scroll CPU frame duration | Emulator P95 816.5 ms in documented run | Emulator/SwiftShader is not a device SLA; investigate on physical hardware |
| Release security | Upstream AI key excluded from release APK; secret scan reported clean | Preserve release assertion |
| Production audit | 10.0/10 READY reported | Does not replace console/deployment and real-device checks |

**Documentation integrity issue:** README describes 40 instrumentation tests in its opening summary, while its detailed test table and current comprehensive report enumerate 42 (12 database + 4 cipher + 25 Compose UI + 1 app boot). Reconcile this count and date-stamp all test figures. Do not silently choose one.

## 1. Product strategy and differentiation

### 1.1 Positioning

Build the trusted, deadline-first career operating system for Indian students. The differentiator is a connected loop, not a larger checklist of isolated AI tools:

1. Discover opportunities across internships, hackathons, scholarships, research, open-source programs, certifications, and workshops.
2. Establish provenance: original source, publisher, last-verified time, deadline confidence, application URL, and verification status.
3. Match against a student's skills, interests, location, eligibility, academic profile, and career goal.
4. Convert a saved opportunity into a pipeline with a next action and reminder.
5. Improve readiness through learning, DSA, projects, certifications, and interview practice.
6. Measure outcomes and use consented, anonymized aggregates to improve cohort intelligence.

### 1.2 Defensible advantages

| Strategic asset | Build next | Success measure |
|---|---|---|
| Verified opportunity graph | Source registry, deduplication, freshness checks, expired-state workflow, report/appeal queue | <1% user-reported invalid listings; >98% active listings have source and verification timestamp |
| Personal career memory | Timeline of skills, projects, learning, applications, interviews, goals, and outcomes; user-editable and exportable | >60% of active users have meaningful profile history after 30 days |
| Explainable readiness | Preserve on-device inference; show factor contributions, data freshness, confidence calibration and "what changes the score" | Calibration error and forecast error tracked by cohort; no score without enough evidence |
| Action orchestration | Turn readiness gaps and deadlines into a weekly plan with measurable tasks | Weekly-plan completion and D7/D30 retention |
| Cohort intelligence | Minimum cohort size, privacy review, suppression of sparse cells, differential privacy where appropriate | Zero re-identification findings; published cohort size and freshness |
| Trust and portability | User-controlled export, deletion, consent, source reporting and transparent data use | Export/delete completion SLO; consent withdrawal tested end-to-end |

Do not market the readiness score as a guarantee of placement or a probability of employment unless the model's target, calibration, population validity, and limitations are clearly disclosed.

### 1.3 Product KPIs

Instrument funnels rather than relying on vanity metrics:

- Opportunity: impression → detail → save → apply intent → application recorded → outcome.
- Trust: source coverage, verification age, expiry detection latency, duplicate rate, invalid-report rate, report resolution time.
- Readiness: score coverage, missing-signal rate, factor freshness, forecast calibration, plan completion.
- Retention: D1/D7/D30, weekly active users, meaningful actions per active week.
- AI: time-to-first-token p50/p95, completion latency, failure rate, quota rejection rate, cost per successful session, citation validity.
- Performance: cold/warm startup TTID, frame duration P50/P90/P95, ANR rate, crash-free sessions, memory peak, network bytes.
- Accessibility: critical journeys completed with TalkBack, 200% font scale, reduced motion, and high-contrast testing.

Every metric needs a definition, event owner, denominator, privacy classification, and dashboard before it becomes a target.

## 2. Issues and required fixes

Severity is based on user harm, security, data integrity, and launch risk. Priority P0 = immediate blocker, P1 = next release, P2 = planned iteration, P3 = polish. CVSS is not assigned unless a reproducible vulnerability and attack preconditions have been established.

| ID | Finding grounded in repository docs | Severity / priority | Estimate | Dependencies | Recommended timeline |
|---|---|---|---:|---|---|
| S-01 | App Check / Play Integrity not yet enforced for callable/proxy abuse | High / P1 | 1–2 days engineering + console setup | Firebase project, Play Console, debug/test tokens, rollout plan | Before public beta |
| S-02 | Firebase client API key package + SHA-256 restrictions remain console-gated | Medium / P1 | 2–4 h + console access | Release signing certificate fingerprints, Firebase project | Before public beta |
| S-03 | Crashlytics plugin/dependency prepared but not enabled; release diagnostics lack a production reporting path | High / P1 | 0.5–1.5 days | Firebase config, privacy/consent copy, retention policy | Before public beta |
| S-04 | Scraper host and secret lifecycle need operational verification; historical audit references a Render-hosted LeetCode mirror risk while newer docs describe an owned callable | High / P1 until resolved | 0.5–1 day investigation; 1–3 days remediation if still present | Current deployment inventory, DNS, CI secrets, backend source | Immediate validation |
| Q-01 | Seven modules retain lint baselines (historical issue I-06) | Medium / P2 | 2–4 days | Current lint reports, module owners | Next 2 sprints |
| Q-02 | Swallowed exception cleanup is partial (historical I-11) | Medium / P2 | 1–3 days | Error taxonomy, logging policy | Next 2 sprints |
| Q-03 | Production Kotlin TODO/FIXME/HACK count was reduced to nine, not zero | Low / P3 | 0.5–2 days | Current grep and triage | Ongoing |
| Q-04 | README and audit documentation contain inconsistent instrumentation counts and historical snapshots | Medium / P1 | 2–4 h | Re-run all suites; single report-generation source | Immediate |
| P-01 | Physical-device startup and frame-time budgets are not established | High / P1 | 1–2 days measurement + 2–5 days optimization if needed | Mid-tier Android devices, repeatable benchmark harness | Before public launch |
| P-02 | Documented emulator scroll CPU P95 is 816.5 ms; emulator/SwiftShader makes it unsuitable as a device SLA, but it is a signal to investigate | Medium / P1 | 1–2 days profiling | Physical device, trace capture, screen-specific benchmark | Next performance pass |
| P-03 | Paging 3 coverage is incomplete for large opportunity/catalog surfaces | Medium / P2 | 2–5 days | Repository paging APIs, Room/remote paging strategy | Next 2 sprints |
| P-04 | AI first-token p95 target (<3 s) is specified but deployed-key measurement is outstanding | Medium / P1 | 1–2 days instrumentation + 1–3 days tuning | Deployed backend, regional test accounts, provider telemetry | Before public beta |
| T-01 | Compose UI tests cover selected feature screens; most older feature screens lack UI suites | Medium / P1 | 5–10 days staged | Stateless content composables, fake repositories | Continuous |
| T-02 | Manual TalkBack pass is still outstanding in the documented audit | Medium / P1 | 1–2 days | Accessibility test devices, test script | Before public beta |
| T-03 | Quota exhaustion is unit-tested; emulator-scale transactional concurrency assertion remains open | Medium / P1 | 0.5–1.5 days | Firebase emulator, deterministic concurrency test | Before public beta |
| T-04 | Hilt component tests for selected ViewModels remain constrained by direct Firebase singleton bindings | Low / P2 | 2–4 days | Test bindings / repository abstractions | Next 2 sprints |
| L-01 | Product is referred to as both Nexus and Sagitta across repository artifacts | Medium / P2 | 1–2 days inventory + approved migration plan | Product/brand decision, store assets, legal/domain review | Before store listing |
| L-02 | English-only copy is documented; localization is still open | Medium / P2 | 3–7 days for first locale, excluding translation review | String inventory, locale QA | Pilot after core flows |
| O-01 | Firebase deployment, App Check, Crashlytics and API-key restriction require account/console actions; repo-only changes cannot complete them | High / P1 | Account-owner dependent | Authorized Firebase/Google Cloud access | Release gate |

### 2.1 Security handling

- Re-run secret scanning, dependency review, Firestore rules tests, backend emulator tests, and release APK inspection on the current branch.
- Inspect android:allowBackup and Android 12+ data extraction rules. The security policy describes a conditional risk; it does not prove that backup is currently unsafe.
- Verify CORS and API-key rotation on the actual scraper deployment.
- Verify App Check enforcement in staged mode before enforcing it; test legitimate debug, internal, and production clients.
- Ensure AI prompt contents and resume/interview data are not written to analytics or crash logs.
- Review publicProfiles fields for consent, user expectations, and deletion propagation.
- Keep server-authoritative Pro, XP, rank, quota and cohort fields protected by rules and callable validation.
- Do not publish a CVSS score for a hypothetical issue. Record CVSS only after validating exploitability and impact.

### 2.2 Reproduction quality standard

Every future bug report must include:
- Commit SHA, build variant, device/API level, account state and network state.
- Exact navigation/action steps and input values (with private data redacted).
- Expected result, actual result, logs/trace, frequency and scope.
- Regression test added before or alongside the fix.
- Verification on the affected device class.

The current repository report contains historical examples of fixed defects (scraper unauthenticated writes, blank opportunity titles from role/title schema mismatch, expired Firebase token retry, migration fixture defects, and negative achievement padding). They must not be re-reported as current bugs without reproducing them on the current branch.

## 3. Enhancements and modifications

### 3.1 Home / Career Command Centre

Current feature/home/.../HomeScreen.kt already has a signal field, editorial greeting, readiness card, telemetry strip, career orbit, opportunity row, application summaries and reveal motion. Do not replace this with a generic card dashboard.

Enhance it by:
- Making the first viewport answer three questions: "What changed?", "What matters today?", "What is the next action?"
- Promoting one time-sensitive action (deadline or readiness gap) to the primary action.
- Showing freshness and provenance on opportunity snippets.
- Making readiness explanations expandable and linked to the actual contributing data.
- Keeping the career orbit as a meaningful summary, with a text equivalent for accessibility.
- Adding deterministic screenshot tests for loading, empty, offline, returning-user and data-rich states.

### 3.2 Opportunity discovery

- Add a canonical Opportunity schema and explicit source, sourceUrl, verifiedAt, deadlineAt, deadlineConfidence, eligibility, status, canonicalFingerprint, and reportedAt.
- Deduplicate by canonical URL + normalized publisher/title + deadline, with human review for ambiguous matches.
- Build a verification state machine: UNVERIFIED → VERIFIED → STALE → EXPIRED with REPORTED and UNDER_REVIEW branches.
- Never show an unverified listing as verified; never infer an unknown deadline.
- Add saved search and notification preferences; avoid sending repeated reminders.
- Use Paging 3 and stable keys for large feeds; cache last-known-good data for offline use.

### 3.3 Placement readiness / forecast

- Keep inference on-device and deterministic.
- Display model version, last-calculated time, missing inputs, confidence/calibration caveats and a plain-language explanation.
- Add data-quality guards: stale inputs, sparse profiles, impossible values, and distribution drift.
- Store score snapshots with model version and feature-schema version so historical comparisons remain valid.
- Separate "score change caused by new evidence" from "score change caused by model update".
- Make counterfactual changes clearly hypothetical and do not imply guaranteed outcomes.

### 3.4 AI coach and research

- Add a consented, user-editable memory summary with inspect, correct, export and delete controls.
- Retrieve only relevant career facts, attach provenance, and show why a recommendation was made.
- Require citations for external factual research and distinguish model inference from sourced fact.
- Add structured actions (create goal, save opportunity, add application, plan practice) behind explicit user confirmation.
- Add cancellation, retry, partial-response recovery, streaming backpressure and idempotency.
- Add token/cost budgets per route and observability without logging raw private prompts.

### 3.5 Growth, applications and portfolio

- Create a shared timeline of goals, DSA, learning, projects, applications, interviews and outcomes.
- Add a weekly plan generated from readiness gaps and upcoming deadlines.
- Add exportable portfolio (PDF/share sheet) with user preview and privacy controls.
- Add application follow-up templates and reminders; user must approve external sends.
- Make streak and XP subordinate to real progress, not a pressure loop.

### 3.6 Design-system engineering

Preserve the existing core/designsystem tokens and components:
- NexusCard, NexusButton, NexusIconButton, NexusTextField, NexusChip
- InstrumentGauge, InstrumentPlate, TelemetryStrip, MomentumChart, SkillConstellation, LearningHeatmap
- EditorialComposition, OpportunityEditorial, StreamingAIResponse
- RevealOnLoad, RevealWords, NexusTransitions, LocalReducedMotion

Add only missing primitives after an inventory:
- NexusBottomSheet / modal sheet conventions
- NexusFilterBar and accessible multi-select filter
- NexusDateDeadline / countdown with timezone-aware semantics
- NexusSourceBadge with verification state and timestamp
- NexusSkeleton variants per real screen geometry
- NexusSnackbar / undo action
- NexusChartLegend and accessible chart summary
- Adaptive layouts for compact, medium and expanded windows

Rules:
- One loud signal per screen; at most one signature gradient per screen.
- Do not introduce a new typography family or arbitrary colors without design-direction review.
- No decorative animation that obscures state or delays interaction.
- All infinite motion must be gated by reduced motion and lifecycle visibility.
- All controls have 48dp minimum touch targets, semantics, focus states and 200% font-scale support.
- Add previews in light, dark and AMOLED for each reusable component.

## 4. Advanced features (ranked)

| Rank | Feature | Implementation approach | Value | Estimate |
|---|---|---|---|---:|
| A1 | Verified opportunity provenance graph | Canonical source registry, fingerprint dedupe, freshness jobs, expiry state machine, report review | Trust moat and repeat usage | 1–3 weeks |
| A2 | Career memory graph | Room-backed event timeline + skill/goal/application graph; retrieval with provenance | Compounding personalization | 2–4 weeks |
| A3 | Adaptive weekly career plan | Rule/ML driver mapping, task scheduling, completion feedback, recalibration | Converts insights to action | 1–2 weeks |
| A4 | Differentially private cohort intelligence | Minimum cohort threshold, suppression, DP aggregation, privacy budget and tests | Safer institutional analytics | 2–4 weeks |
| A5 | On-device mock interview | Android audio capture, on-device ASR evaluation, local transcript controls, rubric feedback | Privacy-led interview practice | 3–6 weeks |
| A6 | Hybrid semantic opportunity search | BM25 + embeddings + eligibility filter + reranking + explanation | Better relevance | 2–4 weeks |
| A7 | Career evidence portfolio | Exportable evidence pack, PDF/share sheet, selective disclosure | Shareability and institutional value | 1–2 weeks |
| A8 | Campus/TPO console | Role-based web portal, aggregate-only cohorts, employer verification workflow, audit trail | B2B distribution | 4–8 weeks |

Sequence A1 → A3 → A2 → A4 before investing in advanced generative features. Trust and actionable outcomes compound more than novelty.

## 5. New additions and integrations

Prioritize integrations that strengthen the opportunity graph and reduce user effort:
- Calendar reminders for deadlines and interview schedules, with explicit notification permissions and user-controlled cadence.
- Android Sharesheet and App Links for importing opportunities from supported sources.
- GitHub public profile import as an opt-in portfolio signal; clearly distinguish imported public data from verified achievements.
- Official employer/campus feeds and organizer submission portal; avoid brittle scraping or violating source terms.
- PDF resume parsing with local redaction controls and explicit consent.
- Play Integrity/App Check, Crashlytics, performance traces and structured backend metrics.
- User export/delete workflows covering Room, Firestore, AI memory, public profile, analytics identifiers and backups subject to retention rules.

Do not add an integration solely because it is popular. Require a product owner, data-flow diagram, permission review, failure mode, rate limit, test plan, and measurable success metric.

## 6. UI/UX resource review and native adaptation

The following sites were opened during this review. Most libraries target React/TypeScript/Tailwind or web primitives; use them as inspiration and source only when the license and implementation platform permit. Do not install their web dependencies into the Android app.

| Resource | What to study / adapt natively | Compatibility |
|---|---|---|
| [21st.dev](https://21st.dev/) | Component composition, search, polished interaction patterns, AI-assisted component discovery | Inspiration; web-first |
| [Aceternity UI](https://ui.aceternity.com/) | Spotlight, background effects, layered hero composition, restrained hover/scroll effects | Rebuild with Compose Canvas/graphicsLayer; web-first |
| [Magic UI](https://magicui.design/) | Text reveal, shimmer, number ticker, marquee, border effects | Rebuild selectively; web-first |
| [React Bits](https://reactbits.dev/) | Creative backgrounds, text motion, micro-interactions | Rebuild selectively; web-first |
| [Motion Primitives](https://motion-primitives.com/) | Motion recipes and transition patterns | Site returned 403 in this review; not individually inspected |
| [Animate UI](https://animate-ui.com/) | Accessible animated state transitions and component behaviour | Translate principles to Compose; web-first |
| [Origin UI / COSS UI](https://originui.com/) | Form controls, autocomplete, dialogs, menus and accessibility conventions | Site redirects to COSS UI; Base UI/web ecosystem |
| [Kibo UI](https://www.kibo-ui.com/) | Composable charts, color picker, QR, media and data patterns | shadcn/web registry; native adaptation required |
| [ReUI](https://reui.io/) | App shell, analytics, charts, AI/agent layouts, filters and tables | shadcn/web; use as workflow reference |
| [Motion](https://motion.dev/) | Layout, gestures, scroll-linked animation, orchestration | React/JS; use Compose animation APIs instead |
| [GSAP](https://gsap.com/) | Timeline choreography, scroll storytelling, SVG motion | Web/JS; do not add to Android |
| [AutoAnimate](https://auto-animate.formkit.com/) | List insertion/removal and layout transition principles | Web/JS; use Compose AnimatedContent/animateItem |
| [Godly](https://godly.website/) | Editorial art direction and web storytelling | Redirected to recent.design and could not be inspected |
| [Awwwards](https://www.awwwards.com/) | Typography, interaction, visual hierarchy, motion restraint | Inspiration only |
| [Mobbin](https://mobbin.com/) | Shipped mobile flows, onboarding, search, settings, empty/error states | Strong mobile UX research source; access may require account |
| [Dribbble](https://dribbble.com/) | Visual exploration and concept directions | Inspiration only; validate usability against real flows |
| [Refero](https://refero.design/) | Product UI patterns and screen references | Page returned no readable content in this review |

### Native component translation map

| Web pattern | Compose implementation direction |
|---|---|
| Spotlight / cursor-following glow | Static or pointer-independent Canvas glow; avoid cursor metaphor on touch |
| Animated border / beam | DrawScope path animation with lifecycle + reduced-motion gate |
| Text reveal / scramble | AnimatedContent, TextMotion where appropriate, staggered alpha/offset |
| Number ticker | Animatable with semantic final value; no misleading intermediate announcements |
| Scroll reveal | LazyList item placement / AnimatedVisibility; avoid nested scroll observers |
| Sticky sections | LazyColumn sticky headers and clear section hierarchy |
| Bottom sheet / drawer | Material 3 sheet + predictive back + adaptive navigation |
| Command palette | Existing NexusCommandPalette; improve search indexing and keyboard/TalkBack support |
| Skeletons | Existing shimmer/loading state variants sized to actual content |
| Charts | Existing Canvas/Vico components, real data, semantics and textual summary |
| Glass surface | Reserve for true layered chrome; use tonal surface + hairline elsewhere |
| Confetti / celebration | Existing AchievementBurst; only for earned milestones and skippable |

### Design references to gather before coding

Use Mobbin to collect 3–5 shipped references per flow: onboarding, opportunity discovery, search/filter, opportunity detail, application tracking, AI chat, forecast, settings and accessibility. Use Awwwards/Dribbble only to enrich art direction. Record screenshots, source, date, interaction notes, and the exact problem solved. Never copy a complete screen or proprietary asset.

### Asset and licensing gate

Before downloading or committing any component, font, icon, illustration, animation or image:
1. Record source URL, author, exact license/version and attribution requirement.
2. Check commercial use, redistribution, modification and trademark restrictions.
3. Prefer source-owned Compose code, AndroidX, Material 3 and OFL fonts already in the repo.
4. Avoid shipping copied React/Tailwind code, remote hotlinked images, unlicensed illustrations or large animation bundles.
5. Add third-party notices and a reproducible asset manifest.
6. Measure APK size, startup, memory and accessibility impact.

No third-party component was downloaded or added by this review.

## 7. Verification and release strategy

### 7.1 Required CI gates

Run from a clean checkout and archive logs/reports:

    ./gradlew clean
    ./gradlew testDebugUnitTest
    ./gradlew coverageGate
    ./scripts/detekt.sh
    ./gradlew runAudit
    ./gradlew :app:assembleDebug
    ./gradlew :app:assembleRelease
    ./gradlew connectedDebugAndroidTest
    ./gradlew :macrobenchmark:connectedBenchmarkReleaseAndroidTest
    cd backend/functions && npm ci && npm run typecheck && npm test
    npm run test:emulator
    npm run test:rules

Run scraper pytest from its documented working directory. Run ML dataset reproducibility and Python/Kotlin parity tests. Confirm CI workflows cover the same checks; a local-only test is not a release gate.

### 7.2 Test matrix

- Unit: use cases, mappers, ranking, dedupe, deadline/timezone logic, score model, confidence, quota reservation.
- Repository: Room + remote source + offline fallback + cache expiry + paging.
- Integration: Firebase Auth, Firestore rules, callable auth, App Check, AI SSE, cancellation, retry, quota exhaustion and concurrency.
- Migration: every Room migration from the oldest supported schema, including data preservation.
- UI: each primary screen's loading/empty/error/offline/data-rich state; navigation and back; state restoration; dynamic type.
- Accessibility: TalkBack, Switch Access, 48dp touch targets, 200% font scale, contrast, reduced motion, semantics for every chart.
- Performance: physical-device startup, frame timing, memory, network, large lists, low-memory and thermal conditions.
- Security: secret scan, dependency/CVE review, APK inspection, exported component review, backup rules, Firestore deny-by-default, API abuse, privacy/log review.
- UAT: fresh install, onboarding, discover/save/apply, deadline reminder, add DSA progress, readiness refresh, AI stream, offline mode, export/delete, billing restore and account deletion.

### 7.3 Suggested performance budgets (to validate on physical hardware)

| Metric | Initial target | Measurement |
|---|---:|---|
| Cold TTID | ≤1.8 s p50, ≤2.5 s p95 | 5+ repeated cold starts on reference mid-tier device |
| Warm navigation | ≤300 ms p95 | Macrobenchmark navigation journey |
| Scroll | <1% frames >50 ms; no sustained jank | FrameTimingMetric + Perfetto |
| Readiness calculation | ≤80 ms p95 | Trace around Room reads + model inference |
| AI first token | <3 s p95 on supported network | Client and server correlated trace |
| Crash-free sessions | ≥99.8% beta target | Crashlytics after deployment |
| ANR rate | <0.1% beta target | Play Console / Android vitals |
| Feed memory | Stable under 500+ items with paging | Heap and scroll stress test |

Targets are proposals, not current measured performance claims. Define reference devices and network profiles before enforcing them.

## 8. Phased implementation roadmap

### Phase 0 — Evidence refresh (1–2 days)
- Freeze a baseline commit and record branch/commit SHA.
- Re-run tests, audit, detekt, APK scan, backend emulators, scraper and ML tests.
- Reconcile README test counts and all stale reports.
- Verify current Firebase deployment, Render usage, App Check and API-key restrictions.
- Capture current screen recordings and screenshots across light/dark/AMOLED.

**Exit:** reproducible baseline and signed-off open-issue register.

### Phase 1 — Trust and operational readiness (3–7 days)
- Enable Crashlytics with consent-aware privacy policy.
- Configure and stage App Check / Play Integrity.
- Restrict Firebase API key and rotate deployment secrets as needed.
- Verify backup/data extraction rules and public profile consent/deletion.
- Add quota concurrency emulator tests and deployed AI latency telemetry.

**Exit:** no unresolved P0; all P1 security controls verified or explicitly accepted with owner/date.

### Phase 2 — Product trust loop (1–3 weeks)
- Canonical opportunity schema and source provenance.
- Deduplication, verification, expiry and report-review workflow.
- Paging 3 and saved searches.
- Deadline-aware reminders and application next actions.
- Add end-to-end tests for discover → save → apply → outcome.

**Exit:** verified opportunity funnel works end-to-end and survives offline/restart.

### Phase 3 — Native design excellence (1–2 weeks, parallelizable)
- Audit all 24 feature screens against Sagitta design law.
- Create screen inventory, state matrix and screenshot baseline.
- Fill design-system primitive gaps only where repeated patterns justify them.
- Improve Home, Explore/Internships, Opportunity Detail, AI Hub and Forecast first.
- Add Compose previews, screenshot tests, TalkBack and 200% font-scale verification.
- Use native Compose motion; no React/GSAP runtime dependency.

**Exit:** no inconsistent controls/tokens; all primary flows have loading/empty/error/offline states and visual regression coverage.

### Phase 4 — Compounding intelligence (2–5 weeks)
- Career memory graph and user controls.
- Adaptive weekly plan.
- Model versioning, calibration and data-quality indicators.
- Cohort privacy review and differential privacy implementation.
- Portfolio export and user-owned evidence timeline.

**Exit:** users can understand, act on, inspect and export their career progress.

### Phase 5 — Scale and distribution (4–8 weeks)
- Campus/TPO portal with strict role-based access and aggregate-only analytics.
- Employer/organizer verification workflow.
- Official integrations and partner feeds.
- Localization, regional onboarding and low-bandwidth UX.
- Physical-device performance and accessibility sign-off.

**Exit:** operationally supportable campus pilot with measurable trust, retention and outcome metrics.

## 9. Governance and definition of done

A feature is not complete until:
- Product requirement and target metric are defined.
- Data flow, permissions, privacy and threat model are reviewed.
- UI states include loading, empty, error, offline and success.
- Strings are localized resources; semantics and dynamic type work.
- Unit + integration + UI regression tests pass.
- Detekt, audit, release build and secret scan pass.
- Physical-device performance is measured where relevant.
- Screenshots and release notes are archived.
- Rollback/feature flag and owner are documented.
- The user-facing claim matches the evidence.

## 10. Immediate next actions

1. Re-run the current repository checks; treat October/November report figures as historical until reproduced.
2. Resolve App Check, Crashlytics, Firebase key restriction and deployment verification with the account owner.
3. Reconcile README's 40 vs report's 42 instrumentation count.
4. Validate whether the historical Render/LeetCode dependency still exists in any deployed service.
5. Establish physical-device performance baselines before tuning Compose animations.
6. Implement opportunity provenance and expiry as the next product feature.
7. Conduct a screen-by-screen Sagitta design audit and upgrade only components that improve comprehension, trust or task completion.

**North star:** the most trusted, useful and beautiful career instrument for students — not the app with the most animations or the most AI buttons.
