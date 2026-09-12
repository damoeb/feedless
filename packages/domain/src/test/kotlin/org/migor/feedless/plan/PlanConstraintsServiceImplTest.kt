package org.migor.feedless.plan

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.TemporalUnitWithinOffset
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.EntityVisibility
import org.migor.feedless.any
import org.migor.feedless.any2
import org.migor.feedless.eq
import org.migor.feedless.feature.FeatureGroup
import org.migor.feedless.feature.FeatureGroupId
import org.migor.feedless.feature.FeatureGroupRepository
import org.migor.feedless.feature.FeatureName
import org.migor.feedless.feature.FeatureValue
import org.migor.feedless.feature.FeatureValueRepository
import org.migor.feedless.group.GroupId
import org.migor.feedless.product.ProductRepository
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.core.env.Environment
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlanConstraintsServiceImplTest {

  @Mock
  lateinit var featureValueRepository: FeatureValueRepository

  @Mock
  lateinit var userRepository: UserRepository

  @Mock
  lateinit var featureGroupRepository: FeatureGroupRepository

  @Mock
  lateinit var planRepository: PlanRepository

  @Mock
  lateinit var productRepository: ProductRepository

  @Mock
  lateinit var environment: Environment

  lateinit var service: PlanConstraintsService

  private lateinit var userId: UserId
  private lateinit var groupId: GroupId
  private lateinit var user: User

  @BeforeEach
  fun beforeEach() = runTest {
    userId = UserId()
    groupId = GroupId()

    service = PlanConstraintsService(
      planRepository,
      environment,
      mock(RepositoryRepository::class.java),
      featureValueRepository,
      productRepository,
      featureGroupRepository
    )

    user = mock(User::class.java)
    `when`(user.id).thenReturn(userId)
    `when`(userRepository.findById(any2())).thenReturn(user)

    val system = mock(FeatureGroup::class.java)
    `when`(system.id).thenReturn(FeatureGroupId())
    `when`(system.name).thenReturn("")
    `when`(featureGroupRepository.findByParentFeatureGroupIdIsNull()).thenReturn(system)

    `when`(featureGroupRepository.findByGroupId(groupId)).thenReturn(system)


//    val mockProduct = mock(Product::class.java)
//    `when`(mockProduct.featureGroupId).thenReturn(FeatureGroupId())

//    val mockPlan = mock(Plan::class.java)
//    `when`(mockPlan.product(any2())).thenAnswer { ff -> mockProduct }
//    `when`(
//      planRepository.findActiveByUserAndProductIn(
//        any2(),
//        anyList(),
//        any2()
//      )
//    ).thenReturn(mockPlan)
  }

  @Test
  fun `give maxItems is defined when coerceRetentionMaxItems works`() {
    val maxItems = 50L
    mockFeatureValue(FeatureName.repositoryCapacityUpperLimitInt, intValue = maxItems)
    mockFeatureValue(FeatureName.repositoryCapacityLowerLimitInt, intValue = 2)
    assertThat(service.coerceRetentionMaxCapacity(null, groupId)).isEqualTo(maxItems)
    assertThat(service.coerceRetentionMaxCapacity(56, groupId)).isEqualTo(maxItems)
    assertThat(service.coerceRetentionMaxCapacity(1, groupId)).isEqualTo(2)
  }

  @Test
  fun `give maxItems is undefined when coerceRetentionMaxItems works`() {
    mockFeatureValue(FeatureName.repositoryCapacityUpperLimitInt, intValue = null)
    mockFeatureValue(FeatureName.repositoryCapacityLowerLimitInt, intValue = 2)
    assertThat(service.coerceRetentionMaxCapacity(null, groupId)).isNull()
    assertThat(service.coerceRetentionMaxCapacity(56, groupId)).isEqualTo(56)
    assertThat(service.coerceRetentionMaxCapacity(1, groupId)).isEqualTo(2)
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
  fun `given publicScrapeSourceBool is true, when coerceVisibility works`() {
    mockFeatureValue(FeatureName.publicRepositoryBool, boolValue = true)
    // fallback
    assertThat(service.coerceVisibility(groupId, null)).isEqualTo(EntityVisibility.isPrivate)
    // isPrivate
    assertThat(service.coerceVisibility(groupId, EntityVisibility.isPrivate)).isEqualTo(EntityVisibility.isPrivate)
    // isPublic
    assertThat(service.coerceVisibility(groupId, EntityVisibility.isPublic)).isEqualTo(EntityVisibility.isPublic)
  }

  @Test
  fun `given publicScrapeSourceBool is false, when coerceVisibility works`() {
    mockFeatureValue(FeatureName.publicRepositoryBool, boolValue = false)

    // fallback
    assertThat(service.coerceVisibility(groupId, null)).isEqualTo(EntityVisibility.isPrivate)
    // isPrivate
    assertThat(service.coerceVisibility(groupId, EntityVisibility.isPrivate)).isEqualTo(EntityVisibility.isPrivate)
    // isPublic
    assertThat(service.coerceVisibility(groupId, EntityVisibility.isPublic)).isEqualTo(EntityVisibility.isPrivate)
  }

  @Test
  fun `given invalid refreshRate when coerceMinScheduledNextAt returns minimum rate`() {
    mockFeatureValue(FeatureName.refreshRateInMinutesLowerLimitInt, intValue = 4)
    val now = LocalDateTime.now()
    val minNext = now.plusMinutes(4)

    assertThat(
      service.coerceMinScheduledNextAt(
        LocalDateTime.now(),
        now.minusDays(2),
        groupId,
      )
    ).isCloseTo(minNext, TemporalUnitWithinOffset(10, ChronoUnit.SECONDS))
  }

  @Test
  fun `given valid refreshRate when coerceMinScheduledNextAt returns refreshRate`() {
    mockFeatureValue(FeatureName.refreshRateInMinutesLowerLimitInt, intValue = 4)
    val now = LocalDateTime.now()
    val future = now.plusDays(2)

    assertThat(
      service.coerceMinScheduledNextAt(
        LocalDateTime.now(),
        future,
        groupId
      )
    ).isAfterOrEqualTo(future)
  }

  @Test
  fun `given maxAge is undefined, RetentionMaxAgeDays is undefined`() {
    assertThat(service.coerceRetentionMaxAgeDays(null, groupId)).isEqualTo(null)
  }

  @Test
  fun `given maxAge is defined, RetentionMaxAgeDays is at least 2`() {
    mockFeatureValue(FeatureName.repositoryRetentionMaxDaysLowerLimitInt, intValue = 2)
    assertThat(service.coerceRetentionMaxAgeDays(-3, groupId)).isEqualTo(2)
    assertThat(service.coerceRetentionMaxAgeDays(0, groupId)).isEqualTo(2)
    assertThat(service.coerceRetentionMaxAgeDays(12, groupId)).isEqualTo(12)
  }

  @Test
  fun `given scrapeRequestActionMaxCountInt is undefined, all actionsCount will pass`() {
    mockFeatureValue(FeatureName.scrapeRequestActionMaxCountInt, intValue = null)
    service.auditScrapeRequestMaxActions(null, groupId)
    service.auditScrapeRequestMaxActions(10000, groupId)
  }

  @Test
  fun `given scrapeRequestActionMaxCountInt is defined, violating actionsCounts will fail`() {
    Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      mockFeatureValue(FeatureName.scrapeRequestActionMaxCountInt, intValue = 4)
      service.auditScrapeRequestMaxActions(12, groupId)
    }
  }

  @Test
  fun `given scrapeRequestActionMaxCountInt is defined, valid actionsCounts will pass`() {
    mockFeatureValue(FeatureName.scrapeRequestActionMaxCountInt, intValue = 4)
    service.auditScrapeRequestMaxActions(4, groupId)
    service.auditScrapeRequestMaxActions(0, groupId)
  }

  @Test
  fun `given scrapeRequestTimeoutInt is defined, violating actionsCounts will fail`() {
    Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      mockFeatureValue(FeatureName.scrapeRequestTimeoutMsecInt, intValue = 4)
      service.auditScrapeRequestTimeout(12, groupId)
    }
  }

  @Test

  fun `given scrapeRequestTimeoutInt is defined, valid actionsCounts will pass`() {
    mockFeatureValue(FeatureName.scrapeRequestTimeoutMsecInt, intValue = 4)
    service.auditScrapeRequestTimeout(4, groupId)
    service.auditScrapeRequestTimeout(0, groupId)
  }

  @Test
  fun `given scrapeSourceMaxCountTotalInt is undefined, all sourceCounts will pass`() {
    mockFeatureValue(FeatureName.repositoriesMaxCountTotalInt, intValue = null)
    service.auditRepositoryMaxCount(0, groupId)
    service.auditRepositoryMaxCount(10000, groupId)
  }

  @Test
  fun `given scrapeSourceMaxCountTotalInt is defined, violating sourceCounts will fail`() {
    Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      mockFeatureValue(FeatureName.repositoriesMaxCountTotalInt, intValue = 4)
      service.auditRepositoryMaxCount(12, groupId)
    }
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "2",
      "4"
    ]
  )
  fun `given scrapeSourceMaxCountTotalInt is defined, valid sourceCounts will pass`(sourceCount: Int) {
    mockFeatureValue(FeatureName.repositoriesMaxCountTotalInt, intValue = 4)
    service.auditRepositoryMaxCount(sourceCount, groupId)
  }

  @Test
  @Disabled
  fun violatesRepositoriesMaxActiveCount() {
    // todo
  }

  @Test
  fun `given scrapeRequestMaxCountPerSourceInt is undefined, all counts will pass`() {
    mockFeatureValue(FeatureName.sourceMaxCountPerRepositoryInt, intValue = null)
    service.auditSourcesMaxCountPerRepository(0, groupId)
    service.auditSourcesMaxCountPerRepository(10000, groupId)
  }

  @Test
  fun `given scrapeRequestMaxCountPerSourceInt is defined, violating counts will fail`() {
    Assertions.assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      mockFeatureValue(FeatureName.sourceMaxCountPerRepositoryInt, intValue = 4)
      service.auditSourcesMaxCountPerRepository(12, groupId)
    }
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "2",
      "4"
    ]
  )
  fun `given scrapeRequestMaxCountPerSourceInt is defined, valid counts will pass`(count: Int) {
    mockFeatureValue(FeatureName.sourceMaxCountPerRepositoryInt, intValue = 4)
    service.auditSourcesMaxCountPerRepository(count, groupId)
  }

  private fun mockFeatureValue(featureName: FeatureName, boolValue: Boolean? = null, intValue: Long? = null) {
    val feature = mock(FeatureValue::class.java)
    `when`(feature.valueBoolean).thenReturn(boolValue)
    `when`(feature.valueInt).thenReturn(intValue)
    `when`(
      featureValueRepository.resolveByFeatureGroupIdAndName(
        any(FeatureGroupId::class.java),
        eq(featureName.name),
      )
    ).thenReturn(feature)
  }

}
