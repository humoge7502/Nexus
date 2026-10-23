# Store5 Caching Layer — Implementation Plan

## Overview
Store5 (by MobileNativeFoundation) provides a repository pattern with built-in
caching, TTL management, and stale-while-revalidate semantics.

## Current State
Nexus repositories manually manage caching via Room + API calls in try-catch blocks.

## Implementation

### Step 1: Add dependency
```kotlin
// libs.versions.toml
store5 = "5.0.0"
store5 = { group = "org.mobilenativefoundations.store", name = "store5", version.ref = "store5" }
```

### Step 2: Create Store factory
```kotlin
class NexusStoreFactory @Inject constructor(
    private val internshipApi: NexusApiService,
    private val internshipDao: InternshipDao,
) {
    fun createInternshipStore(): Store<Int, List<Internship>> {
        return StoreBuilder.from(
            fetcher = Fetcher.of { page ->
                internshipApi.getInternships(page = page).body()?.data ?: emptyList()
            },
            sourceOfTruth = SourceOfTruth.of(
                reader = { page -> internshipDao.getAllInternships() },
                writer = { page, internships ->
                    internshipDao.deleteAll()
                    internshipDao.insertAll(internships.map { it.toEntity() })
                }
            )
        ).build()
    }
}
```

### Step 3: Replace repository patterns
Replace manual try-catch with Store.stream(StoreRequest.cached(0, refresh=true))

### Step 4: NonBlockingMemoryCache for UI state
```kotlin
val cache = NonBlockingMemoryCache<Int, List<Internship>>(
    maxSize = 20
)
// Wire into StoreBuilder
StoreBuilder.from(...)
    .cache(cache)
    .build()
```

## Benefits
- Automatic cache TTL (configurable per data type)
- Stale-while-revalidate (show cached data while refreshing)
- Deduplication of in-flight requests
- No more boilerplate try-catch in repositories

## Migration Strategy
1. Introduce `NexusStoreFactory` as a side-by-side module (no existing code changes)
2. Pick one screen (Browse) as a pilot — migrate its ViewModel to consume `Store<Int, List<Internship>>`
3. After pilot is stable, migrate remaining screens one at a time
4. Remove legacy repository classes and manual cache logic

## Testing
- Unit-test `Store` behavior via `TestStore` from the Store5 testing artifact
- Verify cache-hit vs. cache-miss flows with controlled `Fetcher` and `SourceOfTruth`
