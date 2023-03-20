package org.migor.rich.rss.data.jpa.seed

import jakarta.annotation.PostConstruct
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.data.jpa.models.FeatureEntity
import org.migor.rich.rss.data.jpa.models.FeatureName
import org.migor.rich.rss.data.jpa.models.FeatureState
import org.migor.rich.rss.data.jpa.models.FeatureValueType
import org.migor.rich.rss.data.jpa.models.PlanAvailability
import org.migor.rich.rss.data.jpa.models.PlanEntity
import org.migor.rich.rss.data.jpa.models.PlanFeatureConfig
import org.migor.rich.rss.data.jpa.models.PlanName
import org.migor.rich.rss.data.jpa.repositories.FeatureDAO
import org.migor.rich.rss.data.jpa.repositories.PlanDAO
import org.migor.rich.rss.service.PropertyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
@Order(1)
@Profile("${AppProfiles.bootstrap} && ${AppProfiles.database}")
class SeedPlans {

  @Autowired
  lateinit var planDAO: PlanDAO

  @Autowired
  lateinit var featureDAO: FeatureDAO

  @Autowired
  lateinit var propertyService: PropertyService

  @PostConstruct
  @Transactional(propagation = Propagation.REQUIRED)
  fun postConstruct() {
    val free = toPlan(PlanName.free, 0.0, PlanAvailability.available)
    free.primary = true
    planDAO.save(free)
    planDAO.save(toPlan(PlanName.basic, 9.99, PlanAvailability.by_request))
    featureDAO.saveAll(getFeatures())
  }

  private fun toIntPlanFeature(name: PlanName, value: Int): PlanFeatureConfig {
    return PlanFeatureConfig(name, value, null, FeatureValueType.number)
  }

  private fun toBoolPlanFeature(name: PlanName, value: Boolean): PlanFeatureConfig {
    return PlanFeatureConfig(name, null , value, FeatureValueType.bool)
  }

  private fun toFeature(featureName: FeatureName, state: FeatureState, planFeatures: List<PlanFeatureConfig>): FeatureEntity {
    val feature = FeatureEntity()
    feature.name = featureName
    feature.state = state
    feature.planFeatureConfigs = planFeatures
    return feature
  }

  private fun getFeatures(): List<FeatureEntity> {
    return listOf(
      toFeature(
        FeatureName.rateLimit,
        FeatureState.stable,
        listOf(
          toIntPlanFeature(PlanName.free, 40),
          toIntPlanFeature(PlanName.basic, 120)
        )
      ),
      toFeature(
        FeatureName.feedsMaxRefreshRate,
        FeatureState.stable,
        listOf(
          toIntPlanFeature(PlanName.free, 120),
          toIntPlanFeature(PlanName.basic, 10)
        )
      ),
      toFeature(
        FeatureName.bucketsMaxCount,
        FeatureState.stable,
        listOf(
          toIntPlanFeature(PlanName.free, 3),
          toIntPlanFeature(PlanName.basic, 100)
        )
      ),
      toFeature(
        FeatureName.feedsMaxCount,
        FeatureState.stable,
        listOf(
          toIntPlanFeature(PlanName.free, 30),
          toIntPlanFeature(PlanName.basic, 1000)
        )
      ),
      toFeature(
        FeatureName.itemsRetention,
        FeatureState.stable,
        listOf(
          toIntPlanFeature(PlanName.free, 400),
          toIntPlanFeature(PlanName.basic, 10000)
        )
      ),
      toFeature(
        FeatureName.bucketsAccessOther,
        FeatureState.stable,
        listOf(
          toBoolPlanFeature(PlanName.free, true),
          toBoolPlanFeature(PlanName.basic, true)
        )
      ),
      toFeature(
        FeatureName.notifications,
        FeatureState.stable,
        listOf(
          toBoolPlanFeature(PlanName.free, true),
          toBoolPlanFeature(PlanName.basic, true)
        )
      ),
      toFeature(
        FeatureName.genFeedFromWebsite,
        FeatureState.stable,
        listOf(
          toBoolPlanFeature(PlanName.free, true),
          toBoolPlanFeature(PlanName.basic, true)
        )
      ),
      toFeature(
        FeatureName.genFeedFromFeed,
        FeatureState.stable,
        listOf(
          toBoolPlanFeature(PlanName.free, true),
          toBoolPlanFeature(PlanName.basic, true)
        )
      ),
      toFeature(
        FeatureName.genFeedFromPageChange,
        FeatureState.experimental,
        listOf(
          toBoolPlanFeature(PlanName.free, true),
          toBoolPlanFeature(PlanName.basic, true)
        )
      ),
      toFeature(
        FeatureName.genFeedWithPrerender,
        FeatureState.stable,
        listOf(
          toBoolPlanFeature(PlanName.free, true),
          toBoolPlanFeature(PlanName.basic, true)
        )
      ),
      toFeature(
        FeatureName.genFeedWithPuppeteerScript,
        FeatureState.experimental,
        listOf(
          toBoolPlanFeature(PlanName.free, false),
          toBoolPlanFeature(PlanName.basic, true)
        )
      ),
      toFeature(
        FeatureName.feedAuthentication,
        FeatureState.experimental,
        listOf(
          toBoolPlanFeature(PlanName.free, false),
          toBoolPlanFeature(PlanName.basic, true)
        )
      ),
      toFeature(
        FeatureName.feedsPrivateAccess,
        FeatureState.beta,
        listOf(
          toBoolPlanFeature(PlanName.free, false),
          toBoolPlanFeature(PlanName.basic, true)
        )
      ),
      toFeature(
        FeatureName.bucketsPrivateAccess,
        FeatureState.beta,
        listOf(
          toBoolPlanFeature(PlanName.free, false),
          toBoolPlanFeature(PlanName.basic, true)
        )
      ),
      toFeature(
        FeatureName.feedsFulltext,
        FeatureState.stable,
        listOf(
          toBoolPlanFeature(PlanName.free, false),
          toBoolPlanFeature(PlanName.basic, true)
        )
      ),
      toFeature(
        FeatureName.itemsInlineImages,
        FeatureState.stable,
        listOf(
          toBoolPlanFeature(PlanName.free, false),
          toBoolPlanFeature(PlanName.basic, true)
        )
      ),
      toFeature(
        FeatureName.itemsNoUrlShortener,
        FeatureState.experimental,
        listOf(
          toBoolPlanFeature(PlanName.free, true),
          toBoolPlanFeature(PlanName.basic, true)
        )
      ),
      toFeature(
        FeatureName.api,
        FeatureState.off,
        listOf(
          toBoolPlanFeature(PlanName.free, false),
          toBoolPlanFeature(PlanName.basic, true)
        )
      ),
      toFeature(
        FeatureName.itemEmailForward,
        FeatureState.off,
        listOf(
          toBoolPlanFeature(PlanName.free, false),
          toBoolPlanFeature(PlanName.basic, true)
        )
      ),
      toFeature(
        FeatureName.itemWebhookForward,
        FeatureState.off,
        listOf(
          toBoolPlanFeature(PlanName.free, true),
          toBoolPlanFeature(PlanName.basic, true)
        )
      )
    )
  }

  private fun toPlan(name: PlanName, costs: Double, availability: PlanAvailability): PlanEntity {
    val plan = PlanEntity()
    plan.name = name
    plan.costs = costs
    plan.availability = availability
    return plan
  }


}
