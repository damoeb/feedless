package org.migor.feedless.plan

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PostgreSQLExtension
import org.migor.feedless.data.jpa.featureGroup.FeatureGroupDAO
import org.migor.feedless.feature.Feature
import org.migor.feedless.feature.FeatureGroup
import org.migor.feedless.feature.FeatureGroupId
import org.migor.feedless.feature.FeatureGroupRepository
import org.migor.feedless.feature.FeatureId
import org.migor.feedless.feature.FeatureName
import org.migor.feedless.feature.FeatureRepository
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.feature.FeatureValue
import org.migor.feedless.feature.FeatureValueRepository
import org.migor.feedless.feature.FeatureValueType
import org.migor.feedless.product.ProductRepository
import org.migor.feedless.session.SessionService
import org.migor.feedless.session.StatelessAuthService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest
@ExtendWith(PostgreSQLExtension::class)
@DirtiesContext
@ActiveProfiles(
  "test",
  AppProfiles.features,
  AppProfiles.properties,
  AppLayer.repository,
  AppLayer.service,
)
@MockitoBean(
  types = [
    SessionService::class,
    ProductRepository::class,
    PlanRepository::class,
    StatelessAuthService::class,
  ]
)
class FeatureServiceIntTest {

  private lateinit var childFeatureGroup: FeatureGroup
  private lateinit var parentFeatureGroup: FeatureGroup

  @Autowired
  lateinit var featureRepository: FeatureRepository

  @Autowired
  lateinit var featureService: FeatureService

  @Autowired
  lateinit var featureValueRepository: FeatureValueRepository

  @Autowired
  lateinit var featureGroupRepository: FeatureGroupRepository

  @Autowired
  lateinit var featureGroupDAO: FeatureGroupDAO

  private suspend fun createFeatureGroup(name: String, parentId: FeatureGroupId?): FeatureGroup {
    val group = FeatureGroup(
      name = name,
      parentFeatureGroupId = parentId
    )
    return featureGroupRepository.findByNameEqualsIgnoreCase(name) ?: featureGroupRepository.save(group)
  }

  @BeforeEach
  fun beforeEach() {
    runBlocking {
      parentFeatureGroup = createFeatureGroup("parent", null)
      childFeatureGroup = createFeatureGroup("child", parentFeatureGroup.id)
    }
  }

  @AfterEach
  fun afterEach() {
    featureGroupDAO.deleteById(childFeatureGroup.id.uuid)
    featureGroupDAO.deleteById(parentFeatureGroup.id.uuid)
    assertThat(featureGroupDAO.count()).isEqualTo(0)
  }

  @Test
  fun `given a featureGroup inheritance, feature values will be inherited from parent`() = runTest {
    // given

    val featureName = FeatureName.repositoriesMaxCountActiveInt
    val feature = resolveFeature(featureName)

    assertThat(featureValueRepository.resolveByFeatureGroupIdAndName(parentFeatureGroup.id, feature.name)).isNull()
    val randomInt = (Math.random() * 100).toLong()

    // when
    featureService.assignFeatureValues(parentFeatureGroup, mapOf(featureName to createFeatureValue(randomInt)))

    // then
    val parentValue = featureValueRepository.resolveByFeatureGroupIdAndName(parentFeatureGroup.id, feature.name)
    val childValue = featureValueRepository.resolveByFeatureGroupIdAndName(childFeatureGroup.id, feature.name)
    assertThat(parentValue!!.valueInt).isEqualTo(randomInt)
    assertThat(childValue!!.valueInt).isEqualTo(randomInt)

    assertThat(featureValueRepository.resolveAllByFeatureGroupId(parentFeatureGroup.id)[0].valueInt).isEqualTo(randomInt)
    assertThat(featureValueRepository.resolveAllByFeatureGroupId(childFeatureGroup.id)[0].valueInt).isEqualTo(randomInt)
  }

  @Test
  fun `given a featureGroup inheritance, feature values will be overwritten by child`() = runTest {
    // given
    val featureNameA = FeatureName.repositoriesMaxCountActiveInt
    val featureA = resolveFeature(featureNameA)

    val featureNameB = FeatureName.refreshRateInMinutesLowerLimitInt
    val featureB = resolveFeature(featureNameB)

    val randomInt = (Math.random() * 1002).toLong()
    val otherRandomInt = randomInt + 2

    // when
    featureService.assignFeatureValues(parentFeatureGroup, mapOf(featureNameA to createFeatureValue(randomInt)), false)
    featureService.assignFeatureValues(parentFeatureGroup, mapOf(featureNameB to createFeatureValue(randomInt)), false)
    featureService.assignFeatureValues(
      childFeatureGroup,
      mapOf(featureNameA to createFeatureValue(otherRandomInt)),
      false
    )

    // then
    val parentValue = featureValueRepository.resolveByFeatureGroupIdAndName(parentFeatureGroup.id, featureA.name)
    val childValue = featureValueRepository.resolveByFeatureGroupIdAndName(childFeatureGroup.id, featureA.name)
    assertThat(parentValue!!.valueInt).isEqualTo(randomInt)
    assertThat(childValue!!.valueInt).isEqualTo(otherRandomInt)

    val flatten = { list: List<FeatureValue> ->
      list.fold(mutableMapOf<String, Long>()) { acc, featureValue ->
        runBlocking {
          acc[featureRepository.findById(featureValue.featureId)!!.name] =
            featureValue.valueInt!!
          acc
        }
      }
    }

    val allParent = flatten(featureValueRepository.resolveAllByFeatureGroupId(parentFeatureGroup.id))
    assertThat(allParent.size).isEqualTo(2)
    assertThat(allParent).containsEntry(featureA.name, randomInt)
    assertThat(allParent).containsEntry(featureB.name, randomInt)


    val allChild = flatten(featureValueRepository.resolveAllByFeatureGroupId(childFeatureGroup.id))
    assertThat(allChild.size).isEqualTo(2)
    assertThat(allChild).containsEntry(featureA.name, otherRandomInt)
    assertThat(allChild).containsEntry(featureB.name, randomInt)
  }

  private fun createFeatureValue(
    valueInt: Long
  ): FeatureValue {
    val featureValue = FeatureValue(
      valueType = FeatureValueType.number,
      valueInt = valueInt,
      // --
      featureGroupId = FeatureGroupId(),
      featureId = FeatureId(),
    )
    return featureValue
  }

  private suspend fun resolveFeature(featureName: FeatureName): Feature {
    return featureRepository.findByName(featureName.name) ?: featureRepository.save(
      Feature(
        name = featureName.name
      )
    )
  }
}
