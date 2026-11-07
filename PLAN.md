# Plan: Product Catalogue PoC

The spec we agreed before building. It is committed so reviewers can see how the AI was directed (see `AI_USAGE.md`).

> **Changes made during the build** (the plan below is kept as written, and these notes override it):
> - **Navigation:** type-safe **Navigation Compose** (`@Serializable` routes plus `toRoute()`), as I decided. The agent had first started on Navigation 3.
> - **HTTP client:** **Ktor** client with OkHttp instead of Retrofit. It's Kotlin-first, uses kotlinx.serialization natively, and `MockEngine` makes the client testable without a server. MockWebServer isn't needed.
> - **Search matching:** every word must *start* a word, not substring matching. Substring matching made "mens" match "womens".
> - **Enquiry response:** `201 { reference }`, not 202. The email has already been accepted by the time we respond, so nothing is pending.
> - **Lint:** Biome instead of ESLint. typescript-eslint doesn't yet support TypeScript 7.
> - **Second iteration, after my review:**
>   - **Android** became a 10-module clean architecture with convention plugins and Retrofit + OkHttp (HTTP disk cache), replacing the single module and Ktor.
>   - **ViewModels** became Flow-based, with use cases where there's logic and one class per file.
>   - **Backend:** an indexed `CatalogueSnapshot`, plus `PRODUCT_SOURCE`/`EMAIL_TRANSPORT` factories behind a `container.ts` composition root. See DECISIONS.md, "Architecture decisions".
> - **Search as you type:** a Flow `debounce` replaced the Search button, and the keyboard Search key still searches at once. The reason is in DECISIONS.md.
> - **Third iteration:** feature-wise modules. Each feature owns its data/domain/presentation packages, and `:core` keeps only shared code.

## Stack and why

| Layer | Choice | Why |
|---|---|---|
| Client | Android app, Kotlin, Jetpack Compose | 8 years of Android experience, so the time-box goes on judgement rather than learning a framework. Compose is the current standard UI toolkit. |
| Backend | Node.js + TypeScript, Fastify | Typed end to end, fast to start, built-in schema validation, `inject()` for HTTP tests without a running server. Runs on AWS ECS/Fargate or Lambda later. |
| Email | Nodemailer over SMTP, Mailpit locally | Mailpit runs in Docker Compose and acts as the "sales inbox" the reviewer can open at `http://localhost:8025`. No paid service or account. Amazon SES speaks SMTP too, so production only changes env vars. |
| Data | Fake Store API, behind the backend only | Required by the brief. The app never calls it directly. |

Every tool and library uses its latest stable version, looked up when it's added.

## Repository layout

```
officeworks-catalogue/
  backend/            Node + TS API (Dockerfile)
  android-app/        Single :app module
  docker-compose.yml  api + mailpit
  .env.example
  README.md  DECISIONS.md  AI_USAGE.md  PLAN.md  CLAUDE.md
```

## Boundaries (the seams that let it grow)

The brief says the data source, the look and feel, and the email provider will all change. Each one sits behind a seam:

- **Product data source** goes behind `ProductRepository` (backend). `FakeStoreProductRepository` is the first adapter, with an in-memory TTL cache because Fake Store is slow and sometimes unavailable. A CMS, PIM or database adapter can replace it without touching routes or the app.
- **Featured selection** goes behind `FeaturedProductsProvider`. For now it reads the configured IDs from env/config (`FEATURED_PRODUCT_IDS`).
- **Email provider** goes behind `EnquiryNotifier` / `EmailSender`. The SMTP adapter is used now, and SES/SendGrid adapters can follow.
- **Look and feel** lives in a Compose `CatalogueTheme` (colour, type and shape tokens). Screens use tokens, never hard-coded colours, so the creative work can be dropped in later.
- **API contract**: the backend returns its own DTOs, not Fake Store's shape, so changing the source doesn't break the app.

## Backend API

| Method | Path | Returns |
|---|---|---|
| GET | `/health` | `{ status: "ok" }` |
| GET | `/api/products/featured` | the 5 chosen products |
| GET | `/api/categories` | `[{ slug, name }]` |
| GET | `/api/categories/:slug/products` | `{ category, topRated: Product[], others: Product[] }` |
| GET | `/api/products?search=term` | products whose text matches |
| GET | `/api/products/:id` | one product, 404 if missing |
| POST | `/api/enquiries` | `202 { reference }` once the email is accepted |

`Product = { id, title, description, price, currency, category, imageUrl, rating: { rate, count } }`

Errors use one shape, `{ error: { code, message, details? } }`: 400 validation, 404 not found, 429 rate limited, 502 upstream or email failure.

Backend pieces: Zod schemas for input and config validation, `@fastify/rate-limit` on enquiries, a CORS lockdown, a body size limit, structured logs (pino), and config validated at start-up so the server fails fast on bad env.

## Android app

- **Architecture:** a single `:app` module, package-by-feature (`home`, `category`, `search`, `product`, `enquiry`) plus `data` and `designsystem`. Each feature has a `ViewModel` exposing `StateFlow<UiState>` (Loading / Content / Error with retry).
- **Libraries:** Compose Material 3, type-safe Navigation Compose, Hilt, Retrofit + OkHttp + kotlinx.serialization, Coil for images, coroutines.
- **Data:** a `CatalogueRepository` interface in the app with a `RemoteCatalogueRepository` (Retrofit) behind it. ViewModels depend on the interface, which keeps them easy to test.
- **Backend URL:** `BuildConfig.API_BASE_URL` defaults to `http://10.0.2.2:3000/` (the emulator's view of the host) and can be overridden with the Gradle property `apiBaseUrl`. Cleartext is allowed only for `10.0.2.2`/`localhost`, through a debug-only network security config. Release builds are HTTPS-only.
- **Screens:**
  - Home: featured row, category links, search bar.
  - Category: top-rated section, then "More in {category}".
  - Search results: an explicit Search button plus the keyboard search action.
  - Product details: an Enquire button.
  - Enquiry form: client-side validation, submit, then a Thank you screen.

## Assumptions (copy into DECISIONS.md)

1. **"I have chosen 5 top-rated products":** "chosen" means a person picks them, so the backend reads 5 configured product IDs (`FEATURED_PRODUCT_IDS`, with defaults in `.env.example`). Unknown IDs are skipped and logged rather than failing the page.
2. **Category "top rated":** the top 3 products in the category by `rating.rate`, with `rating.count` as the tie-break. All remaining products in that category appear under "additional products". The count is configurable.
3. **Category links:** Fake Store names contain spaces and apostrophes ("men's clothing"), so the backend produces URL-safe slugs and maps them back.
4. **Search:** server-side, case-insensitive, trimmed substring match on title, description and category. Results only change when the Search button is pressed. An empty or whitespace-only term is rejected on both client and server. Minimum 2 and maximum 100 characters.
5. **Enquiry form fields:** name (required), email (required, validated), phone (optional), message (optional, max 1000 characters). The product ID is attached, and the backend looks up the product title so the client can't spoof it.
6. **"Sales inbox receives an email":** the inbox is set by `SALES_INBOX_EMAIL`. Mailpit stands in for it locally. The customer gets no confirmation email; it isn't in the criteria and is listed as a next step.
7. **"Thank you" appears only after the backend accepts the email.** If sending fails, the customer sees an error they can retry, never a false Thank you.
8. **No persistence:** enquiries are not stored. The next step is persisting them with an outbox and a queue (SQS) so an email outage doesn't lose leads.
9. **Spam and abuse:** the enquiry endpoint is rate limited per IP and all input is validated. Captcha is out of scope.
10. **Currency is configuration, not code.** Fake Store prices have no currency. The backend reads `CATALOGUE_CURRENCY` (ISO 4217, default `AUD` for an assumed Australian launch) and puts it in every product DTO. The app formats the price with the device locale. A real catalogue source would supply the currency per product.

## Dropped or simplified (copy into DECISIONS.md)

- No auth, cart, pagination, favourites or admin UI. None of them are in the acceptance criteria.
- A single Android module rather than multi-module, to fit the time-box. The package boundaries line up with future modules.
- An in-memory cache, not Redis. That's fine for one instance; ElastiCache would come with horizontal scaling.
- No offline support or Room cache in the app.
- No infrastructure as code. A Dockerfile is the AWS-compatible artifact (ECS/Fargate), and CDK/Terraform is a next step.

## Tests (critical path first)

**Backend (Vitest):**
- Search matching rules.
- The category top-rated/others split.
- Featured ID resolution.
- Route tests via `fastify.inject()` with a fake repository and fake email sender:
  - each endpoint's happy path
  - 400 validation
  - 404
  - an upstream failure becoming 502
  - an enquiry triggering exactly one email to the sales inbox

**Android (JUnit + Turbine + coroutines-test):**
- ViewModel state tests against a fake repository for home, category, search and enquiry, including the validation and error paths.
- `RemoteCatalogueRepository` tests against MockWebServer, covering JSON mapping and error mapping.
- One Compose UI test for the enquiry form: invalid input, then valid input, then the Thank you screen.

## Run (target: under 10 minutes for a reviewer)

1. `cp .env.example .env`, then `docker compose up --build`. This starts the API on `:3000` and Mailpit on `:8025`.
2. Open `android-app` in Android Studio and run on an emulator, or run `./gradlew installDebug`.
3. Submit an enquiry, then open `http://localhost:8025` to see the email.

## Git and PR

`main` holds only an initial commit (`.gitignore`). All the work is a single commit on `feature/catalogue-poc`, with a PR into `main` that stays open. The PR description summarises the approach, the decisions, how to test, and the time spent.

## Time budget (actuals recorded in README)

| Activity | Budget |
|---|---|
| Planning | 20 min |
| Backend | 50 min |
| Android | 70 min |
| Tests | 30 min |
| Review and docs | 20 min |
