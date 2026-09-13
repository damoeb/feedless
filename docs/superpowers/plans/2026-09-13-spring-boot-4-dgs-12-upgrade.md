# Spring Boot 4.1 and DGS 12 Upgrade Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Move the backend from Spring Boot 3.4.11 / DGS 9.2.2 (legacy WebSocket stack) to Spring Boot 4.1.1 / DGS 12.0.1 on Spring for GraphQL, with Kotlin 2.3.21 and Gradle 8.14.5, without changing observable behaviour.

**Architecture:** Pin the behaviour that the upgrade could change silently (ETag bytes, Telegram update parsing, the agent's WebSocket subscription) with tests on today's stack first. Then upgrade in three green steps: Gradle; Boot 3.5 + DGS 10 (switches subscriptions to Spring for GraphQL); Boot 4.1 + DGS 12 + Kotlin 2.3 (one commit, because nothing compiles in between).

**Tech Stack:** Kotlin, Spring Boot, Netflix DGS, Spring for GraphQL, Jackson 3, Hibernate 7, Flyway, Testcontainers, JUnit 5, Mockito-Kotlin, Gradle.

**Spec:** `docs/superpowers/specs/2026-09-13-spring-boot-4-dgs-12-upgrade-design.md`

## Global Constraints

- Target versions: Spring Boot `4.1.1`, DGS `12.0.1`, dgs-codegen `8.6.0`, Kotlin `2.3.21`, Gradle `8.14.5`. Intermediate step: Spring Boot `3.5.16`, DGS `10.6.0`.
- Versions of Flyway, Hibernate (`hibernate-spatial`), Reactor, Testcontainers, kotlinx-coroutines and `spring-graphql-test` come from the Spring Boot BOM; the catalog carries no version for them after this plan.
- kotlin-jdsl `3.9.0` with `spring-data-jpa-boot4-support`; `telegrambots-meta` `10.3.0` replaces `telegrambots-spring-boot-starter`.
- Subscriptions stay on `/subscriptions` (`spring.graphql.websocket.path: /subscriptions`); `spring.graphql.websocket.connection-init-timeout: 10s` (DGS 9.2.2's default).
- Flyway migrations stay byte-identical (AGENTS.md rule 5); `spring.flyway.target` stays `92`.
- Every step ends with `./gradlew lint test` exit 0 and an executed test count that is not lower than the baseline from Task 1.
- Commits: Conventional Commits, `type(scope): subject`, ending with the session's attribution lines. Stage only files this task touched (never the untracked `.sdkmanrc`, `.superpowers/` or the three untracked plans under `docs/superpowers/plans/`).
- Use `trash`, never `rm`, to delete files outside git; use `git rm` for tracked files.
- Code comments: one line, why only.

## Deviations from the spec, decided while planning

- **Flyway loses its pin in Task 4 (Boot 3.5), not Task 5.** Boot 3.5.16 manages Flyway 11.7.2, and its auto-configuration running against the pinned 9.22.3 is untested territory. The pin had no reason. So the Flyway checksum validation runs after Task 4 and again after Task 5.
- **No `@EnableDgsTest` is needed.** No test assembles `DgsAutoConfiguration` (verified: the only hits are commented-out lines in `RepositoryResolverIntTest`).
- **Jackson 3 sorts properties alphabetically by default** (`MapperFeature.SORT_PROPERTIES_ALPHABETICALLY` is `true` in 3.1.5, `false` in 2.x). `ETagCalculator` disables it explicitly, and Boot's mapper gets `spring.jackson.mapper.sort-properties-alphabetically: false`. Jackson 3 also defaults `FAIL_ON_UNKNOWN_PROPERTIES` to `false`, which only makes reading more lenient.
- **Six tests mock `WebSocketHandler`** to silence the legacy DGS handler. Under Spring for GraphQL that mock breaks context startup (the WebSocket mapping injects the concrete `GraphQlWebSocketHandler`), so Task 4 removes those mocks.
- **The Elasticsearch exclusions in `FeedlessApplication` go away in Task 5.** Their classes live in Boot 4's `spring-boot-elasticsearch`/`spring-boot-data-elasticsearch` modules, which are not on the classpath.

## Verified class moves (Boot 3 → Boot 4.1.1)

| Boot 3 | Boot 4.1.1 |
|---|---|
| `org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration` | `org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration` |
| `org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration` | `org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration` |
| `org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration` | `org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration` |
| `org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration` | `org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration` |
| `org.springframework.boot.autoconfigure.mail.MailSenderValidatorAutoConfiguration` | `org.springframework.boot.mail.autoconfigure.MailSenderValidatorAutoConfiguration` |
| `org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration` | `org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration` |
| `org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations` | `org.springframework.boot.webmvc.autoconfigure.WebMvcRegistrations` |
| `org.springframework.boot.autoconfigure.domain.EntityScan` | `org.springframework.boot.persistence.autoconfigure.EntityScan` |
| `org.springframework.boot.web.servlet.error.ErrorController` | `org.springframework.boot.webmvc.error.ErrorController` |
| `org.springframework.boot.test.web.client.TestRestTemplate` | `org.springframework.boot.resttestclient.TestRestTemplate` |
| `org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest` | `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest` |
| `org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc` | `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc` |
| `org.springframework.boot.http.client.ClientHttpRequestFactorySettings.Redirects.DONT_FOLLOW` | `org.springframework.boot.http.client.HttpRedirects.DONT_FOLLOW` (via `TestRestTemplate.withRedirects`) |
| `com.fasterxml.jackson.databind.*` | `tools.jackson.databind.*` (annotations keep `com.fasterxml.jackson.annotation`) |
| `com.fasterxml.jackson.module.kotlin.*` | `tools.jackson.module.kotlin.*` |
| `org.telegram.telegrambots.meta.api.objects.Message` | `org.telegram.telegrambots.meta.api.objects.message.Message` |

Unchanged: `LocalServerPort`, `SpringBootTest`, `TestConfiguration`, `FilterRegistrationBean`, `ConfigurationProperties`, the `condition.*` annotations, `AutoConfiguration`, `SpringBootApplication`, `KotlinJdslAutoConfiguration`, Testcontainers' `PostgisContainerProvider`/`JdbcDatabaseContainer`/`Slf4jLogConsumer`/`@Testcontainers`.

## Test count

Used as the gate in every task. Run from the repo root:

```bash
find packages -type d -path '*/build/test-results/test' -prune -exec trash {} +
./gradlew test
find packages -path '*/build/test-results/test/TEST-*.xml' -exec grep -ho '<testsuite [^>]*' {} + \
  | grep -oE ' tests="[0-9]+"' | grep -oE '[0-9]+' | paste -sd+ - | bc
```

---

### Task 1: Pin Jackson-dependent behaviour on today's stack

**Files:**
- Create: `packages/http-api/src/test/kotlin/org/migor/feedless/http/ETagCalculatorTest.kt`
- Create: `packages/server-core/src/test/kotlin/org/migor/feedless/transport/TelegramUpdatesResponseTest.kt`

**Interfaces:**
- Consumes: `ETagCalculator.compute(value: Any): String` (`packages/http-api/src/main/kotlin/org/migor/feedless/http/ETagCalculator.kt`); `data class TelegramUpdatesResponse(val ok: Boolean, val result: List<Update>)` (`TelegramBotService.kt:304`).
- Produces: two tests Task 5 relies on to catch Jackson 3 changes.

- [ ] **Step 1: Record the baseline test count**

Run the "Test count" block above on the unchanged branch. Write the number into the task's report; every later task compares against it.

- [ ] **Step 2: Write the ETag test**

```kotlin
package org.migor.feedless.http

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.security.MessageDigest
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.HexFormat

class ETagCalculatorTest {

  // Declared out of alphabetical order, so a mapper that sorts properties changes the hash.
  data class Sample(val name: String, val at: OffsetDateTime)

  @Test
  fun `hashes declaration-ordered JSON with ISO dates`() {
    val value = Sample(name = "a", at = OffsetDateTime.of(2026, 9, 13, 10, 15, 30, 0, ZoneOffset.UTC))
    val json = """{"name":"a","at":"2026-09-13T10:15:30Z"}"""
    val expected = "\"" + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(json.toByteArray())) + "\""

    assertThat(ETagCalculator().compute(value)).isEqualTo(expected)
  }
}
```

- [ ] **Step 3: Run it; it must pass on today's stack**

Run: `./gradlew :packages:http-api:test --tests 'org.migor.feedless.http.ETagCalculatorTest'`
Expected: PASS. If it fails, the expected JSON does not match what Jackson 2 writes today: print `String(jacksonObjectMapper().registerModule(JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).writeValueAsBytes(value))` in the test, put that exact string into `json`, and rerun. The test pins today's bytes; it must not be made to pass by changing `ETagCalculator`.

- [ ] **Step 4: Write the Telegram parsing test**

```kotlin
package org.migor.feedless.transport

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TelegramUpdatesResponseTest {

  @Test
  fun `parses a getUpdates payload into telegrambots updates`() {
    val payload = """
      {"ok":true,"result":[{"update_id":7,"message":{"message_id":1,"date":0,
      "chat":{"id":42,"type":"private"},"text":"/start",
      "entities":[{"type":"bot_command","offset":0,"length":6}]}}]}
    """.trimIndent()

    val response = jacksonObjectMapper().readValue<TelegramUpdatesResponse>(payload)

    val update = response.result.single()
    assertThat(update.updateId).isEqualTo(7)
    assertThat(update.message.chatId).isEqualTo(42L)
    assertThat(update.message.text).isEqualTo("/start")
    assertThat(update.message.isCommand).isTrue()
  }
}
```

- [ ] **Step 5: Run it; it must pass on today's stack**

Run: `./gradlew :packages:server-core:test --tests 'org.migor.feedless.transport.TelegramUpdatesResponseTest'`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add packages/http-api/src/test/kotlin/org/migor/feedless/http/ETagCalculatorTest.kt \
        packages/server-core/src/test/kotlin/org/migor/feedless/transport/TelegramUpdatesResponseTest.kt
git commit -m "test(server-core): pin ETag bytes and Telegram update parsing before the Jackson 3 move"
```

---

### Task 2: WebSocket regression test for the agent subscription

**Files:**
- Create: `packages/server-core/src/test/kotlin/org/migor/feedless/browserautomation/RegisterAgentSubscriptionIntTest.kt`

**Interfaces:**
- Consumes: `BrowserAutomationService.registerAgent(data: RegisterAgentInput): Publisher<AgentEvent>` (suspend, implements `BrowserAutomationGateway`); generated `AgentEvent(callbackId, corrId, authentication, scrape)` and `AgentAuthentication(token)`; `PostgreSQLExtension` (jpa-data test fixture, needs Docker).
- Produces: `RegisterAgentSubscriptionIntTest`, which the browser-automation-adapter plan later extends with trust assertions.

The test speaks raw `graphql-transport-ws` so it is independent of the server stack: it must pass on DGS 9 now, after Task 4 and after Task 5.

- [ ] **Step 1: Write the test**

```kotlin
package org.migor.feedless.browserautomation

import com.google.gson.Gson
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PostgreSQLExtension
import org.migor.feedless.api.graphql.ServerConfigResolver
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.document.DocumentUseCase
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.generated.types.AgentAuthentication
import org.migor.feedless.generated.types.AgentEvent
import org.migor.feedless.order.OrderRepository
import org.migor.feedless.payment.PaymentUseCase
import org.migor.feedless.pipeline.SourcePipelineService
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.product.ProductRepository
import org.migor.feedless.product.ProductUseCase
import org.migor.feedless.repository.InboxService
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.wheneverBlocking
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler
import reactor.core.publisher.Flux
import java.net.URI
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/** Raw graphql-transport-ws, so the same test guards the legacy DGS handler and Spring for GraphQL. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(PostgreSQLExtension::class)
@DirtiesContext
@MockitoBean(
  types = [
    OAuth2AuthorizedClientService::class,
    ServerConfigResolver::class,
    PaymentUseCase::class,
    ProductRepository::class,
    ProductUseCase::class,
    DocumentRepository::class,
    DocumentUseCase::class,
    InboxService::class,
    OrderRepository::class,
    SourcePipelineService::class,
    PlanConstraintsService::class,
  ]
)
@ActiveProfiles(
  "test",
  "database",
  AppLayer.api,
  AppLayer.service,
  AppLayer.repository,
  AppLayer.security,
  AppProfiles.properties,
  AppProfiles.session,
  AppProfiles.user,
  AppProfiles.repository,
  AppProfiles.source,
  AppProfiles.scrape,
  AppProfiles.secrets,
  AppProfiles.browserAutomation,
)
class RegisterAgentSubscriptionIntTest {

  @LocalServerPort
  private var port = 0

  @MockitoBean
  private lateinit var browserAutomationService: BrowserAutomationService

  @MockitoBean
  private lateinit var featureService: FeatureService

  private val registerAgentQuery = """
    subscription {
      registerAgent(data: {
        secretKey: {email: "agent@example.org", secretKey: "secret"},
        os: {arch: "x64", platform: "linux"},
        version: "1.0.0", name: "ws-test", connectionId: "ws-test-connection"
      }) { callbackId authentication { token } }
    }
  """.trimIndent()

  @Test
  fun `an agent subscribes to registerAgent over graphql-transport-ws`() {
    wheneverBlocking { browserAutomationService.registerAgent(any()) }
      .thenReturn(Flux.just(AgentEvent(callbackId = "none", corrId = "c1", authentication = AgentAuthentication(token = "test-token"))))

    val received = LinkedBlockingQueue<String>()
    val handler = object : TextWebSocketHandler() {
      override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        received.add(message.payload)
      }
    }
    val headers = WebSocketHttpHeaders().apply { secWebSocketProtocol = listOf("graphql-transport-ws") }
    val session = StandardWebSocketClient()
      .execute(handler, headers, URI.create("ws://localhost:$port/subscriptions"))
      .get(10, TimeUnit.SECONDS)

    try {
      session.sendMessage(TextMessage("""{"type":"connection_init","payload":{}}"""))
      assertThat(received.poll(10, TimeUnit.SECONDS)).containsPattern(typeIs("connection_ack"))

      val subscribe = mapOf("id" to "1", "type" to "subscribe", "payload" to mapOf("query" to registerAgentQuery))
      session.sendMessage(TextMessage(Gson().toJson(subscribe)))
      assertThat(received.poll(10, TimeUnit.SECONDS))
        .containsPattern(typeIs("next"))
        .contains("test-token")
    } finally {
      session.close()
    }

    verifyBlocking(browserAutomationService) { registerAgent(argThat { secretKey.email == "agent@example.org" }) }
  }

  private fun typeIs(type: String) = "\"type\"\\s*:\\s*\"$type\""
}
```

- [ ] **Step 2: Run it; it must pass on today's stack**

Run: `./gradlew :packages:server-core:test --tests 'org.migor.feedless.browserautomation.RegisterAgentSubscriptionIntTest'`
Expected: PASS. It needs Docker (PostGIS via Testcontainers). If the context fails to start with "no such bean", a profile is missing: compare `@ActiveProfiles` with the failing bean's `@Profile` (AGENTS.md rule 1) and add the missing profile; do not add mocks for real beans.

- [ ] **Step 3: Commit**

```bash
git add packages/server-core/src/test/kotlin/org/migor/feedless/browserautomation/RegisterAgentSubscriptionIntTest.kt
git commit -m "test(server-core): guard the agent's registerAgent subscription over graphql-transport-ws"
```

---

### Task 3: Gradle wrapper 8.14.5 (step A)

**Files:**
- Modify: `gradle/wrapper/gradle-wrapper.properties`, `gradle/wrapper/gradle-wrapper.jar`, `gradlew`, `gradlew.bat`

**Interfaces:** none.

- [ ] **Step 1: Upgrade the wrapper**

```bash
./gradlew wrapper --gradle-version 8.14.5 --distribution-type bin
./gradlew wrapper --gradle-version 8.14.5 --distribution-type bin
```

Run it twice: the second run regenerates `gradle-wrapper.jar` and the scripts with 8.14.5 itself.

- [ ] **Step 2: Verify**

Run: `grep distributionUrl gradle/wrapper/gradle-wrapper.properties`
Expected: `distributionUrl=https\://services.gradle.org/distributions/gradle-8.14.5-bin.zip`

Run: `./gradlew lint test`, then the "Test count" block.
Expected: exit 0; count ≥ baseline.

- [ ] **Step 3: Commit**

```bash
git add gradle/wrapper/gradle-wrapper.properties gradle/wrapper/gradle-wrapper.jar gradlew gradlew.bat
git commit -m "build(deps): upgrade the Gradle wrapper to 8.14.5"
```

---

### Task 4: Spring Boot 3.5.16 + DGS 10.6.0 on Spring for GraphQL (step B)

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `packages/server-core/build.gradle.kts`
- Modify: `packages/jpa-data/build.gradle.kts`
- Delete: `packages/server-core/src/main/kotlin/org/migor/feedless/config/CustomDgsWebSocketConfig.kt`
- Delete: `packages/server-core/src/main/kotlin/org/migor/feedless/session/AuthenticationHttpSessionHandshakeInterceptor.kt`
- Modify: `packages/server-core/src/main/kotlin/org/migor/feedless/FeedlessApplication.kt`
- Modify: `packages/server-core/src/main/resources/application.yaml`
- Modify: `packages/server-core/src/test/kotlin/org/migor/feedless/TestConfigurations.kt`
- Modify: `packages/server-core/src/test/kotlin/org/migor/feedless/document/DocumentControllerIntTest.kt`
- Modify: `packages/server-core/src/test/kotlin/org/migor/feedless/api/throttle/ThrottleAspectIntTest.kt`
- Modify: `packages/server-core/src/test/kotlin/org/migor/feedless/api/graphql/QueryResolverIntTest.kt`
- Modify: `packages/server-core/src/test/kotlin/org/migor/feedless/api/graphql/ScrapeQueryResolverIntTest.kt`
- Modify: `packages/server-core/src/test/kotlin/org/migor/feedless/transform/WebToFeedTransformerIntTest.kt`
- Modify: `packages/server-core/src/test/kotlin/org/migor/feedless/feed/FeedControllerIntTest.kt`

**Interfaces:**
- Consumes: `RegisterAgentSubscriptionIntTest` (Task 2) as the transport guard.
- Produces: subscriptions served by Spring for GraphQL on `/subscriptions`; catalog aliases `libs.flyway.database.postgresql` and a versionless `libs.flyway.core`, `libs.hibernate.spatial`, `libs.reactor.core`, `libs.reactor.test`, `libs.spring.graphql.test`.

- [ ] **Step 1: Version catalog**

In `gradle/libs.versions.toml`:

- `[versions]`: set `spring-boot = "3.5.16"` and `dgs = "10.6.0"`; delete the lines `reactor = "3.5.0"`, `hibernate-spatial = "6.4.10.Final"`, `flyway = "9.22.3"`, `spring-graphql-test = "2.0.0-M3"`.
- `[libraries]`: replace these entries exactly:

```toml
dgs-starter = { module = "com.netflix.graphql.dgs:dgs-starter", version.ref = "dgs" }
flyway-core = { module = "org.flywaydb:flyway-core" }
flyway-database-postgresql = { module = "org.flywaydb:flyway-database-postgresql" }
hibernate-spatial = { module = "org.hibernate.orm:hibernate-spatial" }
reactor-core = { module = "io.projectreactor:reactor-core" }
reactor-test = { module = "io.projectreactor:reactor-test" }
spring-graphql-test = { module = "org.springframework.graphql:spring-graphql-test" }
```

and delete `dgs-subscriptions` and `dgs-subscriptions-autoconfigure`.

- [ ] **Step 2: Build files**

`packages/server-core/build.gradle.kts`: delete the lines `implementation("org.springframework.security:spring-security-messaging")`, `implementation(libs.dgs.subscriptions)` and `implementation(libs.dgs.subscriptions.autoconfigure)`; directly under `implementation(libs.flyway.core)` add `implementation(libs.flyway.database.postgresql)`.

`packages/jpa-data/build.gradle.kts`: replace

```kotlin
  // match server-core, whose dependency-management plugin keeps the catalog's Flyway over the BOM's
  testImplementation(libs.flyway.core) { version { strictly(libs.versions.flyway.get()) } }
```

with

```kotlin
  testImplementation(libs.flyway.core)
  testImplementation(libs.flyway.database.postgresql)
```

- [ ] **Step 3: Remove the legacy WebSocket stack**

```bash
git rm packages/server-core/src/main/kotlin/org/migor/feedless/config/CustomDgsWebSocketConfig.kt \
       packages/server-core/src/main/kotlin/org/migor/feedless/session/AuthenticationHttpSessionHandshakeInterceptor.kt
```

In `FeedlessApplication.kt` delete `import com.netflix.graphql.dgs.subscriptions.websockets.DgsWebSocketAutoConfig` and the `DgsWebSocketAutoConfig::class,` entry in `exclude`.

- [ ] **Step 4: Serve subscriptions through Spring for GraphQL**

In `packages/server-core/src/main/resources/application.yaml`, under the existing top-level `spring:` key (next to `jpa:`), add:

```yaml
  graphql:
    websocket:
      path: /subscriptions
      # DGS 9's default; Spring for GraphQL would wait 60s for connection_init
      connection-init-timeout: 10s
```

- [ ] **Step 5: Drop the WebSocket mocks that targeted the legacy handler**

In `TestConfigurations.kt` delete the `DisableWebSocketsConfiguration` class with its `@TestConfiguration`/`@MockitoBean(types = [WebSocketHandler::class])` annotations, and the imports `org.springframework.web.socket.WebSocketHandler` and (if now unused) `org.springframework.test.context.bean.override.mockito.MockitoBean`.

In each of `DocumentControllerIntTest.kt`, `ThrottleAspectIntTest.kt`, `QueryResolverIntTest.kt`, `ScrapeQueryResolverIntTest.kt`, `WebToFeedTransformerIntTest.kt`: delete `import org.migor.feedless.DisableWebSocketsConfiguration` and remove `DisableWebSocketsConfiguration::class` from the `@Import(...)` list (in `ThrottleAspectIntTest` the line becomes `@Import(DisableDatabaseConfiguration::class, DisableSecurityConfiguration::class)`).

In `FeedControllerIntTest.kt` remove `WebSocketHandler::class,` from the `@MockitoBean(types = [...])` list and delete `import org.springframework.web.socket.WebSocketHandler`.

- [ ] **Step 6: Run the transport guard**

Run: `./gradlew :packages:server-core:test --tests 'org.migor.feedless.browserautomation.RegisterAgentSubscriptionIntTest'`
Expected: PASS, now served by Spring for GraphQL.

- [ ] **Step 7: Run everything**

Run: `./gradlew lint test`, then the "Test count" block.
Expected: exit 0; count ≥ baseline. `AuthenticationIntTest` and `MailAuthResolverIntTest` passing proves the cookie code still finds a `ServletWebRequest` in `DgsWebMvcRequestData`. If they fail on the `DgsWebMvcRequestData` cast, replace the lookup in `AuthAnonymousResolver`, `MailAuthResolver` and `SessionResolver` with `(RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.response` and rerun.

- [ ] **Step 8: Flyway checksum validation (Flyway 9.22.3 → 11.7.2)**

```bash
git worktree add ../feedless-develop develop
docker compose up -d postgis
(cd ../feedless-develop && ./gradlew :packages:server-core:bootRun --args='--spring.profiles.active=dev,database')
```

Stop it (Ctrl-C) once the log shows Flyway's `Successfully applied` or `Schema "public" is up to date`; the database at `localhost:5433/feedless` (local dev's database) is now migrated by develop's Flyway. Flyway runs before any other bean, so this holds even if the `dev,database` context stops later for an unrelated missing profile. Then on this branch:

```bash
./gradlew :packages:server-core:bootRun --args='--spring.profiles.active=dev,database'
```

Expected: the log shows Flyway's `Successfully validated` and no `FlywayValidateException`. Stop it, then `git worktree remove ../feedless-develop`.

- [ ] **Step 9: e2e smoke**

```bash
./gradlew buildImages
./gradlew :packages:cli:e2eTest
```

Expected: PASS. The e2e stack starts `damoeb/feedless:core-latest` and `damoeb/feedless:browser-automation-app-latest`, the tags `buildImages` just produced, and waits for the core to log `Adding Agent` (`packages/cli/e2e/stack_test.go:173`), which only happens after the agent's `registerAgent` subscription succeeds over `/subscriptions`. This is the spec's agent smoke.

- [ ] **Step 10: Commit**

```bash
git add -u gradle packages
git commit -m "build(deps): move to Spring Boot 3.5 and DGS 10 with Spring for GraphQL subscriptions"
```

---

### Task 5: Spring Boot 4.1.1 + DGS 12.0.1 + Kotlin 2.3.21 (step C)

One commit: nothing compiles between its steps.

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `packages/domain/build.gradle.kts`, `packages/http-api/build.gradle.kts`, `packages/server-core/build.gradle.kts`, `packages/jpa-data/build.gradle.kts`
- Modify (imports): the files listed in Step 3
- Modify (code): `packages/http-api/src/main/kotlin/org/migor/feedless/http/ETagCalculator.kt`, `packages/http-api/src/main/kotlin/org/migor/feedless/http/mapper/HttpScrapeFlowMapper.kt`, `packages/server-core/src/main/kotlin/org/migor/feedless/FeedlessApplication.kt`, `packages/jpa-data/src/test/kotlin/org/migor/feedless/data/jpa/JpaDataTestApplication.kt`, `packages/mail-adapter/src/test/kotlin/org/migor/feedless/mail/MailServiceWithJavaMailIntTest.kt`, `packages/mail-adapter/src/test/kotlin/org/migor/feedless/mail/MailServiceWithMailgunIntTest.kt`, `packages/server-core/src/test/kotlin/org/migor/feedless/feed/FeedControllerIntTest.kt`, `packages/server-core/src/test/kotlin/org/migor/feedless/document/DocumentControllerIntTest.kt`, `packages/server-core/src/main/resources/application.yaml`

**Interfaces:**
- Consumes: `ETagCalculatorTest`, `TelegramUpdatesResponseTest` (Task 1), `RegisterAgentSubscriptionIntTest` (Task 2).
- Produces: the upgraded platform the browser-automation-adapter plan builds on (Spring for GraphQL 2.0.5 `WebSocketGraphQlInterceptor`).

- [ ] **Step 1: Version catalog**

`[versions]`: set `kotlin = "2.3.21"`, `spring-boot = "4.1.1"`, `dgs = "12.0.1"`, `dgs-codegen = "8.6.0"`, `kotlin-jdsl = "3.9.0"`; delete `kotlinx-coroutines = "1.7.2"`, `testcontainers = "1.21.4"`, `telegrambots = "6.9.0"`.

`[plugins]`: add

```toml
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

`[libraries]`: replace these entries exactly:

```toml
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core" }
kotlinx-coroutines-reactor = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-reactor" }
kotlinx-coroutines-slf4j = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-slf4j" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test" }
kotlin-jdsl-spring-support = { module = "com.linecorp.kotlin-jdsl:spring-data-jpa-boot4-support", version.ref = "kotlin-jdsl" }
testcontainers-core = { module = "org.testcontainers:testcontainers" }
testcontainers-junit = { module = "org.testcontainers:testcontainers-junit-jupiter" }
testcontainers-postgresql = { module = "org.testcontainers:testcontainers-postgresql" }
telegrambots-meta = { module = "org.telegram:telegrambots-meta", version = "10.3.0" }
```

delete `telegrambots-spring-boot-starter`, and add:

```toml
spring-boot-flyway = { module = "org.springframework.boot:spring-boot-starter-flyway", version.ref = "spring-boot" }
spring-boot-webmvc-test = { module = "org.springframework.boot:spring-boot-starter-webmvc-test", version.ref = "spring-boot" }
spring-boot-resttestclient = { module = "org.springframework.boot:spring-boot-resttestclient", version.ref = "spring-boot" }
spring-boot-restclient = { module = "org.springframework.boot:spring-boot-starter-restclient", version.ref = "spring-boot" }
```

- [ ] **Step 2: Build files**

`packages/domain/build.gradle.kts`: replace `kotlin("plugin.serialization") version "1.9.0"` with `alias(libs.plugins.kotlin.serialization)`.

`packages/http-api/build.gradle.kts`: replace `implementation("com.fasterxml.jackson.module:jackson-module-kotlin")` with `implementation("tools.jackson.module:jackson-module-kotlin")`; add `testImplementation(libs.spring.boot.webmvc.test)`.

`packages/server-core/build.gradle.kts`: replace `implementation("com.fasterxml.jackson.module:jackson-module-kotlin")` with `implementation("tools.jackson.module:jackson-module-kotlin")`; replace `implementation(libs.flyway.core)` with `implementation(libs.spring.boot.flyway)`; replace `implementation(libs.telegrambots.spring.boot.starter)` with `implementation(libs.telegrambots.meta)`; add `testImplementation(libs.spring.boot.resttestclient)` and `testImplementation(libs.spring.boot.restclient)`.

`packages/jpa-data/build.gradle.kts`: replace `testImplementation(libs.flyway.core)` with `testImplementation(libs.spring.boot.flyway)`.

- [ ] **Step 3: Import moves**

Run from the repo root; each command rewrites only the listed files. The heredoc makes it run under bash even from zsh.

```bash
bash <<'EOF'
p() { perl -pi -e "$1" "${@:2}"; }
S=packages/server-core/src
H=packages/http-api/src

p 's/org\.springframework\.boot\.autoconfigure\.jdbc\.DataSourceAutoConfiguration/org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration/' \
  $S/test/kotlin/org/migor/feedless/TestConfigurations.kt
p 's/org\.springframework\.boot\.autoconfigure\.mail\.MailSenderValidatorAutoConfiguration/org.springframework.boot.mail.autoconfigure.MailSenderValidatorAutoConfiguration/' \
  $S/test/kotlin/org/migor/feedless/TestConfigurations.kt \
  packages/mail-adapter/src/test/kotlin/org/migor/feedless/mail/MailServiceWithJavaMailIntTest.kt \
  packages/mail-adapter/src/test/kotlin/org/migor/feedless/mail/MailServiceWithMailgunIntTest.kt
p 's/org\.springframework\.boot\.autoconfigure\.security\.servlet\.SecurityAutoConfiguration/org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration/; s/org\.springframework\.boot\.actuate\.autoconfigure\.security\.servlet\.ManagementWebSecurityAutoConfiguration/org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration/' \
  $S/test/kotlin/org/migor/feedless/TestConfigurations.kt
p 's/org\.springframework\.boot\.autoconfigure\.web\.servlet\.WebMvcRegistrations/org.springframework.boot.webmvc.autoconfigure.WebMvcRegistrations/' \
  $S/main/kotlin/org/migor/feedless/http/HttpApiWebMvcConfiguration.kt \
  $H/test/kotlin/org/migor/feedless/http/RequestContextBridge.kt
p 's/org\.springframework\.boot\.web\.servlet\.error\.ErrorController/org.springframework.boot.webmvc.error.ErrorController/' \
  $H/main/kotlin/org/migor/feedless/api/http/AppErrorController.kt
p 's/org\.springframework\.boot\.test\.autoconfigure\.web\.servlet\.(WebMvcTest|AutoConfigureMockMvc)/org.springframework.boot.webmvc.test.autoconfigure.$1/g' \
  $(grep -rlE 'boot\.test\.autoconfigure\.web\.servlet' $H/test)
p 's/org\.springframework\.boot\.test\.web\.client\.TestRestTemplate/org.springframework.boot.resttestclient.TestRestTemplate/' \
  $(grep -rl 'boot.test.web.client.TestRestTemplate' $S/test)
p 's/com\.fasterxml\.jackson\.databind\./tools.jackson.databind./; s/com\.fasterxml\.jackson\.module\.kotlin\./tools.jackson.module.kotlin./' \
  $H/main/kotlin/org/migor/feedless/http/HttpApiPreHandlerExceptionResolver.kt \
  $S/test/kotlin/org/migor/feedless/api/graphql/AuthenticationIntTest.kt \
  $S/test/kotlin/org/migor/feedless/api/graphql/QueryResolverIntTest.kt \
  $S/test/kotlin/org/migor/feedless/config/SecurityConfigIntTest.kt \
  $S/test/kotlin/org/migor/feedless/mail/MailAuthResolverIntTest.kt \
  $S/test/kotlin/org/migor/feedless/transport/TelegramUpdatesResponseTest.kt
p 's/org\.telegram\.telegrambots\.meta\.api\.objects\.Message$/org.telegram.telegrambots.meta.api.objects.message.Message/' \
  $S/test/kotlin/org/migor/feedless/transport/TelegramBotServiceTest.kt
# Jackson 3 renamed JsonNode.asText() to asString()
p 's/\.asText\(\)/.asString()/g' \
  $S/test/kotlin/org/migor/feedless/config/SecurityConfigIntTest.kt
EOF
```

Then check nothing is left: `grep -rnE "boot\.autoconfigure\.(jdbc|mail|security|web|data|cache|domain)\.|boot\.test\.web\.client|boot\.web\.servlet\.error|com\.fasterxml\.jackson\.(databind|module|datatype)" packages --include='*.kt' --exclude-dir=build --exclude-dir=bin` must print only the commented lines in `FlywayConfig.kt` and whatever Steps 4–6 below still rewrite by hand (`FeedlessApplication.kt`, `JpaDataTestApplication.kt`, `MailServiceWithJavaMailIntTest.kt`, `MailServiceWithMailgunIntTest.kt`, `ETagCalculator.kt`, `HttpScrapeFlowMapper.kt`, `FeedControllerIntTest.kt`).

- [ ] **Step 4: Jackson 3 mappers**

`ETagCalculator.kt` becomes:

```kotlin
package org.migor.feedless.http

import org.springframework.stereotype.Component
import tools.jackson.databind.MapperFeature
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.module.kotlin.jacksonMapperBuilder
import java.security.MessageDigest
import java.util.HexFormat

/** The one ETag source for GET and PATCH, so they never disagree: a quoted SHA-256 of the endpoint's JSON. */
@Component
class ETagCalculator {

  // Declaration order and ISO dates, as under Jackson 2, so existing ETags stay valid.
  private val json = jacksonMapperBuilder()
    .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
    .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
    .build()

  /** A strong ETag, e.g. `"3f2504e0…"` — quotes included, ready for the `ETag`/`If-Match` headers. */
  fun compute(value: Any): String {
    val bytes = json.writeValueAsBytes(value)
    val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
    return "\"${HexFormat.of().formatHex(digest)}\""
  }
}
```

In `HttpScrapeFlowMapper.kt` replace `import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper` with `import tools.jackson.module.kotlin.jacksonMapperBuilder` and line 47 with:

```kotlin
  private val storedFlowJson = jacksonMapperBuilder()
    .changeDefaultPropertyInclusion { JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL) }
    .build()
```

(`import com.fasterxml.jackson.annotation.JsonInclude` stays.)

In `application.yaml`, under `spring:` add:

```yaml
  jackson:
    mapper:
      # Jackson 3 sorts alphabetically by default; keep declaration order in responses
      sort-properties-alphabetically: false
```

- [ ] **Step 5: Auto-configuration references**

`FeedlessApplication.kt` becomes:

```kotlin
package org.migor.feedless

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration

@Configuration
@SpringBootApplication(
  exclude = [
    DataJpaRepositoriesAutoConfiguration::class,
    CacheAutoConfiguration::class,
  ]
)
class FeedlessApplication

fun main(args: Array<String>) {
  runApplication<FeedlessApplication>(*args)
}
```

`JpaDataTestApplication.kt`: replace `import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration` with `import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration`, `import org.springframework.boot.autoconfigure.domain.EntityScan` with `import org.springframework.boot.persistence.autoconfigure.EntityScan`, and `exclude = [JpaRepositoriesAutoConfiguration::class]` with `exclude = [DataJpaRepositoriesAutoConfiguration::class]`.

`MailServiceWithJavaMailIntTest.kt` and `MailServiceWithMailgunIntTest.kt`: delete `import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration` and the `DataSourceAutoConfiguration::class,` entry in `exclude`. `mail-adapter` has no JDBC module under Boot 4, so there is nothing to exclude.

- [ ] **Step 6: Test client API moves**

`FeedControllerIntTest.kt`: replace `import org.springframework.boot.http.client.ClientHttpRequestFactorySettings` with `import org.springframework.boot.http.client.HttpRedirects`, and

```kotlin
    val restTemplate = TestRestTemplate()
      .withRequestFactorySettings { it.withRedirects(ClientHttpRequestFactorySettings.Redirects.DONT_FOLLOW) }
```

with

```kotlin
    val restTemplate = TestRestTemplate().withRedirects(HttpRedirects.DONT_FOLLOW)
```

`DocumentControllerIntTest.kt` is the one test that autowires `TestRestTemplate`: add `import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate` and annotate the class with `@AutoConfigureTestRestTemplate` next to its `@SpringBootTest`.

- [ ] **Step 7: Compile**

Run: `./gradlew compileKotlin compileTestKotlin`
Expected: BUILD SUCCESSFUL. Fix remaining errors by cause; the verified moves are in the table at the top. Do not hand-edit anything under `build/generated/**` (AGENTS.md rule 2); regenerate instead.

- [ ] **Step 8: Run the three pinning tests**

Run: `./gradlew :packages:http-api:test --tests 'org.migor.feedless.http.ETagCalculatorTest' :packages:server-core:test --tests 'org.migor.feedless.transport.TelegramUpdatesResponseTest' --tests 'org.migor.feedless.browserautomation.RegisterAgentSubscriptionIntTest'`
Expected: all PASS.

If `TelegramUpdatesResponseTest` fails because Jackson 3 cannot build telegrambots' types, parse updates with Jackson 2, which `telegrambots-meta` brings along. In `TelegramBotService.kt` add below the class's `private val log`:

```kotlin
  // telegrambots-meta types are built for Jackson 2; Spring's default converter is Jackson 3.
  private val telegramJson = com.fasterxml.jackson.databind.ObjectMapper()
    .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
```

replace `val response = restTemplate.getForObject(uri, TelegramUpdatesResponse::class.java)` and the `response?.result?.let { … }` block's source with `val updates = parseTelegramUpdates(telegramJson, restTemplate.getForObject(uri, String::class.java) ?: return@withMdcCorrId)`, iterating `updates` where the block iterated `response.result`, and add at file level:

```kotlin
internal fun parseTelegramUpdates(json: com.fasterxml.jackson.databind.ObjectMapper, body: String): List<Update> =
  json.readTree(body).path("result").map { json.treeToValue(it, Update::class.java) }
```

Point `TelegramUpdatesResponseTest` at `parseTelegramUpdates(com.fasterxml.jackson.databind.ObjectMapper(), payload)` instead of `TelegramUpdatesResponse`, and rerun.

- [ ] **Step 9: Run everything**

Run: `./gradlew lint test`, then the "Test count" block.
Expected: exit 0; count ≥ baseline. `jpa-data`'s persistence tests cover Hibernate 7's query validation and the kotlin-jdsl boot4 module; `AuthenticationIntTest`/`MailAuthResolverIntTest` cover the cookie path (same fallback as Task 4 Step 7 if the `DgsWebMvcRequestData` cast fails).

- [ ] **Step 10: Flyway checksum validation (→ 12.4.0)**

Repeat Task 4 Step 8 exactly. Expected: `Successfully validated`, `Started FeedlessApplication`, no `FlywayValidateException`.

- [ ] **Step 11: e2e smoke**

Repeat Task 4 Step 9. Expected: PASS. If a client fails on a GraphQL request-level error answered with HTTP 4xx and `application/graphql-response+json`, stop and report the failing request; do not change the response media type without a decision.

- [ ] **Step 12: Commit**

```bash
git add -u gradle packages
git commit -m "build(deps): move to Spring Boot 4.1, DGS 12, Jackson 3 and Kotlin 2.3"
```

---

### Task 6: Docs and final gate (step D)

**Files:**
- Modify: `AGENTS.md:30`, `AGENTS.md:81`

**Interfaces:** none.

- [ ] **Step 1: Update AGENTS.md**

Line 30: replace `Gradle 8.9 via \`./gradlew\`` with `Gradle 8.14.5 via \`./gradlew\``.

Line 81: replace `**Stack:** Kotlin / JDK 21 / Spring Boot / Netflix DGS /` with `**Stack:** Kotlin 2.3 / JDK 21 / Spring Boot 4.1 / Netflix DGS 12 (Spring for GraphQL) /`, and `· Gradle 8.9 ·` with `· Gradle 8.14.5 ·`.

`docs/rules/kotlin-spring.md` names none of the changed classes (verified); leave it.

- [ ] **Step 2: Final gate**

Run: `./gradlew lint test`, then the "Test count" block.
Expected: exit 0; count ≥ baseline + 3 (the tests added in Tasks 1–2).

- [ ] **Step 3: Commit**

```bash
git add AGENTS.md
git commit -m "docs: record Gradle 8.14.5, Spring Boot 4.1 and DGS 12 in AGENTS.md"
```
