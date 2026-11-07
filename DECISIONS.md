# Decisions and assumptions

## Assumptions (where the acceptance criteria leave gaps)

1. **"I have chosen 5 products to be top rated"** means a person picks them, so by default the home page shows a **configured list of product ids** (`FEATURED_PRODUCT_IDS`), in that order.
   - "Chosen" is the important word: featuring products is a merchandising decision (new stock, margin, seasons), not just "highest rated".
   - An id that no longer exists is skipped and logged as a warning, rather than breaking the home page.
   - **Automatic fallback:** if the business doesn't want to curate, it can set `FEATURED_PRODUCT_IDS=` (empty). The home page then shows the **top 5 across the catalogue by weighted rating**, the same rule as category "top rated" (assumption 2), so the number of reviews counts and a 5.0 from one review can't buy a place.
   - On the live data, automatic mode picks the same 5 products as the defaults, but ranks 4.6 from 400 reviews above 4.7 from only 130.
   - Later, the chosen list becomes a CMS field behind the same `CatalogueService.featured()`.
2. **"Top rated" on a category page means rated highly by many people, not just a high average.** It's the 3 best products by a **weighted (Bayesian) rating**, with the count configurable (`CATEGORY_TOP_RATED_COUNT`).
   - The weighted rating blends each product's average with its category's average, weighted by review count. A 5.0 from one review sits near the category average; 4.8 from 400 reviews stays near 4.8. It's the approach IMDb uses for its Top 250.
   - `RATING_PRIOR_WEIGHT` (default 25) is how many reviews it takes for a product's own average to count half.
   - I chose this over a hard minimum-review cut-off, which would hide good new products entirely.
   - "Additional products" are the rest of the category in the same order. Search results use the same ranking.
   - The **home page is not ranked**: it shows the 5 products the business *chose*, as the criterion says.
3. **Category links:** Fake Store category names contain spaces and apostrophes ("men's clothing"), so the backend publishes URL-safe slugs (`mens-clothing`) with display names.
4. **Search:**
   - It runs on the server and is case-insensitive.
   - A product matches when **every word of the term starts a word** in its title, description or category. "mens jacket" finds "Mens Cotton Jacket", and "jack" finds "Jacket". "mens" does **not** find "Women's clothing", which a plain substring search would.
   - **A deliberate departure from the criterion's wording:** the criterion says "WHEN I click the search button". I chose **search as you type** instead, because a separate Search button felt dated next to modern shopping apps.
     - Results appear about 300 ms after the customer stops typing (a Flow `debounce`).
     - The keyboard's **Search key** searches immediately, so that is the "search button".
     - The intent of the criterion, "I only see products that match the term", holds exactly.
     - If the business wants a visible button back, it's a small, self-contained change in `SearchBar` and `SearchViewModel.onSearchNow()`.
   - The home page shows a search bar that opens the Search screen with the keyboard up.
   - Terms must be 2 to 100 characters, and the server also checks. While the customer is still typing, a term that's too short shows a prompt rather than an error.
   - Results are ordered best-rated first (weighted rating).
5. **The enquiry form asks for** name and email (required), plus phone and message (optional). That's enough for sales to reply. The app sends only the **product id**: the backend looks up the title and price itself, so a client can't put a made-up product or price in front of the sales team.
6. **"The sales inbox will receive an email":** the inbox address is configuration (`SALES_INBOX_EMAIL`). Locally, Mailpit is the inbox. The email's Reply-To is the customer, so sales can just reply. No confirmation email goes to the customer, because the criteria don't ask for one.
7. **"Thank you" means the email really went.** The backend returns success only after the email provider accepts the message. If sending fails, the customer sees "We couldn't send your enquiry just now" and can retry. They never see a false Thank you. The form leaves the back stack after success, so pressing Back can't resubmit it. From Thank you, both **Continue browsing** and Back return the customer to **where they opened the product** (Home, the same category, or the same search results), not to the product they just enquired about.
8. **Currency is configuration** (`CATALOGUE_CURRENCY`, default `AUD`). Fake Store prices have no currency. The backend states one on every product, and the app formats it for the device's locale ("$55.99" in Australia, "A$55.99" elsewhere).
9. **Product images are media, not product data.** The app loads each image from the URL the backend returns, which is currently Fake Store's image host. All product *data* (titles, prices, ratings, categories, search) comes only through our backend, as the brief requires. Because the backend owns the URL, moving images to S3 + CloudFront, or proxying them, is a backend-only change.
10. **Fake Store is slow and sometimes unavailable.** The backend caches the catalogue for 5 minutes. If a refresh fails, it serves the last good copy and logs a warning. With nothing cached at all, it returns a clear 502 (`UPSTREAM_UNAVAILABLE`), and the app shows "Something went wrong on our side" with Try again. ("We can't reach the catalogue" is for when the app can't reach *our* backend.)

## Dropped or simplified, and why

| Item | Decision | Why |
|---|---|---|
| Enquiry persistence | Not stored | An enquiry exists only as the email. This is the biggest production gap. Next step: store it and send through an outbox/queue (SQS) so an email outage delays the email rather than failing the customer. |
| Use cases | Only where there is logic | Home loading (two calls in parallel, all or nothing), search validation, enquiry validation and submission. Simple reads go straight from ViewModel to repository interface: a pass-through use case per method would be ceremony with nothing to test. |
| Pagination | None | Fake Store has 20 products. The list responses are wrapped (`{ products: [...] }`) so `nextCursor` can be added later without breaking clients. |
| Offline cache in the app | None | Not in the criteria. `CatalogueRepository` is the seam where Room would go. |
| Strings | In Kotlin, not `strings.xml` | Faster in the time-box. Moving them to resources is the first step towards localisation. |
| Captcha / bot protection | Rate limit only | 5 enquiries per minute per IP, plus strict validation. Captcha or Play Integrity would come before launch. |
| Infrastructure as code | Dockerfile + Compose only | The image is AWS-compatible (ECS/Fargate, App Runner). CDK/Terraform comes once the hosting choice is made. |
| CORS | Not enabled | The only client is a mobile app, which CORS doesn't apply to. With CORS off, other websites can't call the API from a browser. Add an allow-list when a web client arrives. |
| Prices | `Double` | Display only, with no arithmetic. A cart would move to minor units. |
| Release signing | Debug key | There's no upload keystore for a PoC. It's documented in `app/build.gradle.kts`. |

## Architecture decisions

### Android
- **Feature modules, each with its own data / domain / presentation.**
  - A feature owns its whole vertical slice: its Retrofit service and DTOs, its repository interface and implementation, its use cases, and its screens. Changing or removing a feature touches one module.
  - `:core` holds only what several features share: the HTTP client, design system, UI state helpers, shared models and test helpers.
  - The history:
    1. one module, to fit the time-box;
    2. layer-based core modules (`:core:domain`, `:core:data`);
    3. this, at my request after review. With layer modules, every new feature had to spread across three shared modules, which is the opposite of easy expansion.
  - I chose **one module per feature with layered packages** over three modules per feature (`:feature:x:data` / `:domain` / `:presentation`). That would have doubled the module count for six screens. The layer rules are kept instead by:
    - `internal` visibility on all data classes;
    - the domain package having no Android, Retrofit or Hilt imports (checked; `@Inject` is `javax.inject`).

    If the team grows, a feature can be split into three modules without changing any code inside it.
- **Gradle convention plugins** (`build-logic/`, the Now in Android pattern). SDK levels, Compose, Hilt and test setup are defined once, so a feature module's build file is 2 lines. The version catalog says *which versions*; the convention plugins say *how each kind of module is built*.
- **MVVM with one immutable `StateFlow<UiState>` per screen**, derived with Flow operators:
  - a refresh trigger feeds `flatMapLatest`, which feeds `stateIn(Lazily)`;
  - retrying, or a newer search, cancels the request still in flight;
  - `Lazily` loads once per screen and keeps the data while the screen is on the back stack, so coming back from a product doesn't refetch or flash a spinner.
- **Repositories are `suspend`, not `Flow`.** A network request has one answer. Flow is used where values change over time (UI state, search requests). An offline Room cache would add Flow-returning reads.
- **Retrofit + OkHttp + kotlinx.serialization.** This is the industry-standard Android HTTP stack. `NetworkFactory` builds it for both Hilt and tests, so tests exercise the real configuration. OkHttp's **disk cache** honours the backend's `Cache-Control: max-age=60`: repeating a search or reopening a page within a minute costs no network call. That's verified on the emulator, where three repeated searches reached the backend zero times.
- **One top-level class per file.** Sealed hierarchies keep their subclasses nested (`DataResult.Success`), and private composables stay in their screen's file. That's the Kotlin style guide's rule for closely related declarations.
- **Errors are values.** `DataResult`/`DataError` come from one function (`safeApiCall`). Customer wording lives in `core:ui`, and field-level wording in the enquiry feature. The domain only says *what* is wrong (`EnquiryFieldError.REQUIRED`), never the words.

### Backend
- **Configuration-driven adapters.** `PRODUCT_SOURCE` and `EMAIL_TRANSPORT` pick implementations in small factories (`productSourceFactory.ts`, `emailSenderFactory.ts`). `container.ts` is the composition root, and `server.ts` only boots. Adding a provider means one class, one `case` and one enum value.
- **Indexed catalogue snapshot.** On each cache refresh the catalogue is turned into a `CatalogueSnapshot`:
  - a lookup by id;
  - per-category lists and the category list;
  - each product's search words normalised once.

  Requests then read from these structures instead of re-scanning. Search response time locally is 1 to 3 ms. Most of the wait a customer sees is the network round trip, which is why the app-side HTTP cache matters more.

## How the design supports growth beyond the PoC

The brief says the **data source, look and feel, and email provider will all change**. Each one sits behind a seam, so changing one is a new adapter plus one line of wiring:

- **Product data source:** routes and business rules depend only on `ProductRepository` (`backend/src/catalogue/productRepository.ts`). `FakeStoreProductRepository` is the only file that knows Fake Store exists. It also validates Fake Store's JSON with Zod, so upstream changes fail loudly at the boundary. To move to a PIM, CMS or database:
  1. write a class implementing the interface;
  2. add it to `productSourceFactory.ts`;
  3. set `PRODUCT_SOURCE`.

  Search and category queries are *in the interface*, so a real search engine (OpenSearch, Algolia) can do the work itself.
- **Our own API contract:** the backend returns its own `Product` shape (`price: { amount, currency }`, `rating: { average, count }`, `category: { slug, name }`), not Fake Store's. Changing the source can't break installed apps. The app also ignores unknown JSON fields, so the backend can add fields safely.
- **Email provider:** enquiry logic builds an `EmailMessage` and calls `EmailSender.send()`. SMTP works for Mailpit and for Amazon SES's SMTP endpoint through env vars alone. An SES-API or SendGrid adapter would be one new class.
- **Look and feel:** every colour, type style and shape comes from `CatalogueTheme` in `:core:designsystem` (`theme/Color.kt`, `Type.kt` and `Shape.kt`), and screens use theme tokens and shared components only. When the creative work lands, it's applied in that module. Dark mode already works.
- **Business rules are separate from transport:** `CatalogueService` (what is featured, how a category splits, search ordering) is plain TypeScript, tested without HTTP.
- **App architecture:**
  - Each feature's presentation layer depends only on its own domain interfaces. A Room-backed offline repository, or a different API, is a change in that feature's `data/` package, bound in its Hilt module (`CatalogueDataModule`, `EnquiryDataModule`).
  - Each feature publishes a `NavGraphBuilder` extension (`catalogueGraph`, `enquiryGraph`), so adding a feature such as favourites or a cart is a new module plus one line in `CatalogueNavHost`.
  - Screens are a stateful wrapper plus stateless `*Content` composables, so they can be previewed and tested.
  - Routes carry ids, so deep links and process death just work.
  - The backend URL is injected from `:app` (`@ApiBaseUrl`), so build flavours (staging, production) only change values there.
- **Operations:** config is validated at start-up, logs are structured JSON with request ids and no customer personal data, there's a health check, graceful shutdown, a non-root Docker user, security headers, a body size limit and a rate limit.

## About the time-box

The brief asks for 2 to 3 hours and honesty about the actual time (see "Time spent" in the README). The core (both apps, all five acceptance criteria, tests and docs) was built first. The time after that went on deliberate, reviewed iterations, listed here so they can be judged separately:

1. Restructuring the Android app into feature modules with data, domain and presentation layers.
2. A fair "top rated" ranking (weighted rating).
3. UI polish and search as you type.
4. Beginner-friendly setup docs, plus architecture and flow docs.

## What I would do next with another day

1. **Persist enquiries and send asynchronously:** an `enquiries` table (RDS) plus an outbox and SQS worker sending through SES, with retries and a dead-letter queue. That gives a CRM-ready record and no lost leads.
2. **A confirmation email to the customer** with their reference, and a branded HTML template.
3. **A CMS-backed featured list** (or a small admin endpoint) so the business can change it without a deploy.
4. **Infrastructure as code** (CDK): ECS Fargate behind an ALB, SES, Secrets Manager for SMTP credentials, CloudWatch alarms. Plus a CI pipeline running lint, typecheck, both test suites and the Android build on every PR.
5. **App polish:** move strings to `strings.xml`, pull-to-refresh, a Room cache for offline browsing, a deep link to a product, an accessibility pass with TalkBack, and screenshot tests of the design system.
6. **A contract test** that checks the backend's responses against the app's DTOs, so the two can't drift.
