# Platform upgrade: Spring Boot 4.1, DGS 12, Kotlin 2.3

**Status:** design, approved in conversation 2026-09-13. Lands on `feature/browser-automation-adapter` before `2026-09-13-browser-automation-adapter-design.md`, which builds on it.

**Goal:** move to a supported Spring Boot line and to DGS's Spring for GraphQL stack. Spring Boot 3.4 left OSS support on 2025-12-31 (3.5 on 2026-06-30; 4.1 is supported until 2027-07-31). DGS 10 is the last line for Boot 3; DGS 11 and 12 are built against Boot 4.0. The browser automation adapter needs Spring for GraphQL's `WebSocketGraphQlInterceptor`, which exposes the WebSocket session's remote address; DGS 9's legacy WebSocket handler passes subscription resolvers no request at all.

**Non-goal:** behaviour changes. Where a new default changes observable output, the old behaviour is configured back explicitly.

## Target

| | Now | Target |
|---|---|---|
| Spring Boot | 3.4.11 | 4.1.1 |
| DGS | 9.2.2 (`graphql-dgs-spring-boot-starter`, legacy WebSocket modules) | 12.0.1 (`dgs-starter`, Jackson 3 default) |
| dgs-codegen | 8.1.1 | 8.6.0 |
| Kotlin | 2.2.21 | 2.3.21 |
| Gradle | 8.9 | 8.14.5 |

Managed by the Boot 4.1.1 BOM, catalog pins removed: Spring Framework 7.0.9, Spring Security 7.1.1, Hibernate 7.4.5 (`hibernate-spatial`), Flyway 12.4.0, Jackson 3.1.5, Spring for GraphQL 2.0.5 (`spring-graphql-test`, today pinned to the milestone 2.0.0-M3), Testcontainers 2.0.5, kotlinx-coroutines 1.10.2, Reactor 2025.0.7. The Flyway pin had no reason; both the catalog version and `jpa-data`'s `strictly(...)` go.

Library moves: kotlin-jdsl 3.5.1 → 3.9.0 with `spring-data-jpa-boot4-support`; `telegrambots-spring-boot-starter` 6.9.0 → `telegrambots-meta` 10.3.0 (`TelegramBotService` only uses `Update`).

Gradle 8.14.5 rather than 9.x: it is Boot 4.1's minimum and leaves the third-party build plugins (`gradle-node` 7.1, `openapi-generator` 7.14, `javacc` 4.0.1, freefair 9.0, `grgit` 5.0) untouched.

## Inventory

Measured in the repo before designing:

- **DGS:** every legacy class in use still exists in 12.0.1 (`DgsContext`, `DgsCustomContextBuilder`, `DgsQueryExecutor`, `DgsWebMvcRequestData`, `DgsDirective`). `MonoGraphQLClient` and `WebClientGraphQLClient` are `@Deprecated` in favour of the `Dgs*` clients; they are used in four tests. `CustomDgsWebSocketConfig`, `DgsWebSocketAutoConfig`'s exclusion in `FeedlessApplication`, and the two `graphql-dgs-subscriptions-websockets*` dependencies go away.
- **Jackson:** 11 imports: `ObjectMapper` (5), `jacksonObjectMapper` (2), `SerializationFeature`, the JSR-310 module, and two annotations. Annotations keep `com.fasterxml.jackson.annotation`; databind code moves to `tools.jackson`. `jackson-module-kotlin` in `http-api` and `server-core`.
- **Hibernate:** annotations (`OnDelete`, `JdbcTypeCode`, `Cache`, …) plus one custom dialect function via `MetadataBuilderContributor`, the one API likely to change.
- **Boot autoconfigure references** that move packages in Boot 4: `DataSourceAutoConfiguration`, `JpaRepositoriesAutoConfiguration`, `SecurityAutoConfiguration`, `MailSenderValidatorAutoConfiguration`, `CacheAutoConfiguration`, the two Elasticsearch repository auto-configurations, `FlywayConfigurationCustomizer`, `WebMvcRegistrations`, `EntityScan`.
- **Spring Security:** none of the APIs removed in 7 (`and()`, `authorizeRequests`, `antMatchers`, `WebSecurityConfigurerAdapter`, …) are used.
- **Tests:** one file with `@MockBean`/`@MockBeans`; `TestRestTemplate` (7 files) and `LocalServerPort` (10) move and need explicit auto-configuration in Boot 4; Testcontainers 2 renames artifacts.

## Steps

Each step is its own commit (or commit series) and ends with `./gradlew lint test` green.

| # | Step |
|---|---|
| A | Gradle wrapper 8.14.5. |
| B | Spring Boot 3.5 + DGS 10.6: `dgs-starter`; subscriptions over Spring for GraphQL with `spring.graphql.websocket.path: /subscriptions` so `app-web` and the agent keep their URL; `spring.graphql.websocket.connection-init-timeout: 10s`, DGS 9.2.2's default (Spring for GraphQL defaults to 60s); drop `CustomDgsWebSocketConfig`, `AuthenticationHttpSessionHandshakeInterceptor` and the legacy WebSocket modules; `@EnableDgsTest` where tests assembled `DgsAutoConfiguration`. Isolates the transport switch from the Boot major. |
| C | Spring Boot 4.1.1 + DGS 12.0.1 + dgs-codegen 8.6.0 + Kotlin 2.3.21, one commit because nothing compiles in between. Work order: (1) Framework 7, Security 7, moved auto-configuration packages, the per-technology starters Boot 4 requires (Flyway included), kotlin-jdsl boot4 support; (2) Jackson 3; (3) Hibernate 7.4 and Flyway 12 with `flyway-database-postgresql`; (4) tests: `@MockitoBean`, `TestRestTemplate`/`LocalServerPort`, Testcontainers 2, the deprecated DGS clients only if they break; (5) Telegram. |
| D | Docs: AGENTS.md prerequisites (Gradle 8.14.5) and stack line; `docs/rules/kotlin-spring.md` where it names DGS WebSocket classes or `@MockBean`. |

## Verification

After every step: `./gradlew lint test`; the executed test count does not drop (stale results deleted first, counted from JUnit XML).

After B and after C:

- **e2e smoke:** `./gradlew buildImages`, bring the stack up, run `./gradlew :packages:cli:e2eTest` against the fresh core image.
- **Agent smoke:** `docker compose up` with core, agent and postgis; the agent logs `Connected` over `/subscriptions`, and a source that needs prerendering harvests.

New in B: a WebSocket integration test in `server-core` (random port) where a `graphql-transport-ws` client subscribes to `registerAgent` over `/subscriptions` with an invalid key and receives the error. It guards the transport switch; the adapter spec extends it.

In C: **Flyway checksums (rule 5).** Migrations stay byte-identical. The upgraded core boots against a database migrated by the current `develop` build; Flyway's validate-on-migrate must pass.

## Behaviour changes to watch

| Change | Where it bites | Caught by, and the fix if it does |
|---|---|---|
| Spring for GraphQL may answer `application/graphql-response+json` with HTTP 4xx for request-level errors (parse, validation); field errors stay 200 | `app-web`, agent HTTP client | e2e and agent smoke; configure plain `application/json` responses if a client breaks |
| Cookie code casts `DgsContext.getRequestData(dfe)` to `DgsWebMvcRequestData` holding a `ServletWebRequest` | anonymous auth, mail auth, session | `AuthenticationIntTest`, `MailAuthResolverIntTest` |
| Jackson 3 defaults; hand-built mappers do not get Boot's settings | JSON Feed output, `/api/v1` payloads | `http-api` and `feed-parser` tests; configure the old behaviour on the mapper where a fixture changes |
| Hibernate 7 validates queries more strictly | kotlin-jdsl JPQL, the custom dialect function | `jpa-data` persistence tests (Testcontainers/PostGIS) |
| WebSocket init timeout and keep-alive move from DGS to `spring.graphql.websocket.*` | agent reconnects | agent smoke; values set explicitly in B |

## Rollback

Code and configuration only: no schema change, no new migration, and Flyway 12 keeps the schema history table. Reverting the step commits restores the previous build against the same database.

## Out of scope

- Gradle 9.
- Replacing the deprecated DGS test clients unless DGS 12 breaks them.
- Replacing `DgsCustomContextBuilder` with Spring for GraphQL context.
- Frontend library upgrades.

## Risks

- Step C is large and cannot be split into green commits; the work order inside it keeps compile errors grouped by cause.
- DGS 12 is built against Boot 4.0.0 and runs on 4.1.1; a minor-version incompatibility would show up in step C's test run, with Boot 4.0.x as the fallback (supported until 2026-12-31).

## References

- DGS releases and v10/v11 notes: https://github.com/Netflix/dgs-framework/releases
- Spring Boot 4.0 migration guide: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide
- Spring Boot system requirements: https://docs.spring.io/spring-boot/system-requirements.html
- DGS subscriptions: https://netflix.github.io/dgs/advanced/subscriptions/
