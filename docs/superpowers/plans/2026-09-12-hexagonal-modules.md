# Hexagonal Modules Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Move all use cases and guards to `domain`, all GraphQL resolvers and mappers to `graphql-api`, all HTTP controllers to `http-api`, and persistence tests plus Flyway migrations to `jpa-data`, leaving `server-core` as the Spring Boot application with infrastructure adapters.

**Architecture:** Hexagonal: `domain` holds the core and outbound ports; `graphql-api` and `http-api` are inbound adapters that depend only on `domain`; `jpa-data` and the other adapter modules implement outbound ports; `server-core` assembles everything. The move goes feature by feature (vertical), every task ends green.

**Tech Stack:** Kotlin, Spring Boot 3.4, Netflix DGS (codegen in `graphql-api`), OpenAPI generator (`http-api`), JPA/Flyway/PostGIS, Testcontainers, JUnit 5, Mockito, MapStruct (kapt), Gradle 8.9.

**Spec:** `docs/superpowers/specs/2026-09-12-hexagonal-modules-design.md`

**Inventory:** `docs/superpowers/plans/2026-09-12-hexagonal-modules-inventory.md` — exact `from → to` paths, port signatures, build snippets and gotchas per step. Each task below names its inventory sections; the implementer reads them together with the task. The inventory was researched, not compiled: where the compiler disagrees with it, follow the compiler and report the deviation.

## Global Constraints

- No behaviour change. Moves plus the minimum indirection the module boundaries force; no renames, no refactoring beyond that.
- Packages stay unchanged (`org.migor.feedless.*`); only the source root changes. Every move is `git mv`.
- `graphql-api` and `http-api` never depend on `server-core`. `domain` depends on no project module.
- Spring annotations are allowed in `domain`; `@Service`, `@Profile`, `@Transactional`, `@Cacheable`, `@Lazy` move unchanged.
- Use cases never import `org.migor.feedless.generated` (GraphQL DTOs) or `org.migor.feedless.data.jpa`.
- Port names are fixed by the inventory's "Port names" table and the spec's "Outbound ports" table; `UserAuthenticator` and `SessionCookies` are not created.
- Decisions that override the inventory: `RepositoryHarvester` moves to `domain` with its own scrape-result types (inventory step 5 main path; **not** "Alternative A"); Flyway migrations move to `jpa-data`.
- Never modify a Flyway migration's content; moving the directory keeps checksums.
- Every Spring bean stays profile-gated; tests keep their `@ActiveProfiles`.
- Code comments in English, one line, only the why (AGENTS.md checklist).
- Test count never drops: before and after every task, delete stale results and count executed tests (see "Test count" below); the after-value must be ≥ the before-value.
- Conventional Commits, scope = target module (e.g. `refactor(domain): move plan use case`). Commit trailer:
  ```
  Co-Authored-By: Claude Opus 5 <noreply@anthropic.com>
  Claude-Session: https://claude.ai/code/session_01J8aGTLwRiqCMfgzmWs8HNu
  ```
- Never push from a task (the controller pushes to PR #86 after review). Never bare `git stash`. `trash`, not `rm`.

## Test count

Run before the task's first change and after its last:

```bash
trash packages/*/build/test-results 2>/dev/null; ./gradlew :packages:domain:test :packages:jpa-data:test :packages:graphql-api:test :packages:http-api:test :packages:server-core:test :packages:feed-parser:test :packages:mail-adapter:test :packages:stripe-payments:test --continue
python3 -c "import glob,collections,xml.etree.ElementTree as E;c=collections.defaultdict(collections.Counter);[c[f.split('/')[1]].update({k:int(E.parse(f).getroot().get(k,0)) for k in ('tests','skipped','failures','errors')}) for f in glob.glob('packages/*/build/test-results/test/*.xml')];[print(m,dict(v),'executed=',v['tests']-v['skipped']) for m,v in sorted(c.items())];t=sum(c.values(),collections.Counter());print('TOTAL',dict(t),'executed=',t['tests']-t['skipped'])"
```

Expected after: every module `failures=0 errors=0`, `TOTAL executed` ≥ the before-value. Record both values in the task report.

## e2e smoke test (tasks 7 and 10)

```bash
./gradlew :packages:server-core:bootJar
(cd packages/server-core && docker build --build-context cli=../cli --build-arg APP_VERSION=0.3.0-hex --build-arg APP_GIT_COMMIT=$(git rev-parse --short HEAD) --build-arg APP_BUILD_TIMESTAMP=$(date +%s)000 --platform=linux/arm64 -t feedctl-e2e:core .)
FEEDCTL_E2E_CORE_IMAGE=feedctl-e2e:core ./gradlew :packages:cli:e2eTest
```

Expected: `--- PASS: TestBrokenSourceFixLoop` including `cli_downloads`. The core container applies all Flyway migrations on a fresh PostGIS, which proves they are found in the `jpa-data` jar. Re-run once on a Docker startup stall and report both runs.

---

### Task 1: Cross-cutting foundation (spec step 0)

**Files:** inventory "Step 0 — Cross-cutting" §0.1–0.12 (37 main moves, 4 test moves plus the `Mother` merge, 5 deletions, 8 files split out of `@Service` files, 91 migrations to `jpa-data`).

**Interfaces:**
- Consumes: nothing.
- Produces: `domain` builds with Spring (BOM, `spring-context`, `spring-tx`, `spring-security-core`, `-oauth2-client`, `-oauth2-jose`, `micrometer-core`, jsoup, `kotlin.spring`); `org.migor.feedless.common.AppConfig` with `val apiGatewayUrl: String`; security bridge functions `injectCapabilitiesFromSecurityContext` / `injectCapabilitiesFromJwt` in `domain` (package `session`); single `org.migor.feedless.throttle.Throttled`; shared exceptions in `domain/.../Exceptions.kt`; feed-parser shared types in `domain`; all `api/mapper` files in `graphql-api` with `GraphqlApiAutoConfiguration`; `JpaDataTestApplication` and `PostgreSQLExtension` as `jpa-data` test fixture; one `object Mother` plus `MockitoHelpers.kt` in `domain` test fixtures; migrations under `packages/jpa-data/src/main/resources/db/migration`.

- [ ] **Step 1:** Record the test-count baseline (section "Test count"). Server-core needs Docker.
- [ ] **Step 2:** Apply the build changes from §0.10 (`domain`, `graphql-api`, `jpa-data`, `http-api`, `server-core`, `feed-parser` build files) and the auto-configuration from §0.9. Run `./gradlew :packages:domain:compileKotlin :packages:graphql-api:compileKotlin` — expected: success.
- [ ] **Step 3:** Move the feed-parser shared types (§0.2) and merge `Exceptions.kt` (§0.3); delete the duplicate `HttpResponse`/`FeedUtil`/`HtmlUtil` declarations in `server-core`. Compile all modules: `./gradlew compileKotlin compileTestKotlin` — expected: success.
- [ ] **Step 4:** Unify `@Throttled` (§0.7): switch the 16 resolvers and `FeedController` to `org.migor.feedless.throttle.Throttled`, delete the server-core copy. Move the security bridge (§0.8). Compile.
- [ ] **Step 5:** Create `AppConfig` and detach `ProductUseCaseImpl`, `AgentService`, `RepositoryUseCase`, `FeedsPlugin` from the GraphQL mappers (§0.4). Move `GraphQLExceptionHandler`, `GraphqlConfig`, `DgsCustomContext`, `DtoMapperExtensions`, `DtoEnumMapper` and all `api/mapper` files plus the value types they need (§0.1, reconciliation rule "Mappers move in step 0"). Compile.
- [ ] **Step 6:** Move the migrations to `jpa-data`, add `JpaDataTestApplication`, test resources and `PostgreSQLExtension` fixture (§0.6); update `FlywayTargetTest` paths so both its tests still pass. Merge `Mother` and add `MockitoHelpers.kt` (§0.5).
- [ ] **Step 7:** Run "Test count". Expected: all green, `TOTAL executed` ≥ baseline. Also `./gradlew :packages:server-core:test --tests '*FlywayTargetTest*' --rerun` — both tests pass.
- [ ] **Step 8:** Commit in logical pieces, e.g. `build(domain): allow Spring in the domain module`, `refactor(domain): take the types feed-parser shares with use cases`, `refactor(graphql-api): host GraphQL mappers and config`, `refactor(jpa-data): own the Flyway migrations and persistence test setup`, `test(domain): merge the Mother fixtures`.

### Task 2: plan (spec step 1)

**Files:** inventory "Step 1 — plan" (4 main, 4 test moves) and the reconciliation rule on the coroutine-context helpers.

**Interfaces:**
- Consumes: Task 1's `domain` build setup, `Mother`, `graphql-api` auto-configuration.
- Produces: `PlanUseCase`, `PlanGuard`, `PlanConstraintsService` and the coroutine-context helpers `userId()`, `userIdMaybe()`, `groupId()`, `corrId()`, `isAdmin()` in `domain`; `PlanResolver` in `graphql-api`; `PlanHttpController` uses `PlanUseCase` directly; `PlanUseCasePort` deleted.

- [ ] **Step 1:** Record "Test count" before.
- [ ] **Step 2:** `git mv` every row of the inventory's step-1 move table; move the coroutine-context helpers out of `user/UserUseCase.kt` into `domain` (same package, same names).
- [ ] **Step 3:** Delete `PlanUseCasePort`; switch `PlanHttpController` and its test to `PlanUseCase`.
- [ ] **Step 4:** Compile all modules; run "Test count". Expected: green, count ≥ before.
- [ ] **Step 5:** Commit: `refactor(domain): move the plan use case` (plus separate commits for `graphql-api`/`http-api` parts if they read better).

### Task 3: feature, product, order (spec step 2)

**Files:** inventory "Step 2 — feature, product, order" (9 main, 1 test move; 3–4 dead mappers deleted) and the reconciliation rules on `UserGuard` and `AppConfig.appHost`.

**Interfaces:**
- Consumes: Task 2's helpers; `AppConfig`.
- Produces: `FeatureService`, `ProductUseCaseImpl`, `OrderUseCaseImpl`, `OrderGuard`, `UserGuard` in `domain` (with the new domain input types the inventory lists instead of generated DTOs); `FeatureResolver`, `ProductResolver`, `ProductDataLoader` in `graphql-api`, the loader using the new repository method from the inventory instead of `ProductDAO`; `PaymentController` in `http-api`; `AppConfig.appHost: String`.

- [ ] **Step 1:** Record "Test count" before.
- [ ] **Step 2:** Add the product repository method and its `jpa-data` adapter implementation (inventory "Edits outside moves"); switch `ProductDataLoader` to it.
- [ ] **Step 3:** Replace generated DTOs in the moving use cases with the inventory's domain input types; map in the resolvers; adjust the affected tests.
- [ ] **Step 4:** `git mv` the step-2 move table; delete the dead `product/ProductMapper.kt`, `product/PricedProductMapper.kt`, `order/OrderMapper.kt`.
- [ ] **Step 5:** Compile; run "Test count". Expected: green, count ≥ before.
- [ ] **Step 6:** Commit: `refactor(domain): move feature, product and order use cases`, `refactor(http-api): host the payment controller`.

### Task 4: user, group, auth, secrets (spec step 3)

**Files:** inventory "Step 3 — user, group, auth, secrets" (about 11 main, 8 test moves), reconciliation rules on `Notifications`, `TokenIssuer` and `OrderResolver`.

**Interfaces:**
- Consumes: Tasks 1–3.
- Produces: `session.TokenIssuer` (2 methods, signatures in the inventory, implemented by `JwtTokenIssuer`); `message.Notifications` (2 methods) implemented by the new unconditional `NotificationsAdapter` in `server-core`; `GroupGuard`, `GroupUseCase`, `AuthUseCase`, `UserUseCase`, `ConnectedAppUseCase`, `UserSecretUseCase` in `domain`; `UserResolver`, `GroupResolver`, `SecretsResolver`, `OrderResolver` in `graphql-api`; `AuthUseCasePort` and `GroupUseCasePort` deleted, consumers switched.

- [ ] **Step 1:** Record "Test count" before.
- [ ] **Step 2:** Create `TokenIssuer` and `Notifications` with the inventory's exact signatures; make `JwtTokenIssuer` implement `TokenIssuer`; add `NotificationsAdapter` (inject `Notifications`, not `Optional<Notifications>`).
- [ ] **Step 3:** Switch `UserSecretUseCase` and `UserUseCase` to the ports; replace generated DTOs with the inventory's domain types.
- [ ] **Step 4:** `git mv` the step-3 move table; delete the two `*Port` files; switch consumers (`AuthHttpController`, `GroupHttpController`, `RepositoryAccessGuard`, `AuthUseCase`).
- [ ] **Step 5:** Compile; run "Test count". Expected: green, count ≥ before.
- [ ] **Step 6:** Commit: `refactor(domain): move user, group, auth and secret use cases`.

### Task 5: session, mail-auth (spec step 4)

**Files:** inventory "Step 4 — session, mail-auth" (5 main, 1 test move).

**Interfaces:**
- Consumes: `TokenIssuer` (Task 4), `SessionTokenPort`.
- Produces: `TokenIssuer` grown to 4 methods (inventory); `MailAuthenticationService`, `OneTimePasswordService` in `domain`; `MailAuthResolver`, `SessionResolver`, `AuthAnonymousResolver` in `graphql-api`. No `SessionCookies` port.

- [ ] **Step 1:** Record "Test count" before.
- [ ] **Step 2:** Extend `TokenIssuer`; switch `MailAuthenticationService` from `JwtTokenIssuer`/`CookieProvider` to `TokenIssuer`/`SessionTokenPort`; replace generated DTOs per the inventory.
- [ ] **Step 3:** `git mv` the step-4 move table.
- [ ] **Step 4:** Compile; run "Test count". Expected: green, count ≥ before.
- [ ] **Step 5:** Commit: `refactor(domain): move mail authentication`.

### Task 6: repository block, ports first (spec step 5a)

**Files:** inventory "Step 5 — 5a" (5 whole-file moves plus up to 10 conditional ones, 17 new domain files, 3 new server-core files, harvester test fixtures).

**Interfaces:**
- Consumes: Tasks 1–5.
- Produces: domain scrape-result types (`ScrapeResult` and friends, per inventory) and `LogCollector`; ports `scrape.Scraper` (implemented by new `ScraperAdapter`), `pipeline.PipelinePlugins`, `analytics.Analytics`, `source.StoredFlowParser` (implemented by `HttpScrapeFlowMapper` in `http-api`), `document.DocumentQueryParser` (implemented by `GraphqlDocumentQueryParser`), `Notifications` grown to 3 methods; `RepositoryHarvester`, `RepositoryUseCase`, `DocumentUseCase`, `SourceUseCase` still in `server-core` but already using only ports and domain types; `QueuedHarvestExecutor` no longer imports `http-api`.

- [ ] **Step 1:** Record "Test count" before.
- [ ] **Step 2:** Create the domain value types, helpers and ports exactly as the inventory lists them; add `ScraperAdapter`, `NotificationsAdapter` extension, `GraphqlDocumentQueryParser`; make `HttpScrapeFlowMapper` implement `StoredFlowParser`. Profile rules from the inventory's 5b risks (`ScraperAdapter` carries `scrape & service`, `NotificationsAdapter` only `AppLayer.service`).
- [ ] **Step 3:** Switch `RepositoryHarvester`, `RepositoryUseCase`, `DocumentUseCase`, `SourceUseCase`, `QueuedHarvestExecutor` to the ports and domain types. Keep every `@Lazy`.
- [ ] **Step 4:** Rewrite `RepositoryHarvesterTest`, `QueuedHarvestExecutorTest`, `OneRealHarvestPerSourceIntTest` fixtures on the domain scrape-result types (37 test methods); assertions stay semantically identical.
- [ ] **Step 5:** Compile; run "Test count". Expected: green, count ≥ before.
- [ ] **Step 6:** Commit: `refactor(domain): add scrape, plugin and analytics ports`, `test(server-core): run harvester tests on domain scrape results`.

### Task 7: repository block, moves (spec step 5b)

**Files:** inventory "Step 5 — 5b" (18 main moves, 12 test moves: 6 to `domain`, 3 to `graphql-api`, 3 to `jpa-data`; 5 `*Port` files deleted).

**Interfaces:**
- Consumes: Task 6's ports and types.
- Produces: `RepositoryGuard`, `RepositoryUseCase`, `SourceUseCase`, `DocumentGuard`, `DocumentUseCase`, `HarvestService`, `RepositoryHarvester`, `SourcePipelineService`, `DocumentPipelineService`, `InboxService` in `domain`; `RepositoryResolver`, `DocumentResolver` in `graphql-api`; `RepositoryController`, `DocumentController` in `http-api` (scan per inventory); `HarvestRepositoryIntTest`, `SourceRepositoryIntTest`, `AbstractRepositoryEntityTest` in `jpa-data`; `DocumentGuardPort`, `DocumentUseCasePort`, `HarvestUseCasePort`, `RepositoryUseCasePort`, `SourceUseCasePort` deleted.

- [ ] **Step 1:** Record "Test count" before.
- [ ] **Step 2:** Make `internal` declarations used across modules public (inventory 5b risks); copy `DocumentUseCase.findAllByRepositoryId` defaults before deleting its port.
- [ ] **Step 3:** `git mv` the 5b tables; delete the five `*Port` files; switch `http-api` controllers and `RepositoryAccessGuard` to the use cases.
- [ ] **Step 4:** Move the three persistence tests to `jpa-data` (drop their `StatelessAuthService` mock, use `JpaDataTestApplication`); place `feed.xsl` per the inventory.
- [ ] **Step 5:** Compile; run "Test count". Expected: green, count ≥ before.
- [ ] **Step 6:** Run the e2e smoke test (section above). Expected: PASS.
- [ ] **Step 7:** Commit: `refactor(domain): move repository, source, document and harvest use cases`, `refactor(http-api): host repository feed and document controllers`, `test(jpa-data): run persistence tests next to the adapters`.

### Task 8: attachment, annotation, report (spec step 6)

**Files:** inventory "Step 6 — attachment, annotation, report" (14 main, 5 test moves).

**Interfaces:**
- Consumes: Tasks 1–7; `TokenIssuer`, `PipelinePlugins`, `Analytics`.
- Produces: `AttachmentGuard`, `AttachmentUseCase`, `AnnotationGuard`, `AnnotationUseCase`, `ReportGuard`, `ReportUseCase`, `ApiUrls` in `domain`; `AttachmentResolver`, `AnnotationResolver` (+ `AnnotationDtoMapper`), `ReportResolver` in `graphql-api`; `AttachmentController`, `ReportController`, `MailController` in `http-api`; `TokenIssuer` extended as the inventory lists.

- [ ] **Step 1:** Record "Test count" before.
- [ ] **Step 2:** Apply the step-6 port additions and the "Edits outside moves"; replace generated DTOs in `AnnotationUseCase`/`ReportUseCase` per the inventory.
- [ ] **Step 3:** `git mv` the step-6 move tables.
- [ ] **Step 4:** Compile; run "Test count". Expected: green, count ≥ before.
- [ ] **Step 5:** Commit: `refactor(domain): move attachment, annotation and report use cases`.

### Task 9: feed, license, status, agent, scrape, plugins (spec step 7)

**Files:** inventory "Step 7" (15 main, 6 test moves).

**Interfaces:**
- Consumes: Tasks 1–8.
- Produces: ports `feed.FeedParser`, `scrape.WebToFeed`, `pipeline.ItemFilter`, `common.HttpFetcher`, `agent.AgentDirectory`; `Scraper` gains `fetch`, `PipelinePlugins` gains `describeAll`, `TokenIssuer` reaches 6 methods; graphql-api-local ports `AgentGateway` (and `ScrapeRunner` only if needed) per inventory; `FeedService` (without the unused `AuthService` parameter), `LicenseUseCase`, `ServerStatusService` in `domain`; `FeedResolver`, `LinceseResolver`, `ServerConfigResolver`, `AgentResolver`, `ScrapeQueryResolver`, `PluginResolver` in `graphql-api`; `FeedController`, `CliInstallScriptController` in `http-api`; `ServerStatusPort` deleted.

- [ ] **Step 1:** Record "Test count" before.
- [ ] **Step 2:** Create/extend the step-7 ports and their server-core implementations with the inventory's exact signatures.
- [ ] **Step 3:** `git mv` the step-7 move tables; delete `ServerStatusPort`, switch `StatusHttpController` to `ServerStatusService`. `LicenseUseCaseTest` stays tagged `unstable`; `domain`'s test task excludes that tag like `server-core`'s.
- [ ] **Step 4:** Compile; run "Test count". Expected: green, count ≥ before.
- [ ] **Step 5:** Commit: `refactor(domain): move feed, license and status use cases`, `refactor(graphql-api): move the remaining resolvers`.

### Task 10: wrap-up (spec step 8)

**Files:** inventory "Step 8 — wrap-up".

**Interfaces:**
- Consumes: everything.
- Produces: `HttpExceptionHandler` (and `AppErrorController` if it separates cleanly) in `http-api`; up-to-date AGENTS.md and `docs/rules/kotlin-spring.md`.

- [ ] **Step 1:** Record "Test count" before.
- [ ] **Step 2:** Move `HttpExceptionHandler`; move `AppErrorController` only if it has no server-core dependencies after the earlier steps (otherwise leave it and say why in the report).
- [ ] **Step 3:** Update AGENTS.md: modules table (domain holds use cases; graphql-api and http-api hold adapters; jpa-data holds migrations; `cli` is a Gradle module), rule 2 wording on contract modules, rule 5 migration path `packages/jpa-data/src/main/resources/db/migration`. Update `docs/rules/kotlin-spring.md` where it says contract modules hold no hand-written Kotlin and that `/api/v1` interfaces are implemented in `server-core`. One paragraph per line, no hard wrap.
- [ ] **Step 4:** Run the inventory's step-8 sanity grep (nothing use-case- or entry-point-shaped left in `server-core`). Expected: no output other than infrastructure listed in the spec.
- [ ] **Step 5:** Run "Test count" (≥ before), the e2e smoke test (PASS) and `./gradlew lint test` (known environmental failures: `document-classifier` model file, `app-web` Jest bootstrap — report them, don't fix).
- [ ] **Step 6:** Commit: `refactor(http-api): host the remaining web error handling`, `docs: describe the hexagonal module layout`.
