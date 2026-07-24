# SmartEvents (Android)

Kotlin/Jetpack Compose Android client for **Evenro**, an event-discovery/booking platform. Talks to the [Evenro backend](../backend/README.md).

## Stack

- **Kotlin** + **Jetpack Compose** (Material 3) — UI
- **Hilt** — dependency injection
- **Retrofit** + **OkHttp** + **kotlinx.serialization** — networking
- **Room** — local database (chat, reviews, wishlist, notifications)
- **DataStore (Preferences)** — session/auth state, onboarding flags
- **Coil 3** — image loading
- **Navigation Compose** — in-app navigation

See `gradle/libs.versions.toml` for exact versions.

## Getting started

### Prerequisites

- Android Studio (current stable)
- JDK 11
- An emulator or device running API 24+
- The [Evenro backend](../backend/README.md) running locally on port `4000`

### Run

1. Start the backend first (`cd ../backend && npm run dev`).
2. Open the `SmartEvents/` folder in Android Studio, let Gradle sync.
3. Run the `app` configuration on an emulator or device.

No additional configuration is needed for the emulator — see [Backend connection](#backend-connection) below.

### Build from the command line

```bash
./gradlew assembleDebug
./gradlew test              # unit tests
./gradlew connectedAndroidTest  # instrumented tests
```

## Backend connection

`core/di/NetworkModule.kt` hardcodes the API base URL:

```kotlin
private const val BASE_URL = "http://10.0.2.2:4000/api/"
```

`10.0.2.2` is the Android emulator's alias for the host machine's loopback interface (not `localhost`, which would resolve to the emulator itself). This points at the backend's default `PORT=4000`.

- **Running on a physical device**: replace `10.0.2.2` with your machine's LAN IP.
- **Different backend port**: update `BASE_URL` to match the backend's `.env` `PORT`.

Auth tokens are attached to outgoing requests via `core/network/AuthTokenInterceptor.kt`, sourced from the session stored in `core/datastore/EvenroPreferences.kt` (DataStore).

## Project structure

```
app/src/main/java/com/okbatech/smartevents/
  core/
    common/           Shared utilities (e.g. PasswordHasher)
    database/         Room database + DatabaseSeeder
    datastore/         EvenroPreferences (session, onboarding state)
    designsystem/       Shared Compose design system components
    di/                 Hilt modules (NetworkModule, etc.)
    navigation/         Navigation Compose graph
    network/            ApiService (Retrofit), DTOs, AuthTokenInterceptor, safeApiCall
  feature/
    auth/               Sign in/up, verification, reset password, interests, location
    booking/            Buy ticket, payment, tickets, "my events"
    events/              Event browsing, details, search, calendar, wishlist, reviews
    home/                Home feed
    map/                 Map view, location picker
    onboarding/          Splash, onboarding flow
    profile/             Profile, edit profile, organizer profile, menu
    social/              Chat, groups, notifications, messages
  ui/theme/              Compose theme (colors, typography)
  util/                  General utilities
```

Each `feature/<name>` module follows a `data/` (repository impls), `domain/` (models, repository interfaces, use cases), `presentation/<screen>/` (ViewModel + Composables) layering.

## Backend-backed vs. local-only features

Only some features are backed by the Evenro API — the backend currently exposes `auth`, `users`, `events`, and `bookings`. Repository implementations that call `ApiService` (via `core/network/ApiService.kt`) hit the real backend:

- `feature/auth/data/AuthRepositoryImpl.kt` — register, login, profile, user lookups
- `feature/events/data/EventRepositoryImpl.kt` — event listing, detail, create/update
- `feature/booking/data/BookingRepositoryImpl.kt` — bookings

Everything else — **chat, reviews, wishlist, notifications** (`feature/social/data/*`, `feature/events/data/ReviewRepositoryImpl.kt`, `feature/events/data/WishlistRepositoryImpl.kt`) — is backed by the local Room database (seeded via `core/database/DatabaseSeeder.kt`), since the backend has no corresponding routes yet.

## API contract

`core/network/ApiService.kt` defines the Retrofit interface consumed by repositories; `core/network/ApiModels.kt` defines the request/response DTOs. These map directly onto the backend's route handlers — see the [backend README](../backend/README.md#api) for the endpoint list. Field names in `EventDto`/`UserDto` are expected to match the backend's `mapEvent`/`mapUser` output exactly; coordinate any backend response-shape change with this app.

Network calls are wrapped in `core/network/safeApiCall` / `runCatching`, surfacing failures as `Result`/`ApiResult` rather than throwing.
