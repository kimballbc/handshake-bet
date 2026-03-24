# HandshakeBet — Architecture

This document describes the architectural decisions, patterns, and conventions used in HandshakeBet. It should be read before contributing and updated whenever a significant structural decision is made.

---

## Overview

HandshakeBet is a Kotlin/Compose Android app backed by Supabase. The architecture follows **MVVM with a clean layered structure**, built on three principles:

1. **Testability** — business logic lives in ViewModels and repositories, never in composables.
2. **Separation of concerns** — each layer has a single responsibility and depends only on the layer below it.
3. **Feature cohesion** — code is organised by feature, not by type. Everything belonging to a feature lives together.

---

## Package Structure

```
com.bck.handshakebet/
├── HandshakeBetApplication.kt   # @HiltAndroidApp entry point
├── MainActivity.kt              # Single activity — theme + NavGraph only
│
├── core/                        # App-wide infrastructure (not feature-specific)
│   ├── navigation/
│   │   ├── Screen.kt            # Type-safe nav destinations (sealed interface)
│   │   └── AppNavGraph.kt       # NavHost wiring all destinations
│   └── network/
│       └── di/
│           └── NetworkModule.kt # Hilt module — SupabaseClient + plugins
│
└── feature/                     # One sub-package per feature vertical
    ├── auth/
    │   ├── data/
    │   │   ├── model/           # DTOs / Supabase-specific data classes
    │   │   ├── remote/          # Supabase service calls
    │   │   └── repository/      # AuthRepositoryImpl
    │   ├── domain/
    │   │   └── repository/      # AuthRepository interface
    │   └── ui/
    │       ├── AuthViewModel.kt
    │       ├── LoginScreen.kt
    │       └── components/      # Screen-specific composables
    ├── bets/
    ├── friends/
    ├── home/
    ├── profile/
    ├── records/
    └── stats/
```

Each feature follows the identical `data / domain / ui` split described below.

---

## Layers

### Data Layer (`feature/*/data/`)

Responsible for all communication with external systems (Supabase).

- **DTOs** (in `model/`) map directly to Supabase table columns using `@Serializable` and `@SerialName`. They are never exposed above this layer.
- **Remote sources** (in `remote/`) contain the raw Supabase calls (`postgrest`, `auth`, `storage`). Each class has a single responsibility (e.g. `BetRemoteSource`).
- **Repository implementations** (in `repository/`) translate DTOs into domain models and implement the interface defined in the domain layer.

### Domain Layer (`feature/*/domain/`)

Defines the contract the rest of the app depends on.

- **Repository interfaces** — the only thing ViewModels and other callers import from this layer.
- **Domain models** — plain Kotlin data classes with no Android or Supabase dependencies, making them trivially unit-testable.
- **Use cases** are added here when business logic is complex enough to warrant extraction from the ViewModel (e.g. computing accolades in the stats feature).

### UI Layer (`feature/*/ui/`)

Owns all Compose UI and presentation logic.

- **ViewModels** hold a single `uiState: StateFlow<UiState>` and expose event functions (e.g. `onLoginClicked(email, password)`). They never import Compose.
- **Screen composables** observe `uiState` via `collectAsStateWithLifecycle()` and delegate all user events to the ViewModel. They contain no business logic.
- **Component composables** (in `components/`) are stateless, receive only the data they need, and are individually previewable.

---

## Dependency Injection — Hilt

Hilt is the single DI framework. Key conventions:

- `@HiltAndroidApp` on `HandshakeBetApplication` is the root component.
- `@AndroidEntryPoint` on `MainActivity` enables injection into the activity.
- `@HiltViewModel` on every ViewModel enables `hiltViewModel()` in composables.
- Feature-level Hilt modules live inside `feature/*/data/di/` and are installed in `SingletonComponent` for repositories and `ViewModelComponent` for ViewModel-scoped dependencies.
- `core/network/di/NetworkModule` provides the `SupabaseClient` and its plugins (`Auth`, `Postgrest`, `Storage`) as singletons.

**Rule:** Nothing outside of `@Module` classes should call `NetworkModule` directly. All dependencies arrive via constructor injection.

---

## Navigation

Navigation uses the **Navigation Compose 2.8+ type-safe API**.

- All destinations are defined as `@Serializable` objects/data classes in `Screen.kt`.
- `AppNavGraph.kt` contains the `NavHost` and owns the `NavHostController`.
- Screen composables receive navigation callbacks as lambdas — they never hold a `NavController` reference.
- Arguments are modelled as constructor parameters on data class destinations (e.g. `Screen.BetDetail(betId: String)`), providing compile-time type safety.

---

## State Management

Each ViewModel follows this pattern:

```kotlin
@HiltViewModel
class ExampleViewModel @Inject constructor(
    private val repository: ExampleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExampleUiState>(ExampleUiState.Loading)
    val uiState: StateFlow<ExampleUiState> = _uiState.asStateFlow()

    fun onSomeEvent() {
        viewModelScope.launch {
            _uiState.value = ExampleUiState.Loading
            repository.doSomething()
                .onSuccess { data -> _uiState.value = ExampleUiState.Success(data) }
                .onFailure { error -> _uiState.value = ExampleUiState.Error(error.message) }
        }
    }
}

sealed interface ExampleUiState {
    data object Loading : ExampleUiState
    data class Success(val data: ExampleData) : ExampleUiState
    data class Error(val message: String?) : ExampleUiState
}
```

Repositories return `Result<T>` to keep error handling explicit and consistent.

---

## Testing

### Philosophy

Tests are written alongside each feature — not after. The definition of done for every phase includes passing unit tests.

### Tools

| Tool | Purpose |
|---|---|
| JUnit 4 | Test runner |
| MockK | Mocking Kotlin classes and coroutines |
| Turbine | Testing `StateFlow` / `Flow` emissions |
| `kotlinx-coroutines-test` | `runTest`, `TestDispatcher` |
| `MainDispatcherRule` | Swap `Dispatchers.Main` in unit tests |

### Conventions

- **ViewModel tests** mock the repository interface and verify `uiState` transitions using Turbine's `test {}` block.
- **Repository tests** mock the remote source and verify correct mapping between DTOs and domain models.
- Test files mirror the source structure: `feature/auth/ui/AuthViewModelTest.kt` tests `feature/auth/ui/AuthViewModel.kt`.
- Test method names use backtick strings describing behaviour: `` `login with valid credentials emits Success state` ``.

### MainDispatcherRule

Every test class containing a ViewModel includes this rule:

```kotlin
@get:Rule
val mainDispatcherRule = MainDispatcherRule()
```

This replaces `Dispatchers.Main` with a `TestDispatcher` for the duration of the test. See `core/testing/MainDispatcherRule.kt`.

The choice of dispatcher matters:

| Dispatcher | Default? | Use when |
|---|---|---|
| `StandardTestDispatcher` | Yes | You need to observe intermediate states (e.g. `Loading` → `Success`). Coroutines suspend until you call `advanceUntilIdle()`. |
| `UnconfinedTestDispatcher` | No — pass explicitly | The ViewModel launches work in `init {}` and tests rely on that work completing before the test body runs. Coroutines execute eagerly. |

**Example — eager init (HomeViewModelTest):**
```kotlin
@get:Rule
val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())
```

**Example — observable Loading state (AuthViewModelTest):**
```kotlin
@get:Rule
val mainDispatcherRule = MainDispatcherRule()   // StandardTestDispatcher
```

---

## Credentials & Environment

Supabase credentials are never committed to source control. Add the following to `local.properties` (git-ignored):

```properties
SUPABASE_URL_DEV=your_dev_supabase_url
SUPABASE_KEY_DEV=your_dev_supabase_anon_key
SUPABASE_URL_PROD=your_prod_supabase_url
SUPABASE_KEY_PROD=your_prod_supabase_anon_key
```

Debug builds use `DEV` credentials; release builds use `PROD` credentials. Values are injected into `BuildConfig` at compile time by `app/build.gradle.kts`.

---

## Naming Conventions

| Artefact | Convention | Example |
|---|---|---|
| Screen composable | `<Feature>Screen` | `LoginScreen` |
| ViewModel | `<Feature>ViewModel` | `AuthViewModel` |
| UI state | `<Feature>UiState` | `AuthUiState` |
| Repository interface | `<Feature>Repository` | `AuthRepository` |
| Repository impl | `<Feature>RepositoryImpl` | `AuthRepositoryImpl` |
| Remote source | `<Feature>RemoteSource` | `AuthRemoteSource` |
| DTO | `Supabase<Entity>` | `SupabaseUser` |
| Domain model | `<Entity>` | `User` |
| Hilt module | `<Feature>Module` | `AuthModule` |
| Test class | `<Class>Test` | `AuthViewModelTest` |

---

## Decision Log

| Date | Decision | Rationale |
|---|---|---|
| 2026-03 | Hilt over manual DI | Eliminates `NetworkModule` service locator anti-pattern from v1; enables `@HiltViewModel` and removes repository params from screen composables |
| 2026-03 | Feature-based packages over type-based | Co-locates related code; easier to find, delete, or extract a feature |
| 2026-03 | Type-safe Navigation 2.8+ | Compile-time route verification; eliminates string-based route bugs from v1 |
| 2026-03 | `Result<T>` return type for repositories | Explicit, consistent error handling without checked exceptions |
| 2026-03 | Centred FAB nav bar — [My Bets][Feed][+][Records][Profile] | Intentional left-to-right user journey (mine → discover → create → history → me). FAB prominence encourages the core action. Stats demoted to a tab within Records (Phase 5) rather than a top-level nav item to keep the bar uncluttered. |
| 2026-03 | `Scaffold` `innerPadding` passed to `NavHost` modifier | Ensures `LazyColumn` content scrolls fully above the `BottomNavBar` without the last card being clipped. The padding is zero when the bottom bar is hidden (e.g. Login screen). |
