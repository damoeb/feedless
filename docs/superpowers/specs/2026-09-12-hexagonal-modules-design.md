# Hexagonal modules: use cases in `domain`, adapters in their own modules

**Status:** design, approved in conversation 2026-09-12. Lands on `feature/feed-ctl` as part of PR #86.

**Goal:** finish the refactoring that `docs/superpowers/plans/2026-07-17-http-api.md` started for `/api/v1` and stopped at ports. All use cases and guards live in `domain`; all GraphQL resolvers live in `graphql-api`; all HTTP controllers live in `http-api`; `server-core` is the Spring Boot application that assembles them and hosts infrastructure adapters. This supersedes that plan's rule that GraphQL resolvers stay in `server-core`.

**Non-goal:** behaviour changes. Every step is a move plus the minimum indirection the module boundaries force.

## Decisions

- **Spring annotations are allowed in `domain`.** Use cases move with `@Service`, `@Profile`, `@Transactional` and `@Cacheable` unchanged; `domain` gains `spring-context`, `spring-tx`, `spring-security-core` and `micrometer-core`. Profile gating therefore needs no rework.
- **Vertical, feature by feature.** Each step moves one feature's use cases, guards, resolvers, mappers, controllers and tests together, and ends green.
- **Outbound ports for infrastructure.** A use case that needs something only `server-core` has gets an interface in `domain`, named after the capability; `server-core` implements it with the existing class.
- **The `*Port` interfaces of `/api/v1` go away.** Once use cases are in `domain`, controllers and resolvers call them directly.
- **The security bridge moves to `domain`.** `injectCapabilitiesFromSecurityContext` / `injectCapabilitiesFromJwt` sit next to `RequestContext`, so resolvers, controllers and `server-core`'s filters share one copy.

## Modules

| Module | Holds | Depends on |
|---|---|---|
| `domain` | domain types, repository interfaces, all use cases and guards, outbound ports, security bridge, `@Throttled` | libraries only |
| `graphql-api` | schema and DGS codegen, all resolvers, `ProductDataLoader`, GraphQL mappers, `GraphQLExceptionHandler`, `GraphqlConfig`, `DgsCustomContext` | `domain`, DGS, Spring Security, MapStruct (kapt) |
| `http-api` | OpenAPI codegen, `/api/v1` controllers, and the other web controllers (feed export, repository feeds, documents, attachments, payment callbacks, CLI install script, mail and report links), `HttpExceptionHandler` | `domain` |
| `jpa-data`, `mail-adapter`, `stripe-payments`, `freemarker-templates`, `github-connector`, `feed-parser` | outbound adapters, unchanged | `domain` |
| `server-core` | Spring Boot application, security composition (`SecurityConfig`, JWT filters, `TokenAuthenticator`), scheduler executors, infrastructure behind outbound ports, `ThrottleAspect`, `TestingEndpoint` | all of the above |

Rules:

- `graphql-api` and `http-api` never depend on `server-core`; Gradle enforces it because the dependency does not exist.
- `server-core` references adapter modules only to assemble them. The existing reverse import (`QueuedHarvestExecutor` → `http-api`'s `HttpScrapeFlowMapper`) is removed.
- Use cases do not import generated GraphQL types or `data.jpa`; mapping to DTOs happens in the adapters.
- Everything stays in package `org.migor.feedless`, so `@SpringBootApplication`'s scan keeps finding beans; `graphql-api` also gets an auto-configuration like `http-api`'s `HttpApiAutoConfiguration`.

## Outbound ports

| Capability | Implemented by (server-core) | Needed by |
|---|---|---|
| Token issuing | `JwtTokenIssuer` | secrets, mail-auth, feed, report, agent |
| App config and URLs | `PropertyService`, `@Value` | document, repository, feed, CLI, payment |
| Pipeline plugins | `PluginService` | document, report |
| Notifications | `TelegramBotService`, `MessageService` | document, user |
| Scraping | `ScrapeService`, `SourceDryRunner` | repository/harvest, feed |
| Feed parsing and web-to-feed | `FeedParserService`, `WebToFeedTransformer` | feed |
| Session cookies | `CookieProvider` | mail-auth, anonymous auth |
| User authentication | `AuthService` | feed, agent |
| Agent registry | `AgentService`, `AgentRegistry` | status, agent |
| Analytics | `AnalyticsService` | server config, document, feed, attachment |

The plugin and notification ports also dissolve three of today's four `@Lazy` cycles (document ↔ plugins, scrape ↔ plugins, user ↔ Telegram). The fourth (source → harvester → repository → source) stays inside one step.

## Order

| # | Step | Includes |
|---|---|---|
| 0 | Cross-cutting | Resolvers switch to `domain`'s `@Throttled` (the aspect already matches both) and the `server-core` copy is deleted; security bridge to `domain`; `GraphQLExceptionHandler`, `GraphqlConfig`, `DgsCustomContext` to `graphql-api`; `graphql-api` build setup (DGS runtime, kapt/MapStruct, Java 21 toolchain, auto-configuration); shared mappers (`DtoMapperExtensions`, `DtoEnumMapper`, `EnumMapper`, `MapperUtil`, `MapStructConfig`) to `graphql-api`, after detaching `ProductUseCaseImpl`, `AgentService`, `RepositoryUseCase` and `FeedsPlugin` from them; test fixtures (below) |
| 1 | plan | `PlanUseCase`, `PlanGuard`, `PlanConstraintsService`, `PlanResolver`, `PlanHttpController` |
| 2 | feature, product, order | `FeatureService`, `ProductUseCaseImpl`, `OrderUseCaseImpl`, `OrderGuard`, their resolvers and mappers, `ProductDataLoader` (via a repository method instead of the JPA DAO), `PaymentController` |
| 3 | user, group, auth, secrets | `UserGuard`, `GroupGuard`, `GroupUseCase`, `AuthUseCase`, `UserUseCase`, `ConnectedAppUseCase`, `UserSecretUseCase`; token-issuing port |
| 4 | session, mail-auth | `MailAuthenticationService`, `OneTimePasswordService`, `MailAuthResolver`, `SessionResolver`, `AuthAnonymousResolver`; session-cookie port |
| 5 | repository, source, document, harvest | `RepositoryGuard`, `RepositoryUseCase`, `SourceUseCase`, `DocumentGuard`, `DocumentUseCase`, `HarvestService`, `RepositoryHarvester`, pipeline services, `InboxService`, their resolvers, mappers and controllers; scraping, plugin, notification and config ports. If it grows too large: ports first, then the move |
| 6 | attachment, annotation, report | use cases, guards, resolvers, `AttachmentController`, `ReportController`, `MailController` |
| 7 | feed, license, status, agent, scrape, plugins | `FeedService`, `LicenseUseCase`, `ServerStatusService`, the remaining resolvers, `FeedController`, `CliInstallScriptController`; feed-parsing, authentication and agent-registry ports |
| 8 | Wrap-up | `HttpExceptionHandler` and, if separable, `AppErrorController` to `http-api`; AGENTS.md module table and rule 2; `docs/rules/kotlin-spring.md` (contract modules now hold code) |

## Tests

- Unit tests without a Spring context move with their code: use-case tests to `domain`, resolver tests to `graphql-api`, controller tests to `http-api`.
- Persistence integration tests (DAOs, `*JpaRepository` adapters, Postgres-backed repository and registry tests) move to `jpa-data`, which gains the Testcontainers setup; `PostgreSQLExtension` becomes a `jpa-data` test fixture.
- Tests that need the whole application (`@SpringBootTest` across layers) stay in `server-core`.
- `Mother.kt` becomes a `domain` test fixture (`java-test-fixtures`) so every module's tests can use it.

## Verification per step

- All modules compile; `domain`, `graphql-api`, `http-api`, `jpa-data` and `server-core` tests pass.
- The total number of executed tests does not drop (counted from JUnit XML before and after), so no test gets lost in a move.
- Files move with `git mv`.
- After steps 5 and 8: the e2e smoke test against a freshly built core image. After step 8: `./gradlew lint test`.
- Each step gets its own commits, a task review, and a push to PR #86; a whole-branch review closes the refactoring.

## Out of scope (to `docs/tasks.md`)

- Inconsistent profile gating (`PlanGuard` on `repository`, `service & repository` pairs, `&&`, services without a profile).
- Renaming `LinceseResolver`; splitting `LicenseUseCase` into provider and use case.
- Moving `TestingEndpoint` out of `server-core`.

## Risks

- Step 5 is large because the remaining cycle forces it into one piece.
- kapt in `graphql-api` adds build time.
- Work on `develop` that touches use cases or resolvers conflicts with the moves for as long as the refactoring is in progress.
