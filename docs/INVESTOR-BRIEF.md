# Sagitta — Investor Brief

*Prepared from the MiroFish multi-agent market simulation (240 rounds, 12
personas, dual-platform world) run on the real product listing, plus the
product changes shipped in response. Every number below is either real
(product data) or explicitly labeled as a simulation finding — nothing is
projected.*

---

## What Sagitta is

A career workspace for Indian college students: a **verified** internship
and hackathon catalog, deadline and skill-match alerts, an application
pipeline tracker, DSA practice tracking, and AI career tools that read the
user's own profile. Free core; Pro unlocks the AI tools.

## Why now

The placement-preparation tools Indian students actually use — WhatsApp
groups, spreadsheets, PDF hoarding — lose track of deadlines and show
match scores that mean nothing. The catalogs that exist today (Internshala,
Unstop) are discovery-only: nothing holds the student's pipeline, practice,
and readiness in one verifiable place.

## What the market simulation found (the honest part)

A 240-round multi-agent simulation of the product's launch season surfaced
three first-day churn gates, in order of reach:

1. **Sign-up friction + empty first screen.** The profile-first flow delays
   value; students want deadlines visible before they invest.
2. **Overlap with existing channels.** Groups and Internshala feel "good
   enough" — until a deadline is missed. The catalog cannot beat the group's
   zero-cost forwarding; it must *bridge* it.
3. **Empty first-day value.** Tracker/practice screens are blank states
   until fed; the deepest-loss gate.

**Pricing:** the $49.99/yr anchor triggered rejection *before evaluation*;
the monthly option backfired (students computed $120/yr). What the cohort
accepted: a **commitment window that ends with their season**. A 3-month
Season Pass is now the lead tier.

**The feature students invented:** forward a group listing into Sagitta and
have it checked against the verified catalog and tracked in one tap. This —
the bridge between where listings are discovered (groups) and where they're
tracked (the app) — is shipped as the **Share into Sagitta** flow.

**Trust finding:** claims verifiable on day one (honest match scores,
unverifiable listings excluded, real deletion) were accepted; superlatives
were dismissed as marketing. The store listing was rewritten to lead with
the verifiable claims.

## What was shipped in response

- **Share into Sagitta** — share a forwarded listing from any app; Sagitta
  searches the verified catalog for it and tracks it.
- **Placement Season Pass** — a 3-month Pro tier that ends with the season,
  now the lead plan on the paywall.
- **Store listing rewritten** around the claims the simulation found
  credible, removing the ones it flagged as trust-damaging.
- A command palette, an editorial design system, and the full career
  workspace (applications, forecast, portfolio, AI tools) — see
  [DESIGN_DIRECTION.md](DESIGN_DIRECTION.md).

## Honest limitations

- The simulation's interview channel timed out (0/12 agent interviews
  returned), so findings rest on the graph facts the world generated — the
  report itself labels this.
- No users yet: all findings are simulation-derived. The next validation
  gate is a real internal-testing cohort.
- The Season Pass and share-flow need a Play Console billing product
  (`nexus_pro_season`) and a listing update before release.

## The ask, when the time comes

Not in this document — this brief exists to make the product's reasoning
and its evidence legible to whoever reads it next. The numbers that matter
(retention past day one, alert accuracy, verified-listing coverage) are
instrumented in the app and will be real before they are quoted.
