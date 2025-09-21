package org.migor.feedless.plan

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.Mother.randomUserId
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.enums.Vertical
import org.migor.feedless.feature.FeatureGroupDAO
import org.migor.feedless.feature.FeatureGroupEntity
import org.migor.feedless.feature.FeatureName
import org.migor.feedless.feature.FeatureValueDAO
import org.migor.feedless.feature.FeatureValueEntity
import org.migor.feedless.repository.any2
import org.migor.feedless.repository.anyList
import org.migor.feedless.repository.eq
import org.migor.feedless.session.RequestContext
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserEntity
import org.migor.feedless.user.UserId
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.core.env.Environment
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlanConstraintsServiceImplTest {

  @Mock
  lateinit var featureValueDAO: FeatureValueDAO

  @Mock
  lateinit var userDAO: UserDAO

  @Mock
  lateinit var featureGroupDAO: FeatureGroupDAO

  @Mock
  lateinit var planDAO: PlanDAO

  @Mock
  lateinit var environment: Environment

  @InjectMocks
  lateinit var service: PlanConstraintsService

  private lateinit var userId: UserId
  private val currentUserId = randomUserId()
  private lateinit var user: UserEntity
  private val product = Vertical.feedless

  @BeforeEach
  fun beforeEach() = runTest {
    userId = randomUserId()
    user = mock(UserEntity::class.java)
    `when`(user.id).thenReturn(userId.value)
    `when`(userDAO.findById(any2())).thenReturn(Optional.of(user))

    val system = mock(FeatureGroupEntity::class.java)
    `when`(system.id).thenReturn(UUID.randomUUID())
    `when`(system.name).thenReturn("")
    `when`(featureGroupDAO.findByParentFeatureGroupIdIsNull()).thenReturn(system)


    val mockProduct = mock(ProductEntity::class.java)
    `when`(mockProduct.featureGroupId).thenReturn(UUID.randomUUID())

    val mockPlan = mock(PlanEntity::class.java)
    `when`(mockPlan.product).thenReturn(mockProduct)
    `when`(
      planDAO.findActiveByUserAndProductIn(
        any2(),
        anyList(),
        any2()
      )
    ).thenReturn(mockPlan)
  }

  @Test
  fun `give maxItems is defined when coerceRetentionMaxItems works`() =
    runTest(context = RequestContext(userId = currentUserId, product = Vertical.feedless)) {
      val maxItems = 50L
      mockFeatureValue(FeatureName.repositoryCapacityUpperLimitInt, intValue = maxItems)
      mockFeatureValue(FeatureName.repositoryCapacityLowerLimitInt, intValue = 2)
      assertThat(service.coerceRetentionMaxCapacity(null, userId, product)).isEqualTo(maxItems)
      assertThat(service.coerceRetentionMaxCapacity(56, userId, product)).isEqualTo(maxItems)
      assertThat(service.coerceRetentionMaxCapacity(1, userId, product)).isEqualTo(2)
    }

  @Test
  fun `give maxItems is undefined when coerceRetentionMaxItems works`() =
    runTest(context = RequestContext(userId = currentUserId, product = Vertical.feedless)) {
      mockFeatureValue(FeatureName.repositoryCapacityUpperLimitInt, intValue = null)
      mockFeatureValue(FeatureName.repositoryCapacityLowerLimitInt, intValue = 2)
      assertThat(service.coerceRetentionMaxCapacity(null, userId, product)).isNull()
      assertThat(service.coerceRetentionMaxCapacity(56, userId, product)).isEqualTo(56)
      assertThat(service.coerceRetentionMaxCapacity(1, userId, product)).isEqualTo(2)
    }

  @Test
  fun `given an invalid cron string, auditRefreshCron will fail`() {
    Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      service.auditCronExpression("")
    }
  }

  @Test
  fun `given a valid cron string, auditRefreshCron will pass`() {
    service.auditCronExpression("* * * * * 2")
  }

  @Test
  fun `given publicScrapeSourceBool is true, when coerceVisibility works`() =
    runTest(context = RequestContext(userId = currentUserId, product = Vertical.feedless)) {
      mockFeatureValue(FeatureName.publicRepositoryBool, boolValue = true)
      // fallback
      assertThat(service.coerceVisibility(null)).isEqualTo(EntityVisibility.isPrivate)
      // isPrivate
      assertThat(service.coerceVisibility(EntityVisibility.isPrivate)).isEqualTo(EntityVisibility.isPrivate)
      // isPublic
      assertThat(service.coerceVisibility(EntityVisibility.isPublic)).isEqualTo(EntityVisibility.isPublic)
    }

  @Test
  fun `given publicScrapeSourceBool is false, when coerceVisibility works`() =
    runTest(context = RequestContext(userId = currentUserId, product = Vertical.feedless)) {
      mockFeatureValue(FeatureName.publicRepositoryBool, boolValue = false)

      // fallback
      assertThat(service.coerceVisibility(null)).isEqualTo(EntityVisibility.isPrivate)
      // isPrivate
      assertThat(service.coerceVisibility(EntityVisibility.isPrivate)).isEqualTo(EntityVisibility.isPrivate)
      // isPublic
      assertThat(service.coerceVisibility(EntityVisibility.isPublic)).isEqualTo(EntityVisibility.isPrivate)
    }

  @Test
  fun `given invalid refreshRate when coerceMinScheduledNextAt returns minimum rate`() =
    runTest(context = RequestContext(userId = currentUserId, product = Vertical.feedless)) {
      mockFeatureValue(FeatureName.refreshRateInMinutesLowerLimitInt, intValue = 4)
      val now = LocalDateTime.now()
      val minNext = now.plusMinutes(4)

      assertThat(
        service.coerceMinScheduledNextAt(
          LocalDateTime.now(),
          now.minusDays(2),
          userId,
          product
        )
      ).isAfterOrEqualTo(minNext)
    }

  @Test
  fun `given valid refreshRate when coerceMinScheduledNextAt returns refreshRate`() =
    runTest(context = RequestContext(userId = currentUserId, product = Vertical.feedless)) {
      mockFeatureValue(FeatureName.refreshRateInMinutesLowerLimitInt, intValue = 4)
      val now = LocalDateTime.now()
      val future = now.plusDays(2)

      assertThat(
        service.coerceMinScheduledNextAt(
          LocalDateTime.now(),
          future,
          userId,
          product
        )
      ).isAfterOrEqualTo(future)
    }

  @Test
  fun `given maxAge is undefined, RetentionMaxAgeDays is undefined`() =
    runTest(context = RequestContext(userId = currentUserId, product = Vertical.feedless)) {
      assertThat(service.coerceRetentionMaxAgeDays(null, userId, product)).isEqualTo(null)
    }

  @Test
  fun `given maxAge is defined, RetentionMaxAgeDays is at least 2`() =
    runTest(context = RequestContext(userId = currentUserId, product = Vertical.feedless)) {
      mockFeatureValue(FeatureName.repositoryRetentionMaxDaysLowerLimitInt, intValue = 2)
      assertThat(service.coerceRetentionMaxAgeDays(-3, userId, product)).isEqualTo(2)
      assertThat(service.coerceRetentionMaxAgeDays(0, userId, product)).isEqualTo(2)
      assertThat(service.coerceRetentionMaxAgeDays(12, userId, product)).isEqualTo(12)
    }

  @Test
  fun `given scrapeRequestActionMaxCountInt is undefined, all actionsCount will pass`() =
    runTest(context = RequestContext(userId = currentUserId, product = Vertical.feedless)) {
      mockFeatureValue(FeatureName.scrapeRequestActionMaxCountInt, intValue = null)
      service.auditScrapeRequestMaxActions(null, userId)
      service.auditScrapeRequestMaxActions(10000, userId)
    }

  @Test
  fun `given scrapeRequestActionMaxCountInt is defined, violating actionsCounts will fail`() {
    mockFeatureValue(FeatureName.scrapeRequestActionMaxCountInt, intValue = 4)
    Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runTest(context = RequestContext(userId = currentUserId, product = Vertical.feedless)) {
        service.auditScrapeRequestMaxActions(12, userId)
      }
    }
  }

  @Test
  fun `given scrapeRequestActionMaxCountInt is defined, valid actionsCounts will pass`() =
    runTest(context = RequestContext(userId = currentUserId, product = Vertical.feedless)) {
      mockFeatureValue(FeatureName.scrapeRequestActionMaxCountInt, intValue = 4)
      service.auditScrapeRequestMaxActions(4, userId)
      service.auditScrapeRequestMaxActions(0, userId)
    }

  @Test
  fun `given scrapeRequestTimeoutInt is defined, violating actionsCounts will fail`() {
    mockFeatureValue(FeatureName.scrapeRequestTimeoutMsecInt, intValue = 4)
    Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runTest(context = RequestContext(userId = currentUserId, product = Vertical.feedless)) {
        service.auditScrapeRequestTimeout(12, userId)
      }
    }
  }

  @Test

  fun `given scrapeRequestTimeoutInt is defined, valid actionsCounts will pass`() =
    runTest(context = RequestContext(userId = currentUserId, product = Vertical.feedless)) {
      mockFeatureValue(FeatureName.scrapeRequestTimeoutMsecInt, intValue = 4)
      service.auditScrapeRequestTimeout(4, userId)
      service.auditScrapeRequestTimeout(0, userId)
    }

  @Test
  fun `given scrapeSourceMaxCountTotalInt is undefined, all sourceCounts will pass`() =
    runTest(context = RequestContext(userId = currentUserId, product = Vertical.feedless)) {
      mockFeatureValue(FeatureName.repositoriesMaxCountTotalInt, intValue = null)
      service.auditRepositoryMaxCount(0, userId)
      service.auditRepositoryMaxCount(10000, userId)
    }

  @Test
  fun `given scrapeSourceMaxCountTotalInt is defined, violating sourceCounts will fail`() {
    mockFeatureValue(FeatureName.repositoriesMaxCountTotalInt, intValue = 4)
    Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runTest(context = RequestContext(userId = currentUserId, product = Vertical.feedless)) {
        service.auditRepositoryMaxCount(12, userId)
      }
    }
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "2",
      "4"
    ]
  )
  fun `given scrapeSourceMaxCountTotalInt is defined, valid sourceCounts will pass`(sourceCount: Int) =
    runTest(context = RequestContext(userId = currentUserId, product = Vertical.feedless)) {
      mockFeatureValue(FeatureName.repositoriesMaxCountTotalInt, intValue = 4)
      service.auditRepositoryMaxCount(sourceCount, userId)
    }

  @Test
  @Disabled
  fun violatesRepositoriesMaxActiveCount() {
    // todo
  }

  @Test
  fun `given scrapeRequestMaxCountPerSourceInt is undefined, all counts will pass`() =
    runTest(context = RequestContext(userId = currentUserId, product = Vertical.feedless)) {
      mockFeatureValue(FeatureName.sourceMaxCountPerRepositoryInt, intValue = null)
      service.auditSourcesMaxCountPerRepository(0, userId, product)
      service.auditSourcesMaxCountPerRepository(10000, userId, product)
    }

  @Test
  fun `given scrapeRequestMaxCountPerSourceInt is defined, violating counts will fail`() {
    mockFeatureValue(FeatureName.sourceMaxCountPerRepositoryInt, intValue = 4)
    Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runTest(context = RequestContext(userId = currentUserId, product = Vertical.feedless)) {
        service.auditSourcesMaxCountPerRepository(12, userId, product)
      }
    }
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "2",
      "4"
    ]
  )
  fun `given scrapeRequestMaxCountPerSourceInt is defined, valid counts will pass`(count: Int) = runTest {
    mockFeatureValue(FeatureName.sourceMaxCountPerRepositoryInt, intValue = 4)
    service.auditSourcesMaxCountPerRepository(count, userId, product)
  }

  private fun mockFeatureValue(featureName: FeatureName, boolValue: Boolean? = null, intValue: Long? = null) {
    val feature = mock(FeatureValueEntity::class.java)
    `when`(feature.valueBoolean).thenReturn(boolValue)
    `when`(feature.valueInt).thenReturn(intValue)
    `when`(
      featureValueDAO.resolveByFeatureGroupIdAndName(
        any2(),
        eq(featureName.name),
      )
    ).thenReturn(
      feature
    )
  }

}
