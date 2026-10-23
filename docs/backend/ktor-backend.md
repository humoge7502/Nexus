# Ktor Backend Plan — Kotlin-Native Backend for Nexus

## Motivation
Nexus currently relies on third-party APIs (JOIN, externship aggregators).
A Ktor backend provides:
- **BFF (Backend-for-Frontend)** — aggregate and reshape third-party data
- **Server-side caching** — reduce API call volume to rate-limited sources
- **WebSocket push** — real-time internship alerts without polling
- **Shared Kotlin types** — DTOs defined once in `:shared` module, used by
  both server and Android client

## Project Structure
```
nexus-backend/
  build.gradle.kts
  settings.gradle.kts
  src/
    main/
      kotlin/
        com/nexus/backend/
          Application.kt            # entry point
          plugin/
            Networking.kt           # Ktor client for 3rd-party APIs
            Serialization.kt        # kotlinx.serialization config
            Database.kt             # Exposed + H2/Postgres setup
          route/
            InternshipRoutes.kt     # /api/v1/internships
            ReviewRoutes.kt         # /api/v1/reviews
            AuthRoutes.kt           # /api/v1/auth
          service/
            InternshipAggregator.kt # calls JOIN, Indeed, etc.
            CacheService.kt         # in-memory + Redis TTL cache
          model/
            InternshipDto.kt        # @Serializable data class
            ReviewDto.kt
      resources/
        application.conf            # Ktor config (port, DB, API keys)
  shared/                           # symlink or Git submodule
    :shared                          # Kotlin Multiplatform shared module
```

## Dependencies
```kotlin
// nexus-backend/build.gradle.kts (Gradle 8.x + Kotlin 2.0+)
plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    id("io.ktor.plugin") version "3.0.3"
}

dependencies {
    implementation("io.ktor:ktor-server-netty:3.0.3")
    implementation("io.ktor:ktor-server-content-negotiation:3.0.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.3")
    implementation("io.ktor:ktor-server-auth:3.0.3")
    implementation("io.ktor:ktor-server-websockets:3.0.3")
    implementation("io.ktor:ktor-client-okhttp:3.0.3")
    implementation("org.jetbrains.exposed:exposed-core:0.57.0")
    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("ch.qos.logback:logback-classic:1.5.13")

    // Shared module (same JVM target)
    implementation(project(":shared"))
}
```

## Application.kt
```kotlin
fun main() {
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) { json() }
        install(CallLogging)
        install(WebSockets)

        routing {
            internships()
            reviews()
            auth()
        }
    }.start(wait = true)
}
```

## Internship Aggregator Service
```kotlin
class InternshipAggregator(private val client: HttpClient) {
    suspend fun search(query: String, location: String): List<InternshipDto> {
        val jobs = client.get("https://api.join.com/search") { ... }
            .body<List<InternshipDto>>()
        val indeed = client.get("https://api.indeed.com/search") { ... }
            .body<List<InternshipDto>>()
        return (jobs + indeed).distinctBy { it.url }
    }
}
```

## Deployment Considerations
- **Container**: Docker image via `ktor-docker` plugin (`gradle ktorDockerBuild`)
- **Hosting**: Railway / Fly.io / self-hosted VPS — budget-friendly options
- **CI/CD**: GitHub Actions → build Docker image → push to registry → deploy
- **Observability**: Ktor metrics plugin + structured logging via Logback

## Gradual Rollout
1. Phase 0: Stand up Ktor with a single `/health` endpoint
2. Phase 1: `/api/v1/internship/search` — aggregates JOIN + Indeed, returns JSON
3. Phase 2: Replace Android client's direct JOIN API calls with calls to BFF
4. Phase 3: Add WebSocket endpoint for real-time internship alerts
5. Phase 4: Add `/api/v1/reviews` — persisted in Postgres via Exposed ORM
