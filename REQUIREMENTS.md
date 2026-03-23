# HandshakeBet — Requirements

A rebuild of Handshake with intentional architecture, test coverage, and documentation from the ground up. Each phase must satisfy its definition of done before the next begins.

---

## Definition of Done (All Phases)

- [ ] Feature fully implemented (model → repository → ViewModel → screen)
- [ ] Unit tests written and passing for all ViewModels and repositories
- [ ] KDoc on all public classes, functions, and properties
- [ ] `REQUIREMENTS.md` updated with completed items
- [ ] `ARCHITECTURE.md` updated if new patterns were introduced

---

## Phase 0 — Foundation

> Infrastructure that every subsequent phase builds on.

- [x] Version catalog (`libs.versions.toml`) with all dependencies
- [x] Hilt configured — `@HiltAndroidApp` on `HandshakeBetApplication`
- [x] `@AndroidEntryPoint` on `MainActivity`
- [x] `NetworkModule` — `SupabaseClient`, `Auth`, `Postgrest`, `Storage` provided as singletons
- [x] `BuildConfig` wired for dev/prod Supabase credentials via `local.properties`
- [x] Feature-based package structure established (`core/`, `feature/`)
- [x] Type-safe navigation — `Screen` sealed interface, `AppNavGraph`
- [x] Test infrastructure — `MainDispatcherRule`, MockK, Turbine, coroutines-test
- [x] `ARCHITECTURE.md` written
- [x] `REQUIREMENTS.md` written

---

## Phase 1 — Authentication

> Login and sign-up. Everything else requires a signed-in user.

### Data Layer
- [ ] `SupabaseUser` DTO
- [ ] `AuthRemoteSource` — sign in, sign up, sign out, session check
- [ ] `AuthRepositoryImpl`

### Domain Layer
- [ ] `User` domain model
- [ ] `AuthRepository` interface

### UI Layer
- [ ] `AuthUiState` — Loading, Idle, Success, Error states
- [ ] `AuthViewModel` — login, signup, validation logic
- [ ] `LoginScreen` — login and sign-up in one screen with mode toggle
- [ ] Login form components: `EmailField`, `PasswordField`, `ActionButton`, `ErrorMessage`

### Tests
- [ ] `AuthViewModelTest` — login success, login failure, signup success, signup failure, input validation

### Documentation
- [ ] KDoc on all public classes
- [ ] `REQUIREMENTS.md` updated

---

## Phase 2 — Home Feed

> Main screen showing public bets and friend activity.

### Data Layer
- [ ] `SupabaseBet` DTO
- [ ] `BetRemoteSource` — fetch public bets, fetch friend bets
- [ ] `BetRepositoryImpl` (read methods only)

### Domain Layer
- [ ] `Bet` domain model
- [ ] `BetRepository` interface (read methods)

### UI Layer
- [ ] `HomeUiState` — tabs (Public / Friends), loading, empty, error states
- [ ] `HomeViewModel` — tab management, refresh logic
- [ ] `HomeScreen` — tabbed layout
- [ ] `PublicBetCard`, `FriendBetCard` components
- [ ] Bottom navigation bar (`BottomNavBar`)

### Tests
- [ ] `HomeViewModelTest` — tab switching, refresh, empty state, error state

### Documentation
- [ ] KDoc on all public classes
- [ ] `REQUIREMENTS.md` updated

---

## Phase 3 — Bets (Create & Manage)

> Creating new bets and managing active ones (accept, reject, complete).

### Data Layer
- [ ] `BetRemoteSource` extended — create, accept, reject, complete
- [ ] `UserRemoteSource` — fetch users for bet participant selection
- [ ] `BetRepositoryImpl` updated

### Domain Layer
- [ ] `BetRepository` interface updated with mutation methods
- [ ] `UserRepository` interface

### UI Layer
- [ ] `NewBetUiState`, `NewBetViewModel` — participant selection, form validation
- [ ] `NewBetScreen`, `HandshakeSlider` component
- [ ] `AccountUiState`, `AccountViewModel` — active bets, bet actions
- [ ] `AccountScreen`, `BetCard`, `OutcomeDialog` components

### Tests
- [ ] `NewBetViewModelTest` — form validation, participant search, submission
- [ ] `AccountViewModelTest` — bet loading, accept/reject/complete flows

### Documentation
- [ ] KDoc on all public classes
- [ ] `REQUIREMENTS.md` updated

---

## Phase 4 — Friends

> Friend requests, acceptance/rejection, and friends list.

### Data Layer
- [ ] `SupabaseFriendship` DTO
- [ ] `FriendshipRemoteSource`
- [ ] `FriendshipRepositoryImpl`

### Domain Layer
- [ ] `Friendship` domain model
- [ ] `FriendshipRepository` interface

### UI Layer
- [ ] `FriendsUiState`, `FriendsViewModel`
- [ ] `FriendsScreen`, `FriendCard`, `FriendRequestCard`, `AddFriendDialog`

### Tests
- [ ] `FriendsViewModelTest` — send request, accept, reject, list loading

### Documentation
- [ ] KDoc on all public classes
- [ ] `REQUIREMENTS.md` updated

---

## Phase 5 — Records & Stats

> Win/loss/draw records and achievements/accolades.

### Data Layer
- [ ] `UserRecordRemoteSource`
- [ ] `RecordsRepositoryImpl`, `StatsRepositoryImpl`

### Domain Layer
- [ ] `UserRecord` domain model
- [ ] `Accolade` domain model
- [ ] `RecordsRepository`, `StatsRepository` interfaces
- [ ] Accolade computation use cases (pure functions, heavily unit-tested)

### UI Layer
- [ ] `RecordsUiState`, `RecordsViewModel`
- [ ] `RecordsScreen`
- [ ] `StatsUiState`, `StatsViewModel`
- [ ] `StatsScreen`, `StatsAccolades` component

### Accolades (carried over from v1)
- [ ] Most Active Bettor
- [ ] Most Popular Bettor
- [ ] Biggest Risk Taker
- [ ] Most Competitive Duo
- [ ] Most Balanced Rivalry
- [ ] Streak Master
- [ ] The Peacemaker
- [ ] The Early Bird
- [ ] The Night Owl
- [ ] The High Roller
- [ ] The Safe Player
- [ ] The Social Butterfly
- [ ] The Risk Averse
- [ ] The Consistent One

### Tests
- [ ] `RecordsViewModelTest`
- [ ] `StatsViewModelTest`
- [ ] Unit tests for each accolade computation function

### Documentation
- [ ] KDoc on all public classes
- [ ] `REQUIREMENTS.md` updated

---

## Phase 6 — Profile & Settings

> Avatar selection, display name management, sign out.

### Data Layer
- [ ] `ProfileRemoteSource` — fetch/update profile, upload avatar
- [ ] `ProfileRepositoryImpl`

### Domain Layer
- [ ] `Profile` domain model
- [ ] `ProfileRepository` interface

### UI Layer
- [ ] `ProfileUiState`, `ProfileViewModel`
- [ ] `ProfileScreen`
- [ ] `AvatarSelectionScreen`, `UserAvatar` component

### Tests
- [ ] `ProfileViewModelTest` — avatar selection, display name update, sign-out

### Documentation
- [ ] KDoc on all public classes
- [ ] `REQUIREMENTS.md` updated

---

## Future Enhancements

- [ ] Push notifications for bet updates
- [ ] Social login options (Google, Apple)
- [ ] Custom avatar photo upload
- [ ] Friend activity feed
- [ ] Leaderboards
- [ ] Friend recommendations
- [ ] Pride balance system
- [ ] Public bet discovery / explore feed
