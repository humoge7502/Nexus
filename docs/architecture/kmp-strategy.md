# Decompose KMP Strategy — Expanding NEXUS to iOS

## Goal
Share 70%+ of business logic between Android and iOS using Kotlin Multiplatform
(KMP) with the Decompose componentization library. The iOS app will use SwiftUI
for UI, consuming shared Kotlin components via Apple's Kotlin/Native interop.

## Why Decompose (vs. Compose Multiplatform)

| Factor | Decompose + SwiftUI | Compose Multiplatform |
|---|---|---|
| iOS UX | Native (SwiftUI) | Cross-platform (Compose) |
| Adoption risk | Mature (4+ years) | Rapidly changing API |
| Navigation | Built-in (ComponentContext, Router) | Needs Voyager/Decompose anyway |
| Team skills | Android team knows Decompose | New learning for all |
| App Store review | No risk (native UI) | Compose rendering may trigger review issues |

**Decision**: Decompose for components + navigation, SwiftUI for UI layer.

## Shared Module Architecture
```
:shared/
  src/
    commonMain/
      kotlin/
        com/nexus/shared/
          component/
            BrowseComponent.kt
            DetailComponent.kt
          model/
            Internship.kt
            Company.kt
          repository/
            InternshipRepository.kt
          util/
            CoroutineDispatchers.kt
    androidMain/
      kotlin/                            # Android-specific DI, API impls
    iosMain/
      kotlin/                            # iOS-specific Ktor engine, etc.
```

## Decompose Component (shared)
```kotlin
class BrowseComponent(
    componentContext: ComponentContext,
    private val repository: InternshipRepository,
) : ComponentContext by componentContext {

    private val _state = MutableValue(BrowseState())
    val state: Value<BrowseState> = _state

    private val scope = componentScope()

    fun onRefresh() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = repository.getInternships()
            _state.value = _state.value.copy(
                internships = result.getOrDefault(emptyList()),
                isLoading = false,
            )
        }
    }
}

data class BrowseState(
    val internships: List<Internship> = emptyList(),
    val isLoading: Boolean = false,
)
```

## SwiftUI Integration (iOS)
```swift
// ios/Nexus/Components/BrowseView.swift
import SwiftUI
import Shared  // KMP framework

struct BrowseView: View {
    let component: BrowseComponent
    @State private var state: BrowseState

    var body: some View {
        List(state.internships, id: \.id) { internship in
            Text(internship.title)
        }
        .overlay {
            if state.isLoading { ProgressView() }
        }
        .task { component.onRefresh() }
    }
}
```

## Navigating with Router (shared)
```kotlin
sealed class Screen {
    data object Browse : Screen()
    data class Detail(val id: String) : Screen()
}

class RootComponent(
    componentContext: ComponentContext,
) : ComponentContext by componentContext {

    val router = Router<Screen, Any>(
        initialConfiguration = Screen.Browse,
        initialBackStackSize = 1,
        handleBackButton = true,
    ) { screen, componentContext ->
        when (screen) {
            Screen.Browse -> BrowseComponent(componentContext, ...)
            is Screen.Detail -> DetailComponent(screen.id, componentContext, ...)
        }
    }
}
```

## Build Configuration

### Gradle (root)
```kotlin
plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
}

kotlin {
    androidTarget()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation("com.arkivanov.decompose:decompose:3.1.0")
            implementation("com.arkivanov.essenty:lifecycle:2.1.0")
            implementation("io.ktor:ktor-client-core:3.0.3")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
        }
        androidMain.dependencies {
            implementation("io.ktor:ktor-client-okhttp:3.0.3")
        }
        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:3.0.3")
        }
    }
}
```

## Rollout Strategy
1. **Phase 0 — Module creation** (Sprint 1)
   - Create `:shared` KMP module, move `model/` and `repository/` to `commonMain`
   - Android app builds and runs from shared code (no behavior change)

2. **Phase 1 — First component** (Sprint 2)
   - Port one screen (Browse) to Decompose `BrowseComponent`
   - Android: consume component in existing Compose UI (wraps component in `ComponentController`)
   - iOS: build SwiftUI `BrowseView` consuming same shared component

3. **Phase 2 — Expand coverage** (Sprint 3-4)
   - Port remaining screens: Detail, Search, Saved, Profile
   - Share networking (Ktor), serialization (kotlinx.serialization), and
     business logic (repositories) across platforms

4. **Phase 3 — Polish & CI** (Sprint 5)
   - Set up iOS CI on GitHub Actions (macOS runner + Xcode build)
   - Integration tests on both platforms
   - Performance profiling (Decompose component tree depth, state overhead)

## Risks & Mitigations

| Risk | Mitigation |
|---|---|
| iOS build times | Use Gradle remote cache; modularize shared module |
| Swift ↔ Kotlin interop friction | Keep API surface small: `Value<T>` + function calls |
| Team iOS expertise | Hire one iOS contractor for Phase 1; knowledge transfer |
| Decompose version churn | Pin to stable release; upgrade on 2-3 month cadence |
