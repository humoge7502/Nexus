# sagitta-ai — Blaze-free AI backend

A Cloudflare Worker that serves the same contracts as `backend/functions`
(`proxyAI`, `streamAI`, `deepResearchFn`, `leetcodeStats`, `health`) as plain
HTTPS, so the AI coach runs **without Firebase Blaze** — no payment method,
no per-request Firestore quota infra.

The Firebase path stays in the repo and remains the upgrade path: it carries
the stronger per-user Firestore quota (40 req/day) and `ai_proxy_logs`. This
Worker is the port that makes launch possible on a $0 budget.

## Deploy (one-time, ~3 minutes, free)

```bash
cd sagitta-ai
npx wrangler login          # opens the browser; click Allow
npx wrangler secret put OPENROUTER_API_KEY   # paste the key (the one in local.properties)
npx wrangler secret put FIREBASE_PROJECT_ID  # enter: nexus-app-6acc7
npx wrangler deploy
```

`wrangler deploy` prints the URL: `https://sagitta-ai.<your-subdomain>.workers.dev`.

Then point the app at it (build-time constant, gitignored):

```bash
# local.properties, at the repo root
AI_PROXY_URL=https://sagitta-ai.<your-subdomain>.workers.dev/proxyAI
```

…rebuild the AAB, and `AIStreamClient`, `AIRequestRouter`, `deepResearch`
and `leetcodeStats` all route through the Worker. `AI_PROXY_URL` must end in
`/proxyAI` — that suffix is how the client derives the sibling endpoints
(`/streamAI`, `/deepResearchFn`, `/leetcodeStats`).

## Endpoints

| Endpoint | Body | Response |
|---|---|---|
| `POST /proxyAI` | `{model?, messages, max_tokens?\|maxTokens?, temperature?}` | `{id, content, finishReason, usage{promptTokens, completionTokens, totalTokens}}` |
| `POST /streamAI` | same | OpenRouter SSE passthrough (deltas, usage, `[DONE]`) |
| `POST /deepResearchFn` | `{question, depth?}` | `{question, answer, sources[], followUps[], usedLiveSearch}` |
| `POST /leetcodeStats` | `{username}` | `{totalSolved, easySolved, mediumSolved, hardSolved, ranking, cached}` |
| `GET /health` | — | `{status, timestamp, version}` |

Every endpoint except `/health` requires `Authorization: Bearer <Firebase ID
token>` for `FIREBASE_PROJECT_ID`, verified RS256 against Google's public
JWKs — the same guarantee the callable functions gave via `context.auth`.

## Costs and limits

- Model: `nvidia/nemotron-3-ultra-550b-a55b:free` (single-model allowlist;
  clients cannot steer cost upward). Verified live 2026-11-08.
- Cloudflare Workers free tier: 100k requests/day — far above launch traffic.
- OpenRouter free tier: 50 free-model requests/day **per account** while the
  account holds <$10 credits. The app's own `AIRateLimiter` (10/day per
  device) sits under that; the account cap is the hard bound. Buying $10 of
  OpenRouter credits lifts it to 1000/day — optional, still no card needed
  until then.
- No server-side quota here (that needs Firestore, i.e. the Firebase path).
  With a free model there is nothing to over-spend; abuse is bounded by the
  account cap.

## Privacy note

Free-tier OpenRouter providers may retain prompts to improve their models.
`docs/privacy-policy.html` and the in-app disclosure string say exactly that —
if either changes, update them in the same commit (see the Data Safety table
in `docs/LAUNCH_CHECKLIST.md`).
