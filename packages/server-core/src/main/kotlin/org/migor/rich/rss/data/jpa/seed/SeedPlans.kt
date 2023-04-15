package org.migor.rich.rss.data.jpa.seed

import jakarta.annotation.PostConstruct
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.data.jpa.models.FeatureEntity
import org.migor.rich.rss.data.jpa.models.FeatureName
import org.migor.rich.rss.data.jpa.models.FeatureState
import org.migor.rich.rss.data.jpa.models.FeatureValueType
import org.migor.rich.rss.data.jpa.models.PlanAvailability
import org.migor.rich.rss.data.jpa.models.PlanEntity
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
@Profile("${AppProfiles.seed} && ${AppProfiles.database}")
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
    val freePlan = planDAO.save(free)
    val basicPlan = planDAO.save(toPlan(PlanName.basic, 9.99, PlanAvailability.by_request))
    featureDAO.saveAll(toFeatures(freePlan, basicPlan))
  }

  private fun toIntPlanFeature(plan: PlanEntity, value: Int): FeatureEntity {
    val feature = FeatureEntity()
    feature.plan = plan
    feature.valueType = FeatureValueType.number
    feature.valueInt = value
    return feature
  }

  private fun toBoolPlanFeature(plan: PlanEntity, value: Boolean): FeatureEntity {
    val feature = FeatureEntity()
    feature.plan = plan
    feature.valueType = FeatureValueType.bool
    feature.valueBoolean = value
    return feature
  }

  private fun toFeature(featureName: FeatureName, state: FeatureState, features: List<FeatureEntity>): List<FeatureEntity> {
    return features.map { run {
        it.name = featureName
        it.state = state
        it
      }
    }
  }

  private fun toFeatures(freePlan: PlanEntity, basicPlan: PlanEntity): List<FeatureEntity> {
    return listOf(
      toFeature(
        FeatureName.rateLimit,
        FeatureState.stable,
        listOf(
          toIntPlanFeature(freePlan, 40),
          toIntPlanFeature(basicPlan, 120)
        )
      ),
      toFeature(
        FeatureName.feedsMaxRefreshRate,
        FeatureState.stable,
        listOf(
          toIntPlanFeature(freePlan, 120),
          toIntPlanFeature(basicPlan, 10)
        )
      ),
      toFeature(
        FeatureName.bucketsMaxCount,
        FeatureState.stable,
        listOf(
          toIntPlanFeature(freePlan, 3),
          toIntPlanFeature(basicPlan, 100)
        )
      ),
      toFeature(
        FeatureName.feedsMaxCount,
        FeatureState.stable,
        listOf(
          toIntPlanFeature(freePlan, 30),
          toIntPlanFeature(basicPlan, 1000)
        )
      ),
      toFeature(
        FeatureName.itemsRetention,
        FeatureState.stable,
        listOf(
          toIntPlanFeature(freePlan, 400),
          toIntPlanFeature(basicPlan, 10000)
        )
      ),
      toFeature(
        FeatureName.bucketsAccessOther,
        FeatureState.stable,
        listOf(
          toBoolPlanFeature(freePlan, true),
          toBoolPlanFeature(basicPlan, true)
        )
      ),
      toFeature(
        FeatureName.notifications,
        FeatureState.stable,
        listOf(
          toBoolPlanFeature(freePlan, true),
          toBoolPlanFeature(basicPlan, true)
        )
      ),
      toFeature(
        FeatureName.genFeedFromWebsite,
        FeatureState.stable,
        listOf(
          toBoolPlanFeature(freePlan, true),
          toBoolPlanFeature(basicPlan, true)
        )
      ),
      toFeature(
        FeatureName.genFeedFromFeed,
        FeatureState.stable,
        listOf(
          toBoolPlanFeature(freePlan, true),
          toBoolPlanFeature(basicPlan, true)
        )
      ),
      toFeature(
        FeatureName.genFeedFromPageChange,
        FeatureState.experimental,
        listOf(
          toBoolPlanFeature(freePlan, true),
          toBoolPlanFeature(basicPlan, true)
        )
      ),
      toFeature(
        FeatureName.genFeedWithPrerender,
        FeatureState.stable,
        listOf(
          toBoolPlanFeature(freePlan, true),
          toBoolPlanFeature(basicPlan, true)
        )
      ),
      toFeature(
        FeatureName.genFeedWithPuppeteerScript,
        FeatureState.experimental,
        listOf(
          toBoolPlanFeature(freePlan, false),
          toBoolPlanFeature(basicPlan, true)
        )
      ),
      toFeature(
        FeatureName.feedAuthentication,
        FeatureState.experimental,
        listOf(
          toBoolPlanFeature(freePlan, false),
          toBoolPlanFeature(basicPlan, true)
        )
      ),
      toFeature(
        FeatureName.feedsPrivateAccess,
        FeatureState.beta,
        listOf(
          toBoolPlanFeature(freePlan, false),
          toBoolPlanFeature(basicPlan, true)
        )
      ),
      toFeature(
        FeatureName.bucketsPrivateAccess,
        FeatureState.beta,
        listOf(
          toBoolPlanFeature(freePlan, false),
          toBoolPlanFeature(basicPlan, true)
        )
      ),
      toFeature(
        FeatureName.feedsFulltext,
        FeatureState.stable,
        listOf(
          toBoolPlanFeature(freePlan, false),
          toBoolPlanFeature(basicPlan, true)
        )
      ),
      toFeature(
        FeatureName.itemsInlineImages,
        FeatureState.stable,
        listOf(
          toBoolPlanFeature(freePlan, false),
          toBoolPlanFeature(basicPlan, true)
        )
      ),
      toFeature(
        FeatureName.itemsNoUrlShortener,
        FeatureState.experimental,
        listOf(
          toBoolPlanFeature(freePlan, true),
          toBoolPlanFeature(basicPlan, true)
        )
      ),
      toFeature(
        FeatureName.api,
        FeatureState.off,
        listOf(
          toBoolPlanFeature(freePlan, false),
          toBoolPlanFeature(basicPlan, true)
        )
      ),
      toFeature(
        FeatureName.itemEmailForward,
        FeatureState.off,
        listOf(
          toBoolPlanFeature(freePlan, false),
          toBoolPlanFeature(basicPlan, true)
        )
      ),
      toFeature(
        FeatureName.itemWebhookForward,
        FeatureState.off,
        listOf(
          toBoolPlanFeature(freePlan, true),
          toBoolPlanFeature(basicPlan, true)
        )
      )
    ).flatten()
  }

  private fun toPlan(name: PlanName, costs: Double, availability: PlanAvailability): PlanEntity {
    val plan = PlanEntity()
    plan.name = name
    plan.costs = costs
    plan.availability = availability
    return plan
  }


}
