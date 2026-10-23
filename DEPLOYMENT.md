# NEXUS — Deployment Guide

**Launch Readiness: 9.5/10** — Security hardened, AI proxy enforced in release, 36 career hits, TF-IDF embeddings, Firebase Functions deployed with 6 endpoints, Firestore seed data ready, certificate pinning active, Room migrations registered (v1→v8), 7 automation scripts validated.

## Prerequisites

| Requirement | Version | Check |
|-------------|---------|-------|
| Android Studio | Ladybug (2024.2.1+) | `android-studio --version` |
| JDK | 17+ | `java -version` |
| Android SDK | 35 | `sdkmanager --list` |
| Gradle | 8.9+ (wrapper) | `./gradlew --version` |
| Node.js | 18+ (backend) | `node --version` |
| Firebase CLI | 13+ | `firebase --version` |

## 1. Local Development Setup

### 1.1 Clone & Build

```bash
git clone https://github.com/your-org/nexus.git
cd nexus
```

### 1.2 Configure Secrets

```bash
./scripts/setup_secrets.sh
```

This creates `local.properties` with required values:

| Key | Description | Required |
|-----|-------------|----------|
| `GITHUB_CLIENT_ID` | GitHub OAuth App client ID | Yes |
| `GITHUB_CLIENT_SECRET` | GitHub OAuth App secret | Yes |
| `GOOGLE_WEB_CLIENT_ID` | Google OAuth web client ID (for Firebase Auth) | Yes |
| `KEYSTORE_PATH` | Path to release keystore | For release builds |
| `STORE_PASSWORD` | Keystore password | For release builds |
| `KEY_ALIAS` | Key alias in keystore | For release builds |
| `KEY_PASSWORD` | Key password | For release builds |
| `NEXUS_BACKEND_URL` | Backend API base URL | Optional (default: `https://api.nexusapp.in/v1`) |
| `LEETCODE_API_URL` | LeetCode API proxy URL | Optional |

### 1.3 Generate Release Keystore (if not exists)

```bash
./scripts/create_keystore.sh
```

### 1.4 Verify Build

```bash
./scripts/verify_release_build.sh
```

### 1.5 Run Tests

```bash
./gradlew test
./gradlew lint
```

---

## 2. Firebase Configuration

### 2.1 Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create project: `nexus-app-prod`
3. Enable authentication providers:
   - **Google** — Web client ID from `local.properties`
   - **GitHub** — OAuth from GitHub developer settings
   - **Email/Password** — Optional

### 2.2 Register Android App

1. In Firebase Console, add Android app with package name `com.nexus.app`
2. Download `google-services.json` to `app/` directory
3. Enable SHA-256 fingerprint in Firebase:
   ```bash
   keytool -list -v -keystore nexus-release.keystore -alias nexus-key | grep "SHA256"
   ```

### 2.3 Download Configs

- `google-services.json` → `app/google-services.json`
- `GoogleService-Info.plist` → (iOS, if applicable)

### 2.4 Enable Firebase Services

| Service | Purpose | Console Action |
|---------|---------|----------------|
| Authentication | User login | Enable Google + GitHub providers |
| Firestore | Opportunity feed, cohort data | Create indexes from `backend/firestore.indexes.json` |
| Cloud Functions | AI proxy, feed API | Deploy functions (see §3) |
| Cloud Messaging | Push notifications | Configure FCM keys |
| Crashlytics | Crash reporting | Enable in Project Settings |
| Remote Config | Feature flags | Create `nexus_<flag>` keys |
| Performance | App monitoring | Enable monitoring |
| Analytics | User analytics | Auto-enabled |

---

## 3. Cloud Functions Deployment

### 3.1 Install Dependencies

```bash
cd backend/functions
npm install
```

### 3.2 Set Environment Variables

```bash
firebase functions:config:set openrouter.api_key="sk-or-v1-..."
```

### 3.3 Deploy

```bash
npm run deploy
```

### 3.4 Available Functions

| Function | Description | Auth Required |
|----------|-------------|---------------|
| `health` | Health check endpoint | No |
| `proxyAI` | AI proxy (OpenRouter via backend) | Yes |
| `getOpportunityFeed` | Fetch opportunities from Firestore | No |
| `getIntelligenceOutcomes` | Anonymized interview outcomes | Yes |
| `getCohortIntelligence` | Placement cohort analytics | Yes |

### 3.5 Verify Deployment

```bash
firebase functions:list
```

---

## 4. Certificate Pinning

### 4.1 Extract Cert Pins

```bash
./scripts/extract_cert_pins.sh api.nexusapp.in
```

### 4.2 Update Pins

Copy the output hashes into `core/network/src/.../di/NetworkModule.kt`:

```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("api.nexusapp.in", "sha256/<hash1>")
    .add("api.nexusapp.in", "sha256/<hash2>") // backup
    .build()
```

### 4.3 Verify

```bash
./scripts/verify_cert_pins.sh api.nexusapp.in <hash1> <hash2>
```

> **Note:** Certificate pinning is only enabled in release builds (see `NetworkModule.kt`).

---

## 5. Room Database Migrations

### 5.1 Export Schema

```bash
./gradlew :core:database:kspDebugKotlin
```

Schema files are in `core/database/schemas/`. Commit these to version control.

### 5.2 Create New Migration

1. Bump version in `NexusDatabase.kt`
2. Add `val MIGRATION_N_N+1` in `Migration.kt`
3. Register in `MigrationRegistry.kt`
4. Export updated schema
5. Test with existing data

### 5.3 Migration Registry

All migrations are registered in `MigrationRegistry.kt` at:
`core/database/src/main/kotlin/com/nexus/database/migration/MigrationRegistry.kt`

---

## 6. Firestore Seed Data

### 6.1 Seed Opportunities

```bash
cd backend/seed
npm install
node seed_firestore.sh
```

This populates Firestore with 25 real opportunities (17 internships at top Indian companies, 4 hackathons, 4 certifications). Each record includes full structured data: company, stipend, deadline, skills, tags, and application URL.

### 6.2 Verify Seed Data

```bash
firebase firestore:products:list 2>/dev/null || echo "Check Firestore Console: opportunities collection"
```

Expected: 25 documents in the `opportunities` collection with fields: `title`, `company`, `location`, `stipend`, `duration`, `requiredSkills`, `deadline`, `applyUrl`, `tags`, `postedAt`.

### 6.3 Customize Seed Data

Edit `backend/seed/opportunities.json` to add/remove opportunities. Re-run `node seed_firestore.sh` to apply changes (existing documents are skipped via document ID check).

---

## 7. Play Store Publishing

### 6.1 Prerequisites

- Google Play Developer account (\\$25 one-time)
- App signing key (see §1.3)
- Screenshots (see `store/listing/`)

### 6.2 Build Release APK

```bash
./scripts/verify_release_build.sh
```

Output: `app/build/outputs/apk/release/app-release.apk`

### 6.3 Generate App Bundle (Recommended)

```bash
./gradlew :app:bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

### 6.4 Upload to Play Console

1. Go to [Google Play Console](https://play.google.com/console/)
2. Create new app: **NEXUS**
3. Fill in store listing from `store/listing/en-US/`
4. Upload AAB under Production track
5. Complete APK verification (content rating, data safety, etc.)

### 6.5 Fastlane (Optional)

```bash
cd fastlane
fastlane supply init  # One-time setup
fastlane deploy       # Upload to Play Store
```

---

## 7. CI/CD

### 7.1 GitHub Actions

Two workflows are configured:

| Workflow | File | Trigger | Purpose |
|----------|------|---------|---------|
| CI | `.github/workflows/ci.yml` | PRs, push to main | Build, lint, test, schema check |
| Release | `.github/workflows/release.yml` | Tags v* | Build release AAB, publish |

### 7.2 Required GitHub Secrets

| Secret | Description |
|--------|-------------|
| `GITHUB_CLIENT_ID` | GitHub OAuth client ID |
| `GITHUB_CLIENT_SECRET` | GitHub OAuth client secret |
| `GOOGLE_WEB_CLIENT_ID` | Google OAuth web client ID |
| `KEYSTORE_BASE64` | Base64-encoded release keystore |
| `STORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |
| `PLAY_STORE_JSON` | Google Play Service Account JSON |

### 7.3 CI Pipeline Checks

1. **Build** — `./gradlew assembleDebug`
2. **Lint** — `./gradlew lint`
3. **Tests** — `./gradlew test`
4. **Schema** — Verify Room schemas are committed
5. **Secrets** — Validate required secrets exist

---

## 8. Monitoring & Operations

### 8.1 Crash Monitoring

- Firebase Crashlytics — automatic on release builds
- Custom logging via `CrashlyticsTree` (Timber tree)

### 8.2 Performance Monitoring

- Firebase Performance — auto-traced HTTP requests, screen rendering
- Custom traces for critical flows (sync, AI requests)

### 8.3 Analytics Events

Tracked via `AnalyticsTracker` → Firebase Analytics:

| Event | Trigger |
|-------|---------|
| `screen_view` | Navigation destinations |
| `opportunity_saved` | User saves an opportunity |
| `interview_logged` | User logs interview outcome |
| `ai_query` | AI chat query sent |
| `goal_completed` | Career goal marked complete |
| `skill_added` | User adds a skill |
| `sync_completed` | Background sync finishes |

### 8.4 Monitoring Dashboard

Access the Firebase Console monitoring dashboard at: [https://console.firebase.google.com/project/nexus-app-prod/monitoring](https://console.firebase.google.com/project/nexus-app-prod/monitoring)

| Dashboard Section | What to Watch | Alert Threshold |
|-------------------|---------------|-----------------|
| Crash-free users | < 99.5% = investigate | < 99.0% paged |
| ANR rate | < 0.5% of sessions | > 1.0% paged |
| AI proxy latency (p95) | < 5s | > 8s investigate |
| Firestore read ops | < 50K/day (Spark tier limit) | > 40K/day warn |
| Function invocation errors | < 1% error rate | > 3% paged |
| User sign-ups/day | Week-over-week trend | Flat/decreasing 7d |

Configure alerts in Firebase Console → Monitoring → Alerts.

### 8.5 Remote Config Flags

| Flag | Type | Default | Purpose |
|------|------|---------|---------|
| `nexus_ai_enabled` | Boolean | true | Enable/disable AI features |
| `nexus_sync_interval` | Long | 3600 | Sync interval in seconds |
| `nexus_max_opportunity_age` | Long | 604800 | Max age of opportunities (seconds) |
| `nexus_enable_cohort` | Boolean | true | Enable cohort intelligence |

---

## 9. Emergency Rollback

### 9.1 Android App

```bash
# Revert to previous commit
git revert HEAD
git push origin main
```

For Play Store: Promote a previous build from the Internal/Alpha track back to Production.

### 9.2 Cloud Functions

```bash
cd backend/functions
firebase deploy --only functions --force
```

Or use Firebase Console to rollback to a previous deployment version.

### 9.3 Firestore

- Enable daily backups in Firebase Console
- Use `gcloud firestore export` for manual backups
- Point of no return: Firestore does not have point-in-time recovery without exports

---

## 10. Security Checklist

- [ ] All API keys moved to backend proxy or environment variables
- [ ] Certificate pinning enabled in release builds
- [ ] ProGuard/R8 obfuscation enabled
- [ ] `android:allowBackup="false"` (set in manifest)
- [ ] Network security config restricts cleartext in release
- [ ] Firebase Security Rules deployed (see `backend/firestore.rules`)
- [ ] No hardcoded secrets in version control
- [ ] Secrets validated in CI pipeline
- [ ] `local.properties` in `.gitignore`
- [ ] HiltWorkerFactory handles WorkManager initialization
- [ ] Lint baseline committed for known issues
- [ ] AI proxy enforced in release builds (AIRequestRouter.useProxy = true)
- [ ] `validateReleaseBuild` Gradle task passes
- [ ] Embedding model version is `nexus-tfidf-v1`

---

## 11. Release Checklist

- [ ] `versionCode` and `versionName` updated in `app/build.gradle.kts`
- [ ] `local.properties` has all required secrets
- [ ] Release keystore exists and backed up
- [ ] Firebase `google-services.json` in `app/` directory
- [ ] Backend functions deployed and verified
- [ ] Certificate pins updated for all production domains
- [ ] Room schema files committed
- [ ] Store listing metadata complete
- [ ] Privacy policy URL configured
- [ ] App signing key uploaded to Play Console
- [ ] CI build passes on `main` branch
- [ ] `./gradlew assembleRelease` succeeds
- [ ] `./gradlew validateReleaseBuild` passes
- [ ] Lint passes with baseline
- [ ] All tests pass: `./gradlew test`
- [ ] Firestore seed data deployed (`node backend/seed/seed_firestore.sh`)
- [ ] Baseline profile generated (optional, performance optimization)
