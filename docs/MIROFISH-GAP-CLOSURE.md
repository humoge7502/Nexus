# MiroFish Gap Closure — Sagitta

> Audit of the two MiroFish outputs for this product
> (the 05-11-2026 240-round simulation report and the corrections already
> shipped in its wake) against the codebase, plus the defects found and
> fixed in the 06-11-2026 pass. Every claim below is checkable in-repo.

## What the simulation found, and where each finding stands

| # | MiroFish finding | Status before this pass | This pass |
|---|---|---|---|
| 1 | Sign-up friction; students abandon before seeing value | Auth + onboarding both skippable; opener screens show seeded real listings | Skip affordances now say what they do: "Continue without an account" (auth), "Skip — see the catalog first" (onboarding) |
| 2 | Functional overlap with WhatsApp/Internshala; catalog can't beat zero-cost forwarding | **Share into Sagitta** flow shipped (ACTION_SEND → verified-catalog search → track in one tap) | Now also **taught in-app**: Settings → "Bring a listing in from your group" shows the 3 steps — the behaviour students invented is discoverable, not just present |
| 3 | Empty first-day value (deepest churn gate) | Real feed bundled + seeded on first launch; named instrument empty states wired for Applications, Explore, DSA tracker; all screens carry an empty state with a next action | Verified — no regression |
| 4 | Pro at $49.99/yr rejected before evaluation; monthly backfired | Season Pass leads the paywall; yearly badge derived from Play prices, never a fixed "Save N%" | **Paywall copy rewritten to the truth** (see below) |
| 5 | Superlatives distrusted; verifiable specifics trusted | Store listing, auth disclosure and privacy copy rewritten around falsifiable claims | Verified; one residual inflated claim found and fixed (below) |
| 6 | "Deletion is real" is a high-trust claim | AccountDeletionScreen + DataExporter existed **but were dead code — unreachable from the UI**; deletion left Room/DataStore intact on device | **Both wired into Settings and made real** (see below) |
| 7 | Offline determinism beats live freshness; students void-trust "real-time" claims | Offline-first architecture; store copy leads with OFFLINE FIRST; no "always up to date" claims found in user-facing copy | Verified |
| 8 | Habit replacement needs a daily ritual, not just replaced hoards | 07:00 briefing / 08:00 insight / 21:00 review / deadline sweep (max one ping, once per item); Home opens on readiness + deadlines; streak tracked | Verified |

## Fixed in this pass (06-11-2026)

1. **Account deletion is now reachable and actually deletes.**
   - Settings → *Delete Account* opens the (previously orphaned) deletion flow.
   - On success it now also wipes **Room tables, every DataStore key, and the
     app's files/cache** via `LocalDataWiper` — before this, a "deleted"
     account left the whole local record on the device.
   - Students who never signed in get the same wipe instead of a failure
     against a nonexistent account.
   - After deletion the app relaunches into the auth screen (task stack
     cleared); the wiped sign-in preference routes boot correctly.
2. **"Export My Data" works.** Settings → export launches the Storage
   Access Framework document picker and writes the JSON via the (previously
   orphaned) `DataExporter`. Outcome is reported in a snackbar; nothing
   transits the network.
3. **Paywall sells only what Pro does.** Removed: "Priority application
   tracking" (does not exist), "Advanced career insights & analytics"
   (not Pro-gated), "Ad-free experience" (the app has no ads — the Play
   Data Safety form says so), "Resume optimization" and "Custom skill gap
   analysis" (these are the free AI tools; only the daily cap is Pro),
   "Unlimited AI chat sessions" → now "AI answers beyond the 10-a-day free
   limit". Hero changed from "Supercharge your career journey with premium
   features" to the plain rule: *Finding, saving, tracking and practising
   are free and stay that way. Pro only lifts the 10-a-day AI cap.*
   Section renamed "Pro features" → "What Pro changes".
4. **Settings Pro subtitle corrected** ("AI Interview Coach, Oracle, Arena
   and more" → "Unlimited AI answers — the 10-a-day free cap is gone");
   those tools are not Pro unlocks.
5. **Privacy policy and store listing are now fully true.** Both already
   promised Settings → Export My Data and Settings → Delete Account; those
   paths exist and do what the pages say.

## Verified

- `:core:datastore :core:data :feature:settings :feature:paywall
  :feature:auth :feature:onboarding :assembleDebug` green (JDK 17).
- Unit tests green: `:feature:paywall`, `:core:data`, `:feature:onboarding`.

## Still open — needs a human, not more code

- **The simulation's own interview channel timed out (0/12 agents).** All
  findings rest on graph facts and post-level observation, not first-person
  interviews. Re-running MiroFish with a `ZEP_API_KEY` (and an LLM key, with
  the owner's consent for spend) is the validation gate; the current
  evidence cannot support stronger claims.
- **No real users yet.** Trust and churn conclusions are
  simulation-derived; the next evidence gate is the internal-testing
  cohort.
- **Play billing product `nexus_pro_season`** and the store-listing update
  are still required before release (see LAUNCH_CHECKLIST.md).
- Store screenshots show Home/Explore/AI/Growth/Me; none of the changed
  surfaces appear, so no re-shoot is forced — but the *Settings* screen
  visibly gains two rows and a group-share help item.
