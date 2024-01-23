package org.migor.feedless.data.jpa

import jakarta.annotation.PostConstruct
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiErrorCode
import org.migor.feedless.api.ApiException
import org.migor.feedless.data.jpa.enums.AuthSource
import org.migor.feedless.data.jpa.models.FeatureEntity
import org.migor.feedless.data.jpa.models.FeatureName
import org.migor.feedless.data.jpa.models.FeatureState
import org.migor.feedless.data.jpa.models.FeatureValueType
import org.migor.feedless.data.jpa.models.PlanAvailability
import org.migor.feedless.data.jpa.models.PlanEntity
import org.migor.feedless.data.jpa.models.PlanName
import org.migor.feedless.data.jpa.models.UserEntity
import org.migor.feedless.data.jpa.models.UserSecretEntity
import org.migor.feedless.data.jpa.models.UserSecretType
import org.migor.feedless.data.jpa.repositories.FeatureDAO
import org.migor.feedless.data.jpa.repositories.PlanDAO
import org.migor.feedless.data.jpa.repositories.UserDAO
import org.migor.feedless.data.jpa.repositories.UserSecretDAO
import org.migor.feedless.service.PropertyService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Service
@Order(1)
@Profile(AppProfiles.database)
class Seeder {

  private val log = LoggerFactory.getLogger(Seeder::class.simpleName)

  @Autowired
  lateinit var planDAO: PlanDAO

  @Autowired
  lateinit var featureDAO: FeatureDAO

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var userSecretDAO: UserSecretDAO

  @Autowired
  lateinit var userDAO: UserDAO

  @PostConstruct
  @Transactional(propagation = Propagation.REQUIRED)
  fun onInit() {
    seedPlans()
    seedUsers()
  }

  private fun seedUsers() {
    userDAO.findByEmail(propertyService.anonymousEmail) ?: createAnonymousUser()
    val root =
      userDAO.findRootUser() ?: createUser(propertyService.rootEmail, isRoot = true, authSource = AuthSource.none, plan = PlanName.internal)
    if (root.email != propertyService.rootEmail) {
      log.info("Updated rootEmail")
      root.email = propertyService.rootEmail
      userDAO.save(root)
    }

    if (!userSecretDAO.existsByValueAndOwnerId(propertyService.rootSecretKey, root.id)) {
      log.info("created secretKey for root")
      val userSecret = UserSecretEntity()
      userSecret.ownerId = root.id
      userSecret.value = propertyService.rootSecretKey
      userSecret.type = UserSecretType.SecretKey
      userSecret.validUntil =
        Date.from(LocalDateTime.now().plus(Duration.ofDays(356)).atZone(ZoneId.systemDefault()).toInstant())
      userSecretDAO.save(userSecret)
    }
  }

  private fun createAnonymousUser() = createUser(
    propertyService.anonymousEmail,
    isAnonymous = true,
    authSource = AuthSource.none,
    plan = PlanName.free
  )

  private fun createUser(
    email: String,
    authSource: AuthSource,
    plan: PlanName,
    isRoot: Boolean = false,
    isAnonymous: Boolean = false
  ): UserEntity {
    if (userDAO.existsByEmail(email)) {
      throw ApiException(ApiErrorCode.INTERNAL_ERROR, "user already exists")
    }
    log.info("create internal user $email")
    val user = UserEntity()
    user.email = email
    user.root = isRoot
    user.anonymous = isAnonymous
    user.usesAuthSource = authSource
    user.planId = planDAO.findByName(plan).id
    return userDAO.saveAndFlush(user)
  }



  private fun seedPlans() {
    val free = toPlan(PlanName.free, 0.0, PlanAvailability.available)
    free.primary = true
    val freePlan = planDAO.save(free)
    val basicPlan = planDAO.save(toPlan(PlanName.basic, 9.99, PlanAvailability.by_request))
    val internalPlan = planDAO.save(toPlan(PlanName.internal, 0.0, PlanAvailability.unavailable))
    featureDAO.saveAll(toFeatures(freePlan, basicPlan, internalPlan))
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

  private fun toFeatures(freePlan: PlanEntity, basicPlan: PlanEntity, internalPlan: PlanEntity): List<FeatureEntity> {
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
        FeatureName.minRefreshRateInMinutes,
        FeatureState.stable,
        listOf(
          toIntPlanFeature(freePlan, 120),
          toIntPlanFeature(basicPlan, 10)
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
        FeatureName.scrapeRequestTimeout,
        FeatureState.stable,
        listOf(
          toIntPlanFeature(freePlan, 30000),
          toIntPlanFeature(basicPlan, 60000)
        )
      ),
      toFeature(
        FeatureName.scrapeSourceExpiryInDays,
        FeatureState.stable,
        listOf(
          toIntPlanFeature(internalPlan, 14),
        )
      ),
      toFeature(
        FeatureName.publicScrapeSource,
        FeatureState.stable,
        listOf(
          toBoolPlanFeature(freePlan, false),
          toBoolPlanFeature(basicPlan, true)
        )
      ),
      toFeature(
        FeatureName.notifications,
        FeatureState.off,
        listOf(
          toBoolPlanFeature(freePlan, true),
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
