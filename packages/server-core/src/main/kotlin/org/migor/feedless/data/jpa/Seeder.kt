package org.migor.feedless.data.jpa

import jakarta.annotation.PostConstruct
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiErrorCode
import org.migor.feedless.api.ApiException
import org.migor.feedless.data.jpa.enums.AuthSource
import org.migor.feedless.data.jpa.enums.ProductName
import org.migor.feedless.data.jpa.models.FeatureEntity
import org.migor.feedless.data.jpa.models.FeatureName
import org.migor.feedless.data.jpa.models.FeatureValueType
import org.migor.feedless.data.jpa.models.PlanAvailability
import org.migor.feedless.data.jpa.models.PlanEntity
import org.migor.feedless.data.jpa.models.PlanName
import org.migor.feedless.data.jpa.models.UserEntity
import org.migor.feedless.data.jpa.models.UserSecretEntity
import org.migor.feedless.data.jpa.models.UserSecretType
import org.migor.feedless.data.jpa.models.featureScope
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
      userDAO.findRootUser() ?: createUser(propertyService.rootEmail, isRoot = true, authSource = AuthSource.none, plan = PlanName.system)
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
    plan = PlanName.system
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
    user.product = ProductName.system
    user.anonymous = isAnonymous
    user.usesAuthSource = authSource
    user.planId = planDAO.findByNameAndProduct(plan, ProductName.system)!!.id
    return userDAO.saveAndFlush(user)
  }

  private fun seedPlans() {
    persistPlan(
      PlanName.system, 0.0, PlanAvailability.unavailable, ProductName.system, features = mapOf(
        FeatureName.scrapeSourceExpiryInDaysInt to asIntFeature(7),
//        FeatureName.rateLimitInt to asIntFeature(40),
        FeatureName.minRefreshRateInMinutesInt to asIntFeature(120),
        FeatureName.scrapeSourceRetentionMaxItemsInt to asIntFeature(10),
        FeatureName.scrapeRequestTimeoutInt to asIntFeature(30000),
        FeatureName.scrapeSourceMaxCountTotalInt to asIntFeature(10000),
        FeatureName.scrapeSourceMaxCountActiveInt to asIntFeature(10000),
        FeatureName.scrapeRequestActionMaxCountInt to asIntFeature(5),
        FeatureName.scrapeRequestMaxCountPerSourceInt to asIntFeature(2),
        FeatureName.publicScrapeSourceBool to asBoolFeature(false),
        FeatureName.apiBool to asBoolFeature(false),
        FeatureName.canLogin to asBoolFeature(true),
        FeatureName.canCreateUser to asBoolFeature(false),
        FeatureName.pluginsBool to asBoolFeature(true),
        FeatureName.itemEmailForwardBool to asBoolFeature(true),
        FeatureName.itemWebhookForwardBool to asBoolFeature(true),
      )
    )

    seedPlansForProduct(ProductName.rssBuilder)
    seedPlansForProduct(ProductName.feedless)
    seedPlansForProduct(ProductName.visualDiff)
    seedPlansForProduct(ProductName.untoldNotes)
  }

  private fun seedPlansForProduct(product: ProductName) {
    persistPlan(
      PlanName.minimal, 0.0, PlanAvailability.available, product, primary = true, mapOf(
        FeatureName.rateLimitInt to asIntFeature(40),
        FeatureName.minRefreshRateInMinutesInt to asIntFeature(120),
        FeatureName.scrapeSourceRetentionMaxItemsInt to asIntFeature(10),
        FeatureName.scrapeRequestTimeoutInt to asIntFeature(30000),
        FeatureName.scrapeSourceMaxCountTotalInt to asIntFeature(10),
        FeatureName.scrapeSourceMaxCountActiveInt to asIntFeature(5),
        FeatureName.scrapeRequestActionMaxCountInt to asIntFeature(5),
        FeatureName.scrapeRequestMaxCountPerSourceInt to asIntFeature(2),
        FeatureName.publicScrapeSourceBool to asBoolFeature(false),
        FeatureName.apiBool to asBoolFeature(false),
        FeatureName.pluginsBool to asBoolFeature(true),
        FeatureName.canLogin to asBoolFeature(true),
        FeatureName.canCreateUser to asBoolFeature(false),
        FeatureName.canCreateAsAnonymous to asBoolFeature(true),
        FeatureName.hasWaitList to asBoolFeature(true),
        FeatureName.itemEmailForwardBool to asBoolFeature(false),
        FeatureName.itemWebhookForwardBool to asBoolFeature(true),
      )
    )
    persistPlan(
      PlanName.basic, 9.99, PlanAvailability.by_request, product, features = mapOf(
        FeatureName.rateLimitInt to asIntFeature(120),
        FeatureName.minRefreshRateInMinutesInt to asIntFeature(10),
        FeatureName.scrapeSourceRetentionMaxItemsInt to asIntFeature(100),
        FeatureName.scrapeRequestTimeoutInt to asIntFeature(60000),
        FeatureName.scrapeSourceMaxCountTotalInt to asIntFeature(30),
        FeatureName.scrapeSourceMaxCountActiveInt to asIntFeature(30),
        FeatureName.scrapeRequestActionMaxCountInt to asIntFeature(20),
        FeatureName.scrapeRequestMaxCountPerSourceInt to asIntFeature(10),
        FeatureName.publicScrapeSourceBool to asBoolFeature(true),
        FeatureName.apiBool to asBoolFeature(true),
        FeatureName.pluginsBool to asBoolFeature(true),
        FeatureName.itemEmailForwardBool to asBoolFeature(true),
        FeatureName.itemWebhookForwardBool to asBoolFeature(true)
      )
    )
  }

  private fun persistPlan(
    name: PlanName,
    costs: Double,
    availability: PlanAvailability,
    product: ProductName,
    primary: Boolean = false,
    features: Map<FeatureName, FeatureEntity>
  ) {
    persistFeatures(resolvePlan(name, costs, availability, product, primary), features)
  }

  private fun resolvePlan(
    name: PlanName,
    costs: Double,
    availability: PlanAvailability,
    product: ProductName,
    primary: Boolean = false,
  ): PlanEntity {
    val plan = PlanEntity()
    plan.name = name
    plan.product = product
    plan.currentCosts = costs
    plan.availability = availability
    plan.primaryPlan = primary

    return planDAO.findByNameAndProduct(name, product) ?: planDAO.save(plan)
  }

  private fun persistFeatures(plan: PlanEntity, features: Map<FeatureName, FeatureEntity>) {
    features.forEach { (featureName, featureEntity) ->
      featureDAO.findByPlanIdAndName(plan.id, featureName)
        ?.let {
          it.scope = featureScope(featureName)
          val logValueMismatch = { expected: Any, actual: Any -> log.warn("Feature Value Mismatch! Feature $featureName on ${plan.product}/${plan.name} expects ${expected}, actual $actual") }
          if (it.valueBoolean != featureEntity.valueBoolean) {
            logValueMismatch(featureEntity.valueBoolean!!, it.valueBoolean!!)
          }
          if (it.valueInt != featureEntity.valueInt) {
            logValueMismatch(featureEntity.valueInt!!, it.valueInt!!)
          }
          featureDAO.save(it)
        }
        ?: run {
          featureEntity.name = featureName
          featureEntity.planId = plan.id
          featureEntity.scope = featureScope(featureName)
          featureDAO.save(featureEntity)
        }
      }
  }

  private fun asIntFeature(value: Int): FeatureEntity {
    val feature = FeatureEntity()
    feature.valueType = FeatureValueType.number
    feature.valueInt = value
    return feature
  }

  private fun asBoolFeature(value: Boolean): FeatureEntity {
    val feature = FeatureEntity()
    feature.valueType = FeatureValueType.bool
    feature.valueBoolean = value
    return feature
  }

}
