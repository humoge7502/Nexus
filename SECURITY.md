# Security Policy

## Supported versions

| Version | Supported |
|---|---|
| `main` (latest) | ✅ |
| Older tags | ❌ |

## Reporting a vulnerability

Please **do not open a public issue** for security problems. Report privately to
the maintainers with:

- A description of the issue and its impact
- Reproduction steps or a proof-of-concept
- Affected component (Android app, backend functions, Firestore rules, ML pipeline)
- Any suggested remediation

We aim to acknowledge within 72 hours and to ship a fix or mitigation within
14 days for critical issues.

## Security architecture (what protects user data)

| Control | Where | Notes |
|---|---|---|
| Secrets never ship in release APKs | `core/network` | `OPENROUTER_API_KEY` is empty in release; AI calls go through the backend proxy |
| Server-side AI quota | `backend/functions` | Per-user request + token clamp; prevents key abuse and cost blowups |
| Authenticated AI streaming | `streamAI` onRequest | Firebase ID-token verification, shared daily quota and prompt caps with `proxyAI`, upstream bodies logged server-side only; the client aborts the upstream call on disconnect |
| Token encryption at rest | `core/common/TokenCipher` | AES-256-GCM via Android Keystore, unique IV per write, fails closed |
| Server-authoritative Pro state | Firestore rules + callable | Clients cannot self-grant entitlements |
| Firestore least privilege | `backend/firestore.rules` (deployed; mirrored in `docs/firestore.rules`) | Per-user documents; cohort data aggregated/anonymized |
| Opt-in leaderboard only | `publicProfiles` rules | World-readable docs contain only chosen public fields (name, college, XP, streak); owner-writable, schema allowlisted, numeric ranking fields enforced on create and update |
| Secret scanning | CI `security` job | gitleaks on every push/PR |
| Release artifact assertion | CI `release-build` job | Fails if `sk-or-*` key material appears in the APK |
| Static analysis | CI `detekt` job | Blocking; catches swallowed exceptions, global coroutines, unsafe calls |

## Known residual risks

1. **`android:allowBackup`** — if enabled without backup rules, ADB/cloud backup
   can extract app data. Prefer `allowBackup="false"` or scoped
   `dataExtractionRules`; tokens are Keystore-encrypted regardless.
2. **Third-party API availability** — LeetCode stats are fetched by the
   `leetcodeStats` Cloud Function directly from LeetCode's public GraphQL API.
   The function validates the username, enforces an 8-second upstream timeout,
   caches responses in Firestore for 10 minutes, and serves up to 24-hour-old
   cached data when upstream is down. Clients never call leetcode.com directly.
3. **Firebase client API key** — legitimately present in `resources.arsc`;
   restrict it by package name + SHA-256 in Google Cloud Console.
4. **App Check** — not yet enforced; enabling Play Integrity prevents scripted
   abuse of the AI proxy.

## Dependency hygiene

- Versions are centralized in `gradle/libs.versions.toml`.
- Run `./gradlew dependencyUpdates` (Gradle versions plugin) or the CI
  dependency-review step before merging upgrades.
- Never merge a dependency with a known critical CVE without a documented
  mitigation in the PR.
