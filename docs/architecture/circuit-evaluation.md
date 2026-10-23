# Circuit Framework Evaluation — NEXUS V2

## Overview
Circuit (by Slack) is a Kotlin-centric, Compose-first architecture framework that
unifies navigation, state management, and UI into a single pattern. It is designed
for Android and multiplatform from the ground up.

## Why Circuit for NEXUS V2

### 1. Unidirectional Data Flow (UDF)
Circuit enforces a strict UDF loop: `UiState → UiEvent → Circuit → new UiState`.
This matches NEXUS's existing MVI-like pattern but removes the boilerplate of
manually wiring ViewModels, StateFlows, and event handlers.

### 2. Built-in Navigation
Circuit's `Navigator` and backstack handling replace NEXUS's current Navigation
Component setup. Screens become `Screen` + `Presenter` + `Ui` triplets — no
separate NavGraph XML or `NavHost` composable wiring.

```kotlin
// Example Circuit screen
data class BrowseScreen(val filter: String) : Screen {
    @Parcelize
    data class State(
        val internships: List<Internship> = emptyList(),
        val isLoading: Boolean = false,
    ) : Parcelable

    sealed interface Event {
        data object RefreshClicked : Event
        data class InternshipClicked(val id: String) : Event
    }
}

@ContributesMultibinding
class BrowsePresenter @Inject constructor(
    private val store: Store<Int, List<Internship>>,
) : Presenter<BrowseScreen.State> {
    @Composable
    override fun present(): BrowseScreen.State {
        // No ViewModel needed
    }
}

@ContributesMultibinding
class BrowseUi : Ui<BrowseScreen.State> {
    @Composable
    override fun Content(state: BrowseScreen.State) {
        // Pure @Composable — no lifecycle awareness needed
    }
}
```

### 3. Testability
`Presenter` and `Ui` are pure functions — no Robolectric or Hilt injection
required in unit tests. Circuit provides `testPresenter` and `testNavigator` helpers.

### 4. KMP Ready
Circuit targets Android, iOS, and desktop. Adopting it now avoids a rewrite
when iOS support begins (see `kmp-strategy.md`).

## Trade-offs & Risks

| Concern | Mitigation |
|---|---|
| Learning curve for team | Start with one screen pilot, pair-program |
| Framework adoption risk | Circuit is Apache 2.0, 2k+ GitHub stars, maintained by Slack |
| DI integration (Hilt) | Circuit supports `@ContributesMultibinding` with Anvil or manual Dagger bindings |
| Navigation flexibility | Custom `Navigator` implementations possible if Circuit's built-in is insufficient |

## Pilot Plan
1. Rewrite **Browse** screen as a Circuit triplet (Screen + Presenter + Ui)
2. Keep all other screens on existing architecture
3. Evaluate: testability, build time, dev experience after 2 weeks
4. Decision gate: proceed to full V2 migration or revert

## Migration Path (if approved)
- Phase 1 (2 weeks): Pilot Browse screen
- Phase 2 (4 weeks): Migrate Detail, Search, Saved screens
- Phase 3 (2 weeks): Remove Navigation Component + legacy ViewModels
- Phase 4 (ongoing): Multiplatform enablement (see `kmp-strategy.md`)
