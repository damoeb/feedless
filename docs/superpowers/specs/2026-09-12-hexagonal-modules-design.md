# Hexagonal modules: use cases in `domain`, adapters in their own modules

**Status:** design, approved in conversation 2026-09-12, revised the same day after the implementation inventory (`docs/superpowers/plans/2026-09-12-hexagonal-modules-inventory.md`). Lands on `feature/feed-ctl` as part of PR #86.

**Goal:** finish the refactoring that `docs/superpowers/plans/2026-07-17-http-api.md` started for `/api/v1` and stopped at ports. All use cases and guards live in `domain`; all GraphQL resolvers live in `graphql-api`; all HTTP controllers live in `http-api`; persistence lives with its tests and migrations in `jpa-data`; `server-core` is the Spring Boot application that assembles them and hosts infrastructure adapters. This supersedes that plan's rule that GraphQL resolvers stay in `server-core`.

**Non-goal:** behaviour changes. Every step is a move plus the minimum indirection the module boundaries force.

## Decisions

- **Spring is allowed in `domain`.** Use cases move with `@Service`, `@Profile`, `@Transactional` and `@Cacheable` unchanged. `domain` gains the Spring Boot BOM, `spring-context`, `spring-tx`, `spring-security-core`, `spring-security-oauth2-client`, `spring-security-oauth2-jose`, `micrometer-core`, jsoup, and the `kotlin.spring` plugin (without it `@Transactional`/`@Cacheable` proxies break). Profile gating needs no rework.
- **Vertical, feature by feature.** Each step moves one feature's use cases, guards, resolvers, mappers, controllers and tests together, and ends green.
- **Outbound ports for infrastructure.** A use case that needs something only `server-core` has gets an interface in `domain`, named after the capability; `server-core` implements it.
- **The `*Port` interfaces of `/api/v1` go away.** Once use cases are in `domain`, controllers and resolvers call them directly.
- **The security bridge moves to `domain`** (`injectCapabilitiesFromSecurityContext` / `injectCapabilitiesFromJwt`, package `session`), so resolvers, controllers and `server-core`'s filters share one copy.
- **`RepositoryHarvester` moves to `domain` with its own scrape-result types.** It runs on generated GraphQL types today; `domain` gets mirror types behind the `Scraper` port, and its tests are rewritten on those types.
- **Flyway migrations move to `jpa-data`**, unchanged (same checksums). `spring.flyway.target` stays in `server-core`'s `application-database.yaml`.
- **Types `feed-parser` shares with use cases move to `domain`** (`JsonItem`, `JsonFeed`, `JsonAttachment`, `JsonPoint`, `FeedType`, `HttpResponse`, `FeedUtil`, `HtmlUtil`), because `feed-parser` depends on `domain`. The duplicate declarations of `HttpResponse`, `FeedUtil` and `HtmlUtil` in `server-core` go away.
- **`@Lazy` stays.** Ports remove compile-time module cycles, not bean-graph cycles; the four `@Lazy`-broken cycles keep their `@Lazy`.

## Modules

| Module | Holds | Depends on |
|---|---|---|
| `domain` | domain types, repository interfaces, all use cases and guards, outbound ports, security bridge, `@Throttled`, shared exceptions | libraries only |
| `graphql-api` | schema and DGS codegen, all resolvers, `ProductDataLoader`, GraphQL mappers, `GraphQLExceptionHandler`, `GraphqlConfig`, `DgsCustomContext`, adapters for ports whose types are generated GraphQL types | `domain`, DGS, Spring Security, MapStruct (kapt) |
| `http-api` | OpenAPI codegen, `/api/v1` controllers, the other web controllers (feed export, repository feeds, documents, attachments, payment callbacks, CLI install script, mail and report links), `HttpExceptionHandler` | `domain`, `feed-parser` |
| `jpa-data` | JPA adapters, Flyway migrations, persistence integration tests | `domain` |
| `mail-adapter`, `stripe-payments`, `freemarker-templates`, `github-connector`, `feed-parser` | outbound adapters, unchanged | `domain` |
| `server-core` | Spring Boot application, security composition (`SecurityConfig`, JWT filters, `TokenAuthenticator`), scheduler executors, infrastructure behind outbound ports, `ThrottleAspect`, `TestingEndpoint`, `application-database.yaml` | all of the above |

Rules:

- `graphql-api` and `http-api` never depend on `server-core`; Gradle enforces it because the dependency does not exist.
- `server-core` references adapter modules only to assemble them. The reverse import `QueuedHarvestExecutor` → `HttpScrapeFlowMapper` becomes the domain port `StoredFlowParser`, implemented in `http-api`.
- Use cases do not import generated GraphQL types or `data.jpa`; mapping to DTOs happens in the adapters.
- Packages stay unchanged (`org.migor.feedless.*`), so `@SpringBootApplication`'s scan keeps finding beans; `graphql-api` also gets an auto-configuration like `http-api`'s `HttpApiAutoConfiguration`.

## Outbound ports

| Port (domain) | Implemented by | Needed by |
|---|---|---|
| `session.TokenIssuer` | `JwtTokenIssuer` | secrets, mail-auth, report, feed, license |
| `common.AppConfig` | `PropertyService` | document mapping, payment, repository, feed |
| `pipeline.PipelinePlugins` | `PluginService` | document, report, plugins resolver |
| `message.Notifications` | new `NotificationsAdapter` over `TelegramBotService` + `MessageService` | user, document |
| `scrape.Scraper` | new `ScraperAdapter` over `ScrapeService` | harvester, feed |
| `feed.FeedParser` | `FeedParserService` | feed |
| `scrape.WebToFeed` | `WebToFeedTransformer` | feed |
| `pipeline.ItemFilter` | `CompositeFilterPlugin` | feed |
| `common.HttpFetcher` | `HttpService` | feed |
| `agent.AgentDirectory` | `AgentService` + `AgentRegistry` | status, agent resolver |
| `analytics.Analytics` | `AnalyticsService` | document, feed, attachment controllers, server config |
| `source.StoredFlowParser` | `HttpScrapeFlowMapper` (http-api) | harvest executor |
| `document.DocumentQueryParser` | graphql-api | repository feeds controller |

The session-cookie calls are already covered by the existing `SessionTokenPort`. Exact signatures are in the inventory.

## Order

| # | Step | Includes |
|---|---|---|
| 0 | Cross-cutting | `domain` build setup; `feed-parser` shared types and server-core `Exceptions.kt` to `domain`; `@Throttled` unification; security bridge to `domain`; `GraphQLExceptionHandler`, `GraphqlConfig`, `DgsCustomContext` and all `api/mapper` files to `graphql-api` (with the value types they need moved to `domain`); `graphql-api` build setup and auto-configuration; `AppConfig`; Flyway migrations and persistence test application to `jpa-data`; `Mother` merge into `domain` test fixtures |
| 1 | plan | plan use case, guard, constraints service, resolver, controller; the coroutine-context helpers (`userId()` …) |
| 2 | feature, product, order | their use cases and resolvers, `ProductDataLoader` (repository method instead of the JPA DAO), `UserGuard`, `PaymentController` |
| 3 | user, group, auth, secrets | user, group, auth, connected-app and secret use cases, `OrderResolver`; `TokenIssuer`, `Notifications` |
| 4 | session, mail-auth | mail authentication, one-time passwords, their resolvers |
| 5a | repository block, ports first | scrape-result types, `Scraper`, `PipelinePlugins`, `Analytics`, `StoredFlowParser`, `DocumentQueryParser`, value helpers; harvester tests rewritten on domain types |
| 5b | repository block, moves | repository, source, document, harvest use cases and guards, `RepositoryHarvester`, pipeline services, `InboxService`, resolvers, controllers, persistence tests to `jpa-data` |
| 6 | attachment, annotation, report | use cases, guards, resolvers, `AttachmentController`, `ReportController`, `MailController`, `ApiUrls` |
| 7 | feed, license, status, agent, scrape, plugins | `FeedService`, `LicenseUseCase`, `ServerStatusService`, remaining resolvers, `FeedController`, `CliInstallScriptController`; remaining ports |
| 8 | Wrap-up | `HttpExceptionHandler` and, if separable, `AppErrorController` to `http-api`; AGENTS.md (module table, rule 2, rule 5 migration path, `cli` is a Gradle module); `docs/rules/kotlin-spring.md` |

## Tests

- Unit tests without a Spring context move with their code: use-case tests to `domain`, resolver tests to `graphql-api`, controller tests to `http-api`.
- Persistence integration tests move to `jpa-data`, which gains a test application, Testcontainers and `PostgreSQLExtension` as a test fixture.
- Tests that need the whole application stay in `server-core`.
- The two `object Mother` definitions merge into one `domain` test fixture (`java-test-fixtures`), together with the Mockito helpers.

## Verification per step

- All modules compile; `domain`, `graphql-api`, `http-api`, `jpa-data` and `server-core` tests pass.
- The total number of executed tests does not drop (stale results deleted first, then counted from JUnit XML).
- Files move with `git mv`.
- After steps 5b and 8: the e2e smoke test (`:packages:cli:e2eTest`) against a freshly built core image. After step 8: `./gradlew lint test`.
- Each step gets its own commits, a task review, and a push to PR #86; a whole-branch review closes the refactoring.

## Out of scope (in `docs/tasks.md`)

- Inconsistent profile gating.
- Renaming `LinceseResolver`; splitting `LicenseUseCase` into provider and use case.
- Moving `TestingEndpoint` out of `server-core`.

## Risks

- Steps 0 and 5 are large; 5 is split into ports-first (5a) and moves (5b).
- The harvester's test fixtures (37 test methods) are rewritten on domain types in 5a.
- kapt in `graphql-api` adds build time.
- Work on `develop` that touches use cases or resolvers conflicts with the moves for as long as the refactoring is in progress.
