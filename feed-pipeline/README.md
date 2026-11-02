# Sagitta opportunity feed

Real, verified opportunities for Indian students. No mock rows, no invented deadlines.

## What it publishes

- `feed/opportunities.json` — the live feed served to the app.
- `feed/meta.json` — generation time, per-source status, verification counts.
- `app/src/main/assets/opportunities_seed.json` — offline first-run snapshot (same payload).

## Sources

| Source | What it contributes | Key |
|---|---|---|
| Greenhouse job boards | Early-career roles at global and Indian product companies | none |
| Lever job boards | Early-career roles, including Paytm India | none |
| Devpost | Open online hackathons with real submission deadlines | none |
| Devfolio | Indian student hackathons with registration deadlines | none |
| Official programmes | GSoC, Outreachy, LFX, MLH, C4GT, GSSoC, SoB, SoK, AICTE, NATS, PM Internship, NVIDIA, Google Scholarships, Season of Docs, MLSA | none |

Every item carries `sourceName`. Every item also gets a `verifiedAt` timestamp when the
apply/registration URL answered with a success status during the run. Items whose links
return 404/410/connection failures are dropped; links that block bots (401/403/429) are kept
without a verified stamp.

## Eligibility rules

- Title must look early-career (intern, trainee, graduate, junior, new grad, fresher) and must
  not contain seniority words (senior, staff, principal, manager, director, …).
- Location must be India, or genuinely location-agnostic. "Remote - US" is rejected.
- Deadlines must be in the future. Missing deadlines are published as rolling rather than invented.
- Duplicate roles are removed by normalized URL, then by company + title. At most 3 live roles
  per company, so one employer cannot flood the feed.

## Run it

```bash
node --test feed-pipeline/test/*.test.mjs
node feed-pipeline/src/index.mjs --verify
```

`--no-verify` skips URL checks for offline development. `--allow-short` bypasses the minimum
feed-size safety gate; never use it for a published feed.

## Updates

`.github/workflows/feed-refresh.yml` runs every 6 hours and on demand. It tests the pipeline,
revalidates every link, enforces quality gates, and commits the refreshed feed. A failed run
never overwrites the last good feed. The app syncs every 6 hours through WorkManager and falls
back to the bundled snapshot when offline.

Curated programme deadlines are deliberately `null`: these run on annual or rolling cycles,
and showing an invented date would be worse than showing none.
