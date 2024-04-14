package org.migor.feedless.plan

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.models.FeatureName
import org.migor.feedless.data.jpa.models.FeatureValueEntity
import org.migor.feedless.data.jpa.repositories.FeatureValueDAO
import org.migor.feedless.session.SessionService
import org.migor.feedless.source.any
import org.migor.feedless.source.eq
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserEntity
import org.migor.feedless.util.toDate
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlanConstraintsServiceImplTest {

  @Mock
  lateinit var featureDAO: FeatureValueDAO

  @Mock
  lateinit var userDAO: UserDAO

  @Mock
  lateinit var sessionService: SessionService

  @InjectMocks
  lateinit var service: PlanConstraintsServiceImpl

  private lateinit var userId: UUID
  private lateinit var user: UserEntity
  private var corrId = "test"

  @BeforeEach
  fun beforeEach() {
    userId = UUID.randomUUID()
    user = mock(UserEntity::class.java)
    val planId = UUID.randomUUID()
    `when`(user.id).thenReturn(userId)
    `when`(user.planId).thenReturn(planId)
    `when`(userDAO.findById(any(UUID::class.java))).thenReturn(Optional.of(user))
    `when`(sessionService.userId()).thenReturn(UUID.randomUUID())
  }

  @Test
  fun `give maxItems is defined when coerceRetentionMaxItems works`() {
    val maxItems = 50
    mockFeatureValue(FeatureName.scrapeSourceRetentionMaxItemsInt, intValue = maxItems)
    assertThat(service.coerceRetentionMaxItems(null, userId)).isNull()
    assertThat(service.coerceRetentionMaxItems(56, userId)).isEqualTo(maxItems)
    assertThat(service.coerceRetentionMaxItems(1, userId)).isEqualTo(2)
  }

  @Test
  fun `give maxItems is undefined when coerceRetentionMaxItems works`() {
    mockFeatureValue(FeatureName.scrapeSourceRetentionMaxItemsInt, intValue = null)
    assertThat(service.coerceRetentionMaxItems(null, userId)).isNull()
    assertThat(service.coerceRetentionMaxItems(56, userId)).isEqualTo(56)
    assertThat(service.coerceRetentionMaxItems(1, userId)).isEqualTo(2)
  }

  @Test
  fun `given an invalid cron string, auditRefreshCron will fail`() {
    Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      service.auditRefreshCron("")
    }
  }

  @Test
  fun `given a valid cron string, auditRefreshCron will pass`() {
    service.auditRefreshCron("* * * * * 2")
  }

  @Test
  fun `given publicScrapeSourceBool is true, when coerceVisibility works`() {
    mockFeatureValue(FeatureName.publicScrapeSourceBool, boolValue = true)
    // fallback
    assertThat(service.coerceVisibility(null)).isEqualTo(EntityVisibility.isPrivate)
    // isPrivate
    assertThat(service.coerceVisibility(EntityVisibility.isPrivate)).isEqualTo(EntityVisibility.isPrivate)
    // isPublic
    assertThat(service.coerceVisibility(EntityVisibility.isPublic)).isEqualTo(EntityVisibility.isPublic)
  }

  @Test
  fun `given publicScrapeSourceBool is false, when coerceVisibility works`() {
    mockFeatureValue(FeatureName.publicScrapeSourceBool, boolValue = false)

    // fallback
    assertThat(service.coerceVisibility(null)).isEqualTo(EntityVisibility.isPrivate)
    // isPrivate
    assertThat(service.coerceVisibility(EntityVisibility.isPrivate)).isEqualTo(EntityVisibility.isPrivate)
    // isPublic
    assertThat(service.coerceVisibility(EntityVisibility.isPublic)).isEqualTo(EntityVisibility.isPrivate)
  }

  @Test
  fun `given invalid refreshRate when coerceMinScheduledNextAt returns minimum rate`() {
    mockFeatureValue(FeatureName.minRefreshRateInMinutesInt, intValue = 4)
    val now = LocalDateTime.now()
    val minNext = toDate(now.plusMinutes(4))

    assertThat(
      service.coerceMinScheduledNextAt(
        toDate(now.minusDays(2)),
        userId
      )
    ).isAfterOrEqualTo(minNext)
  }

  @Test
  fun `given valid refreshRate when coerceMinScheduledNextAt returns refreshRate`() {
    mockFeatureValue(FeatureName.minRefreshRateInMinutesInt, intValue = 4)
    val now = LocalDateTime.now()
    val future = toDate(now.plusDays(2))

    assertThat(
      service.coerceMinScheduledNextAt(
        future,
        userId
      )
    ).isAfterOrEqualTo(future)
  }

  @Test
  fun `given maxAge is undefined, RetentionMaxAgeDays is undefined`() {
    assertThat(service.coerceRetentionMaxAgeDays(null)).isEqualTo(null)
  }

  @Test
  fun `given maxAge is defined, RetentionMaxAgeDays is at least 2`() {
    assertThat(service.coerceRetentionMaxAgeDays(-3)).isEqualTo(2)
    assertThat(service.coerceRetentionMaxAgeDays(0)).isEqualTo(2)
    assertThat(service.coerceRetentionMaxAgeDays(12)).isEqualTo(12)
  }

  @Test
  fun `given scrapeRequestActionMaxCountInt is undefined, all actionsCount will pass`() {
    mockFeatureValue(FeatureName.scrapeRequestActionMaxCountInt, intValue = null)
    service.auditScrapeRequestMaxActions(null, userId)
    service.auditScrapeRequestMaxActions(10000, userId)
  }

  @Test
  fun `given scrapeRequestActionMaxCountInt is defined, violating actionsCounts will fail`() {
    mockFeatureValue(FeatureName.scrapeRequestActionMaxCountInt, intValue = 4)
    Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      service.auditScrapeRequestMaxActions(12, userId)
    }
  }

  @Test

  fun `given scrapeRequestActionMaxCountInt is defined, valid actionsCounts will pass`() {
    mockFeatureValue(FeatureName.scrapeRequestActionMaxCountInt, intValue = 4)
    service.auditScrapeRequestMaxActions(4, userId)
    service.auditScrapeRequestMaxActions(0, userId)
  }

  @Test
  fun `given scrapeRequestTimeoutInt is defined, violating actionsCounts will fail`() {
    mockFeatureValue(FeatureName.scrapeRequestTimeoutInt, intValue = 4)
    Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      service.auditScrapeRequestTimeout(12, userId)
    }
  }

  @Test

  fun `given scrapeRequestTimeoutInt is defined, valid actionsCounts will pass`() {
    mockFeatureValue(FeatureName.scrapeRequestTimeoutInt, intValue = 4)
    service.auditScrapeRequestTimeout(4, userId)
    service.auditScrapeRequestTimeout(0, userId)
  }

  @Test
  fun `given user is anonymous, scrapeSourceExpiry will be assigned`() {
    mockFeatureValue(FeatureName.scrapeSourceExpiryInDaysInt, intValue = 4)
    `when`(user.anonymous).thenReturn(true)

    val future = toDate(LocalDateTime.now().plusDays(4))
    assertThat(
      service.coerceScrapeSourceExpiry(
        corrId,
        userId
      )
    ).isAfterOrEqualTo(future)

  }

  @Test
  fun `given user is not anonymous, scrapeSourceExpiry won't be set`() {
    mockFeatureValue(FeatureName.scrapeSourceExpiryInDaysInt, intValue = 4)
    `when`(user.anonymous).thenReturn(false)

    assertThat(
      service.coerceScrapeSourceExpiry(
        corrId,
        userId
      )
    ).isNull()
  }

  @Test
  fun `given scrapeSourceMaxCountTotalInt is undefined, all sourceCounts will pass`() {
    mockFeatureValue(FeatureName.scrapeSourceMaxCountTotalInt, intValue = null)
    service.auditScrapeSourceMaxCount(0, userId)
    service.auditScrapeSourceMaxCount(10000, userId)
  }

  @Test
  fun `given scrapeSourceMaxCountTotalInt is defined, violating sourceCounts will fail`() {
    mockFeatureValue(FeatureName.scrapeSourceMaxCountTotalInt, intValue = 4)
    Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      service.auditScrapeSourceMaxCount(12, userId)
    }
    service.auditScrapeSourceMaxCount(2, userId)
    service.auditScrapeSourceMaxCount(4, userId)
  }


  @Test
  fun violatesScrapeSourceMaxActiveCount() {
    // todo
  }

  @Test
  fun `given scrapeRequestMaxCountPerSourceInt is undefined, all counts will pass`() {
    mockFeatureValue(FeatureName.scrapeRequestMaxCountPerSourceInt, intValue = null)
    service.auditScrapeRequestMaxCountPerSource(0, userId)
    service.auditScrapeRequestMaxCountPerSource(10000, userId)
  }

  @Test
  fun `given scrapeRequestMaxCountPerSourceInt is defined, violating counts will fail`() {
    mockFeatureValue(FeatureName.scrapeRequestMaxCountPerSourceInt, intValue = 4)
    Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      service.auditScrapeRequestMaxCountPerSource(12, userId)
    }
    service.auditScrapeRequestMaxCountPerSource(2, userId)
    service.auditScrapeRequestMaxCountPerSource(4, userId)
  }

  private fun mockFeatureValue(featureName: FeatureName, boolValue: Boolean? = null, intValue: Int? = null) {
    val feature = mock(FeatureValueEntity::class.java)
    `when`(feature.valueBoolean).thenReturn(boolValue)
    `when`(feature.valueInt).thenReturn(intValue)
    `when`(
      featureDAO.findByPlanIdAndName(
        any(String::class.java),
        eq(featureName.name),
      )
    ).thenReturn(
      listOf(feature)
    )
  }

}
