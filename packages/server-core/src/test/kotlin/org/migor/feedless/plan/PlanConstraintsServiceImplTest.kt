package org.migor.feedless.plan

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.migor.feedless.data.jpa.enums.fromDto
import org.migor.feedless.document.any
import org.migor.feedless.document.anyList
import org.migor.feedless.document.eq
import org.migor.feedless.session.SessionService
import org.migor.feedless.subscription.PlanDAO
import org.migor.feedless.subscription.PlanEntity
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
  lateinit var sessionService: SessionService

  @Mock
  lateinit var featureGroupDAO: FeatureGroupDAO

  @Mock
  lateinit var planDAO: PlanDAO

  @Mock
  lateinit var environment: Environment

  @InjectMocks
  lateinit var service: PlanConstraintsService

  private lateinit var userId: UUID
  private lateinit var user: UserEntity
  private var corrId = "test"
  private val product = ProductCategory.feedless

  @BeforeEach
  fun beforeEach() = runTest {
    userId = UUID.randomUUID()
    user = mock(UserEntity::class.java)
    `when`(user.id).thenReturn(userId)
    `when`(userDAO.findById(any(UUID::class.java))).thenReturn(Optional.of(user))
    `when`(sessionService.activeProductFromRequest()).thenReturn(org.migor.feedless.generated.types.ProductCategory.rssProxy.fromDto())
    `when`(sessionService.userId()).thenReturn(UUID.randomUUID())

    val system = mock(FeatureGroupEntity::class.java)
    `when`(system.id).thenReturn(UUID.randomUUID())
    `when`(system.name).thenReturn("")
    `when`(featureGroupDAO.findByParentFeatureGroupIdIsNull()).thenReturn(system)


    val mockProduct = mock(ProductEntity::class.java)
    `when`(mockProduct.featureGroupId).thenReturn(UUID.randomUUID())

    val mockPlan = mock(PlanEntity::class.java)
    `when`(mockPlan.product).thenReturn(mockProduct)
    `when`(planDAO.findActiveByUserAndProductIn(any(UUID::class.java), anyList())).thenReturn(mockPlan)
  }

  @Test
  fun `give maxItems is defined when coerceRetentionMaxItems works`() = runTest {
    val maxItems = 50L
    mockFeatureValue(FeatureName.repositoryCapacityUpperLimitInt, intValue = maxItems)
    mockFeatureValue(FeatureName.repositoryCapacityLowerLimitInt, intValue = 2)
    assertThat(service.coerceRetentionMaxCapacity(null, userId, product)).isEqualTo(maxItems)
    assertThat(service.coerceRetentionMaxCapacity(56, userId, product)).isEqualTo(maxItems)
    assertThat(service.coerceRetentionMaxCapacity(1, userId, product)).isEqualTo(2)
  }

  @Test
  fun `give maxItems is undefined when coerceRetentionMaxItems works`() = runTest {
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
  fun `given publicScrapeSourceBool is true, when coerceVisibility works`() = runTest {
    mockFeatureValue(FeatureName.publicRepositoryBool, boolValue = true)
    // fallback
    assertThat(service.coerceVisibility(corrId, null)).isEqualTo(EntityVisibility.isPrivate)
    // isPrivate
    assertThat(service.coerceVisibility(corrId, EntityVisibility.isPrivate)).isEqualTo(EntityVisibility.isPrivate)
    // isPublic
    assertThat(service.coerceVisibility(corrId, EntityVisibility.isPublic)).isEqualTo(EntityVisibility.isPublic)
  }

  @Test
  fun `given publicScrapeSourceBool is false, when coerceVisibility works`() = runTest {
    mockFeatureValue(FeatureName.publicRepositoryBool, boolValue = false)

    // fallback
    assertThat(service.coerceVisibility(corrId, null)).isEqualTo(EntityVisibility.isPrivate)
    // isPrivate
    assertThat(service.coerceVisibility(corrId, EntityVisibility.isPrivate)).isEqualTo(EntityVisibility.isPrivate)
    // isPublic
    assertThat(service.coerceVisibility(corrId, EntityVisibility.isPublic)).isEqualTo(EntityVisibility.isPrivate)
  }

  @Test
  fun `given invalid refreshRate when coerceMinScheduledNextAt returns minimum rate`() = runTest {
    mockFeatureValue(FeatureName.refreshRateInMinutesLowerLimitInt, intValue = 4)
    val now = LocalDateTime.now()
    val minNext = toDate(now.plusMinutes(4))

    assertThat(
      service.coerceMinScheduledNextAt(
        Date(),
        toDate(now.minusDays(2)),
        userId,
        product
      )
    ).isAfterOrEqualTo(minNext)
  }

  @Test
  fun `given valid refreshRate when coerceMinScheduledNextAt returns refreshRate`() = runTest {
    mockFeatureValue(FeatureName.refreshRateInMinutesLowerLimitInt, intValue = 4)
    val now = LocalDateTime.now()
    val future = toDate(now.plusDays(2))

    assertThat(
      service.coerceMinScheduledNextAt(
        Date(),
        future,
        userId,
        product
      )
    ).isAfterOrEqualTo(future)
  }

  @Test
  fun `given maxAge is undefined, RetentionMaxAgeDays is undefined`() = runTest {
    assertThat(service.coerceRetentionMaxAgeDays(null, userId, product)).isEqualTo(null)
  }

  @Test
  fun `given maxAge is defined, RetentionMaxAgeDays is at least 2`() = runTest {
    mockFeatureValue(FeatureName.repositoryRetentionMaxDaysLowerLimitInt, intValue = 2)
    assertThat(service.coerceRetentionMaxAgeDays(-3, userId, product)).isEqualTo(2)
    assertThat(service.coerceRetentionMaxAgeDays(0, userId, product)).isEqualTo(2)
    assertThat(service.coerceRetentionMaxAgeDays(12, userId, product)).isEqualTo(12)
  }

  @Test
  fun `given scrapeRequestActionMaxCountInt is undefined, all actionsCount will pass`() = runTest {
    mockFeatureValue(FeatureName.scrapeRequestActionMaxCountInt, intValue = null)
    service.auditScrapeRequestMaxActions(null, userId)
    service.auditScrapeRequestMaxActions(10000, userId)
  }

  @Test
  fun `given scrapeRequestActionMaxCountInt is defined, violating actionsCounts will fail`() = runTest {
    mockFeatureValue(FeatureName.scrapeRequestActionMaxCountInt, intValue = 4)
    Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runBlocking {
        service.auditScrapeRequestMaxActions(12, userId)
      }
    }
  }

  @Test

  fun `given scrapeRequestActionMaxCountInt is defined, valid actionsCounts will pass`() = runTest {
    mockFeatureValue(FeatureName.scrapeRequestActionMaxCountInt, intValue = 4)
    service.auditScrapeRequestMaxActions(4, userId)
    service.auditScrapeRequestMaxActions(0, userId)
  }

  @Test
  fun `given scrapeRequestTimeoutInt is defined, violating actionsCounts will fail`() = runTest {
    mockFeatureValue(FeatureName.scrapeRequestTimeoutMsecInt, intValue = 4)
    Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runBlocking {
        service.auditScrapeRequestTimeout(12, userId)
      }
    }
  }

  @Test

  fun `given scrapeRequestTimeoutInt is defined, valid actionsCounts will pass`() = runTest {
    mockFeatureValue(FeatureName.scrapeRequestTimeoutMsecInt, intValue = 4)
    service.auditScrapeRequestTimeout(4, userId)
    service.auditScrapeRequestTimeout(0, userId)
  }

  @Test
  fun `given scrapeSourceMaxCountTotalInt is undefined, all sourceCounts will pass`() = runTest {
    mockFeatureValue(FeatureName.scrapeSourceMaxCountTotalInt, intValue = null)
    service.auditScrapeSourceMaxCount(0, userId)
    service.auditScrapeSourceMaxCount(10000, userId)
  }

  @Test
  fun `given scrapeSourceMaxCountTotalInt is defined, violating sourceCounts will fail`() = runTest {
    mockFeatureValue(FeatureName.scrapeSourceMaxCountTotalInt, intValue = 4)
    Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runBlocking {
        service.auditScrapeSourceMaxCount(12, userId)
      }
    }
    service.auditScrapeSourceMaxCount(2, userId)
    service.auditScrapeSourceMaxCount(4, userId)
  }


  @Test
  fun violatesScrapeSourceMaxActiveCount() {
    // todo
  }

  @Test
  fun `given scrapeRequestMaxCountPerSourceInt is undefined, all counts will pass`() = runTest {
    mockFeatureValue(FeatureName.sourceMaxCountPerRepositoryInt, intValue = null)
    service.auditSourcesMaxCountPerRepository(0, userId, product)
    service.auditSourcesMaxCountPerRepository(10000, userId, product)
  }

  @Test
  fun `given scrapeRequestMaxCountPerSourceInt is defined, violating counts will fail`() = runTest {
    mockFeatureValue(FeatureName.sourceMaxCountPerRepositoryInt, intValue = 4)
    Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      runBlocking {
        service.auditSourcesMaxCountPerRepository(12, userId, product)
      }
    }
    service.auditSourcesMaxCountPerRepository(2, userId, product)
    service.auditSourcesMaxCountPerRepository(4, userId, product)
  }

  private fun mockFeatureValue(featureName: FeatureName, boolValue: Boolean? = null, intValue: Long? = null) {
    val feature = mock(FeatureValueEntity::class.java)
    `when`(feature.valueBoolean).thenReturn(boolValue)
    `when`(feature.valueInt).thenReturn(intValue)
    `when`(
      featureValueDAO.resolveByFeatureGroupIdAndName(
        any(UUID::class.java),
        eq(featureName.name),
      )
    ).thenReturn(
      feature
    )
  }

}
