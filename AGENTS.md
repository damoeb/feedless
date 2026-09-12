# feedless

Middleware and web app for building deterministic, well-behaved web bots: turn a website into a feed, merge and filter feeds, track page changes, ship the result as RSS/Atom/JSON. A Kotlin/Spring Boot core exposes a GraphQL API; headless-Chromium *agents* connect back over a GraphQL subscription to run rendering jobs. One core serves several product verticals (feedless, rss-proxy, visual-diff, untold-notes, reader, page-change-tracker, upcoming, feed-reader, auction-alert).

## Critical Rules

Six rules, each one a mistake this repo actively invites. Everything else is in the spokes.

1. **Every Spring bean is profile-gated — a test that misses a profile fails as "no such bean".** Beans carry `@Profile("${AppProfiles.x} & ${AppLayer.y}")`, and tests must enumerate the same values in `@ActiveProfiles`. **Why:** the failure surfaces as a wiring error, so the instinct is to "fix" the bean or add a mock, which is wrong and makes it worse. See [`docs/rules/kotlin-spring.md`](docs/rules/kotlin-spring.md).
2. **Three generators write code here. Never hand-edit their output.** `schema.graphqls` → Kotlin DGS types (`org.migor.feedless.generated`) *and* `src/generated/graphql.ts` in `app-web`/`agent`/`frontend`; `openapi.yaml` → Kotlin Spring interfaces *and* the `feedctl` Go client (`packages/cli/internal/api`, via `go generate`, checked by `:packages:cli:lint`'s drift check); `FilterByExpression.jj` → the JavaCC filter parser. `graphql-api` and `http-api` also hold hand-written resolvers, controllers and mappers under `src/main/kotlin`; only their `build/generated/**` output belongs to the generators. **Why:** an edit survives locally and vanishes on the next build, so the bug reappears in CI with no diff to explain it. See both spokes.
3. **`ng serve` alone serves the wrong product.** The vertical comes from `src/config.json`, written by the `prestart:*` script. Always run `yarn start:<vertical>` in `app-web` / `npm run start:<app>` in `frontend`. **Why:** it builds and runs cleanly with whatever vertical was configured last, so the mistake looks like a code bug in an unrelated product. See [`docs/rules/angular-nx.md`](docs/rules/angular-nx.md).
4. **`./gradlew lint` lints TypeScript and Go, not Kotlin.** `lint` exists in `app-web`, `agent`, and `frontend` (TypeScript) and in `cli` (Go); no Kotlin module defines it. **Why:** a green gate reads as "backend style checked". It was never checked — Kotlin correctness comes from `test` and compilation.
5. **NEVER modify a shipped Flyway migration.** `packages/jpa-data/src/main/resources/db/migration/` runs to the version pinned in `spring.flyway.target` (`application-database.yaml`, currently `V92`); changes MUST be additive with the next free `V<n>__` number, and a new migration MUST raise that target in the same commit. **Why:** Flyway checksums applied migrations — an edit breaks every environment that already ran it, and it cannot be repaired forward; a migration added without raising the target is validated but never applied.
6. **Branch off `develop`, not `master`, and use Conventional Commits.** CI only runs on PRs targeting `develop`. Format: `type(scope): subject` — `feat` → minor, `fix` → patch, `BREAKING CHANGE:` footer → major. Scope is the module (`fix(app-web): …`, `feat(server-core): …`). **Why:** `master` is the release branch; a PR opened against it runs no CI at all.

## Essential Commands

| Command | Description |
|---------|-------------|
| `./gradlew lint test` | The CI gate and the Definition of Done. |
| `./gradlew :packages:domain:test :packages:graphql-api:test :packages:http-api:test :packages:jpa-data:test :packages:server-core:test` | Backend tests (`jpa-data` and `server-core` need Docker — Testcontainers/PostGIS). |
| `./gradlew :packages:server-core:bootRun` | Run the core with the `dev` profile. |
| `./gradlew buildImages` | Build the `app-web`, `server-core`, and `agent` images. |
| `docker compose up postgis` | Local database. Full stack in `docker-compose.yml`. |
| `yarn start:feedless` (in `app-web`) | Serve one vertical — never bare `ng serve`. |
| `npm run start:upcoming` (in `frontend`) | Serve one Nx app. |
| `yarn codegen` (in `app-web`, `agent`) | Regenerate the GraphQL TS client after a schema change. |
| `./gradlew :packages:cli:test` | Test the `feedctl` CLI. |

Prerequisites: JDK 21, Node 24 (`.nvmrc` per JS module), Docker, Gradle 8.9 via `./gradlew`, Go 1.27 (`packages/cli`).

## Modules

`packages/` holds 22 directories; **only 17 are Gradle modules.** `karma-gate`, `karma-gated-comments`, `ollama-engine`, `plausible-adapter`, and `document-classifier-models` are not — `./gradlew :packages:karma-gate:test` will fail.

| Module | Owns | Build |
|---|---|---|
| `server-core` | The Spring Boot application that assembles the modules: security composition (`SecurityConfig`, JWT filters, `TokenAuthenticator`), scheduler executors, the scraping pipeline and plugins, infrastructure behind `domain`'s outbound ports (`JwtTokenIssuer`, `PropertyService`, `PluginService`, `ScrapeService`, `AnalyticsService`, …), `ThrottleAspect`, `TestingEndpoint`. 114 Kotlin files; start at `FeedlessApplication.kt`. | Gradle |
| `domain` | Domain types, repository interfaces, all use cases and guards, outbound ports (`TokenIssuer`, `AppConfig`, `PipelinePlugins`, `Scraper`, `Analytics`, …), the security bridge (`injectCapabilitiesFrom*`), `@Throttled`, shared exceptions, `AppProfiles`/`AppLayer`. Spring annotations allowed; no generated GraphQL types, no `data.jpa`. Depends on no project module; changes ripple everywhere. | Gradle |
| `jpa-data` | JPA adapters: entities, DAOs, MapStruct mappers, PostGIS types. Flyway migrations (`src/main/resources/db/migration`) and persistence integration tests (Testcontainers/PostGIS; `PostgreSQLExtension` is its test fixture). | Gradle |
| `graphql-api` | `schema.graphqls` (**contract**: generates the Kotlin DGS types and every TS client) plus all DGS resolvers, `ProductDataLoader`, GraphQL mappers (MapStruct via kapt), `GraphQLExceptionHandler`, `GraphqlConfig`. Never depends on `server-core`. | Gradle (codegen, kapt) |
| `http-api` | `openapi.yaml` for `/api/v1` (**contract**: generates Kotlin Spring interfaces (`interfaceOnly`) and the `feedctl` Go client in `packages/cli`) plus the `/api/v1` controllers and the other web controllers (feed export, repository feeds, documents, attachments, payment callbacks, CLI install script, mail and report links), `HttpExceptionHandler`, `AppErrorController`. Never depends on `server-core`. | Gradle (codegen) |
| `feed-parser` | RSS/Atom/JSON/calendar parsing, plus the lenient `BrokenXmlParser`. | Gradle |
| `agent` | NestJS headless-Chromium worker. Dials out to the core over a GraphQL subscription; needs no public IP. Env vars in its `README.md`. | Gradle → yarn |
| `app-web` | Angular 20 + Ionic 8, one build per vertical. The shipped web UI. **yarn.** | Gradle → yarn |
| `frontend` | Nx 22 workspace, Angular 21. Apps `upcoming`, `feed-reader`, `auction-alert`; libs `@feedless/{components,core,geo,graphql-api,guards,testing}`. **npm.** | Gradle → npm |
| `document-classifier` | fastText classifier wrapper. Models/training data in `document-classifier-models`. | Gradle |
| `document-classifier-models` | Python: training data, `categories.yaml`, `build.sh`. | `build.sh` |
| `github-connector` | Git-backed document provider (GitHub accounts, repos as sources). | Gradle |
| `stripe-payments` | Stripe webhook controller and payment service. | Gradle |
| `mail-adapter` | Mailgun and native SMTP gateways, with a fallback config. | Gradle |
| `freemarker-templates` | Freemarker rendering service. | Gradle |
| `nominatim-proxy` | Standalone TS geocoding proxy. | Gradle → yarn |
| `cli` | `feedctl` — a `gh`-style Go CLI over `/api/v1`. Its HTTP client (`internal/api`) is generated from `http-api`'s `openapi.yaml`; a spec change without regeneration fails `lint`'s generate-drift check. | Gradle → go |
| `karma-gate`, `karma-gated-comments`, `ollama-engine`, `plausible-adapter` | Empty stubs. Do not assume they work. | none |

## Pre-Commit Checklist

- [ ] `./gradlew lint test` passes with exit code 0
- [ ] Generated code regenerated, not hand-edited, if a schema or grammar changed
- [ ] Flyway migrations additive only
- [ ] Code comments in English and as concise as possible: the code says how, a comment says only why
- [ ] Commit subject is `type(scope): …`
- [ ] Only this session's scope is staged

## Where to Find More

**When working on...**

- **Kotlin, Spring, DGS resolvers, profiles, JPA, backend tests** → [`docs/rules/kotlin-spring.md`](docs/rules/kotlin-spring.md)
- **Angular, Ionic, Nx, verticals, frontend tests** → [`docs/rules/angular-nx.md`](docs/rules/angular-nx.md)
- **Product overview and features** → [`README.md`](README.md)
- **Auth model** → [`docs/authentication.md`](docs/authentication.md)
- **Agent env vars and local run** → [`packages/agent/README.md`](packages/agent/README.md)
- **Feed format specs** → [`docs/rfcs/`](docs/rfcs/), [`docs/schemas/`](docs/schemas/)
- **Plans and the plot lifecycle** → [`docs/plans/README.md`](docs/plans/README.md)
- **`feedctl` build/test** → [`packages/cli/README.md`](packages/cli/README.md)

`CONTRIBUTING.md` and `docs/development.md` are placeholders — do not rely on them.

**Stack:** Kotlin / JDK 21 / Spring Boot / Netflix DGS / JPA + Flyway + PostGIS / Testcontainers / JUnit 5 · Angular 20–21, Ionic 8, Nx 22, NestJS, Node 24 · Go 1.27 · Gradle 8.9 · Docker

## Plot Config

- **Branch prefixes:** idea/, feature/, bug/, docs/, infra/
- **Plan directory:** docs/plans/
- **Active index:** docs/plans/active/
- **Delivered index:** docs/plans/delivered/
- **Main branch:** develop
- **Git host:** github
- **Tracker:** plot
- **Commit style:** conventional (Angular), drives semver
- **Definition of Done:** `./gradlew lint test`

---

*This file follows the AGENTS.md convention for cross-tool compatibility. `CLAUDE.md` points here.*
