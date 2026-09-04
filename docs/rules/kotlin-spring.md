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
| `packages/http-api/src/main/resources/openapi/openapi.yaml` | openapi-generator, `kotlin-spring`, `interfaceOnly` | `build/generated/src/main/kotlin` | `org.migor.feedless.http.api` / `.model` — implement the interfaces in `server-core` |
| `server-core/.../document/filter/FilterByExpression.jj` | JavaCC | `build/generated/javacc` | the document filter parser |

**Why:** all three regenerate on every build. A hand edit works locally until the next clean build, then disappears — leaving a CI failure with no diff that explains it.

To change the API, edit the **contract** and rebuild. Both contract modules hold no hand-written Kotlin at all.

Note: `server-core/src/generated/java/` holds a checked-in copy of the JavaCC output that is **not** on the source path (`build/generated/javacc` is). Do not edit it and do not assume it is current.

## Package and naming conventions

`server-core` is organised by **feature**, not by layer — `document/`, `repository/`, `source/`, `plan/`, `session/`, `user/`, `scrape/`, `pipeline/`, … Each feature package holds its own vertical slice:

| Suffix | Role | Layer |
|---|---|---|
| `*Resolver` | DGS entry point — `@DgsQuery`/`@DgsMutation`/`@DgsData`, `@Throttled`, `@PreAuthorize` | `apiLayer` |
| `*Controller` | REST entry point, implementing a generated `http-api` interface | `apiLayer` |
| `*Guard` | Authorisation checks for one feature | `apiLayer` |
| `*UseCase` | Orchestration — the unit most business logic belongs in | `serviceLayer` |
| `*Service` | A single capability, often an adapter to something external | `serviceLayer` |
| `*Repository` | Spring Data interface | `repositoryLayer` |

Put a new file in the feature package, not in a layer package. DTO conversion goes through `toDto()` extensions; entity mapping through MapStruct in `jpa-data`.

Resolvers are `suspend` and use `coroutineScope`/`withContext` — do not block inside one.

## Database

- Migrations: `server-core/src/main/resources/db/migration/V<n>__<snake_case>.sql`, currently through `V85`. **Additive only** (AGENTS.md #5) — Flyway checksums what it has applied.
- Entities and repositories live in `jpa-data`, interfaces in `domain`. PostGIS types are in use (`JtsUtil`), so a plain Postgres container is not enough.

## Tests

Run: `./gradlew :packages:server-core:test`. **Docker must be running** — `PostgreSQLExtension` starts a container. Without it the failure looks like a connection bug, not a missing prerequisite.

- `*Test.kt` — unit; `*IntTest.kt` — Spring context (20 of them).
- `@Tag("nlp")` (12 tests) and `@Tag("unstable")` (1) are **excluded from every run** by `build.gradle.kts`. A green `./gradlew test` did not execute them. Do not tag a failing test to make the build pass.
- AssertJ (`assertThat`) for assertions, `mockito-kotlin` for mocks, `@MockitoBean` for context slices, `runTest` for coroutines.
- Some test files are entirely commented out (e.g. `RepositoryResolverIntTest.kt`). They are dead, not disabled — a commented test is not coverage.

Write the failing test before the implementation.
