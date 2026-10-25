# NEXUS Business Model & Monetization Plan

> Status: v1, grounded in what is actually built (Play Billing 3-tier paywall wired
> into navigation; per-user AI quota enforcement live in the backend proxy).

## 1. Product thesis

NEXUS is the **career operating system for engineering students in India**: one
verifiable number (NEXUS Score), one AI mentor that remembers you (Career Memory),
one cohort intelligence layer no competitor has. Monetization follows value, not
ads: students pay for **outcomes and unfair advantages**, not for removing
annoyances.

## 2. Pricing (India-optimized, Play Billing)

The app already ships three SKUs (`ProductTier`): `PRO_MONTHLY`, `PRO_YEARLY`,
`LIFETIME`. Recommended price points:

| Tier | Price | Target ARPU/yr | Positioning |
|---|---|---|---|
| Free | ₹0 | — | Full tracker: internships, DSA tracker, streaks, basic AI chat (quota'd) |
| **Pro Monthly** | ₹99/mo | ₹1,188 | "Interview season mode" — 1–2 months before placements |
| **Pro Yearly** | ₹499/yr (₹42/mo) | ₹499 | Default choice; 58% discount framing vs monthly |
| **Lifetime** | ₹1,999 one-time | — | Anchors yearly as "rational"; early-adopter harvesting |

Why these numbers: Indian student willingness-to-pay clusters at ₹50–150/mo for
outcomes (Unstop pro programs, LeetCode India pricing, test-prep apps). The
lifetime tier exists primarily to make ₹499/yr look small — expect <5% lifetime
mix.

## 3. Free vs Pro matrix (maps 1:1 to `FeatureGate` enum in code)

| Feature (gate) | Free | Pro |
|---|---|---|
| Internships/Opportunities feed | ✅ | ✅ |
| DSA Tracker + daily problem | ✅ | ✅ |
| Streaks + Daily Career Hits | ✅ | ✅ |
| Basic AI Chat | 15 msgs/day | Unlimited (fair-use) |
| Resume Analyzer | 1/mo | Unlimited |
| Roadmap Generator | ✅ 7-day trial | ✅ |
| Skill Gap Analysis | — | ✅ |
| Interview Coach | — | ✅ |
| LinkedIn Post Generator | — | ✅ |
| NEXUS Oracle + Arena | — | ✅ |
| Career Insights (calibration report) | — | ✅ |
| Career Mirror quarterly narrative | — | ✅ |
| Priority opportunity matching (embeddings) | Standard | Priority + more skills |

The free tier is deliberately **complete as a tracker** — retention first,
monetization at the moment of ambition ("I have interviews in 3 weeks").

## 4. Unit economics (verifiable from the code)

Backend AI cost per Pro user, at the enforced quota (40 requests / 60k tokens
per day, `gpt-4o-mini` at ~$0.15/M input, $0.60/M output):

- Worst case: 60k tokens/day ≈ 45k in + 15k out ≈ **$0.0157/day** ≈ ₹1.3/day
- Realistic (5–8 AI interactions/day, ~8k tokens): ≈ **$0.002/day** ≈ ₹0.17/day
- Yearly worst case: ≈ ₹470 vs ₹499 revenue → still non-negative; realistic ≈ ₹62

**Margin holds even at quota maximums.** This is the hard constraint the proxy
allowlist + token clamp protects — never raise the per-request token ceiling
without repricing.

## 5. Revenue ladder (mirrors the research docs' moat plan)

| Phase | Stream | Mechanism | Year-1 realistic |
|---|---|---|---|
| Now–6 mo | B2C subscriptions | Paywall (wired), conversion events tracked via `paywall_purchase` analytics | 2–4% of actives |
| 6–12 mo | B2C add-ons | Placement-season "Interview Sprint" pass (limited-time tier) | seasonal spike |
| 12–18 mo | **B2B campus** | Placement cells buy cohort dashboards (cohort_intelligence is already a Firestore collection + callable API) | ₹25k–1L/campus/yr |
| 18–24 mo | Recruiter intelligence | Anonymized, aggregated skill-velocity benchmarks per college/tier | data licensing |
| Later | Infrastructure | "AWS of career data" — the research docs' 10-year thesis | — |

B2B campus is the strategic prize: `getCohortIntelligence` already exists as an
authenticated callable. A college dashboard is a web view over data the app
already collects — near-zero marginal product cost.

## 6. Growth flywheel (all loops already have code)

1. **Score sharing** — share card for the NEXUS Score (Konfetti + design system
   already build the celebration); every share is a free install path.
2. **Leaderboard** — now wired to real Firestore data with college/domain tabs;
   social pressure → retention → Pro.
3. **Career Mirror quarterly** — a genuinely shareable artifact ("my quarter in
   review") that reactivates dormant users.
4. **Referral XP** — `awardXP()` infra exists; wire invite-link → both sides
   get XP/streak freeze.

## 7. Activation metric to obsess over

**D7 retention of users who log ≥1 application + solve ≥3 DSA problems in week 1.**
Every quota, gate, and notification should be tuned to get new users to that
state. The sync workers (deadline alerts, daily briefing) exist to automate it —
they must be enabled in `NexusApplication` (tracked as R5).

## 8. Non-negotiables

- Never gate data the user created (their applications/goals) — gate
  intelligence, not storage. Trust is the moat.
- Never let AI cost exceed 35% of Pro ARPU (proxy quota makes this impossible
  today; keep it that way).
- Server-authoritative `isPro`: the Firestore rules merged in this repair
  already prevent client self-grants; Play Billing real-time developer
  notifications should be the next integration to revoke refunds.


---

## v2 — Investor-Ready Addendum (Oct 2026)

### What changed since v1
- **Deep Research** (agent-search pattern): cited, source-grounded answers with
  auto-generated follow-up questions — a Perplexity-class experience inside a
  student career app. No competitor in the India placement-prep market has this.
- **Full toolchain modernization**: Android 16 (SDK 36), Kotlin 2.1.21,
  AGP 8.12.3, Gradle 8.14.3, Compose BOM 2025.06 — every library current.
- **Verified live infrastructure**: owned Firebase project, real accounts
  creating themselves end-to-end, emulator-verified UI.

### The one-line pitch
> "NEXUS is the AI career co-pilot for India's 1.5M annual engineering
> graduates — a live NEXUS Score, an AI mentor that remembers you, and Deep
> Research with citations — on a freemium model the free AI quota makes
> margin-safe at scale."

### Investor metrics that matter (all measurable from day 1)
| Metric | Target (6 mo) | Instrumented via |
|---|---|---|
| W1 activation (1 app + 3 DSA) | 40% | ai_proxy_logs + analytics |
| D7 retention | 25% | score_snapshots cadence |
| Free→Pro conversion | 3-5% | BillingManager events |
| AI cost / Pro user / day | < ₹2 | ai_proxy_logs totalTokens |
| Organic K-factor (score shares) | 0.3 | deep-links + shares |

### Why this can't be copied quickly
1. **Career Memory** — a per-user longitudinal dataset that compounds daily;
   a clone starts empty forever.
2. **Cohort Intelligence** — anonymized placement outcomes per college/tier:
   network-effect data no solo competitor can bootstrap.
3. **On-device placement model** — 14-feature explainable logistic model,
   golden-vector tested, zero inference cost; competitors pay per LLM call.
4. **Deep Research loop** — the follow-up-question UX creates session depth
   (5+ turns) that is the strongest retention signal in AI products.

### Series-ready roadmap (what the raise funds)
1. **₹0 → ₹50L ARR path**: 5 college pilot licenses (₹1L/yr each) + 2,000 Pro
   subs at ₹499 — both already built and wired.
2. **Recruiter dashboard** (month 6-9): colleges pay for cohort analytics;
   recruiters pay for verified skill-velocity data. Two-sided from one dataset.
3. **Verifiable NEXUS Score API** (year 2): the "credit score for careers"
   that placement cells and employers query — the infrastructure business.
