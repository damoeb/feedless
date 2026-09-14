# Report Abuse per Address Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps are numbered in bold (no task-list syntax, per this workspace's markdown rules); report progress per step.

**Goal:** Track weekly-report recipients per e-mail address so an abuse link in every report mail switches that address to opt-in, and let a global setting choose opt-out (default) or opt-in for every new subscription.

**Architecture:** A new `t_report_recipient` table keyed by the normalized address carries an `opt_in_required` flag. `ReportUseCase` resolves the recipient on every subscription and every send, decides between "active at once" and "confirmation request" from the global `app.report.subscription-mode` and the flag, and handles confirm and abuse. `ReportController` (http-api) serves the cancel, confirm and abuse links; each checks that the signed token names the id in the path.

**Tech Stack:** Kotlin 2.3, Spring Boot 4.1, JPA + Flyway + PostGIS (Testcontainers), MapStruct, Freemarker, JUnit 5 + Mockito/mockito-kotlin, Angular 21 + Vitest (Nx).

**Spec:** `docs/superpowers/specs/2026-09-14-report-abuse-per-address-design.md`

## Global Constraints

- Work in `/Users/markus.ruepp/dev2/feedless/.worktrees/weekly-report-e2e` on branch `feature/weekly-report-e2e`; every command below runs from that directory unless it says otherwise.
- Flyway: never edit a shipped migration; the new migration is `V93__report_recipient.sql`, and the same commit raises `spring.flyway.target` in `packages/server-core/src/main/resources/application-database.yaml` from `92` to `93`.
- Every Spring bean carries `@Profile("${AppProfiles.x} & ${AppLayer.y}")`; report beans use `AppProfiles.report`. A test missing a profile fails as "no such bean" — fix the test's `@ActiveProfiles`, never the bean.
- Address normalization is `email.trim().lowercase()` and nothing else; plus-tags and Gmail dots stay significant.
- `app.report.subscription-mode` accepts `opt-out` (default) or `opt-in`; it is global, not per product.
- Code comments in English, one line, saying only why.
- Commits use Conventional Commits `type(scope): subject` and end with the two attribution lines shown in each commit step.
- Definition of Done: `./gradlew lint test` exits 0. `lint` runs `prettier --write` in `app-web` and may rewrite `packages/frontend/package-lock.json` with internal-registry URLs; restore those files instead of committing them.

---

### Task 1: Recipient persistence

**Files:**

- Create: `packages/domain/src/main/kotlin/org/migor/feedless/report/ReportRecipient.kt`
- Create: `packages/domain/src/main/kotlin/org/migor/feedless/report/ReportRecipientRepository.kt`
- Modify: `packages/domain/src/main/kotlin/org/migor/feedless/report/ReportRepository.kt`
- Create: `packages/jpa-data/src/main/kotlin/org/migor/feedless/data/jpa/report/ReportRecipientEntity.kt`
- Create: `packages/jpa-data/src/main/kotlin/org/migor/feedless/data/jpa/report/ReportRecipientMapper.kt`
- Create: `packages/jpa-data/src/main/kotlin/org/migor/feedless/data/jpa/report/ReportRecipientDAO.kt`
- Create: `packages/jpa-data/src/main/kotlin/org/migor/feedless/data/jpa/report/ReportRecipientJpaRepository.kt`
- Modify: `packages/jpa-data/src/main/kotlin/org/migor/feedless/data/jpa/IdMappers.kt`
- Modify: `packages/jpa-data/src/main/kotlin/org/migor/feedless/data/jpa/report/ReportDAO.kt`
- Modify: `packages/jpa-data/src/main/kotlin/org/migor/feedless/data/jpa/report/ReportJpaRepository.kt`
- Create: `packages/jpa-data/src/main/resources/db/migration/V93__report_recipient.sql`
- Modify: `packages/server-core/src/main/resources/application-database.yaml` (line `target: 92`)
- Test: `packages/jpa-data/src/test/kotlin/org/migor/feedless/report/ReportRecipientRepositoryIntTest.kt`

**Interfaces:**

- Produces: `data class ReportRecipientId(val uuid: UUID)` (with `String` and no-arg constructors); `data class ReportRecipient(id: ReportRecipientId = ReportRecipientId(), email: String, optInRequired: Boolean = false, createdAt: LocalDateTime = LocalDateTime.now())`; `fun normalizeEmail(email: String): String`; `interface ReportRecipientRepository { fun findById(id: ReportRecipientId): ReportRecipient?; fun findByEmail(email: String): ReportRecipient?; fun save(recipient: ReportRecipient): ReportRecipient }`; `ReportRepository.disableAllByRecipientEmail(email: String, now: LocalDateTime): Int`.

**Step 1: Write the failing test.** Create `ReportRecipientRepositoryIntTest.kt`:

```kotlin
package org.migor.feedless.report

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PostgreSQLExtension
import org.migor.feedless.data.jpa.JpaDataTestApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(classes = [JpaDataTestApplication::class])
@ExtendWith(PostgreSQLExtension::class)
@DirtiesContext
@ActiveProfiles("test", "database", AppProfiles.report, AppLayer.repository)
@Testcontainers
class ReportRecipientRepositoryIntTest {

  @Autowired
  private lateinit var reportRecipientRepository: ReportRecipientRepository

  private fun uniqueEmail() = "hans-${System.nanoTime()}@example.com"

  @Test
  fun `finds a recipient by id and by address`() {
    val saved = reportRecipientRepository.save(ReportRecipient(email = uniqueEmail()))

    assertThat(reportRecipientRepository.findById(saved.id)?.email).isEqualTo(saved.email)
    assertThat(reportRecipientRepository.findByEmail(saved.email)?.id).isEqualTo(saved.id)
  }

  @Test
  fun `keeps the opt-in flag`() {
    val saved = reportRecipientRepository.save(ReportRecipient(email = uniqueEmail(), optInRequired = true))

    assertThat(reportRecipientRepository.findById(saved.id)!!.optInRequired).isTrue()
  }

  @Test
  fun `refuses a second row for the same address`() {
    val email = uniqueEmail()
    reportRecipientRepository.save(ReportRecipient(email = email))

    assertThatThrownBy { reportRecipientRepository.save(ReportRecipient(email = email)) }
      .isInstanceOf(DataIntegrityViolationException::class.java)
  }

  @Test
  fun `normalizes only case and surrounding spaces`() {
    assertThat(normalizeEmail("  Hans+News@Example.COM ")).isEqualTo("hans+news@example.com")
  }
}
```

**Step 2: Run the test to verify it fails.**

Run: `./gradlew :packages:jpa-data:test --tests 'org.migor.feedless.report.ReportRecipientRepositoryIntTest'`
Expected: compilation FAILS with `Unresolved reference 'ReportRecipientRepository'`.

**Step 3: Create the domain types.** `ReportRecipient.kt`:

```kotlin
package org.migor.feedless.report

import java.time.LocalDateTime
import java.util.*

data class ReportRecipientId(val uuid: UUID) {
  constructor(value: String) : this(UUID.fromString(value))
  constructor() : this(UUID.randomUUID())
}

/** One row per address, so opt-in applies to every report sent there, not to one report. */
data class ReportRecipient(
  val id: ReportRecipientId = ReportRecipientId(),
  val email: String,
  val optInRequired: Boolean = false,
  val createdAt: LocalDateTime = LocalDateTime.now(),
)

/** Plus-tags and provider dots stay significant; only case and surrounding spaces do not. */
fun normalizeEmail(email: String): String = email.trim().lowercase()
```

`ReportRecipientRepository.kt`:

```kotlin
package org.migor.feedless.report

interface ReportRecipientRepository {
  fun findById(id: ReportRecipientId): ReportRecipient?
  fun findByEmail(email: String): ReportRecipient?
  fun save(recipient: ReportRecipient): ReportRecipient
}
```

In `ReportRepository.kt`, add below `findAllPendingBatched`:

```kotlin
  fun disableAllByRecipientEmail(email: String, now: LocalDateTime): Int
```

**Step 4: Create the migration and raise the Flyway target.** `V93__report_recipient.sql`:

```sql
-- One row per recipient address; opt_in_required is set when its owner reports abuse.
CREATE TABLE t_report_recipient
(
  id              uuid                           NOT NULL PRIMARY KEY,
  created_at      timestamp(6) without time zone NOT NULL DEFAULT NOW(),
  email           character varying(255)         NOT NULL,
  opt_in_required boolean                        NOT NULL DEFAULT false,
  CONSTRAINT uq_report_recipient__email UNIQUE (email)
);

-- Serves disabling every report to one address, which matches on the normalized address.
CREATE INDEX report_recipient_email_normalized_idx ON t_report (lower(trim(recipient_email)));
```

In `packages/server-core/src/main/resources/application-database.yaml`, change `    target: 92` to `    target: 93`.

**Step 5: Create the JPA adapter.** `ReportRecipientEntity.kt`:

```kotlin
package org.migor.feedless.data.jpa.report

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.report.ReportRecipient

@Entity
@Table(name = "t_report_recipient")
open class ReportRecipientEntity : EntityWithUUID() {

  @Column(nullable = false, unique = true, name = "email")
  open lateinit var email: String

  @Column(nullable = false, name = "opt_in_required")
  open var optInRequired: Boolean = false
}

fun ReportRecipientEntity.toDomain(): ReportRecipient = ReportRecipientMapper.INSTANCE.toDomain(this)

fun ReportRecipient.toEntity(): ReportRecipientEntity = ReportRecipientMapper.INSTANCE.toEntity(this)
```

`ReportRecipientMapper.kt`:

```kotlin
package org.migor.feedless.data.jpa.report

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.report.ReportRecipient

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface ReportRecipientMapper {
  fun toDomain(entity: ReportRecipientEntity): ReportRecipient
  fun toEntity(domain: ReportRecipient): ReportRecipientEntity

  companion object {
    val INSTANCE: ReportRecipientMapper = Mappers.getMapper(ReportRecipientMapper::class.java)
  }
}
```

`ReportRecipientDAO.kt`:

```kotlin
package org.migor.feedless.data.jpa.report

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile("${AppProfiles.report} & ${AppLayer.repository}")
interface ReportRecipientDAO : JpaRepository<ReportRecipientEntity, UUID> {
  fun findByEmail(email: String): ReportRecipientEntity?
}
```

`ReportRecipientJpaRepository.kt`:

```kotlin
package org.migor.feedless.data.jpa.report

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.report.ReportRecipient
import org.migor.feedless.report.ReportRecipientId
import org.migor.feedless.report.ReportRecipientRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrNull

@Component
@Profile("${AppProfiles.report} & ${AppLayer.repository}")
class ReportRecipientJpaRepository(private val reportRecipientDAO: ReportRecipientDAO) : ReportRecipientRepository {
  override fun findById(id: ReportRecipientId): ReportRecipient? =
    reportRecipientDAO.findById(id.uuid).getOrNull()?.toDomain()

  override fun findByEmail(email: String): ReportRecipient? =
    reportRecipientDAO.findByEmail(email)?.toDomain()

  override fun save(recipient: ReportRecipient): ReportRecipient =
    reportRecipientDAO.save(recipient.toEntity()).toDomain()
}
```

In `IdMappers.kt`, add the import `import org.migor.feedless.report.ReportRecipientId` and, directly below the `mapCronScheduleId` pair, add:

```kotlin
  fun mapReportRecipientId(value: UUID?): ReportRecipientId? = value?.let { ReportRecipientId(it) }
  fun mapReportRecipientId(value: ReportRecipientId?): UUID? = value?.uuid
```

In `ReportDAO.kt`, add the import `import org.springframework.data.jpa.repository.Modifying` and, below `findAllEnabledPendingBatched`, add:

```kotlin
  @Modifying
  @Query(
    value = """
      update ReportEntity r set r.disabled = true, r.disabledAt = :now
      where lower(trim(r.recipientEmail)) = :email
      and r.disabled = false
    """
  )
  fun disableAllByRecipientEmail(@Param("email") email: String, @Param("now") now: LocalDateTime): Int
```

In `ReportJpaRepository.kt`, add below `findAllPendingBatched`:

```kotlin
  @Transactional
  override fun disableAllByRecipientEmail(email: String, now: LocalDateTime): Int =
    reportDAO.disableAllByRecipientEmail(email, now)
```

**Step 6: Run the test to verify it passes.**

Run: `./gradlew :packages:jpa-data:test --tests 'org.migor.feedless.report.ReportRecipientRepositoryIntTest'`
Expected: PASS, 4 tests. (Disabling mixed-case report rows is verified against the database in Task 6, where the report fixtures already exist.)

**Step 7: Compile the consumers of `ReportRepository`.**

Run: `./gradlew :packages:domain:compileTestKotlin :packages:server-core:compileTestKotlin`
Expected: BUILD SUCCESSFUL (all existing implementations are Mockito mocks or `ReportJpaRepository`).

**Step 8: Commit.**

```bash
git add packages/domain/src/main/kotlin/org/migor/feedless/report packages/jpa-data/src packages/server-core/src/main/resources/application-database.yaml
git commit -m "feat(jpa-data): store report recipients per normalized address" -m "Co-Authored-By: Claude Opus 5 (1M context) <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01WPAhT2z3eeQCiwtpacyGTU"
```

---

### Task 2: Recipient tokens and link paths

**Files:**

- Modify: `packages/domain/src/main/kotlin/org/migor/feedless/session/JwtCapabilities.kt`
- Modify: `packages/domain/src/main/kotlin/org/migor/feedless/session/TokenIssuer.kt`
- Modify: `packages/domain/src/main/kotlin/org/migor/feedless/api/ApiUrls.kt`
- Modify: `packages/server-core/src/main/kotlin/org/migor/feedless/session/JwtTokenIssuer.kt`
- Test: `packages/server-core/src/test/kotlin/org/migor/feedless/session/JwtTokenIssuerTest.kt`

**Interfaces:**

- Produces: `JwtParameterNames.RECIPIENT_ID = "recipient_id"`; `TokenIssuer.createJwtForRecipient(recipientId: String, validForDays: Long): Jwt`; `ApiUrls.reportConfirm = "/reports/confirm"`; `ApiUrls.reportAbuse = "/reports/abuse"`.

**Step 1: Write the failing test.** Append to `JwtTokenIssuerTest` (inside the class):

```kotlin
  @Test
  fun `createJwtForRecipient names the recipient and grants nothing`() = runTest {
    val jwt = jwtTokenIssuer.createJwtForRecipient("recipient-1", 365)

    val decoded = jwtTokenIssuer.decodeJwt(jwt.tokenValue)
    assertThat(decoded.getClaimAsString(JwtParameterNames.RECIPIENT_ID)).isEqualTo("recipient-1")
    assertThat(decoded.claims).doesNotContainKey(JwtParameterNames.CAPABILITIES)
  }
```

**Step 2: Run the test to verify it fails.**

Run: `./gradlew :packages:server-core:test --tests 'org.migor.feedless.session.JwtTokenIssuerTest'`
Expected: compilation FAILS with `Unresolved reference 'createJwtForRecipient'`.

**Step 3: Implement.** In `JwtCapabilities.kt`, add inside `object JwtParameterNames`, below `REPORT_ID`:

```kotlin
  /** Names the address in the abuse link of report mails; the link must outlive the report it came with. */
  const val RECIPIENT_ID = "recipient_id"
```

In `TokenIssuer.kt`, add below `createJwtForReport`:

```kotlin
  fun createJwtForRecipient(recipientId: String, validForDays: Long): Jwt
```

In `ApiUrls.kt`, replace `  const val reportDelete = "/reports/delete"` with:

```kotlin
  const val reportDelete = "/reports/delete"
  const val reportConfirm = "/reports/confirm"
  const val reportAbuse = "/reports/abuse"
```

In `JwtTokenIssuer.kt`, add below `createJwtForReport`:

```kotlin
  /** For the abuse link in report mails: names one recipient address and grants nothing else. */
  override fun createJwtForRecipient(recipientId: String, validForDays: Long): Jwt {
    meterRegistry.counter(AppMetrics.issueToken, listOf(Tag.of("type", "report-recipient"))).increment()
    return encodeJwt(
      mapOf(
        JwtParameterNames.TYPE to AuthTokenType.ANONYMOUS.value,
        JwtParameterNames.RECIPIENT_ID to recipientId,
      ),
      validForDays.days,
    )
  }
```

**Step 4: Run the test to verify it passes.**

Run: `./gradlew :packages:server-core:test --tests 'org.migor.feedless.session.JwtTokenIssuerTest'`
Expected: PASS.

**Step 5: Commit.**

```bash
git add packages/domain/src/main/kotlin/org/migor/feedless/session packages/domain/src/main/kotlin/org/migor/feedless/api/ApiUrls.kt packages/server-core/src/main/kotlin/org/migor/feedless/session/JwtTokenIssuer.kt packages/server-core/src/test/kotlin/org/migor/feedless/session/JwtTokenIssuerTest.kt
git commit -m "feat(server-core): issue recipient tokens for report abuse links" -m "Co-Authored-By: Claude Opus 5 (1M context) <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01WPAhT2z3eeQCiwtpacyGTU"
```

---

### Task 3: An abuse link in every report mail

**Files:**

- Modify: `packages/domain/src/main/kotlin/org/migor/feedless/template/TemplateService.kt`
- Modify: `packages/domain/src/main/kotlin/org/migor/feedless/pipeline/plugins/PluginParams.kt`
- Modify: `packages/domain/src/main/kotlin/org/migor/feedless/report/ReportUseCase.kt`
- Modify: `packages/server-core/src/main/kotlin/org/migor/feedless/pipeline/plugins/EventsReportPlugin.kt`
- Modify: `packages/freemarker-templates/src/main/resources/markup-templates/mail-report-created.ftl.html`
- Modify: `packages/freemarker-templates/src/main/resources/markup-templates/mail-event-calendar.ftl.html`
- Test: `packages/freemarker-templates/src/test/kotlin/org/migor/feedless/template/FreemarkerTemplateServiceIntTest.kt`
- Test: `packages/domain/src/test/kotlin/org/migor/feedless/report/ReportUseCaseTest.kt`
- Test: `packages/server-core/src/test/kotlin/org/migor/feedless/report/ReportJobExecutorTest.kt`
- Test: `packages/server-core/src/test/kotlin/org/migor/feedless/report/ReportUseCaseIntTest.kt`

**Interfaces:**

- Consumes: Task 1 `ReportRecipient`, `ReportRecipientRepository`, `normalizeEmail`; Task 2 `TokenIssuer.createJwtForRecipient`, `ApiUrls.reportAbuse`.
- Produces: `ReportCreatedParams.abuseLink: String` (after `deactivationLink`); `EventsReportPluginParams.abuseLink: String? = null`; `EventCalendarMailParams.abuseLink: String`; `ReportUseCase` constructor gains a last parameter `reportRecipientRepository: ReportRecipientRepository`; private helpers `recipientFor(email: String): ReportRecipient` and `abuseLink(recipient: ReportRecipient): String`.

**Step 1: Write the failing tests.**

In `FreemarkerTemplateServiceIntTest.testMailTemplateReportCreated`, add `abuseLink = "abuseLink",` after `deactivationLink = "deactivationLink",`, and in the expected string replace

```
If this wasn't you, or you no longer wish to receive this report, you can cancel it here:
</p>

<p>
<a href="deactivationLink"
style="display: inline-block; padding: 10px 15px; background-color: #e74c3c; color: #fff; text-decoration: none; border-radius: 4px;">
Cancel Report
</a>
</p>

</body>
```

with

```
If you no longer wish to receive this report, you can cancel it here:
</p>

<p>
<a href="deactivationLink"
style="display: inline-block; padding: 10px 15px; background-color: #e74c3c; color: #fff; text-decoration: none; border-radius: 4px;">
Cancel Report
</a>
</p>

<p style="margin-top: 20px; font-size: 13px; color: #777;">
Didn't subscribe yourself? <a href="abuseLink">Report it here</a> and this address will only get reports you confirm.
</p>

</body>
```

In `ReportUseCaseTest`:

- add the field `private lateinit var reportRecipientRepository: ReportRecipientRepository`;
- in `setUp`, before `reportUseCase = ReportUseCase(`, add:

```kotlin
    reportRecipientRepository = mock(ReportRecipientRepository::class.java)
    `when`(reportRecipientRepository.save(any(ReportRecipient::class.java))).thenAnswer { it.arguments[0] }
    `when`(tokenIssuer.createJwtForRecipient(anyString(), anyLong())).thenReturn(
      Jwt.withTokenValue("r").header("alg", "HS256").claim("recipient_id", "x").build()
    )
```

- append `reportRecipientRepository,` as the last constructor argument, after `tokenIssuer,`;
- in `a new report is active at once and its confirmation mail can cancel it`, extend the `argThat<MailTemplateReportCreated>` condition to:

```kotlin
        it.params.deactivationLink.contains("/reports/delete/") &&
          it.params.deactivationLink.contains("token=") &&
          it.params.abuseLink.contains("/reports/abuse/")
```

- add this test:

```kotlin
  @Test
  fun `stores the address normalized and resolves its recipient by it`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = anonymousId)) {
      `when`(repository.visibility).thenReturn(EntityVisibility.isPublic)

      reportUseCase.createReport(repositoryId, segment.copy(recipientEmail = "  Hans@Example.COM "))

      assertThat(savedReport().recipientEmail).isEqualTo("hans@example.com")
      verify(reportRecipientRepository).findByEmail("hans@example.com")
    }
```

In `ReportJobExecutorTest.setUp`, before `reportUseCase = ReportUseCase(`, add:

```kotlin
    val reportRecipientRepository = mock(ReportRecipientRepository::class.java)
    `when`(reportRecipientRepository.save(any(ReportRecipient::class.java))).thenAnswer { it.arguments[0] }
    `when`(jwtTokenIssuer.createJwtForRecipient(anyString(), anyLong())).thenReturn(
      Jwt.withTokenValue("token").header("alg", "HS256").claim("recipient_id", "x").build()
    )
```

and append `reportRecipientRepository,` after `jwtTokenIssuer,` in the constructor call.

In `ReportUseCaseIntTest.setUp`, below the existing `createJwtForReport` stub, add:

```kotlin
    whenever(jwtTokenIssuer.createJwtForRecipient(anyString(), anyLong())).thenReturn(
      Jwt.withTokenValue("token").header("alg", "HS256").claim("recipient_id", "x").build()
    )
```

and in `a new report is sent with the events of the coming week`, add `.contains("/reports/abuse/")` after `.contains("/reports/delete/")`.

**Step 2: Run the tests to verify they fail.**

Run: `./gradlew :packages:domain:test --tests 'org.migor.feedless.report.ReportUseCaseTest'`
Expected: compilation FAILS (`No parameter with name 'abuseLink'`, too many constructor arguments).

**Step 3: Implement the params and templates.** In `TemplateService.kt`, change `ReportCreatedParams` to:

```kotlin
data class ReportCreatedParams(
  val language: String,
  val deactivationLink: String,
  val abuseLink: String,
  val reportName: String,
  val cronExpression: String,
  val nextScheduledAt: String,
)
```

In `PluginParams.kt`, add to `EventsReportPluginParams`, after `deactivationLink`:

```kotlin
  /** Reports abuse for the whole address, not just this report. */
  val abuseLink: String? = null,
```

In `EventsReportPlugin.kt`, add `val abuseLink: String,` as the last field of `EventCalendarMailParams`, and in `report(...)` add `abuseLink = params.abuseLink ?: "",` after `deactivationLink = params.deactivationLink ?: "",`.

In `mail-report-created.ftl.html`, German branch: replace

```html
  Falls du das nicht warst oder den Bericht nicht mehr erhalten möchtest, kannst du ihn hier abbestellen:
```

with

```html
  Falls du den Bericht nicht mehr erhalten möchtest, kannst du ihn hier abbestellen:
```

and insert after that branch's closing `</p>` of the "Bericht abbestellen" button block:

```html

<p style="margin-top: 20px; font-size: 13px; color: #777;">
  Du hast das nicht selbst abonniert? <a href="${abuseLink}">Melde es hier</a>, dann bekommt diese Adresse nur noch Berichte, die du selbst bestätigst.
</p>
```

English branch: replace

```html
  If this wasn't you, or you no longer wish to receive this report, you can cancel it here:
```

with

```html
  If you no longer wish to receive this report, you can cancel it here:
```

and insert after the closing `</p>` of the "Cancel Report" button block:

```html

<p style="margin-top: 20px; font-size: 13px; color: #777;">
  Didn't subscribe yourself? <a href="${abuseLink}">Report it here</a> and this address will only get reports you confirm.
</p>
```

In `mail-event-calendar.ftl.html`, insert the same German paragraph after the "Bericht deaktivieren" button block's closing `</p>`, and the same English paragraph after the "Deactivate Report" button block's closing `</p>`.

**Step 4: Implement the use case.** In `ReportUseCase.kt`:

- append `private val reportRecipientRepository: ReportRecipientRepository,` as the last constructor parameter, after `tokenIssuer`;
- add below `deactivationLink(...)`:

```kotlin
  private fun abuseLink(recipient: ReportRecipient): String {
    val token = tokenIssuer.createJwtForRecipient(recipient.id.uuid.toString(), LINK_VALID_FOR_DAYS)
    return "${appConfig.apiGatewayUrl}${ApiUrls.reportAbuse}/${recipient.id.uuid}?token=${token.tokenValue}"
  }

  /** A concurrent first subscription of the same address loses the insert and reads the winner. */
  private fun recipientFor(email: String): ReportRecipient {
    val normalized = normalizeEmail(email)
    return reportRecipientRepository.findByEmail(normalized)
      ?: runCatching { reportRecipientRepository.save(ReportRecipient(email = normalized)) }
        .getOrElse { reportRecipientRepository.findByEmail(normalized) ?: throw it }
  }
```

- in `createReport`, replace `    val email = segment.recipientEmail` with `    val recipient = recipientFor(segment.recipientEmail)`, replace `      recipientEmail = email,` with `      recipientEmail = recipient.email,`, and replace `    sendConfirmationMail(saved, nextReportedAt)` with `    sendConfirmationMail(saved, recipient, nextReportedAt)`;
- change `sendConfirmationMail` to:

```kotlin
  private suspend fun sendConfirmationMail(report: Report, recipient: ReportRecipient, nextReportedAt: LocalDateTime) {
    val params = ReportCreatedParams(
      language = "de",
      deactivationLink = deactivationLink(report),
      abuseLink = abuseLink(recipient),
      reportName = report.recipientName,
      cronExpression = report.cronSchedule?.cronExpression ?: "",
      nextScheduledAt = nextReportedAt.toString(),
    )
```

  (the rest of the function stays as it is);
- in `sendReport`, add `          abuseLink = abuseLink(recipientFor(report.recipientEmail)),` after `          deactivationLink = deactivationLink(report),`.

**Step 5: Run the tests to verify they pass.**

Run: `./gradlew :packages:domain:test --tests 'org.migor.feedless.report.*' :packages:freemarker-templates:test :packages:server-core:test --tests 'org.migor.feedless.report.*'`
Expected: BUILD SUCCESSFUL; `ReportUseCaseIntTest` needs Docker running.

**Step 6: Commit.**

```bash
git add packages/domain/src packages/server-core/src packages/freemarker-templates/src
git commit -m "feat(domain): carry an abuse link in every report mail" -m "Co-Authored-By: Claude Opus 5 (1M context) <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01WPAhT2z3eeQCiwtpacyGTU"
```

---

### Task 4: Subscription mode, confirmation and abuse in the use case

**Files:**

- Create: `packages/domain/src/main/kotlin/org/migor/feedless/report/ReportSubscriptionMode.kt`
- Modify: `packages/domain/src/main/kotlin/org/migor/feedless/template/TemplateService.kt`
- Modify: `packages/domain/src/main/kotlin/org/migor/feedless/report/ReportUseCase.kt`
- Create: `packages/freemarker-templates/src/main/resources/markup-templates/mail-report-confirm-request.ftl.html`
- Modify: `packages/server-core/src/main/resources/application.yaml`
- Test: `packages/freemarker-templates/src/test/kotlin/org/migor/feedless/template/FreemarkerTemplateServiceIntTest.kt`
- Test: `packages/domain/src/test/kotlin/org/migor/feedless/report/ReportUseCaseTest.kt`
- Test: `packages/server-core/src/test/kotlin/org/migor/feedless/report/ReportJobExecutorTest.kt`

**Interfaces:**

- Consumes: Task 3 `recipientFor`, `abuseLink`, constructor order.
- Produces: `enum class ReportSubscriptionMode { OPT_OUT, OPT_IN }` with `ReportSubscriptionMode.parse(value: String)`; `ReportConfirmRequestParams(language: String, confirmationLink: String, abuseLink: String)` and `MailTemplateReportConfirmRequest`; `ReportUseCase` constructor gains a last parameter `@Value("\${app.report.subscription-mode:opt-out}") subscriptionModeSetting: String`; `suspend fun ReportUseCase.confirmReportFromToken(reportId: ReportId)`; `suspend fun ReportUseCase.reportAbuse(recipientId: ReportRecipientId)`.

**Step 1: Write the failing tests.** Add to `FreemarkerTemplateServiceIntTest`:

```kotlin
  @Test
  fun testMailTemplateReportConfirmRequest() {
    assertThat(
      renderTemplate(
        MailTemplateReportConfirmRequest(
          ReportConfirmRequestParams(
            language = "en",
            confirmationLink = "confirmationLink",
            abuseLink = "abuseLink",
          )
        )
      )
    ).isEqualTo(
      """
<!DOCTYPE html>

<html lang="en">
<head>
<meta charset="UTF-8">
<title>Scheduled Report</title>
</head>
<body style="font-family: Arial, sans-serif; line-height: 1.5; color: #333;">

<h2 style="color: #2c3e50;">Please confirm your subscription</h2>
<p>A report was ordered for this address. It starts only once you confirm it:</p>

<p>
<a href="confirmationLink"
style="display: inline-block; padding: 10px 15px; background-color: #12775C; color: #fff; text-decoration: none; border-radius: 4px;">
Confirm subscription
</a>
</p>

<p>Without your confirmation we send nothing.</p>

<p style="margin-top: 20px; font-size: 13px; color: #777;">
Didn't subscribe yourself? <a href="abuseLink">Report it here</a> and this address will only get reports you confirm.
</p>

</body>
</html>
""".trimAllIndents()
    )
  }
```

In `ReportUseCaseTest`:

- add imports `org.assertj.core.api.Assertions.assertThatThrownBy`, `org.migor.feedless.actions.PluginExecutionJson`, `org.migor.feedless.pipelineJob.PluginExecution`, `org.migor.feedless.template.MailTemplateReportConfirmRequest`;
- replace the whole `reportUseCase = ReportUseCase( … )` block in `setUp` with `reportUseCase = newUseCase("opt-out")`, and add this helper to the class:

```kotlin
  private fun newUseCase(subscriptionMode: String) = ReportUseCase(
    reportRepository,
    cronScheduleRepository,
    repositoryRepository,
    segmentationRepository,
    mock(MeterRegistry::class.java),
    RepositoryGuard(
      repositoryRepository,
      UserGuard(userRepository),
      mock(UserGroupAssignmentRepository::class.java),
    ),
    templateService,
    pipelinePlugins,
    mailService,
    mock(ReportGuard::class.java),
    documentRepository,
    "no-reply@test.local",
    appConfig,
    userRepository,
    tokenIssuer,
    reportRecipientRepository,
    subscriptionMode,
  )
```

- add these tests:

```kotlin
  private fun flagged(email: String = "hans@example.com") {
    `when`(reportRecipientRepository.findByEmail(email))
      .thenReturn(ReportRecipient(email = email, optInRequired = true))
  }

  @Test
  fun `a flagged address gets an inactive report and a confirmation request`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = anonymousId)) {
      `when`(repository.visibility).thenReturn(EntityVisibility.isPublic)
      flagged()

      reportUseCase.createReport(repositoryId, segment)

      assertThat(savedReport().authorized).isFalse()
      verify(templateService).renderTemplate(argThat<MailTemplateReportConfirmRequest> {
        it.params.confirmationLink.contains("/reports/confirm/") &&
          it.params.abuseLink.contains("/reports/abuse/")
      })
      verify(templateService, never()).renderTemplate(any(MailTemplateReportCreated::class.java))
    }

  @Test
  fun `in opt-in mode every new report waits for confirmation`() =
    runTest(context = RequestContext(groupId = GroupId(), userId = anonymousId)) {
      `when`(repository.visibility).thenReturn(EntityVisibility.isPublic)
      reportUseCase = newUseCase("opt-in")

      reportUseCase.createReport(repositoryId, segment)

      assertThat(savedReport().authorized).isFalse()
    }

  @Test
  fun `rejects an unknown subscription mode`() {
    assertThatThrownBy { newUseCase("sometimes") }.isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `reporting abuse flags the address and stops its reports`() = runTest {
    val recipient = ReportRecipient(email = "hans@example.com")
    `when`(reportRecipientRepository.findById(recipient.id)).thenReturn(recipient)

    reportUseCase.reportAbuse(recipient.id)

    verify(reportRecipientRepository).save(argThat<ReportRecipient> { it.id == recipient.id && it.optInRequired })
    verify(reportRepository).disableAllByRecipientEmail(eq("hans@example.com"), any(LocalDateTime::class.java))
  }

  @Test
  fun `reporting abuse again keeps the flag and still stops reports`() = runTest {
    val recipient = ReportRecipient(email = "hans@example.com", optInRequired = true)
    `when`(reportRecipientRepository.findById(recipient.id)).thenReturn(recipient)

    reportUseCase.reportAbuse(recipient.id)

    verify(reportRecipientRepository, never()).save(any(ReportRecipient::class.java))
    verify(reportRepository).disableAllByRecipientEmail(eq("hans@example.com"), any(LocalDateTime::class.java))
  }

  private fun storedReport(authorized: Boolean, disabled: Boolean = false): Report = Report(
    recipientEmail = "hans@example.com",
    recipientName = "Hans Muster",
    reporterPlugin = PluginExecution(id = reportPluginId, params = PluginExecutionJson()),
    segmentId = SegmentationId(),
    cronScheduleId = CronSchedule(cronExpression = WEEKLY_REPORT_CRON).id,
    authorized = authorized,
    disabled = disabled,
  ).also { `when`(reportRepository.findById(it.id)).thenReturn(it) }

  @Test
  fun `confirming activates a pending report`() = runTest {
    val report = storedReport(authorized = false)

    reportUseCase.confirmReportFromToken(report.id)

    assertThat(savedReport().authorized).isTrue()
  }

  @Test
  fun `confirming does not revive a report stopped by an abuse report`() = runTest {
    val report = storedReport(authorized = false, disabled = true)

    reportUseCase.confirmReportFromToken(report.id)

    verify(reportRepository, never()).save(any(Report::class.java))
  }
```

- in `setUp`, below the existing `renderTemplate(any2<MailTemplateReportCreated>())` stub, nothing else is needed: `any2` matches every template, so the confirmation request renders `""` too.

In `ReportJobExecutorTest.setUp`, append `"opt-out",` after `reportRecipientRepository,` in the constructor call.

**Step 2: Run the tests to verify they fail.**

Run: `./gradlew :packages:domain:test --tests 'org.migor.feedless.report.ReportUseCaseTest'`
Expected: compilation FAILS (`Unresolved reference 'MailTemplateReportConfirmRequest'`, `'reportAbuse'`, `'confirmReportFromToken'`).

**Step 3: Implement the mode.** `ReportSubscriptionMode.kt`:

```kotlin
package org.migor.feedless.report

enum class ReportSubscriptionMode(val value: String) {
  OPT_OUT("opt-out"),
  OPT_IN("opt-in");

  companion object {
    fun parse(value: String): ReportSubscriptionMode =
      entries.firstOrNull { it.value == value.trim().lowercase() }
        ?: throw IllegalArgumentException("app.report.subscription-mode must be opt-out or opt-in, was '$value'")
  }
}
```

In `application.yaml`, add under the top-level `app:` key, directly above `  dateFormat: dd-MM-yyyy`:

```yaml
  report:
    # opt-out starts new subscriptions at once unless their address asked for opt-in; opt-in confirms every one
    subscription-mode: ${APP_REPORT_SUBSCRIPTION_MODE:opt-out}
```

**Step 4: Implement the template.** In `TemplateService.kt`, add below `MailTemplateReportCreated`:

```kotlin
data class ReportConfirmRequestParams(
  val language: String,
  val confirmationLink: String,
  val abuseLink: String,
)

data class MailTemplateReportConfirmRequest(override val params: ReportConfirmRequestParams) :
  FreemarkerTemplate<ReportConfirmRequestParams>("mail-report-confirm-request")
```

Create `mail-report-confirm-request.ftl.html`:

```html
<!DOCTYPE html>

<#if language == "de">
<html lang="de">
<head>
  <meta charset="UTF-8">
  <title>Geplanter Bericht</title>
</head>
<body style="font-family: Arial, sans-serif; line-height: 1.5; color: #333;">

<h2 style="color: #2c3e50;">Bitte bestätige dein Abo</h2>
<p>Für diese Adresse wurde ein Bericht bestellt. Er startet erst, wenn du ihn bestätigst:</p>

<p>
  <a href="${confirmationLink}"
     style="display: inline-block; padding: 10px 15px; background-color: #12775C; color: #fff; text-decoration: none; border-radius: 4px;">
    Abo bestätigen
  </a>
</p>

<p>Ohne deine Bestätigung schicken wir nichts.</p>

<p style="margin-top: 20px; font-size: 13px; color: #777;">
  Du hast das nicht selbst abonniert? <a href="${abuseLink}">Melde es hier</a>, dann bekommt diese Adresse nur noch Berichte, die du selbst bestätigst.
</p>

</body>
</html>
<#else>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Scheduled Report</title>
</head>
<body style="font-family: Arial, sans-serif; line-height: 1.5; color: #333;">

<h2 style="color: #2c3e50;">Please confirm your subscription</h2>
<p>A report was ordered for this address. It starts only once you confirm it:</p>

<p>
  <a href="${confirmationLink}"
     style="display: inline-block; padding: 10px 15px; background-color: #12775C; color: #fff; text-decoration: none; border-radius: 4px;">
    Confirm subscription
  </a>
</p>

<p>Without your confirmation we send nothing.</p>

<p style="margin-top: 20px; font-size: 13px; color: #777;">
  Didn't subscribe yourself? <a href="${abuseLink}">Report it here</a> and this address will only get reports you confirm.
</p>

</body>
</html>
</#if>
```

**Step 5: Implement the use case.** In `ReportUseCase.kt`:

- add imports `org.migor.feedless.NotFoundException`, `org.migor.feedless.template.MailTemplateReportConfirmRequest`, `org.migor.feedless.template.ReportConfirmRequestParams`;
- append the constructor parameter after `reportRecipientRepository`:

```kotlin
  @Value("\${app.report.subscription-mode:opt-out}") subscriptionModeSetting: String,
```

- add as the first line of the class body:

```kotlin
  private val subscriptionMode = ReportSubscriptionMode.parse(subscriptionModeSetting)
```

- add below `deactivationLink(...)`:

```kotlin
  private fun confirmationLink(report: Report): String {
    val token = tokenIssuer.createJwtForReport(report.id.uuid.toString(), LINK_VALID_FOR_DAYS)
    return "${appConfig.apiGatewayUrl}${ApiUrls.reportConfirm}/${report.id.uuid}?token=${token.tokenValue}"
  }
```

- in `createReport`, below `val recipient = recipientFor(segment.recipientEmail)`, add:

```kotlin
    // opt-out assumes no abuse; opt-in mode or an address whose owner reported abuse needs the owner's click
    val needsConfirmation = subscriptionMode == ReportSubscriptionMode.OPT_IN || recipient.optInRequired
```

- in the `Report(...)` construction, replace

```kotlin
      // no opt-in step: active at once, and every mail carries a cancel link
      authorized = true,
      authorizedAt = LocalDateTime.now(),
```

  with

```kotlin
      authorized = !needsConfirmation,
      authorizedAt = if (needsConfirmation) null else LocalDateTime.now(),
```

- replace `    sendConfirmationMail(saved, recipient, nextReportedAt)` with:

```kotlin
    if (needsConfirmation) {
      sendConfirmationRequest(saved, recipient)
    } else {
      sendConfirmationMail(saved, recipient, nextReportedAt)
    }
```

- add below `sendConfirmationMail`:

```kotlin
  private suspend fun sendConfirmationRequest(report: Report, recipient: ReportRecipient) {
    val body = templateService.renderTemplate(
      MailTemplateReportConfirmRequest(
        ReportConfirmRequestParams(
          language = "de",
          confirmationLink = confirmationLink(report),
          abuseLink = abuseLink(recipient),
        )
      )
    )
    mailService.send(
      OutgoingMail(
        from = mailSender,
        to = listOf(report.recipientEmail),
        subject = "Bitte bestätige dein Abo",
        htmlContent = body
      )
    )
  }

  /** From the confirmation link: the signed token is the proof, so this bypasses ReportGuard. */
  suspend fun confirmReportFromToken(reportId: ReportId) {
    withContext(Dispatchers.IO) {
      log.info("confirmReportFromToken reportId=$reportId")
      val report = reportRepository.findById(reportId) ?: throw NotFoundException("Report $reportId not found")
      // a report stopped by an abuse report stays stopped
      if (!report.authorized && !report.disabled) {
        reportRepository.save(report.copy(authorized = true, authorizedAt = LocalDateTime.now()))
      }
    }
  }

  /** From the abuse link: the address switches to opt-in and every report to it stops. */
  suspend fun reportAbuse(recipientId: ReportRecipientId) {
    withContext(Dispatchers.IO) {
      log.info("reportAbuse recipientId=$recipientId")
      val recipient = reportRecipientRepository.findById(recipientId)
        ?: throw NotFoundException("Recipient $recipientId not found")
      if (!recipient.optInRequired) {
        reportRecipientRepository.save(recipient.copy(optInRequired = true))
      }
      reportRepository.disableAllByRecipientEmail(recipient.email, LocalDateTime.now())
    }
  }
```

**Step 6: Run the tests to verify they pass.**

Run: `./gradlew :packages:domain:test --tests 'org.migor.feedless.report.*' :packages:freemarker-templates:test :packages:server-core:test --tests 'org.migor.feedless.report.*'`
Expected: BUILD SUCCESSFUL.

**Step 7: Commit.**

```bash
git add packages/domain/src packages/server-core/src packages/freemarker-templates/src
git commit -m "feat(domain): choose opt-out or opt-in globally and per address" -m "Co-Authored-By: Claude Opus 5 (1M context) <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01WPAhT2z3eeQCiwtpacyGTU"
```

---

### Task 5: Confirm and abuse endpoints, public without login

**Files:**

- Modify: `packages/http-api/src/main/kotlin/org/migor/feedless/report/ReportController.kt`
- Modify: `packages/domain/src/main/kotlin/org/migor/feedless/template/TemplateService.kt`
- Create: `packages/freemarker-templates/src/main/resources/markup-templates/page-report-abuse.ftl.html`
- Modify: `packages/server-core/src/main/kotlin/org/migor/feedless/config/SecurityConfig.kt`
- Test: `packages/server-core/src/test/kotlin/org/migor/feedless/report/ReportControllerTest.kt` (replace)
- Test: `packages/server-core/src/test/kotlin/org/migor/feedless/config/ReportLinksSecurityIntTest.kt` (create)
- Test: `packages/freemarker-templates/src/test/kotlin/org/migor/feedless/template/FreemarkerTemplateServiceIntTest.kt`

**Interfaces:**

- Consumes: Task 2 `JwtParameterNames.RECIPIENT_ID`, `ApiUrls.reportConfirm`, `ApiUrls.reportAbuse`, `TokenIssuer.createJwtForRecipient`; Task 4 `ReportUseCase.confirmReportFromToken`, `ReportUseCase.reportAbuse`.
- Produces: `class PageTemplateReportAbuse(params: Unit = Unit)` (template `page-report-abuse`); `ReportController(reportUseCase: ReportUseCase, tokenIssuer: TokenIssuer, templateService: TemplateService)` with `deleteReport`, `confirmReport(reportId, token)`, `reportAbuse(recipientId, token)`.

**Step 1: Write the failing tests.** Replace `ReportControllerTest.kt` with:

```kotlin
package org.migor.feedless.report

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.session.JwtParameterNames
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.template.PageTemplateReportAbuse
import org.migor.feedless.template.TemplateService
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verifyBlocking
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.oauth2.jwt.Jwt

/** The links in report mails; recipients usually have no account, so the signed token is the proof. */
class ReportControllerTest {

  private val reportId = ReportId()
  private val recipientId = ReportRecipientId()
  private lateinit var reportUseCase: ReportUseCase
  private lateinit var jwtTokenIssuer: JwtTokenIssuer
  private lateinit var templateService: TemplateService
  private lateinit var controller: ReportController

  private fun tokenWith(claim: String, value: String): Jwt = Jwt.withTokenValue("token")
    .header("alg", "HS256")
    .claim(claim, value)
    .build()

  private fun tokenDecodesTo(jwt: Jwt) {
    jwtTokenIssuer.stub { onBlocking { decodeJwt(any<String>()) } doReturn jwt }
  }

  @BeforeEach
  fun setUp() {
    reportUseCase = mock()
    jwtTokenIssuer = mock()
    templateService = mock()
    controller = ReportController(reportUseCase, jwtTokenIssuer, templateService)
  }

  @Test
  fun `cancels a report through the link in its mail`() = runTest {
    tokenDecodesTo(tokenWith(JwtParameterNames.REPORT_ID, reportId.uuid.toString()))

    controller.deleteReport(reportId.uuid.toString(), "token")

    verifyBlocking(reportUseCase) { deleteReportFromToken(reportId) }
  }

  @Test
  fun `confirms a report through the link in its confirmation request`() = runTest {
    tokenDecodesTo(tokenWith(JwtParameterNames.REPORT_ID, reportId.uuid.toString()))

    controller.confirmReport(reportId.uuid.toString(), "token")

    verifyBlocking(reportUseCase) { confirmReportFromToken(reportId) }
  }

  @Test
  fun `reports abuse through the link and answers with a page`() = runTest {
    tokenDecodesTo(tokenWith(JwtParameterNames.RECIPIENT_ID, recipientId.uuid.toString()))
    templateService.stub { on { renderTemplate(any<PageTemplateReportAbuse>()) } doReturn "<p>danke</p>" }

    val response = controller.reportAbuse(recipientId.uuid.toString(), "token")

    verifyBlocking(reportUseCase) { reportAbuse(recipientId) }
    assertThat(response.body).isEqualTo("<p>danke</p>")
  }

  /** Without this check the link from one's own mail could cancel anyone's report. */
  @Test
  fun `rejects a valid token that names another report`() {
    tokenDecodesTo(tokenWith(JwtParameterNames.REPORT_ID, ReportId().uuid.toString()))

    assertThatExceptionOfType(AccessDeniedException::class.java).isThrownBy {
      runTest { controller.deleteReport(reportId.uuid.toString(), "token") }
    }
    verifyBlocking(reportUseCase, never()) { deleteReportFromToken(any()) }
  }

  @Test
  fun `rejects a report token on the abuse link`() {
    tokenDecodesTo(tokenWith(JwtParameterNames.REPORT_ID, recipientId.uuid.toString()))

    assertThatExceptionOfType(AccessDeniedException::class.java).isThrownBy {
      runTest { controller.reportAbuse(recipientId.uuid.toString(), "token") }
    }
    verifyBlocking(reportUseCase, never()) { reportAbuse(any()) }
  }

  @Test
  fun `rejects a token that does not decode`() {
    jwtTokenIssuer.stub {
      onBlocking { decodeJwt(any<String>()) } doThrow IllegalArgumentException("bad signature")
    }

    assertThatExceptionOfType(AccessDeniedException::class.java).isThrownBy {
      runTest { controller.confirmReport(reportId.uuid.toString(), "forged") }
    }
    verifyBlocking(reportUseCase, never()) { confirmReportFromToken(any()) }
  }
}
```

Create `ReportLinksSecurityIntTest.kt`:

```kotlin
package org.migor.feedless.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.DisableDatabaseConfiguration
import org.migor.feedless.analytics.AnalyticsService
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.api.graphql.ServerConfigResolver
import org.migor.feedless.document.DocumentController
import org.migor.feedless.feed.FeedService
import org.migor.feedless.group.GroupRepository
import org.migor.feedless.group.GroupUseCase
import org.migor.feedless.report.ReportGuard
import org.migor.feedless.report.ReportUseCase
import org.migor.feedless.repository.RepositoryGuard
import org.migor.feedless.secrets.OneTimePasswordService
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.session.SessionResolver
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.template.TemplateService
import org.migor.feedless.user.UserGuard
import org.migor.feedless.user.UserRepository
import org.migor.feedless.user.UserUseCase
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.migor.feedless.userSecret.UserSecretRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

/** Recipients have no account, so every link in a report mail must pass the real filter chain anonymously. */
@ExtendWith(SpringExtension::class)
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = ["app.actuatorPassword=$actuatorPassword"],
)
@MockitoBean(
  types = [
    ServerConfigResolver::class,
    UserRepository::class,
    UserSecretRepository::class,
    UserUseCase::class,
    SessionResolver::class,
    DocumentController::class,
    UserGuard::class,
    OneTimePasswordService::class,
    OAuth2AuthorizedClientService::class,
    FeedService::class,
    SourceRepository::class,
    RepositoryGuard::class,
    UserGroupAssignmentRepository::class,
    GroupRepository::class,
    GroupUseCase::class,
    AnalyticsService::class,
    ReportUseCase::class,
    ReportGuard::class,
    TemplateService::class,
  ]
)
@ActiveProfiles(
  "test",
  AppLayer.api,
  AppLayer.service,
  AppProfiles.properties,
  AppLayer.security,
  AppProfiles.session,
  AppProfiles.feed,
  AppProfiles.report,
  "metrics"
)
@Import(DisableDatabaseConfiguration::class)
class ReportLinksSecurityIntTest {

  @LocalServerPort
  var port = 0

  @Autowired
  private lateinit var jwtTokenIssuer: JwtTokenIssuer

  private fun get(path: String) = TestRestTemplate().getForEntity("http://localhost:$port$path", String::class.java)

  @Test
  fun `the cancel link works without login`() {
    val id = UUID.randomUUID().toString()
    val token = jwtTokenIssuer.createJwtForReport(id, 1).tokenValue

    assertThat(get("${ApiUrls.reportDelete}/$id?token=$token").statusCode).isEqualTo(HttpStatus.OK)
  }

  @Test
  fun `the confirm link works without login`() {
    val id = UUID.randomUUID().toString()
    val token = jwtTokenIssuer.createJwtForReport(id, 1).tokenValue

    assertThat(get("${ApiUrls.reportConfirm}/$id?token=$token").statusCode).isEqualTo(HttpStatus.OK)
  }

  @Test
  fun `the abuse link works without login`() {
    val id = UUID.randomUUID().toString()
    val token = jwtTokenIssuer.createJwtForRecipient(id, 1).tokenValue

    assertThat(get("${ApiUrls.reportAbuse}/$id?token=$token").statusCode).isEqualTo(HttpStatus.OK)
  }
}
```

Add to `FreemarkerTemplateServiceIntTest`:

```kotlin
  @Test
  fun testPageTemplateReportAbuse() {
    assertThat(renderTemplate(PageTemplateReportAbuse()))
      .contains("Danke für die Meldung")
      .contains("Neue Abos starten erst, wenn du sie selbst bestätigst.")
  }
```

**Step 2: Run the tests to verify they fail.**

Run: `./gradlew :packages:server-core:test --tests 'org.migor.feedless.report.ReportControllerTest'`
Expected: compilation FAILS (`Unresolved reference 'PageTemplateReportAbuse'`, `'confirmReport'`, `'reportAbuse'`).

**Step 3: Implement the page.** In `TemplateService.kt`, add below `MailTemplateChangeTrackerAuthorized`:

```kotlin
class PageTemplateReportAbuse(override val params: Unit = Unit) :
  FreemarkerTemplate<Unit>("page-report-abuse")
```

Create `page-report-abuse.ftl.html`:

```html
<!DOCTYPE html>
<html lang="de">
<head>
  <meta charset="UTF-8">
  <title>Meldung erhalten</title>
</head>
<body style="font-family: Arial, sans-serif; line-height: 1.5; color: #333;">
<div style="max-width: 480px; margin: 40px auto; padding: 0 16px; text-align: center;">
  <h2 style="color: #2c3e50;">Danke für die Meldung</h2>
  <p>Diese Adresse bekommt keine Berichte mehr. Neue Abos starten erst, wenn du sie selbst bestätigst.</p>
</div>
</body>
</html>
```

**Step 4: Implement the controller.** Replace the body of `ReportController.kt` from the imports down with:

```kotlin
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiUrls.reportAbuse
import org.migor.feedless.api.ApiUrls.reportConfirm
import org.migor.feedless.api.ApiUrls.reportDelete
import org.migor.feedless.session.JwtParameterNames
import org.migor.feedless.session.TokenIssuer
import org.migor.feedless.template.PageTemplateReportAbuse
import org.migor.feedless.template.TemplateService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

/**
 * The links in report mails. They work without login, since recipients usually have no account: the signed token is
 * the proof, and it must name the id in the path so a valid token cannot act on someone else's report or address.
 */
@Controller
@Profile("${AppProfiles.report} & ${AppLayer.api}")
class ReportController(
  private val reportUseCase: ReportUseCase,
  private val tokenIssuer: TokenIssuer,
  private val templateService: TemplateService,
) {

  private val log = LoggerFactory.getLogger(ReportController::class.simpleName)

  @GetMapping("$reportDelete/{reportId}")
  suspend fun deleteReport(
    @PathVariable("reportId") reportId: String,
    @RequestParam("token") token: String,
  ): ResponseEntity<String> {
    log.info("GET deleteReport id=$reportId")
    requireClaim(token, JwtParameterNames.REPORT_ID, reportId)
    reportUseCase.deleteReportFromToken(ReportId(reportId))
    return ResponseEntity.ok().body("report deleted")
  }

  @GetMapping("$reportConfirm/{reportId}")
  suspend fun confirmReport(
    @PathVariable("reportId") reportId: String,
    @RequestParam("token") token: String,
  ): ResponseEntity<String> {
    log.info("GET confirmReport id=$reportId")
    requireClaim(token, JwtParameterNames.REPORT_ID, reportId)
    reportUseCase.confirmReportFromToken(ReportId(reportId))
    return ResponseEntity.ok().body("report confirmed")
  }

  @GetMapping("$reportAbuse/{recipientId}")
  suspend fun reportAbuse(
    @PathVariable("recipientId") recipientId: String,
    @RequestParam("token") token: String,
  ): ResponseEntity<String> {
    log.info("GET reportAbuse recipientId=$recipientId")
    requireClaim(token, JwtParameterNames.RECIPIENT_ID, recipientId)
    reportUseCase.reportAbuse(ReportRecipientId(recipientId))
    return ResponseEntity.ok()
      .contentType(MediaType.TEXT_HTML)
      .body(templateService.renderTemplate(PageTemplateReportAbuse()))
  }

  private suspend fun requireClaim(token: String, claim: String, expected: String) {
    val claimed = runCatching { tokenIssuer.decodeJwt(token) }
      .getOrElse { throw AccessDeniedException("invalid token") }
      .getClaimAsString(claim)

    if (claimed != expected) {
      throw AccessDeniedException("token does not name this link's target")
    }
  }
}
```

In `SecurityConfig.whitelistedUrls()`, replace `      ApiUrls.mailForwardingAllow + "/**",` with:

```kotlin
      ApiUrls.mailForwardingAllow + "/**",
      // links in report mails; recipients usually have no account
      ApiUrls.reportDelete + "/**",
      ApiUrls.reportConfirm + "/**",
      ApiUrls.reportAbuse + "/**",
```

**Step 5: Run the tests to verify they pass.**

Run: `./gradlew :packages:freemarker-templates:test :packages:http-api:test :packages:server-core:test --tests 'org.migor.feedless.report.ReportControllerTest' --tests 'org.migor.feedless.config.ReportLinksSecurityIntTest' --tests 'org.migor.feedless.config.SecurityConfigIntTest'`
Expected: BUILD SUCCESSFUL. To learn whether the already-pushed cancel link was blocked, run `ReportLinksSecurityIntTest` once with the three whitelist lines commented out and note the result in the commit body; restore the lines before committing.

**Step 6: Commit.**

```bash
git add packages/http-api/src packages/domain/src/main/kotlin/org/migor/feedless/template/TemplateService.kt packages/freemarker-templates/src packages/server-core/src
git commit -m "feat(http-api): serve the confirm and abuse links of report mails" -m "Co-Authored-By: Claude Opus 5 (1M context) <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01WPAhT2z3eeQCiwtpacyGTU"
```

---

### Task 6: Abuse and confirmation against the database

**Files:**

- Test: `packages/server-core/src/test/kotlin/org/migor/feedless/report/ReportUseCaseIntTest.kt`

**Interfaces:**

- Consumes: Task 1 `ReportRecipientRepository`, `ReportRepository.disableAllByRecipientEmail`; Task 4 `reportAbuse`, `confirmReportFromToken`.

**Step 1: Write the tests.** In `ReportUseCaseIntTest`, add the field:

```kotlin
  @Autowired
  private lateinit var reportRecipientRepository: ReportRecipientRepository
```

and the tests:

```kotlin
  @Test
  fun `reporting abuse stops every report to the address, whatever its case`() =
    runTest(context = RequestContext(userId = user.id, groupId = group.id)) {
      val report = createReport()
      // a row stored before addresses were normalized
      reportRepository.save(report.copy(recipientEmail = "EMAIL@Somewhere"))
      val recipient = reportRecipientRepository.findByEmail("email@somewhere")!!

      reportUseCase.reportAbuse(recipient.id)
      reset(mailService)
      reportUseCase.processReportJobs()

      verify(mailService, never()).send(any(OutgoingMail::class.java))
      assertThat(reportRepository.findById(report.id)!!.disabled).isTrue()
      assertThat(reportRecipientRepository.findById(recipient.id)!!.optInRequired).isTrue()
    }

  @Test
  fun `after an abuse report a new subscription waits for its confirmation`() =
    runTest(context = RequestContext(userId = user.id, groupId = group.id)) {
      createReport()
      reportUseCase.reportAbuse(reportRecipientRepository.findByEmail("email@somewhere")!!.id)
      reset(mailService)

      val pending = createReport()

      assertThat(reportRepository.findById(pending.id)!!.authorized).isFalse()
      val captor = argumentCaptor<OutgoingMail>()
      verify(mailService).send(captor.capture())
      assertThat(captor.firstValue.subject).isEqualTo("Bitte bestätige dein Abo")
      assertThat(captor.firstValue.htmlContent).contains("/reports/confirm/").contains("/reports/abuse/")

      reset(mailService)
      reportUseCase.processReportJobs()
      verify(mailService, never()).send(any(OutgoingMail::class.java))

      reportUseCase.confirmReportFromToken(pending.id)
      reportUseCase.processReportJobs()
      verify(mailService).send(any(OutgoingMail::class.java))
    }
```

**Step 2: Run the tests.**

Run: `./gradlew :packages:server-core:test --tests 'org.migor.feedless.report.ReportUseCaseIntTest'`
Expected: PASS, 6 tests (Docker required). A failure here points at Task 1's disable query or Task 4's flow; fix the implementation, not the test.

**Step 3: Commit.**

```bash
git add packages/server-core/src/test/kotlin/org/migor/feedless/report/ReportUseCaseIntTest.kt
git commit -m "test(server-core): cover abuse reports and confirmation against the database" -m "Co-Authored-By: Claude Opus 5 (1M context) <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01WPAhT2z3eeQCiwtpacyGTU"
```

---

### Task 7: One neutral message after subscribing

**Files:**

- Modify: `packages/frontend/apps/upcoming/src/app/components/email-abo-modal/email-abo-modal.component.ts`
- Test: `packages/frontend/apps/upcoming/src/app/components/email-abo-modal/email-abo-modal.component.spec.ts`

**Step 1: Write the failing test.** In the spec, replace the test `closes and tells the visitor the subscription is active and can be cancelled` with:

```ts
  it('closes with one message that does not reveal whether the address must confirm', async () => {
    await component.subscribe(subscription);

    expect(dismiss).toHaveBeenCalled();
    const { header, message } = createAlert.mock.calls[0][0];
    expect(message).toBe('Wir haben dir eine E-Mail geschickt.');
    expect(`${header} ${message}`).not.toMatch(/aktiv|bestätig/i);
  });
```

**Step 2: Run the test to verify it fails.**

Run: `cd packages/frontend && npx nx test upcoming --skip-nx-cache`
Expected: FAIL in `email-abo-modal.component.spec.ts` (message is still "Wir haben dir eine Bestätigung geschickt. …").

**Step 3: Implement.** In the component, replace

```ts
    await this.showAlert(
      'Dein Abo ist aktiv',
      'Wir haben dir eine Bestätigung geschickt. Jede Mail enthält einen Link zum Abbestellen.',
    );
```

with

```ts
    // One text for every outcome, so the answer never reveals whether this address must confirm first.
    await this.showAlert('Danke!', 'Wir haben dir eine E-Mail geschickt.');
```

**Step 4: Run the test to verify it passes.**

Run: `cd packages/frontend && npx nx test upcoming --skip-nx-cache`
Expected: all upcoming tests PASS.

**Step 5: Commit.**

```bash
git add packages/frontend/apps/upcoming/src/app/components/email-abo-modal
git commit -m "feat(frontend): answer every report subscription with one neutral message" -m "Co-Authored-By: Claude Opus 5 (1M context) <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01WPAhT2z3eeQCiwtpacyGTU"
```

---

### Task 8: Definition of Done

**Step 1: Run the gate.**

Run: `./gradlew lint test --continue`
Expected: BUILD SUCCESSFUL, exit 0.

**Step 2: Restore lint side effects.** Run `git status --short`. If only files outside this plan changed (the `app-web` files `prettier --write` touched, or `packages/frontend/package-lock.json` with internal-registry URLs), restore them with `git restore -- <those paths>`. Anything else unexpected: stop and report it.

**Step 3: Mark the spec delivered.** In `docs/superpowers/specs/2026-09-14-report-abuse-per-address-design.md`, change `Status: awaiting review` to `Status: implemented`, then commit:

```bash
git add docs/superpowers/specs/2026-09-14-report-abuse-per-address-design.md
git commit -m "docs: mark the report abuse design implemented" -m "Co-Authored-By: Claude Opus 5 (1M context) <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01WPAhT2z3eeQCiwtpacyGTU"
```

**Step 4: Stop before pushing.** Report the gate result and the commit list; pushing to PR #83 needs the user's go.
