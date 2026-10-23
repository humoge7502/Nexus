# Supabase Migration Plan — Adding Supabase Alongside Firebase

## Motivation
Nexus currently uses Firebase for auth, Firestore for real-time data, and
Firebase Cloud Messaging for push. Supabase offers:
- **Postgres** (relational, migrations, JOINs — unlike Firestore)
- **Row-Level Security (RLS)** — permissions live in the DB, not in app code
- **Open source** — self-hostable, no vendor lock-in
- **Real-time subscriptions** via Realtime + PostgreSQL replication

## Strategy: Side-by-Side, Not Rip-and-Replace

Firebase will NOT be removed immediately. Both backends coexist during a
transition period. New features (e.g., company reviews, interview experiences)
will use Supabase first. Existing features (auth, saved internships) migrate
later.

## Architecture

### Supabase Client Module
```
app/
  supabase/                    # <-- new module
    SupabaseModule.kt          # Provides SupabaseClient via DI
    SupabaseConfig.kt          # URL + anon key from BuildConfig
    auth/
      SupabaseAuthDataSource.kt
    reviews/
      ReviewRepository.kt      # New feature — Supabase-only
    internship/
      SupabaseInternshipDataSource.kt  # Mirror of Firebase version
```

### Dependency
```kotlin
// libs.versions.toml
supabase-kt = "3.1.0"
supabase-kt = { group = "io.github.jan-tennert.supabase", name = "supabase-kt", version.ref = "supabase-kt" }
ktor-client-android = "3.0.3"

// build.gradle.kts (supabase module)
implementation("io.github.jan-tennert.supabo:supabase-kt:$supabaseKtVersion")
implementation("io.github.jan-tennert.supabo:ktor-client-ktor:$supabaseKtVersion")
// BOM manages: Realtime, Postgrest, Auth, Storage modules
```

### SupabaseClient Singleton
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {
    @Provides
    @Singleton
    fun provideSupabaseClient(@ApplicationContext context: Context): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }
    }
}
```

## Feature Comparison

| Capability | Firebase | Supabase |
|---|---|---|
| Database type | NoSQL (Firestore) | Relational (Postgres) |
| Queries | Limited compound queries | Full SQL + JOINs |
| Migrations | Manual (console) | Versioned SQL files |
| Auth providers | Email, Google, Apple | Email, Google, Apple, 20+ more |
| Realtime | Yes (snapshot listeners) | Yes (Postgres replication) |
| Pricing | Usage-based, can spike | Flat-rate, predictable |
| Self-hostable | No | Yes |

## Migration Phases

### Phase 1: Foundation (Sprint 1)
- Create `app/supabase/` module
- Add Supabase Kotlin SDK + Ktor client deps
- Implement `SupabaseAuthDataSource` mirroring existing Firebase Auth
- Ship behind a feature flag — no user-facing change

### Phase 2: New Feature on Supabase (Sprint 2)
- Build **Company Reviews** feature entirely on Supabase
- Schema: `reviews( id, company_id, user_id, rating, body, created_at )`
- RLS policy: users can only CRUD their own reviews
- Expose via `ReviewRepository` consumed by new Circuit screen

### Phase 3: Migrate Read-Heavy Screens (Sprint 3-4)
- Move **Browse** internships to Supabase Postgrest queries
- Keep Firebase Firestore as fallback — compare latency and correctness
- Feature flag: toggle between Firestore and Postgrest datasource

### Phase 4: Auth Unification (Sprint 5)
- Add Supabase Auth alongside Firebase Auth
- Users can link accounts (same email → same profile)
- After 1 month: prompt Firebase-only users to migrate

### Phase 5: Firebase Retirement (Future)
- Once all features run on Supabase and 95%+ users have linked auth:
  - Remove Firebase dependencies
  - Archive Firestore data to Supabase
  - Remove Firebase modules from build

## Rollback Plan
- Every Supabase-backed feature has a Firebase fallback behind a feature flag
- Feature flags controlled by Remote Config (Firebase) initially, migrated to
  Supabase Config table in Phase 4
- If Supabase has an outage, flip flag to fallback — no app update needed
