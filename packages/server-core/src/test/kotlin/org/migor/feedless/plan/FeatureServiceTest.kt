package org.migor.feedless.plan

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PostgreSQLExtension
import org.migor.feedless.data.jpa.feature.FeatureDAO
import org.migor.feedless.data.jpa.feature.FeatureEntity
import org.migor.feedless.data.jpa.featureGroup.FeatureGroupDAO
import org.migor.feedless.data.jpa.featureGroup.FeatureGroupEntity
import org.migor.feedless.data.jpa.featureValue.FeatureName
import org.migor.feedless.data.jpa.featureValue.FeatureValueDAO
import org.migor.feedless.data.jpa.featureValue.FeatureValueEntity
import org.migor.feedless.data.jpa.plan.PlanDAO
import org.migor.feedless.data.jpa.product.ProductDAO
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.feature.FeatureValueType
import org.migor.feedless.session.SessionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.*

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
    ProductDAO::class,
    PlanDAO::class,
  ]
)
class FeatureServiceTest {

  private lateinit var childFeatureGroup: FeatureGroupEntity
  private lateinit var parentFeatureGroup: FeatureGroupEntity

  @Autowired
  lateinit var featureDAO: FeatureDAO

  @Autowired
  lateinit var featureService: FeatureService

  @Autowired
  lateinit var featureValueDAO: FeatureValueDAO

  @Autowired
  lateinit var featureGroupDAO: FeatureGroupDAO

  private fun createFeatureGroup(name: String, parentId: UUID?): FeatureGroupEntity {
    val group = FeatureGroupEntity()
    group.name = name
    group.parentFeatureGroupId = parentId

    return featureGroupDAO.findByNameEqualsIgnoreCase(name) ?: featureGroupDAO.save(group)
  }

  @BeforeEach
  fun beforeEach() {
    parentFeatureGroup = createFeatureGroup("parent", null)
    childFeatureGroup = createFeatureGroup("child", parentFeatureGroup.id)
  }

  @AfterEach
  fun afterEach() {
    featureGroupDAO.delete(childFeatureGroup)
    featureGroupDAO.delete(parentFeatureGroup)
    assertThat(featureGroupDAO.count()).isEqualTo(0)
  }

  @Test
  fun `given a featureGroup inheritance, feature values will be inherited from parent`() = runTest {
    // given

    val featureName = FeatureName.repositoriesMaxCountActiveInt
    val feature = resolveFeature(featureName)

    assertThat(featureValueDAO.resolveByFeatureGroupIdAndName(parentFeatureGroup.id, feature.name)).isNull()
    val randomInt = (Math.random() * 100).toLong()

    // when
    featureService.assignFeatureValues(parentFeatureGroup, mapOf(featureName to createFeatureValue(randomInt)))

    // then
    val parentValue = featureValueDAO.resolveByFeatureGroupIdAndName(parentFeatureGroup.id, feature.name)
    val childValue = featureValueDAO.resolveByFeatureGroupIdAndName(childFeatureGroup.id, feature.name)
    assertThat(parentValue!!.valueInt).isEqualTo(randomInt)
    assertThat(childValue!!.valueInt).isEqualTo(randomInt)

    assertThat(featureValueDAO.resolveAllByFeatureGroupId(parentFeatureGroup.id)[0].valueInt).isEqualTo(randomInt)
    assertThat(featureValueDAO.resolveAllByFeatureGroupId(childFeatureGroup.id)[0].valueInt).isEqualTo(randomInt)
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
    val parentValue = featureValueDAO.resolveByFeatureGroupIdAndName(parentFeatureGroup.id, featureA.name)
    val childValue = featureValueDAO.resolveByFeatureGroupIdAndName(childFeatureGroup.id, featureA.name)
    assertThat(parentValue!!.valueInt).isEqualTo(randomInt)
    assertThat(childValue!!.valueInt).isEqualTo(otherRandomInt)

    val flatten = { list: List<FeatureValueEntity> ->
      list.fold(mutableMapOf<String, Long>()) { acc, featureValueEntity ->
        run {
          acc[featureDAO.findById(featureValueEntity.featureId).orElseThrow().name] = featureValueEntity.valueInt!!
          acc
        }
      }
    }

    val allParent = flatten(featureValueDAO.resolveAllByFeatureGroupId(parentFeatureGroup.id))
    assertThat(allParent.size).isEqualTo(2)
    assertThat(allParent).containsEntry(featureA.name, randomInt)
    assertThat(allParent).containsEntry(featureB.name, randomInt)


    val allChild = flatten(featureValueDAO.resolveAllByFeatureGroupId(childFeatureGroup.id))
    assertThat(allChild.size).isEqualTo(2)
    assertThat(allChild).containsEntry(featureA.name, otherRandomInt)
    assertThat(allChild).containsEntry(featureB.name, randomInt)
  }

  private fun createFeatureValue(
    valueInt: Long
  ): FeatureValueEntity {
    val featureValue = FeatureValueEntity()
    featureValue.valueType = FeatureValueType.number
    featureValue.valueInt = valueInt
    return featureValue
  }

  private fun resolveFeature(featureName: FeatureName): FeatureEntity {
    val feature = FeatureEntity()
    feature.name = featureName.name
    return featureDAO.findByName(featureName.name) ?: featureDAO.save(feature)
  }
}
