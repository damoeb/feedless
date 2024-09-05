package org.migor.feedless.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.actions.ScrapeActionDAO
import org.migor.feedless.common.PropertyService
import org.migor.feedless.document.DocumentService
import org.migor.feedless.mail.MailForwardDAO
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.session.SessionService
import org.migor.feedless.source.SourceDAO
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserService
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.scheduling.support.CronExpression
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RepositoryServiceTest {

  @Mock
  private lateinit var sourceDAO: SourceDAO

  @Mock
  private lateinit var mailForwardDAO: MailForwardDAO

  @Mock
  private lateinit var userDAO: UserDAO

  @Mock
  private lateinit var repositoryDAO: RepositoryDAO

  @Mock
  private lateinit var sessionService: SessionService

  @Mock
  private lateinit var userService: UserService

  @Mock
  private lateinit var planConstraintsService: PlanConstraintsService

  @Mock
  private lateinit var documentService: DocumentService

  @Mock
  private lateinit var propertyService: PropertyService

  @Mock
  private lateinit var scrapeActionDAO: ScrapeActionDAO

  @InjectMocks
  lateinit var repositoryService: RepositoryService

  @BeforeEach
  fun setUp() {
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "0 */10 * * * *, 10, MINUTES",
      "0 */15 * * * *, 15, MINUTES",
      "0 */30 * * * *, 30, MINUTES",
      "0 0 * * * *, 1, HOURS",
      "0 0 */6 * * *, 6, HOURS",
      "0 0 */12 * * *, 12, HOURS",
      "0 0 0 * * *, 1, DAYS",
      "0 0 0 * * 0, 1, WEEKS"
    ]
  )
  fun calculateScheduledNextAt(cron: String, increment: Double, unit: String) {
    val chronoUnit = ChronoUnit.valueOf(unit)
    assertThat(chronoUnit.name).isEqualTo(unit)

    val now = LocalDateTime.now()

    val from = nextCronDate(cron, now)
    val to = nextCronDate(cron, from)
    val diff = Duration.between(from, to)
    val diffInUnit = when(chronoUnit) {
      ChronoUnit.MINUTES -> diff.toMinutes().toDouble()
      ChronoUnit.HOURS -> diff.toHours().toDouble()
      ChronoUnit.DAYS -> diff.toDays().toDouble()
      ChronoUnit.WEEKS -> diff.toDays() / 7.0
      else -> IllegalArgumentException()
    }
    assertThat(diffInUnit).isEqualTo(increment)
  }
}
