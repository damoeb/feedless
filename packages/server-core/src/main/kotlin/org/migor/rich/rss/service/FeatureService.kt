package org.migor.rich.rss.service

import org.migor.rich.rss.data.jpa.models.FeatureName
import org.migor.rich.rss.data.jpa.models.FeatureState
import org.migor.rich.rss.data.jpa.models.FeatureValueType
import org.migor.rich.rss.data.jpa.models.PlanName
import org.migor.rich.rss.data.jpa.repositories.FeatureDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class FeatureService {
  private val log = LoggerFactory.getLogger(FeatureService::class.simpleName)

  @Autowired
  lateinit var featureDAO: FeatureDAO

  fun resolveByPlanName(planName: PlanName): List<PlanFeature> {
    return featureDAO.findAll().map {
      run {
        val planConfig = it.planFeatureConfigs.find { it.name == planName }!!
        val value = when(planConfig.valueType) {
          FeatureValueType.bool -> planConfig.valueBoolean
          FeatureValueType.number -> planConfig.valueInt
        }
        PlanFeature(it.name, it.state, value, planConfig.valueType)
      }
    }
  }
}

data class PlanFeature(
  val name: FeatureName,
  val state: FeatureState,
  val value: Comparable<*>?,
  val valueType: FeatureValueType
)
