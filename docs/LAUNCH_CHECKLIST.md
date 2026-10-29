# Ascent — Launch Checklist

Everything buildable/configurable has been done and verified. What follows is the
exact remaining path to the Play Store. Steps 1–4 are account/payment actions that
require the account owner's money and identity; no tooling can substitute for them.

## Done (no action needed)

- Signed release AAB: `app/build/outputs/bundle/release/app-release.aab`,
  verified with `jarsigner -verify` against the upload key
  (`nexus-upload-key.jks`, alias `nexus-upload`; passwords in gitignored
  `local.properties`). Keystore backup: Desktop `NEXUS-UPLOAD-KEY-BACKUP-THIS-FILE.jks`.
- Firebase project `nexus-app-6acc7`: Auth (Email/Password + Google) enabled,
  Firestore rules + indexes deployed, Android app registered with debug + release
  SHA-1. `google-services.json` is local-only (gitignored — never commit it).
- App: brand "Ascent", seeded data, first-launch tour + in-app 20-second
  launch film, premium design system, unit tests green.

## Step 1 — OpenRouter API key (free, 5 minutes)

1. Create a free account at <https://openrouter.ai> (email + password).
2. Keys → Create Key → copy it.
3. Keep it handy for Step 3. **Never commit it.**

## Step 2 — Firebase Blaze plan (payment method required)

1. <https://console.firebase.google.com/project/nexus-app-6acc7/usage/details>
2. Upgrade to Blaze (Spark → Blaze). A payment method on the account is required.
3. Blaze itself charges only for usage beyond the free tier; the app's AI quota
   (40 calls/day free tier) keeps costs near zero at launch.

## Step 3 — Deploy the AI backend (can be run for you after Step 2)

```bash
cd backend
firebase functions:secrets:set OPENROUTER_API_KEY   # paste the Step-1 key
firebase deploy --only functions
```

## Step 4 — Play Console (USD 25 + identity verification)

1. <https://play.google.com/console> — register (one-time USD 25 fee, identity
   verification on the account owner).
2. Create app "Ascent" (package `com.nexus.app` — already fixed).
3. Upload `app-release.aab` to internal testing first; test on a real device.
4. Complete the store listing: screenshots (emulator captures on the Desktop are
   a starting point), the 20-second launch film (`brag-output/brag.mp4`) can be
   uploaded as the promo video, privacy policy URL, Data Safety form
   (data stored: email, display name, college, career progress — all in Firebase;
   no third-party tracking beyond Firebase Analytics).
5. Content rating questionnaire, target audience (13+), release to production.

## Regular opportunity updates (already built)

- App-side: opportunities + Firestore sync every 6 h (WorkManager, unmetered),
  LeetCode sync every 12 h, daily briefing 07:00, deadline notifications 10:00,
  proactive insight 08:00, streak check daily.
- Backend enrichment (a scheduled Cloud Function refreshing opportunities from
  public sources) can be added after Step 2 — the Firestore rules and collection
  shapes already support it.
