# Performance & Security Audit Checklist

## Memory & Leak Detection
- [ ] **LeakCanary** — Verify `LeakCanary.install()` in `Application.onCreate()`. Ensure it is only initialized in debug builds via `if (Debug.isDebuggerConnected())` or `BuildConfig.DEBUG`.
- [ ] **Timber** — Confirm `Timber.plant()` is called with a debug-only `DebugTree`. Production builds must not log PII; use a release `DebugTree` that drops or redacts logs.
- [ ] **ViewModels** — Verify `viewModelScope` is used for all coroutines; no un-cancelled `Job` or `launch` without scope. `clear()` must cancel child scopes.

## Compose Stability
- [ ] **@Stable / @Immutable** — All data classes used as Compose state are annotated `@Stable` or `@Immutable`. Verify no unnecessary recompositions via Layout Inspector.
- [ ] **LazyColumn keys** — Every `LazyColumn`/`LazyGrid` has a unique `key` derived from stable ID (e.g., `key = { item.id }`).
- [ ] **LazyColumn contentType** — `contentType` is provided when items have heterogeneous layouts to avoid unnecessary re-creation.
- [ ] **Compose compiler metrics** — Run with `-P compose:metrics=true` and review stability output for unstable classes.

## Database (Room)
- [ ] **Query indices** — All `@Query` WHERE, ORDER BY, and JOIN columns have corresponding `@Index` in `@Entity` definitions. Verify with Room's `Fts4` or EXPLAIN QUERY PLAN.
- [ ] **Transaction batching** — Bulk writes wrapped in `@Transaction` with `suspend` functions to avoid main-thread contention.
- [ ] **Migration testing** — All migration paths have `MigrationTestHelper` coverage.

## Network & Firestore
- [ ] **Firestore read limits** — Queries paginated with `limit()` / `limitToLast()`. No unbounded `addSnapshotListener` without `limit()`. Offline persistence size capped.
- [ ] **API keys** — All API keys injected via `BuildConfig` (local.properties) only; never hard-coded or in VCS. Keys are restricted by package name/SHA-1 in cloud console.

## Build & Obfuscation
- [ ] **ProGuard / R8** — Full mode enabled (`minifyEnabled true`, `useProguard false`). Rules cover:
  - Data classes used by Gson/Moshi/Kotlinx Serialization (`-keepclassmembers`)
  - Retrofit interfaces
  - Room entities and DAOs
  - Compose `@Stable` / `@Immutable` classes
  - Balloon library classes
  - Lottie model classes
- [ ] **Baseline Profiles** — `baseline-prof-gradle-plugin` generates baseline profiles for critical startup path (MainActivity, first screen, login). Verified with Macrobenchmark.

## Transport Security
- [ ] **Certificate pinning** — OkHttp `CertificatePinner` configured with SHA-256 hashes for production API hosts. Pin update strategy documented.
- [ ] **Firebase App Check** — App Check enforced on Firestore, Storage, and Functions via reCAPTCHA or Play Integrity. No unverified requests allowed.

## Coroutine Safety
- [ ] **Scope cleanup** — Every `ViewModel` uses `viewModelScope`. Custom scopes in `Application` or `Activity` are cancelled in `onDestroy()`. No `GlobalScope` usage.
- [ ] **`Dispatchers.Main`** — Network and disk operations dispatch to `Dispatchers.IO` / `Dispatchers.Default`; no `runBlocking` in production.
