package org.migor.feedless.plan

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.AppProfiles
import org.migor.feedless.license.LicenseService
import org.migor.feedless.pipeline.PluginService
import org.migor.feedless.secrets.UserSecretService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*

@SpringBootTest
@ActiveProfiles(profiles = ["test", AppProfiles.database])
@MockBeans(
  value = [
    MockBean(PluginService::class),
    MockBean(LicenseService::class),
    MockBean(UserSecretService::class),
    MockBean(ProductService::class),
  ]
)
@Testcontainers
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

    return featureGroupDAO.findByName(name) ?: featureGroupDAO.save(group)
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
  fun `given a featureGroup inheritance, feature values will be inherited from parent`() {
    // given

    val featureName = FeatureName.scrapeSourceMaxCountActiveInt
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
  fun `given a featureGroup inheritance, feature values will be overwritten by child`() {
    // given
    val featureNameA = FeatureName.scrapeSourceMaxCountActiveInt
    val featureA = resolveFeature(featureNameA)

    val featureNameB = FeatureName.refreshRateInMinutesLowerLimitInt
    val featureB = resolveFeature(featureNameB)

    val randomInt = (Math.random() * 1002).toLong()
    val otherRandomInt = randomInt + 2

    // when
    featureService.assignFeatureValues(parentFeatureGroup, mapOf(featureNameA to createFeatureValue(randomInt)))
    featureService.assignFeatureValues(parentFeatureGroup, mapOf(featureNameB to createFeatureValue(randomInt)))
    featureService.assignFeatureValues(childFeatureGroup, mapOf(featureNameA to createFeatureValue(otherRandomInt)))

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


  companion object {

    @Container
    private val postgres = PostgreSQLContainer("postgres:15")
      .withDatabaseName("feedless-test")
      .withUsername("postgres")
      .withPassword("admin")

    @JvmStatic
    @DynamicPropertySource
    fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
      registry.add("spring.datasource.url") { "jdbc:tc:postgresql:15://localhost:${postgres.firstMappedPort}/${postgres.databaseName}?TC_REUSABLE=false" }
      registry.add("spring.datasource.username", postgres::getUsername)
      registry.add("spring.datasource.password", postgres::getPassword)
    }
  }

}
