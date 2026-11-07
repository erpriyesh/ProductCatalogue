# AI usage

## Tools and models

- **Claude Code** (Anthropic), desktop app, model **Claude Opus 5.5**, for planning, code, tests, documentation and running the app on the Android emulator.
- The agent ran commands on my machine: npm, Gradle, adb, curl against Fake Store. I approved the plan and the decisions, redirected it where I disagreed, and reviewed the output.

## Agent instruction files

- [`CLAUDE.md`](CLAUDE.md), committed: the architecture rules, commands, and the things an agent must not do (call Fake Store from the app, hard-code colours, log customer data, and so on).

## Plan / spec

- [`PLAN.md`](PLAN.md) was written and agreed **before any code**. It covers the stack, the API contract, the seams, the assumptions and the test plan. I reviewed it and made three decisions myself: a separate repo, **configured** featured ids rather than "highest rated", and (at first) a single Android module.
- Where the build later changed (navigation, HTTP client, modularisation, ranking), the plan says so at the top, with the reason.

## Key prompts and moments that shaped the solution

The prompts below are **paraphrased** from informal chat messages. The decisions, their order and the reasoning are exactly as they happened.

1. **Choosing the stack from my strengths.**
   - *What I asked:* build the client as an Android app in Kotlin rather than React, with a Node.js + TypeScript backend, because Android is where I have 8 years of experience.
   - *Why it mattered:* it set the direction. The time went into judgement on a platform I can defend line by line.
2. **Challenging an assumption.**
   - *What I asked:* why are prices shown in AUD?
   - *What happened:* the agent had hard-coded AUD because the company interviewing me is Australian. That's a weak reason: the entrepreneur in the brief isn't that company, and Fake Store's data has no currency at all. Currency became configuration (`CATALOGUE_CURRENCY`), stated on every product and formatted for the device's locale.
3. **Overruling a library choice.**
   - *What I asked:* don't use Navigation 3; use type-safe Navigation Compose.
   - *What happened:* the agent had reached for Navigation 3 because another project on my machine uses it. I wanted the stable, mainstream choice reviewers will recognise: `@Serializable` routes read with `SavedStateHandle.toRoute()`.
4. **Reviewing and re-shaping the architecture.**
   - *What I asked:* after reviewing the first single-module version, I asked whether the app should be multi-module MVVM with clean architecture. I also asked for Retrofit + OkHttp + Flow, one class per file, fast search and a configurable backend. Later, I asked for modules **by feature**, each with its own data, domain and presentation layers.
   - *What happened:*
     - I raised it as a question, and the agent gave its view before changing anything. It pushed back on parts: no pass-through use cases, and search was already 1 to 3 ms server-side, so the speed gain had to come from an HTTP cache, not a rewrite.
     - Its first split was by *layer* (`:core:domain`, `:core:data`). I redirected it to modules *by feature*, so each feature owns its Retrofit service, repository, use cases and screens.
     - The project is submitted as a single commit, so this file, PLAN.md ("Changes made during the build") and DECISIONS.md are the record of these steps.
5. **Making "top rated" fair.**
   - *What I asked:* top rated should take into account both the stars and the number of people who rated. A product with a single 5.0 rating shouldn't count as top rated.
   - *What happened:*
     - A plain average would rank one 5-star review above 4.8 from 400 people. The category top-rated list and search order now use a **weighted (Bayesian) rating**, the approach IMDb uses: each product's average is blended with its peers' average, weighted by review count (`RATING_PRIOR_WEIGHT`).
     - I then asked whether the home page should use the same ratings. We kept the business's *chosen* five as the default, because the criterion says "I have chosen". We added an automatic weighted top 5 for when no products are chosen (`FEATURED_PRODUCT_IDS=` left empty).

## Where the AI got things wrong, and how it was caught

| What went wrong | How it was caught | Fix |
|---|---|---|
| Hard-coded **AUD** currency on weak reasoning | I questioned it | Made it config, with an ISO 4217 check at start-up |
| Picked **Navigation 3** by copying a pattern from another project | I rejected it | Type-safe Navigation Compose |
| **Search used substring matching**, so "mens" matched "**wo**mens clothing" | Found while writing search tests against real category names | Each search word must start a word (`matchesAllWords`), with a regression test |
| **Malformed JSON would have crashed the app.** `safeRequest` caught `SerializationException`, but Ktor wraps it in `JsonConvertException` | A unit test feeding broken JSON failed | Catch `ContentConvertException`, with a test |
| The **backend's internal error message** ("Product data source responded with HTTP 500") would have been shown to customers | Code review of the error path | The app maps error *codes* to its own customer wording; server messages are for developers |
| The first `server.ts` wired adapters through a **confusing closure over a not-yet-created `app`** | Self-review before running | Rewritten as an explicit `wire(config, logger)` composition root |
| `biome migrate` rewrote the lint config to `"preset": "none"`, **silently turning linting off** | Reading the migrated config | Set to `"recommended"` |
| `navigation-testing`'s `SavedStateHandle(route = …)` **fails in plain JVM tests** (needs Android `Uri`/`Bundle`) | 12 ViewModel tests failed | Robolectric for the three route-reading ViewModel tests, plus a JDK 21 `--add-opens` flag it needs |
| Compose UI tests **failed on API 36** because Compose's test library pulled an old Espresso | Running them on the emulator | Pinned Espresso 3.7.0, with a comment saying why |
| During the refactor, the home screen put **two composables in one lazy-list item**, which would have drawn the section header on top of the product row | Review of the generated screen before running | Each section wrapped in a `Column`, with a comment saying why |
| Gradle 9 **fails a module whose test task finds no tests**, which broke the build for `:core:network` (it has no tests of its own) | The first multi-module `./gradlew test` | Allowed it in the library convention plugin, with a comment saying why |
| The HTTP cache was **claimed but unproven** | My requirement that search be "fast" | Verified on the emulator: three repeated searches within the cache window reached the backend zero times |
| The Compose UI test used the **deprecated `createComposeRule`** | A compiler warning | Moved to `junit4.v2.createComposeRule` |
| The first modularisation was **by layer, not by feature** | I asked for feature-wise modules | Each feature module got its own data/domain/presentation packages; `:core:domain` and `:core:data` were removed |
| After the move, the **app failed to compile with Hilt errors** about deleted classes | `hiltJavaCompileDebug` failed | Stale generated code; a clean build fixed it. Nothing in the source was wrong |

## How I reviewed the output

- **I ran it myself, on a real device.**
  - I ran the backend from a terminal and the app from Android Studio on my own phone (Samsung Galaxy F14, Android 15). That surfaced two real issues:
    - without Docker, enquiries failed because no email server was running;
    - the phone lost its connection to the backend whenever wireless debugging reconnected.
  - Both are now explained in the README (`EMAIL_TRANSPORT=log`, and re-running `adb reverse`).
  - The app was also checked on an emulator against the live Fake Store data, including every acceptance criterion and the offline state.
- **I questioned design choices before accepting them.** For example:
  - why prices were in AUD;
  - why the agent picked Navigation 3;
  - why `build-logic` convention plugins were needed when there's already a version catalog;
  - whether "top rated" should consider the number of ratings;
  - whether the home page should use ratings too;
  - whether a Search button was the right UX.

  Each question led to a change, or to a recorded reason in DECISIONS.md.
- **I rejected or reversed output I didn't want.**
  - Compose previews for every screen were added at my request, then removed after review.
  - After testing the enquiry flow, I changed where the customer lands after Thank you: back to where they opened the product (Home, the category or search), instead of the product page.
  - The first modularisation (by layer) was replaced with modules by feature.
  - Documentation was cut back to what the brief asks for.
  - The prompts in this file were made accurate paraphrases rather than polished quotes.
- **I verified before submitting.**
  - All test suites pass (76 backend, 47 Android unit, 3 Android UI).
  - A fresh clone of the submitted commit builds and passes its tests.
  - `docker compose up --build` starts healthy containers, and a test enquiry arrived in the Mailpit inbox.
- **I checked the security basics:**
  - no secrets committed (`.env` is git-ignored);
  - input validated on the server;
  - HTML escaped in the email;
  - no customer data in logs;
  - cleartext HTTP only in debug builds.
