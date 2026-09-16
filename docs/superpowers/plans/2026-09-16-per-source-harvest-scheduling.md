# Per-Source Harvest Scheduling Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Honour `Retry-After` and host blocks by backing off the whole host persistently, and schedule harvests per source instead of per repository.

**Architecture:** A persisted `t_host_cooldown` table (port `HostCooldown` in `domain`, JPA adapter in `jpa-data`) is written by a `HostCooldownGuard` in `server-core` that `HttpService` and `ScrapeService` consult before every fetch. Sources get `next_harvest_at`; a new `SourceHarvesterExecutor` claims due sources (skipping cooled-down hosts) and `RepositoryHarvester.harvestSource` writes each source's next time as `max(cron next, now + retry delay)`. The repository's `nextUpdateAt` becomes a read-only `@Formula` over its sources.

**Tech Stack:** Kotlin 2.3, Spring Boot 4.1, JPA/Hibernate, Flyway, PostGIS/Testcontainers, AsyncHttpClient, JUnit 5, Mockito, AssertJ.

**Spec:** `docs/superpowers/specs/2026-09-16-per-source-harvest-scheduling-design.md`

## Global Constraints

- Branch `feature/per-source-harvest-scheduling` off `develop`; this worktree is `../feedless-per-source-scheduling`. Never touch the main checkout at `../feedless` (it holds unrelated uncommitted work).
- Definition of Done: `./gradlew lint test` exits 0.
- Beans are profile-gated; tests must enumerate the same profiles in `@ActiveProfiles` (AGENTS.md rule 1).
- Never edit a shipped migration. V96 raises `spring.flyway.target` to 96; V97 is committed pinned out (target stays 96).
- `Retry-After` fallback 5 minutes, cap 24 hours. Block ladder: strike 1 → 5 min, 2 → 30 min, 3 → 2 h, 4 → 12 h, ≥5 → 24 h.
- Host key everywhere: lower-cased host name of the URL, no port, no user info; a URL without scheme is treated as `https://`.
- No changes to `schema.graphqls` or `openapi.yaml`.
- Code comments: one line, English, why only.
- Commits: Conventional Commits `type(scope): subject`, ending with the `Co-Authored-By` / `Claude-Session` trailers given in the session.

## Deviations from the spec (apply in Task 1)

1. **No `t_source.host` column.** The due query derives the host from the source's first fetch action URL with a SQL regex, so it cannot drift from the actions. `hostOf()` in Kotlin and the SQL expression are pinned together by one integration test.
2. **`Repository.triggerScheduledNextAt` becomes `nextHarvestAt`**, a read-only `@Formula` (`min(next_harvest_at)` over enabled sources). Mappers keep producing `nextUpdateAt` from it.
3. **Dry runs may write cooldown rows**: a real 429/403 seen by a dry run is a real signal. Dry runs still never write `next_harvest_at`.
4. **`FlywayTargetTest` learns pinned-out migrations**: a migration whose first line is `-- flyway:pinned-out` is excluded from the "target equals highest version" check.

## File Structure

| File | Responsibility |
|---|---|
| `packages/jpa-data/src/main/resources/db/migration/V96__per_source_schedule_and_host_cooldown.sql` | create | `t_source.next_harvest_at` + backfill + index; `t_host_cooldown` |
| `packages/jpa-data/src/main/resources/db/migration/V97__drop_repository_trigger_scheduled_next_at.sql` | create | pinned-out drop of the old column |
| `packages/server-core/src/main/resources/application-database.yaml` | modify | target 96 |
| `packages/server-core/src/test/kotlin/org/migor/feedless/config/FlywayTargetTest.kt` | modify | pinned-out awareness |
| `packages/domain/src/main/kotlin/org/migor/feedless/common/Hosts.kt` | create | `hostOf(url)` |
| `packages/domain/src/main/kotlin/org/migor/feedless/common/RetryAfter.kt` | create | parse `Retry-After` |
| `packages/domain/src/main/kotlin/org/migor/feedless/hostCooldown/HostCooldown.kt` | create | port, `HostCooldownState`, `HostBlockLadder` |
| `packages/domain/src/main/kotlin/org/migor/feedless/Exceptions.kt` | modify | `HostBlockedException` |
| `packages/jpa-data/src/main/kotlin/org/migor/feedless/data/jpa/hostCooldown/HostCooldownEntity.kt` | create | entity |
| `packages/jpa-data/src/main/kotlin/org/migor/feedless/data/jpa/hostCooldown/HostCooldownDAO.kt` | create | upserts |
| `packages/jpa-data/src/main/kotlin/org/migor/feedless/data/jpa/hostCooldown/HostCooldownJpaRepository.kt` | create | adapter |
| `packages/jpa-data/src/test/kotlin/org/migor/feedless/hostCooldown/HostCooldownRepositoryIntTest.kt` | create | adapter tests |
| `packages/server-core/src/main/kotlin/org/migor/feedless/common/HostCooldownGuard.kt` | create | check before fetch, record after response |
| `packages/server-core/src/main/kotlin/org/migor/feedless/common/HttpService.kt` | modify | use guard, parse `Retry-After`, map 401/403 |
| `packages/server-core/src/main/kotlin/org/migor/feedless/scrape/ScrapeService.kt` | modify | guard before prerender |
| `packages/server-core/src/test/kotlin/org/migor/feedless/common/HostCooldownGuardTest.kt` | create | guard tests |
| `packages/server-core/src/test/kotlin/org/migor/feedless/common/HttpServiceTest.kt` | modify | status mapping against a JDK `HttpServer` |
| `packages/domain/src/main/kotlin/org/migor/feedless/source/Source.kt`, `SourceRepository.kt`, `SourceUseCase.kt` | modify | `nextHarvestAt`, due query, scheduling writes |
| `packages/jpa-data/src/main/kotlin/org/migor/feedless/data/jpa/source/SourceEntity.kt`, `SourceDAO.kt`, `SourceJpaRepository.kt` | modify | column + queries |
| `packages/jpa-data/src/test/kotlin/org/migor/feedless/source/SourceDueForHarvestIntTest.kt` | create | due query + host SQL parity |
| `packages/domain/src/main/kotlin/org/migor/feedless/repository/Repository.kt`, `RepositoryRepository.kt`, `RepositoryUseCase.kt`, `RepositoryHarvester.kt` | modify | per-source scheduling |
| `packages/jpa-data/src/main/kotlin/org/migor/feedless/data/jpa/repository/AbstractRepositoryEntity.kt`, `RepositoryDAO.kt`, `RepositoryJpaRepository.kt` | modify | `@Formula`, remove repo due query |
| `packages/graphql-api/.../api/mapper/RepositoryMapper.kt`, `packages/http-api/.../http/mapper/HttpRepositoryMapper.kt` | modify | read `nextHarvestAt` |
| `packages/server-core/src/main/kotlin/org/migor/feedless/repository/SourceHarvesterExecutor.kt` | create (replaces `RepositoryHarvesterExecutor.kt`) | claim due sources |

---

### Task 1: Migrations, Flyway target, spec amendments

**Files:**
- Create: `packages/jpa-data/src/main/resources/db/migration/V96__per_source_schedule_and_host_cooldown.sql`
- Create: `packages/jpa-data/src/main/resources/db/migration/V97__drop_repository_trigger_scheduled_next_at.sql`
- Modify: `packages/server-core/src/main/resources/application-database.yaml:43`
- Modify: `packages/server-core/src/test/kotlin/org/migor/feedless/config/FlywayTargetTest.kt`
- Modify: `docs/superpowers/specs/2026-09-16-per-source-harvest-scheduling-design.md`
- Modify: `AGENTS.md` (rule 5 target)

**Interfaces:**
- Produces: tables/columns `t_source.next_harvest_at timestamp`, `t_host_cooldown(host text pk, blocked_until timestamp, strikes int, last_status int, updated_at timestamp)`; the `-- flyway:pinned-out` marker convention.

- [ ] **Step 1: Add V97 and watch the existing guard fail**

`V97__drop_repository_trigger_scheduled_next_at.sql`:

```sql
-- flyway:pinned-out
-- Raise spring.flyway.target to 97 only once per-source scheduling (V96) has run cleanly in production.
ALTER TABLE t_repository DROP COLUMN trigger_scheduled_next_at;
```

Run: `./gradlew :packages:server-core:test --tests 'org.migor.feedless.config.FlywayTargetTest'`
Expected: FAIL — `new migration V97 added but spring.flyway.target is 95 — raise the target`.

- [ ] **Step 2: Teach the test about pinned-out migrations** — replace the first test in `FlywayTargetTest.kt` and its helper:

```kotlin
  @Test
  fun `spring flyway target matches the highest shipped migration version`() {
    val highestMigrationVersion = highestAppliedMigrationVersion()
    val configuredTarget = configuredFlywayTarget()

    assertEquals(
      highestMigrationVersion,
      configuredTarget,
      "new migration V$highestMigrationVersion added but spring.flyway.target is $configuredTarget — raise the target"
    )
  }

  @Test
  fun `pinned-out migrations all lie above the target`() {
    val target = configuredFlywayTarget()
    val misplaced = pinnedOutVersions().filter { it <= target }

    assertTrue(misplaced.isEmpty(), "pinned-out migrations at or below target $target would be applied: V$misplaced")
  }

  // Staged destructive migrations ship pinned out, so the target is raised deliberately later.
  private fun pinnedOutVersions(): Set<Int> = migrationDirectory().listFiles().orEmpty()
    .filter { it.readLines().firstOrNull()?.trim() == PINNED_OUT_MARKER }
    .mapNotNull { migrationFilePattern.find(it.name)?.groupValues?.get(1)?.toInt() }
    .toSet()

  private fun highestAppliedMigrationVersion(): Int = (migrationsByVersion().keys - pinnedOutVersions()).max()
```

and add inside the class:

```kotlin
  companion object {
    private const val PINNED_OUT_MARKER = "-- flyway:pinned-out"
  }
```

Remove the old `highestMigrationVersion()` helper.

Run: `./gradlew :packages:server-core:test --tests 'org.migor.feedless.config.FlywayTargetTest'`
Expected: PASS (V97 is excluded, the highest applied migration V95 equals the target).

- [ ] **Step 3: Create V96 and raise the target**

`V96__per_source_schedule_and_host_cooldown.sql`:

```sql
-- Each source carries its own next harvest time, so backpressure can delay one source without delaying its repository.
ALTER TABLE t_source ADD COLUMN next_harvest_at timestamp;

UPDATE t_source s
SET next_harvest_at = r.trigger_scheduled_next_at
FROM t_repository r
WHERE r.id = s.repository_id;

CREATE INDEX idx_source_next_harvest_at ON t_source (next_harvest_at) WHERE is_disabled = false;

-- Shared across core instances and restarts; a 429 or 403 describes the whole host, not one source.
CREATE TABLE t_host_cooldown
(
  host          text PRIMARY KEY,
  blocked_until timestamp NOT NULL,
  strikes       integer   NOT NULL DEFAULT 0,
  last_status   integer,
  updated_at    timestamp NOT NULL
);
```

In `application-database.yaml` change `target: 95` to `target: 96`.

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :packages:server-core:test --tests 'org.migor.feedless.config.FlywayTargetTest'`
Expected: PASS (3 tests).

Sanity check the negative case once: temporarily set target to 97, rerun, expect `pinned-out migrations all lie above the target` to FAIL; restore 96.

- [ ] **Step 5: Amend spec and AGENTS.md**

In the spec: replace every mention of `t_source.host` with the SQL-derived host (deviation 1), rename `triggerScheduledNextAt` read path to the `@Formula` `nextHarvestAt` (deviation 2), change "Dry runs never write `next_harvest_at` or cooldown rows" to "Dry runs never write `next_harvest_at`; a throttle or block they observe is recorded" (deviation 3), and add the pinned-out marker to the V97 paragraph (deviation 4). In `AGENTS.md` rule 5 replace `currently \`V93\`` with `currently \`V96\`; a migration starting with \`-- flyway:pinned-out\` ships above the target on purpose`.

- [ ] **Step 6: Commit**

```bash
git add packages/jpa-data/src/main/resources/db/migration/V96__per_source_schedule_and_host_cooldown.sql \
  packages/jpa-data/src/main/resources/db/migration/V97__drop_repository_trigger_scheduled_next_at.sql \
  packages/server-core/src/main/resources/application-database.yaml \
  packages/server-core/src/test/kotlin/org/migor/feedless/config/FlywayTargetTest.kt \
  docs/superpowers/specs/2026-09-16-per-source-harvest-scheduling-design.md AGENTS.md
git commit -m "feat(jpa-data): add per-source schedule and host cooldown migrations"
```

---

### Task 2: `hostOf`, `RetryAfter`, `HostCooldown` port, `HostBlockedException`

**Files:**
- Create: `packages/domain/src/main/kotlin/org/migor/feedless/common/Hosts.kt`
- Create: `packages/domain/src/main/kotlin/org/migor/feedless/common/RetryAfter.kt`
- Create: `packages/domain/src/main/kotlin/org/migor/feedless/hostCooldown/HostCooldown.kt`
- Modify: `packages/domain/src/main/kotlin/org/migor/feedless/Exceptions.kt`
- Test: `packages/domain/src/test/kotlin/org/migor/feedless/common/HostsTest.kt`
- Test: `packages/domain/src/test/kotlin/org/migor/feedless/common/RetryAfterTest.kt`
- Test: `packages/domain/src/test/kotlin/org/migor/feedless/hostCooldown/HostBlockLadderTest.kt`

**Interfaces:**
- Produces:
  - `fun hostOf(url: String): String?`
  - `object RetryAfter { val FALLBACK: Duration; val CAP: Duration; fun parse(header: String?, now: Instant = Instant.now()): Duration }`
  - `data class HostCooldownState(val host: String, val blockedUntil: LocalDateTime, val strikes: Int, val lastStatus: Int?)`
  - `interface HostCooldown { fun find(host: String): HostCooldownState?; fun recordThrottled(host: String, status: Int, retryAfter: Duration, now: LocalDateTime): HostCooldownState; fun recordBlocked(host: String, status: Int, now: LocalDateTime): HostCooldownState; fun recordSuccess(host: String) }`
  - `object HostBlockLadder { fun delayFor(strikes: Int): Duration }`
  - `class HostBlockedException(val host: String, val status: Int, val strikes: Int, delay: Duration) : ResumableHarvestException`

- [ ] **Step 1: Write the failing tests**

`HostsTest.kt`:

```kotlin
package org.migor.feedless.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class HostsTest {

  @ParameterizedTest
  @CsvSource(
    "https://www.Bueron.ch/index.php?apid=1, www.bueron.ch",
    "http://example.org:8080/a, example.org",
    "https://user:pw@example.org/a, example.org",
    "example.org/path, example.org",
    "HTTPS://EXAMPLE.ORG, example.org",
  )
  fun `hostOf lower-cases the host and ignores scheme, port and user info`(url: String, expected: String) {
    assertThat(hostOf(url)).isEqualTo(expected)
  }

  @ParameterizedTest
  @CsvSource("''", "'://'", "'http://'")
  fun `hostOf is null without a host`(url: String) {
    assertThat(hostOf(url)).isNull()
  }
}
```

`RetryAfterTest.kt`:

```kotlin
package org.migor.feedless.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class RetryAfterTest {

  private val now = Instant.parse("2026-09-16T16:09:01Z")

  @Test
  fun `delta seconds`() {
    assertThat(RetryAfter.parse("600", now)).isEqualTo(Duration.ofSeconds(600))
  }

  @Test
  fun `http date`() {
    assertThat(RetryAfter.parse("Wed, 16 Sep 2026 16:19:01 GMT", now)).isEqualTo(Duration.ofMinutes(10))
  }

  @Test
  fun `missing, blank, garbage, zero, negative and past dates fall back`() {
    listOf(null, "", "soon", "0", "-5", "Wed, 16 Sep 2026 16:00:00 GMT").forEach {
      assertThat(RetryAfter.parse(it, now)).describedAs(it).isEqualTo(RetryAfter.FALLBACK)
    }
  }

  @Test
  fun `values above the cap are capped`() {
    assertThat(RetryAfter.parse("${Duration.ofDays(3).seconds}", now)).isEqualTo(RetryAfter.CAP)
  }

  @Test
  fun `fallback is 5 minutes and cap is 24 hours`() {
    assertThat(RetryAfter.FALLBACK).isEqualTo(Duration.ofMinutes(5))
    assertThat(RetryAfter.CAP).isEqualTo(Duration.ofHours(24))
  }
}
```

`HostBlockLadderTest.kt`:

```kotlin
package org.migor.feedless.hostCooldown

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration

class HostBlockLadderTest {

  @Test
  fun `escalates per strike and settles at 24 hours`() {
    assertThat((1..7).map { HostBlockLadder.delayFor(it) }).containsExactly(
      Duration.ofMinutes(5),
      Duration.ofMinutes(30),
      Duration.ofHours(2),
      Duration.ofHours(12),
      Duration.ofHours(24),
      Duration.ofHours(24),
      Duration.ofHours(24),
    )
  }

  @Test
  fun `strike zero or below is treated as the first strike`() {
    assertThat(HostBlockLadder.delayFor(0)).isEqualTo(Duration.ofMinutes(5))
  }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :packages:domain:test --tests 'org.migor.feedless.common.*' --tests 'org.migor.feedless.hostCooldown.*'`
Expected: FAIL — compilation errors `Unresolved reference: hostOf`, `RetryAfter`, `HostBlockLadder`.

- [ ] **Step 3: Implement**

`Hosts.kt`:

```kotlin
package org.migor.feedless.common

import java.net.URI

/** The key cooldowns are stored under; must match the host expression in SourceDAO.findDueForHarvest. */
fun hostOf(url: String): String? {
  val withScheme = if (Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://").containsMatchIn(url)) url else "https://$url"
  return runCatching { URI(withScheme).host }.getOrNull()?.lowercase()?.takeIf { it.isNotBlank() }
}
```

`RetryAfter.kt`:

```kotlin
package org.migor.feedless.common

import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/** RFC 9110 §10.2.3: delta-seconds or an HTTP-date. */
object RetryAfter {
  val FALLBACK: Duration = Duration.ofMinutes(5)
  val CAP: Duration = Duration.ofHours(24)

  fun parse(header: String?, now: Instant = Instant.now()): Duration {
    val value = header?.trim().orEmpty()
    val parsed = value.toLongOrNull()?.let { Duration.ofSeconds(it) }
      ?: runCatching { Duration.between(now, ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant()) }.getOrNull()
    return when {
      parsed == null || parsed.isNegative || parsed.isZero -> FALLBACK
      parsed > CAP -> CAP
      else -> parsed
    }
  }
}
```

`HostCooldown.kt`:

```kotlin
package org.migor.feedless.hostCooldown

import java.time.Duration
import java.time.LocalDateTime

data class HostCooldownState(
  val host: String,
  val blockedUntil: LocalDateTime,
  val strikes: Int,
  val lastStatus: Int?,
)

/** Persisted per-host backoff, shared by every core instance. */
interface HostCooldown {
  fun find(host: String): HostCooldownState?

  /** Never shortens an existing cooldown; strikes are left untouched. */
  fun recordThrottled(host: String, status: Int, retryAfter: Duration, now: LocalDateTime): HostCooldownState

  /** Adds a strike and extends the cooldown by [HostBlockLadder]. */
  fun recordBlocked(host: String, status: Int, now: LocalDateTime): HostCooldownState

  fun recordSuccess(host: String)
}

object HostBlockLadder {
  private val steps = listOf(
    Duration.ofMinutes(5),
    Duration.ofMinutes(30),
    Duration.ofHours(2),
    Duration.ofHours(12),
    Duration.ofHours(24),
  )

  fun delayFor(strikes: Int): Duration = steps[(strikes - 1).coerceIn(0, steps.lastIndex)]
}
```

Append to `Exceptions.kt`:

```kotlin
/** The host refused us (401/403); retried on [org.migor.feedless.hostCooldown.HostBlockLadder] instead of failing. */
class HostBlockedException(val host: String, val status: Int, val strikes: Int, delay: Duration) :
  ResumableHarvestException("blocked by $host ($status, strike $strikes), retry in ${delay.toMinutes()}m", delay)
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :packages:domain:test --tests 'org.migor.feedless.common.*' --tests 'org.migor.feedless.hostCooldown.*'`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add packages/domain/src/main/kotlin/org/migor/feedless/common/Hosts.kt \
  packages/domain/src/main/kotlin/org/migor/feedless/common/RetryAfter.kt \
  packages/domain/src/main/kotlin/org/migor/feedless/hostCooldown/HostCooldown.kt \
  packages/domain/src/main/kotlin/org/migor/feedless/Exceptions.kt \
  packages/domain/src/test/kotlin/org/migor/feedless/common/HostsTest.kt \
  packages/domain/src/test/kotlin/org/migor/feedless/common/RetryAfterTest.kt \
  packages/domain/src/test/kotlin/org/migor/feedless/hostCooldown/HostBlockLadderTest.kt
git commit -m "feat(domain): add Retry-After parsing and host cooldown port"
```

---

### Task 3: `HostCooldown` JPA adapter

**Files:**
- Create: `packages/jpa-data/src/main/kotlin/org/migor/feedless/data/jpa/hostCooldown/HostCooldownEntity.kt`
- Create: `packages/jpa-data/src/main/kotlin/org/migor/feedless/data/jpa/hostCooldown/HostCooldownDAO.kt`
- Create: `packages/jpa-data/src/main/kotlin/org/migor/feedless/data/jpa/hostCooldown/HostCooldownJpaRepository.kt`
- Test: `packages/jpa-data/src/test/kotlin/org/migor/feedless/hostCooldown/HostCooldownRepositoryIntTest.kt`

**Interfaces:**
- Consumes: `HostCooldown`, `HostCooldownState`, `HostBlockLadder` (Task 2).
- Produces: bean `HostCooldownJpaRepository : HostCooldown`, profile `"${AppProfiles.source} & ${AppLayer.repository}"`.

- [ ] **Step 1: Write the failing test**

```kotlin
package org.migor.feedless.hostCooldown

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PostgreSQLExtension
import org.migor.feedless.data.jpa.JpaDataTestApplication
import org.migor.feedless.data.jpa.hostCooldown.HostCooldownDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@SpringBootTest(classes = [JpaDataTestApplication::class])
@ExtendWith(PostgreSQLExtension::class)
@DirtiesContext
@ActiveProfiles("test", "database", AppProfiles.source, AppLayer.repository)
@Testcontainers
class HostCooldownRepositoryIntTest {

  @Autowired
  private lateinit var hostCooldown: HostCooldown

  @Autowired
  private lateinit var hostCooldownDAO: HostCooldownDAO

  private val host = "www.bueron.ch"
  private val now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)

  @BeforeEach
  fun setUp() {
    hostCooldownDAO.deleteAll()
  }

  @Test
  fun `throttle creates a cooldown without a strike`() {
    val state = hostCooldown.recordThrottled(host, 429, Duration.ofMinutes(10), now)

    assertThat(state.blockedUntil).isEqualTo(now.plusMinutes(10))
    assertThat(state.strikes).isEqualTo(0)
    assertThat(state.lastStatus).isEqualTo(429)
    assertThat(hostCooldown.find(host)).isEqualTo(state)
  }

  @Test
  fun `a shorter throttle never shortens an existing cooldown`() {
    hostCooldown.recordThrottled(host, 429, Duration.ofHours(1), now)

    val state = hostCooldown.recordThrottled(host, 503, Duration.ofMinutes(5), now)

    assertThat(state.blockedUntil).isEqualTo(now.plusHours(1))
    assertThat(state.lastStatus).isEqualTo(503)
  }

  @Test
  fun `blocks climb the ladder`() {
    val first = hostCooldown.recordBlocked(host, 403, now)
    val second = hostCooldown.recordBlocked(host, 403, now)
    val third = hostCooldown.recordBlocked(host, 401, now)

    assertThat(listOf(first.strikes, second.strikes, third.strikes)).containsExactly(1, 2, 3)
    assertThat(third.blockedUntil).isEqualTo(now.plusHours(2))
    assertThat(third.lastStatus).isEqualTo(401)
  }

  @Test
  fun `a throttle keeps existing strikes`() {
    hostCooldown.recordBlocked(host, 403, now)

    assertThat(hostCooldown.recordThrottled(host, 429, Duration.ofMinutes(1), now).strikes).isEqualTo(1)
  }

  @Test
  fun `success deletes the cooldown`() {
    hostCooldown.recordBlocked(host, 403, now)

    hostCooldown.recordSuccess(host)

    assertThat(hostCooldown.find(host)).isNull()
  }

  @Test
  fun `success on an unknown host is a no-op`() {
    hostCooldown.recordSuccess("unknown.example")

    assertThat(hostCooldownDAO.count()).isZero()
  }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :packages:jpa-data:test --tests 'org.migor.feedless.hostCooldown.HostCooldownRepositoryIntTest'`
Expected: FAIL — compilation error `Unresolved reference: HostCooldownDAO`.

- [ ] **Step 3: Implement**

`HostCooldownEntity.kt`:

```kotlin
package org.migor.feedless.data.jpa.hostCooldown

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.migor.feedless.hostCooldown.HostCooldownState
import java.time.LocalDateTime

@Entity
@Table(name = "t_host_cooldown")
open class HostCooldownEntity {

  @Id
  @Column(name = "host", nullable = false)
  open lateinit var host: String

  @Column(name = "blocked_until", nullable = false)
  open lateinit var blockedUntil: LocalDateTime

  @Column(name = "strikes", nullable = false)
  open var strikes: Int = 0

  @Column(name = "last_status")
  open var lastStatus: Int? = null

  @Column(name = "updated_at", nullable = false)
  open lateinit var updatedAt: LocalDateTime

  fun toDomain() = HostCooldownState(host, blockedUntil, strikes, lastStatus)
}
```

`HostCooldownDAO.kt`:

```kotlin
package org.migor.feedless.data.jpa.hostCooldown

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
@Profile("${AppProfiles.source} & ${AppLayer.repository}")
interface HostCooldownDAO : JpaRepository<HostCooldownEntity, String> {

  // Single statements, so concurrent workers on one host never lose an update.
  @Modifying(clearAutomatically = true)
  @Query(
    """
    INSERT INTO t_host_cooldown (host, blocked_until, strikes, last_status, updated_at)
    VALUES (:host, :until, 0, :status, :now)
    ON CONFLICT (host) DO UPDATE
      SET blocked_until = greatest(t_host_cooldown.blocked_until, excluded.blocked_until),
          last_status = excluded.last_status,
          updated_at = excluded.updated_at
  """, nativeQuery = true
  )
  fun upsertThrottled(
    @Param("host") host: String,
    @Param("until") until: LocalDateTime,
    @Param("status") status: Int,
    @Param("now") now: LocalDateTime,
  ): Int

  @Modifying(clearAutomatically = true)
  @Query(
    """
    INSERT INTO t_host_cooldown (host, blocked_until, strikes, last_status, updated_at)
    VALUES (:host, :now, 1, :status, :now)
    ON CONFLICT (host) DO UPDATE
      SET strikes = t_host_cooldown.strikes + 1,
          last_status = excluded.last_status,
          updated_at = excluded.updated_at
  """, nativeQuery = true
  )
  fun upsertStrike(
    @Param("host") host: String,
    @Param("status") status: Int,
    @Param("now") now: LocalDateTime,
  ): Int

  @Modifying(clearAutomatically = true)
  @Query(
    """
    UPDATE t_host_cooldown SET blocked_until = greatest(blocked_until, :until) WHERE host = :host
  """, nativeQuery = true
  )
  fun extendBlockedUntil(@Param("host") host: String, @Param("until") until: LocalDateTime): Int
}
```

`HostCooldownJpaRepository.kt`:

```kotlin
package org.migor.feedless.data.jpa.hostCooldown

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.hostCooldown.HostBlockLadder
import org.migor.feedless.hostCooldown.HostCooldown
import org.migor.feedless.hostCooldown.HostCooldownState
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@Component
@Profile("${AppProfiles.source} & ${AppLayer.repository}")
class HostCooldownJpaRepository(private val hostCooldownDAO: HostCooldownDAO) : HostCooldown {

  override fun find(host: String): HostCooldownState? =
    hostCooldownDAO.findById(host).getOrNull()?.toDomain()

  @Transactional
  override fun recordThrottled(host: String, status: Int, retryAfter: Duration, now: LocalDateTime): HostCooldownState {
    hostCooldownDAO.upsertThrottled(host, now.plus(retryAfter), status, now)
    return find(host)!!
  }

  // The upsert holds the row lock until commit, so the strike read below is this transaction's own.
  @Transactional
  override fun recordBlocked(host: String, status: Int, now: LocalDateTime): HostCooldownState {
    hostCooldownDAO.upsertStrike(host, status, now)
    val strikes = find(host)!!.strikes
    hostCooldownDAO.extendBlockedUntil(host, now.plus(HostBlockLadder.delayFor(strikes)))
    return find(host)!!
  }

  @Transactional
  override fun recordSuccess(host: String) {
    hostCooldownDAO.deleteById(host)
  }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :packages:jpa-data:test --tests 'org.migor.feedless.hostCooldown.HostCooldownRepositoryIntTest'`
Expected: PASS (6 tests). Also confirm in the test log that Flyway reports `Successfully applied` including version 96 (V96 SQL is valid against PostGIS).

- [ ] **Step 5: Commit**

```bash
git add packages/jpa-data/src/main/kotlin/org/migor/feedless/data/jpa/hostCooldown \
  packages/jpa-data/src/test/kotlin/org/migor/feedless/hostCooldown
git commit -m "feat(jpa-data): persist host cooldowns"
```

---

### Task 4: `HostCooldownGuard`, `HttpService` status mapping, prerender check

**Files:**
- Create: `packages/server-core/src/main/kotlin/org/migor/feedless/common/HostCooldownGuard.kt`
- Modify: `packages/server-core/src/main/kotlin/org/migor/feedless/common/HttpService.kt` (constructor, `httpGet`, `execute` at `:157-190`)
- Modify: `packages/server-core/src/main/kotlin/org/migor/feedless/scrape/ScrapeService.kt` (field injection, `handleFetch` at `:267`)
- Test: `packages/server-core/src/test/kotlin/org/migor/feedless/common/HostCooldownGuardTest.kt`
- Test: `packages/server-core/src/test/kotlin/org/migor/feedless/common/HttpServiceTest.kt`

**Interfaces:**
- Consumes: `hostOf`, `RetryAfter`, `HostCooldown`, `HostBlockedException` (Task 2).
- Produces:
  - `@Service class HostCooldownGuard(private val hostCooldown: HostCooldown?)` with `fun requireOpen(url: String)`, `fun onThrottled(url: String, status: Int, retryAfterHeader: String?): Nothing`, `fun onBlocked(url: String, status: Int): Nothing`, `fun onSuccess(url: String)`.
  - `class HttpService(apiGatewayUrl: String, hostCooldownGuard: HostCooldownGuard)`.

- [ ] **Step 1: Write the failing guard test**

`HostCooldownGuardTest.kt` (with an in-memory fake, no Spring):

```kotlin
package org.migor.feedless.common

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.migor.feedless.HostBlockedException
import org.migor.feedless.HostOverloadingException
import org.migor.feedless.hostCooldown.HostBlockLadder
import org.migor.feedless.hostCooldown.HostCooldown
import org.migor.feedless.hostCooldown.HostCooldownState
import java.time.Duration
import java.time.LocalDateTime

class FakeHostCooldown : HostCooldown {
  val rows = mutableMapOf<String, HostCooldownState>()
  var successCalls = 0

  override fun find(host: String) = rows[host]

  override fun recordThrottled(host: String, status: Int, retryAfter: Duration, now: LocalDateTime): HostCooldownState {
    val existing = rows[host]
    val until = maxOf(existing?.blockedUntil ?: now, now.plus(retryAfter))
    return HostCooldownState(host, until, existing?.strikes ?: 0, status).also { rows[host] = it }
  }

  override fun recordBlocked(host: String, status: Int, now: LocalDateTime): HostCooldownState {
    val strikes = (rows[host]?.strikes ?: 0) + 1
    return HostCooldownState(host, now.plus(HostBlockLadder.delayFor(strikes)), strikes, status).also { rows[host] = it }
  }

  override fun recordSuccess(host: String) {
    successCalls++
    rows.remove(host)
  }
}

class HostCooldownGuardTest {

  private val fake = FakeHostCooldown()
  private val guard = HostCooldownGuard(fake)

  @Test
  fun `an open host passes`() {
    guard.requireOpen("https://www.bueron.ch/a")
  }

  @Test
  fun `a cooling host throws before any request with the remaining time`() {
    fake.rows["www.bueron.ch"] = HostCooldownState("www.bueron.ch", LocalDateTime.now().plusMinutes(10), 0, 429)

    assertThatThrownBy { guard.requireOpen("https://WWW.bueron.ch/a") }
      .isInstanceOf(HostOverloadingException::class.java)
      .matches { (it as HostOverloadingException).nextRetryAfter > Duration.ofMinutes(9) }
  }

  @Test
  fun `a throttle records the parsed Retry-After and throws`() {
    assertThatThrownBy { guard.onThrottled("https://www.bueron.ch/a", 429, "600") }
      .isInstanceOf(HostOverloadingException::class.java)
      .hasMessageContaining("throttled by www.bueron.ch (429)")
      .matches { (it as HostOverloadingException).nextRetryAfter == Duration.ofSeconds(600) }
    assertThat(fake.rows["www.bueron.ch"]!!.lastStatus).isEqualTo(429)
  }

  @Test
  fun `a block records a strike and throws HostBlockedException`() {
    assertThatThrownBy { guard.onBlocked("https://www.bueron.ch/a", 403) }
      .isInstanceOf(HostBlockedException::class.java)
      .matches { (it as HostBlockedException).strikes == 1 && it.nextRetryAfter == Duration.ofMinutes(5) }
  }

  @Test
  fun `success only writes for hosts known to have a cooldown`() {
    guard.onSuccess("https://quiet.example/a")
    assertThat(fake.successCalls).isZero()

    runCatching { guard.onBlocked("https://www.bueron.ch/a", 403) }
    guard.onSuccess("https://www.bueron.ch/b")

    assertThat(fake.successCalls).isEqualTo(1)
    assertThat(fake.rows).isEmpty()
  }

  @Test
  fun `an expired row seen by requireOpen is reset on the next success`() {
    fake.rows["www.bueron.ch"] = HostCooldownState("www.bueron.ch", LocalDateTime.now().minusMinutes(1), 2, 403)

    guard.requireOpen("https://www.bueron.ch/a")
    guard.onSuccess("https://www.bueron.ch/a")

    assertThat(fake.rows).isEmpty()
  }

  @Test
  fun `without a cooldown store nothing is checked or recorded`() {
    val bare = HostCooldownGuard(null)
    bare.requireOpen("https://www.bueron.ch/a")
    assertThatThrownBy { bare.onThrottled("https://www.bueron.ch/a", 429, null) }
      .isInstanceOf(HostOverloadingException::class.java)
  }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :packages:server-core:test --tests 'org.migor.feedless.common.HostCooldownGuardTest'`
Expected: FAIL — `Unresolved reference: HostCooldownGuard`.

- [ ] **Step 3: Implement the guard**

```kotlin
package org.migor.feedless.common

import org.migor.feedless.HostBlockedException
import org.migor.feedless.HostOverloadingException
import org.migor.feedless.hostCooldown.HostBlockLadder
import org.migor.feedless.hostCooldown.HostCooldown
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

/** Null [hostCooldown] means the persistence profiles are off; backpressure then only lives in the thrown delay. */
@Service
class HostCooldownGuard(private val hostCooldown: HostCooldown?) {

  private val log = LoggerFactory.getLogger(HostCooldownGuard::class.simpleName)

  // Hosts with a row, so a success on any other host costs no database write.
  private val hostsWithRow: MutableSet<String> = ConcurrentHashMap.newKeySet()

  fun requireOpen(url: String) {
    val host = hostOf(url) ?: return
    val state = hostCooldown?.find(host) ?: return
    hostsWithRow.add(host)
    val remaining = Duration.between(LocalDateTime.now(), state.blockedUntil)
    if (remaining.isPositive) {
      throw HostOverloadingException("host $host cooling down until ${state.blockedUntil}", remaining)
    }
  }

  fun onThrottled(url: String, status: Int, retryAfterHeader: String?): Nothing {
    val host = hostOf(url)
    val delay = RetryAfter.parse(retryAfterHeader)
    if (host != null && hostCooldown != null) {
      hostCooldown.recordThrottled(host, status, delay, LocalDateTime.now())
      hostsWithRow.add(host)
    }
    log.info("throttled by $host ($status), retry in $delay")
    throw HostOverloadingException("throttled by $host ($status), retry in ${delay.toMinutes()}m", delay)
  }

  fun onBlocked(url: String, status: Int): Nothing {
    val host = hostOf(url)
    val now = LocalDateTime.now()
    val state = host?.let { hostCooldown?.recordBlocked(it, status, now) }
    state?.let { hostsWithRow.add(it.host) }
    val strikes = state?.strikes ?: 1
    val delay = state?.let { Duration.between(now, it.blockedUntil) } ?: HostBlockLadder.delayFor(strikes)
    log.info("blocked by $host ($status, strike $strikes), retry in $delay")
    throw HostBlockedException(host ?: url, status, strikes, delay)
  }

  fun onSuccess(url: String) {
    val host = hostOf(url) ?: return
    if (hostCooldown != null && hostsWithRow.remove(host)) {
      hostCooldown.recordSuccess(host)
    }
  }
}
```

- [ ] **Step 4: Run guard test to verify it passes**

Run: `./gradlew :packages:server-core:test --tests 'org.migor.feedless.common.HostCooldownGuardTest'`
Expected: PASS (7 tests).

- [ ] **Step 5: Write the failing HttpService test**

Replace `HttpServiceTest.kt` with a JDK `HttpServer`-backed test (no new dependency). The API gateway host is exempt from Bucket4j only, not from the guard, so point the gateway at `localhost` to avoid the 2-requests-per-minute path bucket:

```kotlin
package org.migor.feedless.common

import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.HostBlockedException
import org.migor.feedless.HostOverloadingException
import org.migor.feedless.SiteNotFoundException
import java.net.InetSocketAddress
import java.net.MalformedURLException
import java.time.Duration

class HttpServiceTest {

  private lateinit var httpService: HttpService
  private lateinit var server: HttpServer
  private lateinit var cooldowns: FakeHostCooldown
  private var status = 200
  private var retryAfter: String? = null
  private var requests = 0

  @BeforeEach
  fun setUp() {
    server = HttpServer.create(InetSocketAddress("localhost", 0), 0)
    server.createContext("/") { exchange ->
      requests++
      retryAfter?.let { exchange.responseHeaders.add("Retry-After", it) }
      val body = "ok".toByteArray()
      exchange.sendResponseHeaders(status, body.size.toLong())
      exchange.responseBody.use { it.write(body) }
    }
    server.start()
    cooldowns = FakeHostCooldown()
    httpService = HttpService("http://localhost", HostCooldownGuard(cooldowns))
    httpService.postConstruct()
  }

  @AfterEach
  fun tearDown() {
    server.stop(0)
  }

  private fun url() = "http://localhost:${server.address.port}/feed"

  @Test
  fun `httpGet will validate url`() {
    assertThatExceptionOfType(MalformedURLException::class.java).isThrownBy {
      runTest { httpService.httpGet("gemma", 200) }
    }
  }

  @Test
  fun `429 with Retry-After records a throttle with the parsed delay`() = runTest {
    status = 429
    retryAfter = "600"

    val e = runCatching { httpService.httpGet(url(), 200) }.exceptionOrNull()

    assertThat(e).isInstanceOf(HostOverloadingException::class.java)
    assertThat((e as HostOverloadingException).nextRetryAfter).isEqualTo(Duration.ofSeconds(600))
    assertThat(cooldowns.rows["localhost"]!!.lastStatus).isEqualTo(429)
  }

  @Test
  fun `429 without Retry-After falls back to 5 minutes`() = runTest {
    status = 429

    val e = runCatching { httpService.httpGet(url(), 200) }.exceptionOrNull() as HostOverloadingException

    assertThat(e.nextRetryAfter).isEqualTo(Duration.ofMinutes(5))
  }

  @Test
  fun `503 is a throttle`() = runTest {
    status = 503
    retryAfter = "120"

    val e = runCatching { httpService.httpGet(url(), 200) }.exceptionOrNull() as HostOverloadingException

    assertThat(e.nextRetryAfter).isEqualTo(Duration.ofSeconds(120))
  }

  @Test
  fun `403 and 401 are blocks`() = runTest {
    status = 403
    assertThat(runCatching { httpService.httpGet(url(), 200) }.exceptionOrNull()).isInstanceOf(HostBlockedException::class.java)
    assertThat(cooldowns.rows["localhost"]!!.strikes).isEqualTo(1)
  }

  @Test
  fun `404 stays not found`() = runTest {
    status = 404
    assertThat(runCatching { httpService.httpGet(url(), 200) }.exceptionOrNull()).isInstanceOf(SiteNotFoundException::class.java)
    assertThat(cooldowns.rows).isEmpty()
  }

  @Test
  fun `a cooling host is not requested`() = runTest {
    status = 429
    runCatching { httpService.httpGet(url(), 200) }
    val before = requests

    val e = runCatching { httpService.httpGet(url(), 200) }.exceptionOrNull()

    assertThat(e).isInstanceOf(HostOverloadingException::class.java)
    assertThat(requests).isEqualTo(before)
  }

  @Test
  fun `success resets a known cooldown`() = runTest {
    cooldowns.rows["localhost"] = org.migor.feedless.hostCooldown.HostCooldownState(
      "localhost", java.time.LocalDateTime.now().minusMinutes(1), 3, 403
    )

    httpService.httpGet(url(), 200)

    assertThat(cooldowns.rows).isEmpty()
  }
}
```

- [ ] **Step 6: Run test to verify it fails**

Run: `./gradlew :packages:server-core:test --tests 'org.migor.feedless.common.HttpServiceTest'`
Expected: FAIL — constructor `HttpService(String, HostCooldownGuard)` does not exist.

- [ ] **Step 7: Implement in HttpService**

Constructor:

```kotlin
@Service
class HttpService(
  @Value("\${app.apiGatewayUrl}")
  private val apiGatewayUrl: String,
  private val hostCooldownGuard: HostCooldownGuard,
) : HttpFetcher {
```

In `httpGet`, before `protectFromOverloading(url)`:

```kotlin
    hostCooldownGuard.requireOpen(url)
```

In `executeRequest`, first line:

```kotlin
    hostCooldownGuard.requireOpen(request.build().uri.toUrl())
```

Replace the status `when` inside `execute` with:

```kotlin
      if (response.statusCode != expectedStatusCode) {
        val url = response.uri.toUrl()
        when (response.statusCode) {
          429, 503 -> hostCooldownGuard.onThrottled(url, response.statusCode, response.getHeader("Retry-After"))
          401, 403 -> hostCooldownGuard.onBlocked(url, response.statusCode)
          500 -> throw ResumableHarvestException("500 received", Duration.ofMinutes(5))
          400 -> throw TemporaryServerException("400 received", Duration.ofHours(1))
          in 400..499 -> throw SiteNotFoundException(url)
          in 500..599 -> throw ResumableHarvestException(url, Duration.ofHours(5))
          else -> throw FatalHarvestException("Expected $expectedStatusCode received ${response.statusCode}")
        }
      } else {
        hostCooldownGuard.onSuccess(response.uri.toUrl())
        log.debug("-> ${response.getHeader("content-type")}")
      }
```

Remove the now-unused `HostOverloadingException` import only if the compiler warns it unused (it is still used by `protectFromOverloading`, so keep it).

- [ ] **Step 8: Guard the prerender path in ScrapeService**

Add the field next to the other injections:

```kotlin
  @Autowired
  private lateinit var hostCooldownGuard: HostCooldownGuard
```

At the top of `handleFetch`, after `context.log("handleFetch $action")`:

```kotlin
    // The agent fetches outside HttpService, so the cooldown is checked here for both paths.
    hostCooldownGuard.requireOpen(action.resolveUrl())
```

- [ ] **Step 9: Run tests to verify they pass**

Run: `./gradlew :packages:server-core:test --tests 'org.migor.feedless.common.*' --tests 'org.migor.feedless.service.ScrapeServiceIntTest'`
Expected: PASS. If `ScrapeServiceIntTest` fails with a missing `HostCooldownGuard` bean, add `HostCooldownGuard::class` to that test's context the same way `HttpService` is provided there.

- [ ] **Step 10: Commit**

```bash
git add packages/server-core/src/main/kotlin/org/migor/feedless/common/HostCooldownGuard.kt \
  packages/server-core/src/main/kotlin/org/migor/feedless/common/HttpService.kt \
  packages/server-core/src/main/kotlin/org/migor/feedless/scrape/ScrapeService.kt \
  packages/server-core/src/test/kotlin/org/migor/feedless/common/HostCooldownGuardTest.kt \
  packages/server-core/src/test/kotlin/org/migor/feedless/common/HttpServiceTest.kt
git commit -m "feat(server-core): honour Retry-After and host blocks"
```

---

### Task 5: Source schedule persistence and the due query

**Files:**
- Modify: `packages/domain/src/main/kotlin/org/migor/feedless/source/Source.kt`
- Modify: `packages/domain/src/main/kotlin/org/migor/feedless/source/SourceRepository.kt`
- Modify: `packages/jpa-data/src/main/kotlin/org/migor/feedless/data/jpa/source/SourceEntity.kt`
- Modify: `packages/jpa-data/src/main/kotlin/org/migor/feedless/data/jpa/source/SourceDAO.kt`
- Modify: `packages/jpa-data/src/main/kotlin/org/migor/feedless/data/jpa/source/SourceJpaRepository.kt`
- Test: `packages/jpa-data/src/test/kotlin/org/migor/feedless/source/SourceDueForHarvestIntTest.kt`

**Interfaces:**
- Consumes: `hostOf` (Task 2), `t_host_cooldown` (Task 1), `HostCooldown` (Task 3).
- Produces on `SourceRepository`:
  - `fun findAllDueForHarvest(now: LocalDateTime, limit: Int): List<Source>` — with actions, ordered `next_harvest_at asc nulls first`.
  - `fun scheduleNextHarvest(id: SourceId, at: LocalDateTime)`
  - `fun scheduleNextHarvestOfRepository(repositoryId: RepositoryId, at: LocalDateTime)`
  - `Source.nextHarvestAt: LocalDateTime?`

- [ ] **Step 1: Write the failing test**

Model the fixture on `HarvestRepositoryIntTest.setUp` (same profiles, same user/group/repository creation). Read that file's `setUp` first and copy its user/group/repository construction; below, `createSource(url, repository)` saves a `Source(title = url, repositoryId = repository.id, actions = listOf(FetchAction(url = url)))` through `sourceRepository.save`.

```kotlin
package org.migor.feedless.source

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PostgreSQLExtension
import org.migor.feedless.common.hostOf
import org.migor.feedless.data.jpa.JpaDataTestApplication
import org.migor.feedless.harvest.HarvestRepository
import org.migor.feedless.hostCooldown.HostCooldown
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration
import java.time.LocalDateTime

@SpringBootTest(classes = [JpaDataTestApplication::class])
@ExtendWith(PostgreSQLExtension::class)
@DirtiesContext
@ActiveProfiles(
  "test",
  "database",
  AppProfiles.repository,
  AppProfiles.source,
  AppProfiles.user,
  AppLayer.repository,
)
@Testcontainers
class SourceDueForHarvestIntTest {

  @Autowired private lateinit var sourceRepository: SourceRepository
  @Autowired private lateinit var harvestRepository: HarvestRepository
  @Autowired private lateinit var hostCooldown: HostCooldown
  @Autowired private lateinit var jdbcTemplate: JdbcTemplate
  // plus the user/group/repository repositories used by HarvestRepositoryIntTest.setUp

  private val now = LocalDateTime.now()

  @BeforeEach
  fun setUp() {
    jdbcTemplate.update("DELETE FROM t_host_cooldown")
    // copy HarvestRepositoryIntTest.setUp: delete users, create user + group + repository with sourcesSyncCron = "0 0 * * * *"
  }

  @Test
  fun `a never-scheduled and an overdue source are due, a future one is not`() {
    val never = createSource("https://a.example/1")
    val overdue = createSource("https://b.example/1")
    val future = createSource("https://c.example/1")
    sourceRepository.scheduleNextHarvest(overdue.id, now.minusMinutes(1))
    sourceRepository.scheduleNextHarvest(future.id, now.plusHours(1))

    assertThat(sourceRepository.findAllDueForHarvest(now, 50).map { it.id })
      .containsExactly(never.id, overdue.id)
  }

  @Test
  fun `due sources come with their actions`() {
    createSource("https://a.example/1")

    assertThat(sourceRepository.findAllDueForHarvest(now, 50).single().actions).isNotEmpty()
  }

  @Test
  fun `sources on a cooling host are not due`() {
    createSource("https://www.Bueron.ch/index.php?apid=1")
    hostCooldown.recordThrottled("www.bueron.ch", 429, Duration.ofMinutes(10), now)

    assertThat(sourceRepository.findAllDueForHarvest(now, 50)).isEmpty()
  }

  @Test
  fun `disabled sources and sources with a running harvest are not due`() {
    val disabled = createSource("https://a.example/1")
    sourceRepository.save(sourceRepository.findByIdWithActions(disabled.id)!!.copy(disabled = true))
    val running = createSource("https://b.example/1")
    harvestRepository.startRun(running.id, now)

    assertThat(sourceRepository.findAllDueForHarvest(now, 50)).isEmpty()
  }

  @Test
  fun `scheduling a repository moves all its sources`() {
    val a = createSource("https://a.example/1")
    val b = createSource("https://b.example/1")
    val at = now.plusHours(2).withNano(0)

    sourceRepository.scheduleNextHarvestOfRepository(a.repositoryId!!, at)

    assertThat(listOf(a, b).map { sourceRepository.findById(it.id)!!.nextHarvestAt }).containsOnly(at)
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "https://www.Bueron.ch/index.php?apid=1659677497",
      "http://example.org:8080/a",
      "https://user:pw@example.org/a",
      "example.org/path",
      "HTTPS://EXAMPLE.ORG",
    ]
  )
  fun `the SQL host expression matches hostOf`(url: String) {
    val sql = jdbcTemplate.queryForObject("SELECT ${SourceHostSql.EXPRESSION.replace("f.url", "?::text")}", String::class.java, url)

    assertThat(sql).isEqualTo(hostOf(url))
  }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :packages:jpa-data:test --tests 'org.migor.feedless.source.SourceDueForHarvestIntTest'`
Expected: FAIL — `Unresolved reference: findAllDueForHarvest`, `SourceHostSql`.

- [ ] **Step 3: Implement**

`Source.kt` — add after `lastErrorMessage`:

```kotlin
  val nextHarvestAt: LocalDateTime? = null,
```

`SourceRepository.kt` — add:

```kotlin
  /** Due, enabled, not on a cooling host, not running, of an active repository; with actions, oldest due first. */
  fun findAllDueForHarvest(now: LocalDateTime, limit: Int): List<Source>

  fun scheduleNextHarvest(id: SourceId, at: LocalDateTime)

  fun scheduleNextHarvestOfRepository(repositoryId: RepositoryId, at: LocalDateTime)
```

`SourceEntity.kt` — add after `lastErrorMessage`:

```kotlin
  @Column(name = "next_harvest_at")
  open var nextHarvestAt: LocalDateTime? = null
```

`SourceDAO.kt` — add a top-level object in the same file and the queries:

```kotlin
/** Host of a fetch url, as [org.migor.feedless.common.hostOf] computes it. */
object SourceHostSql {
  const val EXPRESSION =
    "lower(substring(f.url from '^(?:[a-zA-Z][a-zA-Z0-9+.-]*://)?(?:[^@/?#]*@)?([^/:?#]+)'))"
}
```

```kotlin
  @Query(
    """
    SELECT s.id FROM t_source s
    JOIN t_repository r ON r.id = s.repository_id
    JOIN t_user u ON u.id = r.owner_id
    LEFT JOIN LATERAL (
      SELECT ${SourceHostSql.EXPRESSION} AS host
      FROM t_scrape_action a JOIN t_action_fetch f ON f.id = a.id
      WHERE a.source_id = s.id
      ORDER BY a.pos
      LIMIT 1
    ) fa ON true
    WHERE s.is_disabled = false
      AND (s.next_harvest_at IS NULL OR s.next_harvest_at < :now)
      AND r.is_archived = false
      AND r.scheduler_expression > ''
      AND (r.disabled_from IS NULL OR r.disabled_from > :now)
      AND u.is_locked = false
      AND u.is_banned = false
      AND u.hasapprovedterms = true
      AND u.purge_scheduled_for IS NULL
      AND NOT EXISTS (SELECT 1 FROM t_host_cooldown c WHERE c.host = fa.host AND c.blocked_until > :now)
      AND NOT EXISTS (SELECT 1 FROM t_harvest h WHERE h.source_id = s.id AND h.status = 'running' AND h.dry_run = false)
    ORDER BY s.next_harvest_at ASC NULLS FIRST
    LIMIT :limit
  """, nativeQuery = true
  )
  fun findIdsDueForHarvest(@Param("now") now: LocalDateTime, @Param("limit") limit: Int): List<UUID>

  @Modifying
  @Query("update SourceEntity s set s.nextHarvestAt = :at where s.id = :id")
  fun updateNextHarvestAt(@Param("id") id: UUID, @Param("at") at: LocalDateTime): Int

  @Modifying
  @Query("update SourceEntity s set s.nextHarvestAt = :at where s.repositoryId = :repositoryId")
  fun updateNextHarvestAtByRepositoryId(@Param("repositoryId") repositoryId: UUID, @Param("at") at: LocalDateTime): Int
```

`SourceJpaRepository.kt` — add:

```kotlin
  override fun findAllDueForHarvest(now: LocalDateTime, limit: Int): List<Source> {
    val ids = sourceDAO.findIdsDueForHarvest(now, limit)
    val byId = sourceDAO.findAllWithActionsByIdIn(ids).associateBy { it.id }
    return ids.mapNotNull { byId[it]?.toDomain() }
  }

  @Transactional
  override fun scheduleNextHarvest(id: SourceId, at: LocalDateTime) {
    sourceDAO.updateNextHarvestAt(id.uuid, at)
  }

  @Transactional
  override fun scheduleNextHarvestOfRepository(repositoryId: RepositoryId, at: LocalDateTime) {
    sourceDAO.updateNextHarvestAtByRepositoryId(repositoryId.uuid, at)
  }
```

If `findAllWithActionsByIdIn` returns duplicates from the fetch join, `associateBy` already collapses them.

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :packages:jpa-data:test --tests 'org.migor.feedless.source.*'`
Expected: PASS, including the existing `SourceRepositoryIntTest`.

- [ ] **Step 5: Commit**

```bash
git add packages/domain/src/main/kotlin/org/migor/feedless/source/Source.kt \
  packages/domain/src/main/kotlin/org/migor/feedless/source/SourceRepository.kt \
  packages/jpa-data/src/main/kotlin/org/migor/feedless/data/jpa/source \
  packages/jpa-data/src/test/kotlin/org/migor/feedless/source/SourceDueForHarvestIntTest.kt
git commit -m "feat(jpa-data): query sources due for harvest"
```

---

### Task 6: Harvester writes per-source next time and delays without error

**Files:**
- Modify: `packages/domain/src/main/kotlin/org/migor/feedless/repository/RepositoryHarvester.kt` (`harvestRepository` `:92-129`, `scrapeSources` `:131-176`, `harvestSource` `:183-201`, `handleScrapeException` `:203-227`)
- Modify: `packages/domain/src/test/kotlin/org/migor/feedless/repository/RepositoryHarvesterTest.kt`

**Interfaces:**
- Consumes: `SourceRepository.scheduleNextHarvest`, `Source.nextHarvestAt` (Task 5); `HostBlockedException` (Task 2).
- Produces:
  - `suspend fun RepositoryHarvester.harvestScheduled(source: Source)` — takes the run slot, records lateness against `source.nextHarvestAt`, calls `harvestSource`.
  - `harvestSource(source, harvest)` unchanged signature; now schedules the source.
  - `harvestRepository` and `scrapeSources` removed.

- [ ] **Step 1: Migrate existing tests to the new entry point**

In `RepositoryHarvesterTest.kt`, every `repositoryHarvester.harvestRepository(repositoryId)` (20 call sites) becomes `repositoryHarvester.harvestScheduled(source)`; the offset-timer test uses `harvester.harvestScheduled(source)` with `` `when`(source.nextHarvestAt).thenReturn(LocalDateTime.now().minusMinutes(10)) `` instead of stubbing `repository.triggerScheduledNextAt`. Drop the `findAllByRepositoryIdFiltered` stub from `setUp`. Add to `setUp`:

```kotlin
    `when`(repository.groupId).thenReturn(GroupId())
    `when`(repository.sourcesSyncCron).thenReturn("0 0 * * * *")
```

(replacing the existing `sourcesSyncCron` stub of `""`).

- [ ] **Step 2: Write the new failing tests** (append to the class)

```kotlin
  @Test
  fun `a successful harvest schedules the source at the cron's next date`() = runTest {
    val cronNext = LocalDateTime.now().plusHours(1)
    `when`(repositoryUseCase.calculateScheduledNextAt(any2(), any2(), any2())).thenReturn(cronNext)
    `when`(scraper.scrape(any2(), any2())).thenReturn(ScrapeResult(emptyList(), 0))

    repositoryHarvester.harvestScheduled(source)

    verify(sourceRepository).scheduleNextHarvest(eq(source.id), eq(cronNext))
  }

  @Test
  fun `a throttled harvest is delayed, not failed, and waits for the longer of cron and retry`() = runTest {
    val cronNext = LocalDateTime.now().plusMinutes(1)
    `when`(repositoryUseCase.calculateScheduledNextAt(any2(), any2(), any2())).thenReturn(cronNext)
    `when`(scraper.scrape(any2(), any2())).thenThrow(
      HostOverloadingException("throttled by www.bueron.ch (429), retry in 10m", Duration.ofMinutes(10))
    )

    repositoryHarvester.harvestScheduled(source)

    verify(sourceRepository).recordHarvestInterrupted(eq(source.id), eq("throttled by www.bueron.ch (429), retry in 10m"), any2())
    verify(sourceRepository, never()).recordHarvestFailed(any2(), any2(), any2())
    verify(harvestRepository).save(argThat { !it.errornous && it.logs.contains("delayed until") })
    verify(sourceRepository).scheduleNextHarvest(eq(source.id), argThat { it.isAfter(LocalDateTime.now().plusMinutes(9)) })
  }

  @Test
  fun `a blocked harvest is delayed by its ladder step`() = runTest {
    `when`(repositoryUseCase.calculateScheduledNextAt(any2(), any2(), any2())).thenReturn(LocalDateTime.now())
    `when`(scraper.scrape(any2(), any2())).thenThrow(HostBlockedException("www.bueron.ch", 403, 2, Duration.ofMinutes(30)))

    repositoryHarvester.harvestScheduled(source)

    verify(sourceRepository).recordHarvestInterrupted(eq(source.id), eq("blocked by www.bueron.ch (403, strike 2), retry in 30m"), any2())
    verify(sourceRepository).scheduleNextHarvest(eq(source.id), argThat { it.isAfter(LocalDateTime.now().plusMinutes(29)) })
  }

  @Test
  fun `too many connections is delayed by 2 minutes`() = runTest {
    `when`(repositoryUseCase.calculateScheduledNextAt(any2(), any2(), any2())).thenReturn(LocalDateTime.now())
    `when`(scraper.scrape(any2(), any2())).thenThrow(TooManyConnectionsPerHostException(1))

    repositoryHarvester.harvestScheduled(source)

    verify(sourceRepository).recordHarvestInterrupted(eq(source.id), any2(), any2())
    verify(sourceRepository).scheduleNextHarvest(eq(source.id), argThat { it.isAfter(LocalDateTime.now().plusSeconds(110)) })
  }

  @Test
  fun `a repository without cron is not scheduled`() = runTest {
    `when`(repository.sourcesSyncCron).thenReturn("")
    `when`(scraper.scrape(any2(), any2())).thenReturn(ScrapeResult(emptyList(), 0))

    repositoryHarvester.harvestScheduled(source)

    verify(sourceRepository, never()).scheduleNextHarvest(any2(), any2())
  }

  @Test
  fun `a source whose run slot is taken is skipped`() = runTest {
    `when`(harvestRepository.startRun(any2(), any2())).thenReturn(null)

    repositoryHarvester.harvestScheduled(source)

    verify(scraper, never()).scrape(any2(), any2())
  }
```

Adjust the `ScrapeResult(...)` constructor call to its real signature (check `domain/.../scrape/ScrapeResult.kt`; existing tests in this file build one — reuse that construction). Add imports for `HostOverloadingException`, `HostBlockedException`, `org.asynchttpclient.exception.TooManyConnectionsPerHostException`.

- [ ] **Step 3: Run tests to verify they fail**

Run: `./gradlew :packages:domain:test --tests 'org.migor.feedless.repository.RepositoryHarvesterTest'`
Expected: FAIL — `Unresolved reference: harvestScheduled`.

- [ ] **Step 4: Implement**

Replace `harvestRepository` and `scrapeSources` (including the commented block inside it) with:

```kotlin
  /** One scheduled run of [source]; skipped when a harvest elsewhere holds its run slot. */
  suspend fun harvestScheduled(source: Source) {
    meterRegistry.counter(
      AppMetrics.fetchRepository, listOf(
        Tag.of("type", "source"),
        Tag.of("id", source.id.toString()),
      )
    ).count()
    source.nextHarvestAt?.let { harvestOffsetTimer.record(Duration.between(it, LocalDateTime.now())) }

    val harvest = harvestRepository.startRun(source.id, LocalDateTime.now())
    if (harvest == null) {
      log.info("skipping source ${source.id}: a real harvest of it is running already")
    } else {
      harvestSource(source, harvest)
    }
  }
```

Replace `harvestSource` and `handleScrapeException` with:

```kotlin
  suspend fun harvestSource(source: Source, harvest: Harvest): Harvest {
    val logCollector = LogCollector()
    var outcome = harvest
    var retryAfter: Duration? = null
    try {
      val count = scrapeSource(source, logCollector, harvest.id)
      outcome = outcome.copy(itemsAdded = count.added)
      sourceRepository.recordHarvestSucceeded(source.id, count.retrieved, LocalDateTime.now())
    } catch (e: Throwable) {
      retryAfter = handleScrapeException(e, source, logCollector)
      outcome = outcome.copy(errornous = retryAfter == null)
    } finally {
      scheduleNextHarvest(source, retryAfter, logCollector)
      outcome = outcome.copy(
        status = HarvestStatus.COMPLETED,
        finishedAt = LocalDateTime.now(),
        logs = logCollector.toHarvestLog(),
      )
      harvestRepository.save(outcome)
    }
    return outcome
  }

  /** Returns the delay for a passing failure, null for a real one. */
  private suspend fun handleScrapeException(e: Throwable, source: Source, logCollector: LogCollector): Duration? {
    val reason = e.describe()
    val retryAfter = passingFailureDelay(e)
    if (retryAfter == null) {
      log.error("scrape failed $reason")
      logCollector.log("scrape failed $reason")
      logCollector.log("errors in succession before this one: ${source.errorsInSuccession}")
      log.info("source ${source.id} error '$reason' increment -> '${source.errorsInSuccession}'")
      meterRegistry.counter(AppMetrics.sourceHarvestError).increment()
      sourceRepository.recordHarvestFailed(source.id, reason, LocalDateTime.now())
    } else {
      log.info("scrape delayed $reason")
      logCollector.log("delayed: $reason")
      sourceRepository.recordHarvestInterrupted(source.id, reason, LocalDateTime.now())
    }
    return retryAfter
  }

  private fun passingFailureDelay(e: Throwable): Duration? = when (e) {
    is ResumableHarvestException -> e.nextRetryAfter
    is TooManyConnectionsPerHostException -> Duration.ofMinutes(2)
    is UnknownHostException, is ConnectException -> Duration.ofMinutes(5)
    is NoItemsRetrievedException -> Duration.ZERO
    else -> null
  }

  // Swallows its own failure: a lost schedule write only means the source runs at the old time.
  private suspend fun scheduleNextHarvest(source: Source, retryAfter: Duration?, logCollector: LogCollector) {
    runCatching {
      val repository = source.repositoryId?.let { repositoryRepository.findById(it) } ?: return
      if (repository.sourcesSyncCron.isBlank()) return
      val now = LocalDateTime.now()
      val cronNext = repositoryUseCase.calculateScheduledNextAt(repository.sourcesSyncCron, repository.groupId, now)
      val next = maxOf(cronNext, now.plus(retryAfter ?: Duration.ZERO))
      if (retryAfter != null && retryAfter > Duration.ZERO) {
        logCollector.log("delayed until ${next.format(iso8601DateFormat)}")
      }
      sourceRepository.scheduleNextHarvest(source.id, next)
    }.onFailure { log.error("scheduling source ${source.id} failed: ${it.message}", it) }
  }
```

Add `import org.asynchttpclient.exception.TooManyConnectionsPerHostException`. Remove the now-unused `PageableRequest` import if nothing else uses it.

- [ ] **Step 5: Run tests to verify they pass**

Run: `./gradlew :packages:domain:test --tests 'org.migor.feedless.repository.RepositoryHarvesterTest'`
Expected: PASS. The pre-existing `given scrape fails without message ...` test asserts `scrape failed IllegalArgumentException` in the log — still produced on the real-failure branch.

- [ ] **Step 6: Commit**

```bash
git add packages/domain/src/main/kotlin/org/migor/feedless/repository/RepositoryHarvester.kt \
  packages/domain/src/test/kotlin/org/migor/feedless/repository/RepositoryHarvesterTest.kt
git commit -m "feat(domain): schedule each source after its harvest"
```

---

### Task 7: Repository reads and writes the schedule through its sources

**Files:**
- Modify: `packages/domain/src/main/kotlin/org/migor/feedless/repository/Repository.kt:31`
- Modify: `packages/domain/src/main/kotlin/org/migor/feedless/repository/RepositoryRepository.kt:19`
- Modify: `packages/domain/src/main/kotlin/org/migor/feedless/repository/RepositoryUseCase.kt:204-247`
- Modify: `packages/domain/src/main/kotlin/org/migor/feedless/source/SourceUseCase.kt`
- Modify: `packages/jpa-data/src/main/kotlin/org/migor/feedless/data/jpa/repository/AbstractRepositoryEntity.kt:116-117`
- Modify: `packages/jpa-data/src/main/kotlin/org/migor/feedless/data/jpa/repository/RepositoryDAO.kt:21-38`
- Modify: `packages/jpa-data/src/main/kotlin/org/migor/feedless/data/jpa/repository/RepositoryJpaRepository.kt:149-154`
- Modify: `packages/graphql-api/src/main/kotlin/org/migor/feedless/api/mapper/RepositoryMapper.kt:34`
- Modify: `packages/http-api/src/main/kotlin/org/migor/feedless/http/mapper/HttpRepositoryMapper.kt:38`
- Modify tests: `packages/domain/src/test/kotlin/org/migor/feedless/repository/RepositoryUpdateTest.kt`, `packages/graphql-api/src/test/kotlin/org/migor/feedless/repository/RepositoryResolverTest.kt:339-386`, `packages/http-api/src/test/kotlin/org/migor/feedless/http/RepositoryHttpControllerTest.kt:181`
- Test: `packages/jpa-data/src/test/kotlin/org/migor/feedless/source/SourceDueForHarvestIntTest.kt` (one added case)

**Interfaces:**
- Consumes: `SourceRepository.scheduleNextHarvestOfRepository` (Task 5).
- Produces: `Repository.nextHarvestAt: LocalDateTime?` (read-only); `suspend fun SourceUseCase.scheduleNextHarvestOfRepository(repositoryId: RepositoryId, at: LocalDateTime)`.

- [ ] **Step 1: Write the failing tests**

In `RepositoryUpdateTest.kt`, replace the assertion of `schedule next update now coerces current time`:

```kotlin
      verify(sourceUseCase).scheduleNextHarvestOfRepository(eq(repositoryId), eq(coercedNextAt))
```

and add:

```kotlin
  @Test
  fun `changing the cron re-seeds every source of the repository`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = ownerId)) {
      `when`(repositoryRepository.findById(any2())).thenReturn(repository)
      `when`(planConstraintsService.auditCronExpression(any2())).thenAnswer { it.arguments[0] }
      val coerced = LocalDateTime.of(2026, 9, 16, 17, 0)
      `when`(planConstraintsService.coerceMinScheduledNextAt(any2(), any2(), any2())).thenReturn(coerced)
      `when`(repositoryRepository.save(any2())).thenAnswer { it.arguments[0] }

      repositoryUseCase.updateRepository(repositoryId, RepositoryUpdate(refreshCron = "0 0 * * * *"))

      verify(sourceUseCase).scheduleNextHarvestOfRepository(eq(repositoryId), eq(coerced))
    }
```

In `SourceDueForHarvestIntTest.kt` add:

```kotlin
  @Test
  fun `a repository's nextHarvestAt is the earliest of its enabled sources`() {
    val a = createSource("https://a.example/1")
    val b = createSource("https://b.example/1")
    val early = now.plusMinutes(5).withNano(0)
    sourceRepository.scheduleNextHarvest(a.id, now.plusHours(1))
    sourceRepository.scheduleNextHarvest(b.id, early)

    assertThat(repositoryRepository.findById(a.repositoryId!!)!!.nextHarvestAt).isEqualTo(early)
  }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :packages:domain:test --tests 'org.migor.feedless.repository.RepositoryUpdateTest'`
Expected: FAIL — `Unresolved reference: scheduleNextHarvestOfRepository`.

- [ ] **Step 3: Implement**

`Repository.kt:31`: replace `val triggerScheduledNextAt: LocalDateTime? = null,` with

```kotlin
  /** Earliest next harvest of its enabled sources; read-only. */
  val nextHarvestAt: LocalDateTime? = null,
```

`AbstractRepositoryEntity.kt:116-117`: replace the `trigger_scheduled_next_at` column with

```kotlin
  @Formula("(select min(s.next_harvest_at) from t_source s where s.repository_id = id and s.is_disabled = false)")
  open var nextHarvestAt: LocalDateTime? = null
```

(import `org.hibernate.annotations.Formula`; MapStruct maps it by name, `ReportingPolicy.IGNORE` is already set and a formula is never written.)

`RepositoryDAO.kt`: delete `findAllWhereNextHarvestIsDue` and its `@Query`. `RepositoryRepository.kt:19` and `RepositoryJpaRepository.kt:149-154`: delete `findAllWhereNextHarvestIsDue`.

`SourceUseCase.kt` — add:

```kotlin
  suspend fun scheduleNextHarvestOfRepository(repositoryId: RepositoryId, at: LocalDateTime) = withContext(Dispatchers.IO) {
    sourceRepository.scheduleNextHarvestOfRepository(repositoryId, at)
  }
```

`RepositoryUseCase.updateRepository`: replace the cron block and the `nextUpdateAt` block with

```kotlin
    var scheduleSourcesAt: LocalDateTime? = null
    repository = data.refreshCron?.let {
      scheduleSourcesAt = calculateScheduledNextAt(it, groupId, repository.lastUpdatedAt)
      repository.copy(sourcesSyncCron = planConstraintsService.auditCronExpression(it))
    } ?: repository
```

```kotlin
    if (data.nextUpdateAt != null || data.scheduleNextUpdateNow) {
      val next = data.nextUpdateAt ?: LocalDateTime.now()
      scheduleSourcesAt = planConstraintsService.coerceMinScheduledNextAt(repository.lastUpdatedAt, next, groupId)
      log.info("nextUpdateAt $scheduleSourcesAt")
    }
```

and after the `data.sources?.let { ... }` block (so newly added sources are included), before saving:

```kotlin
    scheduleSourcesAt?.let { sourceUseCase.scheduleNextHarvestOfRepository(repository.id, it) }
```

Mappers: `RepositoryMapper.kt:34` → `nextUpdateAt = nextHarvestAt?.toMillis(),`; `HttpRepositoryMapper.kt:38` → `nextUpdateAt = repo.nextHarvestAt?.toOffsetDateTime(),`. In `RepositoryResolverTest.kt` and `RepositoryHttpControllerTest.kt` rename the `triggerScheduledNextAt = ...` constructor argument to `nextHarvestAt = ...` (keep the local variable name or rename it; assertions stay the same).

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :packages:domain:test :packages:graphql-api:test :packages:http-api:test` and `./gradlew :packages:jpa-data:test --tests 'org.migor.feedless.source.*' --tests 'org.migor.feedless.repository.*'`
Expected: PASS. `server-core` does not compile yet (executor still calls the removed query) — fixed in Task 8.

- [ ] **Step 5: Commit**

```bash
git add packages/domain packages/jpa-data packages/graphql-api packages/http-api
git commit -m "feat(domain): derive a repository's next update from its sources"
```

---

### Task 8: `SourceHarvesterExecutor` replaces `RepositoryHarvesterExecutor`

**Files:**
- Create: `packages/server-core/src/main/kotlin/org/migor/feedless/repository/SourceHarvesterExecutor.kt`
- Delete: `packages/server-core/src/main/kotlin/org/migor/feedless/repository/RepositoryHarvesterExecutor.kt`
- Create: `packages/server-core/src/test/kotlin/org/migor/feedless/repository/SourceHarvesterExecutorTest.kt`
- Delete: `packages/server-core/src/test/kotlin/org/migor/feedless/repository/RepositoryHarvesterExecutorTest.kt`

**Interfaces:**
- Consumes: `SourceRepository.findAllDueForHarvest` (Task 5), `RepositoryHarvester.harvestScheduled` (Task 6), `RepositoryRepository.findById`.
- Produces: bean `SourceHarvesterExecutor`, profile `"${AppProfiles.repository} & ${AppLayer.scheduler}"`, method `refreshSubscriptions()`.

- [ ] **Step 1: Write the failing test**

```kotlin
package org.migor.feedless.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.capability.MdcKeys
import org.migor.feedless.group.GroupId
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.user.UserId
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock
import org.slf4j.MDC
import org.springframework.scheduling.annotation.Scheduled
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger

class SourceHarvesterExecutorTest {

  private val repository = Repository(title = "r", ownerId = UserId(), groupId = GroupId())

  private fun sources(n: Int) = (1..n).map { Source(title = "s$it", repositoryId = repository.id) }

  @Test
  fun `verify refreshSubscriptions is annotated with scheduled`() {
    val method = SourceHarvesterExecutor::class.java.declaredMethods.first { it.name == "refreshSubscriptions" }
    assertThat(method.getAnnotation(Scheduled::class.java)).isNotNull()
  }

  @Test
  fun `each harvest of a run logs under a child of the run's correlation id`() {
    MDC.clear()
    val sourceRepository = mock<SourceRepository> { on { findAllDueForHarvest(any(), any()) } doReturn sources(2) }
    val repositoryRepository = mock<RepositoryRepository> { on { findById(any()) } doReturn repository }
    val seen = Collections.synchronizedList(mutableListOf<String?>())
    val harvester = mock<RepositoryHarvester> {
      onBlocking { harvestScheduled(any()) } doSuspendableAnswer {
        seen.add(withContext(Dispatchers.IO) { MDC.get(MdcKeys.CORR_ID) })
        Unit
      }
    }

    SourceHarvesterExecutor(harvester, sourceRepository, repositoryRepository).refreshSubscriptions()

    assertThat(seen).hasSize(2).allMatch { it != null && it.matches(Regex("[a-zA-Z0-9]{4}/[a-zA-Z0-9]{4}")) }
    assertThat(seen.map { it!!.substringBefore('/') }.distinct()).hasSize(1)
    assertThat(seen.distinct()).hasSize(2)
    assertThat(MDC.get(MdcKeys.CORR_ID)).isNull()
  }

  @Test
  fun `at most 10 sources harvest at once`() {
    val sourceRepository = mock<SourceRepository> { on { findAllDueForHarvest(any(), any()) } doReturn sources(30) }
    val repositoryRepository = mock<RepositoryRepository> { on { findById(any()) } doReturn repository }
    val running = AtomicInteger()
    val peak = AtomicInteger()
    val harvester = mock<RepositoryHarvester> {
      onBlocking { harvestScheduled(any()) } doSuspendableAnswer {
        peak.accumulateAndGet(running.incrementAndGet()) { a, b -> maxOf(a, b) }
        delay(20)
        running.decrementAndGet()
        Unit
      }
    }

    SourceHarvesterExecutor(harvester, sourceRepository, repositoryRepository).refreshSubscriptions()

    assertThat(peak.get()).isBetween(1, 10)
  }

  @Test
  fun `a source whose repository is gone is skipped`() {
    val sourceRepository = mock<SourceRepository> { on { findAllDueForHarvest(any(), any()) } doReturn sources(1) }
    val repositoryRepository = mock<RepositoryRepository> { on { findById(any()) } doReturn null }
    val calls = AtomicInteger()
    val harvester = mock<RepositoryHarvester> {
      onBlocking { harvestScheduled(any()) } doSuspendableAnswer { calls.incrementAndGet(); Unit }
    }

    SourceHarvesterExecutor(harvester, sourceRepository, repositoryRepository).refreshSubscriptions()

    assertThat(calls.get()).isZero()
  }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :packages:server-core:test --tests 'org.migor.feedless.repository.SourceHarvesterExecutorTest'`
Expected: FAIL — `Unresolved reference: SourceHarvesterExecutor` (and the old executor no longer compiles).

- [ ] **Step 3: Implement** — delete `RepositoryHarvesterExecutor.kt` and its test, then create:

```kotlin
package org.migor.feedless.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.capability.childRequestContext
import org.migor.feedless.capability.withMdcCorrId
import org.migor.feedless.source.SourceRepository
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
@Profile("${AppProfiles.repository} & ${AppLayer.scheduler}")
class SourceHarvesterExecutor internal constructor(
  private val repositoryHarvester: RepositoryHarvester,
  private val sourceRepository: SourceRepository,
  private val repositoryRepository: RepositoryRepository,
) {

  private val log = LoggerFactory.getLogger(SourceHarvesterExecutor::class.simpleName)

  @Scheduled(fixedDelay = 1345, initialDelay = 5000)
  fun refreshSubscriptions() {
    withMdcCorrId { corrId ->
      try {
        // Explicit, so an IO dispatcher hop inside the lookup carries this run's id.
        runBlocking(RequestContext(corrId = corrId)) {
          val due = sourceRepository.findAllDueForHarvest(LocalDateTime.now(), BATCH_SIZE)
          log.debug("batch refresh with ${due.size} sources")
          if (due.isEmpty()) {
            return@runBlocking
          }
          val semaphore = Semaphore(MAX_CONCURRENT)
          runCatching {
            coroutineScope {
              due.mapNotNull { source ->
                val repository = source.repositoryId?.let { repositoryRepository.findById(it) }
                if (repository == null) {
                  log.warn("skipping source ${source.id}: repository not found")
                  null
                } else {
                  async(childRequestContext(repository.ownerId, repository.groupId)) {
                    semaphore.withPermit { repositoryHarvester.harvestScheduled(source) }
                  }
                }
              }.awaitAll()
            }
            log.info("done")
          }.onFailure {
            log.error("batch refresh done: ${it.message}", it)
          }
        }
      } catch (e: Exception) {
        log.error("batch refresh failed: ${e.message}")
      }
    }
  }

  private companion object {
    const val BATCH_SIZE = 50
    const val MAX_CONCURRENT = 10
  }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :packages:server-core:test --tests 'org.migor.feedless.repository.*'`
Expected: PASS, including `QueuedHarvestExecutorTest`.

- [ ] **Step 5: Commit**

```bash
git add -A packages/server-core/src/main/kotlin/org/migor/feedless/repository \
  packages/server-core/src/test/kotlin/org/migor/feedless/repository
git commit -m "feat(server-core): harvest due sources instead of due repositories"
```

---

### Task 9: Full gate

- [ ] **Step 1: Search for leftovers**

Run: `grep -rn "triggerScheduledNextAt\|findAllWhereNextHarvestIsDue\|harvestRepository(\|RepositoryHarvesterExecutor" --include='*.kt' packages/*/src`
Expected: no output.

- [ ] **Step 2: Run the Definition of Done**

Run: `./gradlew lint test`
Expected: exit code 0. Docker must be running (jpa-data and server-core use Testcontainers).

- [ ] **Step 3: Fix and commit anything the gate surfaced** with a `fix(<module>): …` commit per module touched; rerun Step 2 until green.
