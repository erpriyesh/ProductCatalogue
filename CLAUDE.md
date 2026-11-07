# Product Catalogue PoC: agent instructions

An Android app (Kotlin, Compose, multi-module) and a Node.js + TypeScript backend (Fastify). Customers browse, search and send enquiries. Read `README.md`, `PLAN.md` and `DECISIONS.md` before changing anything.

## Non-negotiables

- **The app never calls Fake Store** (or any product source) directly. All product data goes through `backend/`.
- **No secrets in git.** New backend configuration goes in `backend/src/config/config.ts` (validated with Zod) and is documented in `.env.example`.
- **Keep the seams.**
  - Product data goes through `ProductRepository`, chosen in `productSourceFactory.ts`.
  - Email goes through `EmailSender`, chosen in `emailSenderFactory.ts`.
  - Look and feel goes through `:core:designsystem`.
  - Only `container.ts` (backend) and the Hilt modules (app) wire concrete implementations.
- **Never log customer personal data** (name, email, phone, message). Log the enquiry reference and product id.
- **The server is the authority on validation.** The app mirrors the rules for instant feedback only. Keep `backend/src/enquiry/enquiry.ts` and `ValidateEnquiryUseCase` in step.
- **The Thank you screen appears only after the backend confirms the email was accepted.**
- Use the latest stable version of every tool and library, looked up, not recalled.
- One top-level class per file. Sealed subclasses stay nested; private composables stay with their screen.

## Backend (`backend/`)

- Node 22+, ESM, TypeScript strict.
- `server.ts` boots, and `container.ts` wires.
- Routes in `src/http/routes` contain no business logic. There's one error shape: `{ error: { code, message, details? } }`.
- Throw `AppError` subclasses (`src/shared/errors.ts`) for expected failures. Anything else becomes a generic 500.
- Commands: `npm test`, `npm run typecheck`, `npm run lint`, `npm run dev`.
- Tests use `buildApp()` + `app.inject()` with a fake `fetch` and `RecordingEmailSender` (`test/support/fixtures.ts`). No network in tests.

## Android (`android-app/`)

- **Feature modules own their vertical slice.** Each of `:feature:catalogue` and `:feature:enquiry` has:
  - `data/`: Retrofit service, DTOs, mappers, repository implementation and Hilt module, all `internal`;
  - `domain/`: models, repository interface, use cases;
  - `presentation/`: screens, UI state, ViewModels, `navigation/`.
- **Shared code only in `:core`:**
  - `:core:model`: shared models plus `DataResult`/`DataError`, pure Kotlin;
  - `:core:network`: OkHttp/Retrofit client, `safeApiCall`, error DTOs;
  - `:core:designsystem`: theme and components;
  - `:core:ui`: `LoadState` and error wording;
  - `:core:testing`;
  - `:app`: NavHost and config.

  Code moves to `:core` only when a second feature needs it.
- **Dependency rules:**
  - Features never depend on features.
  - Inside a feature, `presentation` uses `domain`, and `data` implements `domain`.
  - `domain/` has no Android, Retrofit or Hilt imports (`@Inject` comes from `javax.inject`).
- **Build:** module settings come from convention plugins in `build-logic/` (`catalogue.android.feature`, `catalogue.android.library`, `catalogue.jvm.library`, `catalogue.hilt`, `catalogue.android.compose`). Don't repeat SDK, Compose or Hilt setup in module build files.
- **Navigation:**
  - type-safe Navigation Compose with `@Serializable` routes, one per file, in each feature's `navigation/` package;
  - each feature exposes a `NavGraphBuilder` extension and `NavController.navigateTo…` helpers;
  - `:app/CatalogueNavHost` joins them;
  - ViewModels read arguments with `SavedStateHandle.toRoute<T>()`.
- **State:**
  - each ViewModel exposes one `StateFlow<UiState>`;
  - loads use a refresh trigger feeding `flatMapLatest { loadStateFlow { … } }`, then `stateIn(Lazily)`;
  - screens collect with `collectAsStateWithLifecycle()`;
  - repositories are `suspend` and return `DataResult`, never throwing.
- **Search** is search-as-you-type: `debounce` (300 ms), then trim, then `distinctUntilChanged`, then `flatMapLatest`. The keyboard Search key bypasses the debounce. Keep this behaviour covered by `SearchViewModelTest` (virtual time).
- **Use cases** only where there is logic. Don't add pass-through use cases.
- **UI:** use `MaterialTheme` tokens and `:core:designsystem` components. Each screen has a stateless `*Content` composable.
- **Commands:** `./gradlew assembleDebug`, `./gradlew test`, `./gradlew :feature:enquiry:connectedDebugAndroidTest` (emulator).
- ViewModel tests that read a route need `@RunWith(RobolectricTestRunner::class)`. Lazily started state needs `collectInBackground(viewModel.uiState)` from `:core:testing`.
