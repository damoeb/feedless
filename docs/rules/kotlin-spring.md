# Kotlin / Spring Rules

Kotlin on JDK 21, Spring Boot, Netflix DGS GraphQL, Spring Data JPA + Flyway + PostGIS, JUnit 5 + Testcontainers + AssertJ + mockito-kotlin.

Related: [`angular-nx.md`](angular-nx.md) (the GraphQL schema's other consumers), [`../../AGENTS.md`](../../AGENTS.md) (hub).

## CRITICAL: Every bean is profile-gated

This is AGENTS.md Critical Rule #1.

Beans are gated on **two orthogonal axes** — a feature (`AppProfiles`) and a layer (`AppLayer`), both in `packages/domain/.../AppProfiles.kt` and `AppLayer.kt`:

```kotlin
@DgsComponent
@Profile("${AppProfiles.document} & ${AppLayer.api}")
class DocumentResolver(...)
```

`AppProfiles` names features (`document`, `scrape`, `session`, `plan`, `community`, `saas`, `selfHosted`, …). `AppLayer` names tiers (`apiLayer`, `serviceLayer`, `repositoryLayer`, `securityLayer`, `schedulerLayer`). A deployment activates a set of both; that is how one codebase runs as several products and how the scheduler can be split off from the API.

**Why:** this is not decoration. A missing profile means the bean is genuinely absent from the context, and Spring reports it as `NoSuchBeanDefinitionException` — indistinguishable at a glance from a missing `@Component`. The tempting fixes (adding `@Component`, widening a profile, mocking the bean away) all quietly break the deployment split.

### A new bean

Annotate it with **both** axes, matching its neighbours in the same package. A service goes in `serviceLayer`, a resolver in `apiLayer`. Never leave a bean unprofiled just to make a test pass.

### A test

`@ActiveProfiles` must list `"test"`, any infrastructure profile (`"database"`), and **every** `AppProfiles`/`AppLayer` value whose beans the test needs:

```kotlin
@SpringBootTest
@ExtendWith(PostgreSQLExtension::class)
@DirtiesContext
@ActiveProfiles(
  "test", "database",
  AppProfiles.repository, AppProfiles.source, AppProfiles.user, AppProfiles.scrape,
  AppLayer.repository, AppLayer.service,
)
@MockitoBean(types = [ProductRepository::class, DocumentUseCase::class, /* … */])
class RepositoryUseCaseIntTest
```

Use the constants, never string literals — `AppProfiles.repository` renames with the code, `"repository"` does not.

## CRITICAL: Three generators own their output

This is AGENTS.md Critical Rule #2. On the backend:

| Source | Generator | Output | Consumed as |
|---|---|---|---|
| `packages/graphql-api/src/main/resources/schema/schema.graphqls` | DGS codegen (Gradle) | `build/generated/sources/dgs-codegen` | `org.migor.feedless.generated.types.*`, `DgsConstants`, `DgsClient` |
| `packages/http-api/src/main/resources/openapi/openapi.yaml` | openapi-generator, `kotlin-spring`, `interfaceOnly` | `build/generated/src/main/kotlin` | `org.migor.feedless.http.api` / `.model` — implemented by the controllers in `http-api` |
| `server-core/.../document/filter/FilterByExpression.jj` | JavaCC | `build/generated/javacc` | the document filter parser |

**Why:** all three regenerate on every build. A hand edit works locally until the next clean build, then disappears — leaving a CI failure with no diff that explains it.

To change the API, edit the **contract** and rebuild. Both contract modules also hold hand-written adapters (resolvers, controllers, mappers) under `src/main/kotlin`; only `build/generated/**` is generator output.

Note: `server-core/src/generated/java/` holds a checked-in copy of the JavaCC output that is **not** on the source path (`build/generated/javacc` is). Do not edit it and do not assume it is current.

## Package and naming conventions

Code is organised by **feature**, not by layer — `document/`, `repository/`, `source/`, `plan/`, `session/`, `user/`, `scrape/`, `pipeline/`, … — and the same package spans modules: use cases and guards in `domain`, resolvers in `graphql-api`, controllers in `http-api`, infrastructure in `server-core`. `graphql-api` and `http-api` never depend on `server-core`, and `domain` depends on no project module.

| Suffix | Role | Module | Layer |
|---|---|---|---|
| `*Resolver` | DGS entry point — `@DgsQuery`/`@DgsMutation`/`@DgsData`, `@Throttled`, `@PreAuthorize` | `graphql-api` | `apiLayer` |
| `*Controller` | REST entry point, implementing a generated `http-api` interface | `http-api` | `apiLayer` |
| `*Guard` | Authorisation checks for one feature | `domain` | `serviceLayer` |
| `*UseCase` | Orchestration — the unit most business logic belongs in | `domain` | `serviceLayer` |
| `*Service` | A single capability, often an adapter to something external | `domain`, or `server-core` when it is infrastructure behind a port | `serviceLayer` |
| `*Repository` | Repository interface; Spring Data implementation | interface in `domain`, implementation in `jpa-data` | `repositoryLayer` |
| outbound port | Interface named after the capability (`TokenIssuer`, `Scraper`, …) that a use case needs from infrastructure | interface in `domain`, implemented in `server-core` | — |

Use cases never import generated GraphQL types (`org.migor.feedless.generated`) or `data.jpa`; mapping to DTOs happens in the adapters. Put a new file in the feature package, not in a layer package. DTO conversion goes through `toDto()` extensions; entity mapping through MapStruct in `jpa-data`.

Resolvers are `suspend` and use `coroutineScope`/`withContext` — do not block inside one.

## Database

- Migrations: `jpa-data/src/main/resources/db/migration/V<n>__<snake_case>.sql`, applied up to `spring.flyway.target` in `server-core`'s `application-database.yaml` (currently `V91`). **Additive only** (AGENTS.md #5) — Flyway checksums what it has applied.
- Entities and repositories live in `jpa-data`, interfaces in `domain`. PostGIS types are in use (`JtsUtil`), so a plain Postgres container is not enough. Persistence integration tests live in `jpa-data`.

## Tests

Run: `./gradlew :packages:domain:test :packages:graphql-api:test :packages:http-api:test :packages:jpa-data:test :packages:server-core:test`. **Docker must be running** for `jpa-data` and `server-core` — `PostgreSQLExtension` (a `jpa-data` test fixture) starts a container. Without it the failure looks like a connection bug, not a missing prerequisite.

- Tests live next to their code: use-case tests in `domain`, resolver tests in `graphql-api`, controller tests in `http-api`, persistence tests in `jpa-data`; tests that need the whole application stay in `server-core`.
- `*Test.kt` — unit; `*IntTest.kt` — Spring context (30 `@SpringBootTest` classes).
- `Mother` is a `domain` test fixture: `testImplementation(testFixtures(project(":packages:domain")))`.
- `@Tag("nlp")` (12 tests) and `@Tag("unstable")` (1) are **excluded from every run** by the `domain` and `server-core` `build.gradle.kts`. A green `./gradlew test` did not execute them. Do not tag a failing test to make the build pass.
- AssertJ (`assertThat`) for assertions, `mockito-kotlin` for mocks, `@MockitoBean` for context slices, `runTest` for coroutines.
- Some test files are entirely commented out (e.g. `RepositoryResolverIntTest.kt`). They are dead, not disabled — a commented test is not coverage.

Write the failing test before the implementation.
