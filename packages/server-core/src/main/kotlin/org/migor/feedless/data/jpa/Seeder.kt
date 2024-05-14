package org.migor.feedless.data.jpa

import jakarta.annotation.PostConstruct
import org.migor.feedless.AppProfiles
import org.migor.feedless.BadRequestException
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.enums.AuthSource
import org.migor.feedless.data.jpa.enums.ProductName
import org.migor.feedless.plan.FeatureDAO
import org.migor.feedless.plan.FeatureEntity
import org.migor.feedless.plan.FeatureName
import org.migor.feedless.plan.FeatureValueDAO
import org.migor.feedless.plan.FeatureValueEntity
import org.migor.feedless.plan.FeatureValueType
import org.migor.feedless.plan.PlanAvailability
import org.migor.feedless.plan.PlanDAO
import org.migor.feedless.plan.PlanEntity
import org.migor.feedless.plan.PlanName
import org.migor.feedless.plan.ProductDAO
import org.migor.feedless.plan.ProductEntity
import org.migor.feedless.secrets.UserSecretDAO
import org.migor.feedless.secrets.UserSecretEntity
import org.migor.feedless.secrets.UserSecretType
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserEntity
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Service
@Order(1)
@Profile("${AppProfiles.seed} & ${AppProfiles.database}")
class Seeder {

  private val log = LoggerFactory.getLogger(Seeder::class.simpleName)

  @Autowired
  lateinit var planDAO: PlanDAO

  @Autowired
  lateinit var featureValueDAO: FeatureValueDAO

  @Autowired
  lateinit var featureDAO: FeatureDAO

  @Autowired
  lateinit var environment: Environment

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var productDAO: ProductDAO

  @Autowired
  lateinit var userSecretDAO: UserSecretDAO

  @Autowired
  lateinit var userDAO: UserDAO

  @PostConstruct
  @Transactional(propagation = Propagation.REQUIRED)
  fun onInit() {
    val root = seedRootUser()
    seedProductsAndPlans(root)
    seedUsers()
  }

  private fun seedUsers() {
    userDAO.findByEmail(propertyService.anonymousEmail) ?: createAnonymousUser()
    if (isSelfHosted()) {
//      createUser(
//        propertyService.rootEmail,
//        isRoot = false,
//        authSource = AuthSource.none,
//        plan = PlanName.system
//      )
    }
  }

  private fun seedRootUser(): UserEntity {
    val root =
      userDAO.findFirstByRootIsTrue() ?: createUser(
        propertyService.rootEmail,
        isRoot = true,
        authSource = AuthSource.none,
      )
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

    return root
  }

  private fun createAnonymousUser() = createUser(
    propertyService.anonymousEmail,
    isAnonymous = true,
    authSource = AuthSource.none,
  )

  private fun createUser(
    email: String,
    authSource: AuthSource,
    isRoot: Boolean = false,
    isAnonymous: Boolean = false
  ): UserEntity {
    if (userDAO.existsByEmail(email)) {
      throw BadRequestException("user already exists")
    }
    log.info("create internal user $email")
    val user = UserEntity()
    user.email = email
    user.root = isRoot
    user.product = ProductName.system
    user.anonymous = isAnonymous
    user.usesAuthSource = authSource
    user.hasAcceptedTerms = isRoot || isAnonymous
//    user.planId = planDAO.findByNameAndProduct(plan, ProductName.system)!!.id
    return userDAO.saveAndFlush(user)
  }

  private fun seedProductsAndPlans(root: UserEntity) {
    val systemProduct = resolveProduct(ProductName.system, root)
    persistPlan(
      PlanName.system, 0.0, PlanAvailability.unavailable, systemProduct, features = mapOf(
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

    if (!isSelfHosted()) {
      seedPlansForProduct(ProductName.feedless, root, systemProduct)
      seedPlansForProduct(ProductName.rssProxy, root, systemProduct)
      seedPlansForProduct(ProductName.visualDiff, root, systemProduct)
      seedPlansForProduct(ProductName.untoldNotes, root, systemProduct)
    }
  }

  private fun resolveProduct(
    productName: ProductName,
    owner: UserEntity,
    systemProduct: ProductEntity? = null
  ): ProductEntity {
    val product = ProductEntity()
    val name = productName.name
    product.name = name
    product.ownerId = owner.id
    product.parentProductId = systemProduct?.id

    return productDAO.findByNameAndOwnerId(name, owner.id) ?: productDAO.save(product)
  }

  private fun isSelfHosted() = environment.acceptsProfiles(Profiles.of(AppProfiles.selfHosted))

  private fun seedPlansForProduct(productName: ProductName, root: UserEntity, systemProduct: ProductEntity) {
    val product = resolveProduct(productName, root, systemProduct)
    persistPlan(
      PlanName.waitlist, 0.0, PlanAvailability.availableButHidden, product, features = mapOf(
        FeatureName.canLogin to asBoolFeature(false)
      )
    ).let {
      persistPlan(
        PlanName.free, 0.0, PlanAvailability.available, product, parentPlan = it, features = mapOf(
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
          FeatureName.canCreateUser to asBoolFeature(true), // wait list
          FeatureName.canSignUp to asBoolFeature(true),
          FeatureName.canCreateAsAnonymous to asBoolFeature(true),
          FeatureName.hasWaitList to asBoolFeature(true),
          FeatureName.itemEmailForwardBool to asBoolFeature(false),
          FeatureName.itemWebhookForwardBool to asBoolFeature(true),
        )
      )
//        .let {
//        persistPlan(
//          PlanName.basic, 9.99, PlanAvailability.by_request, product, parentPlan = it, features = mapOf(
//            FeatureName.rateLimitInt to asIntFeature(120),
//            FeatureName.minRefreshRateInMinutesInt to asIntFeature(10),
//            FeatureName.scrapeSourceRetentionMaxItemsInt to asIntFeature(100),
//            FeatureName.scrapeRequestTimeoutInt to asIntFeature(60000),
//            FeatureName.scrapeSourceMaxCountTotalInt to asIntFeature(30),
//            FeatureName.scrapeSourceMaxCountActiveInt to asIntFeature(30),
//            FeatureName.scrapeRequestActionMaxCountInt to asIntFeature(20),
//            FeatureName.scrapeRequestMaxCountPerSourceInt to asIntFeature(10),
//            FeatureName.publicScrapeSourceBool to asBoolFeature(true),
//            FeatureName.apiBool to asBoolFeature(true),
//            FeatureName.pluginsBool to asBoolFeature(true),
//            FeatureName.itemEmailForwardBool to asBoolFeature(true),
//            FeatureName.itemWebhookForwardBool to asBoolFeature(true)
//          )
//        )
//      }
    }
  }

  private fun persistPlan(
      name: PlanName,
      costs: Double,
      availability: PlanAvailability,
      product: ProductEntity,
      features: Map<FeatureName, FeatureValueEntity>,
      parentPlan: PlanEntity? = null
  ): PlanEntity {
    val plan = resolvePlan(name, costs, availability, product, parentPlan)
    persistFeatures(product, plan, features)
    return plan
  }

  private fun resolvePlan(
      name: PlanName,
      costs: Double,
      availability: PlanAvailability,
      product: ProductEntity,
      parentPlan: PlanEntity?,
  ): PlanEntity {
    val plan = PlanEntity()
    plan.name = name.name
    plan.productId = product.id
    plan.currentCosts = costs
    plan.availability = availability
    plan.parentPlanId = parentPlan?.id

    return planDAO.findByNameAndProductId(name.name, product.id) ?: planDAO.save(plan)
  }

  private fun persistFeatures(
      product: ProductEntity,
      plan: PlanEntity,
      features: Map<FeatureName, FeatureValueEntity>
  ) {
    features.forEach { (featureName, featureValue) ->
      run {
        val feature = featureDAO.findByProductIdAndName(
          product.id,
          featureName.name
        ) ?: createFeature(product, featureName)


        featureValueDAO.findByPlanIdAndFeatureId(plan.id, feature.id)
          ?.let {
            val logValueMismatch =
              { expected: Any, actual: Any -> log.warn("Feature Value Mismatch! Feature $featureName on ${plan.product}/${plan.name} expects ${expected}, actual $actual") }
            if (it.valueBoolean != featureValue.valueBoolean) {
              logValueMismatch(featureValue.valueBoolean!!, it.valueBoolean!!)
            }
            if (it.valueInt != featureValue.valueInt) {
              logValueMismatch(featureValue.valueInt!!, it.valueInt!!)
            }
            featureValueDAO.save(it)
          }
          ?: run {
            val value = FeatureValueEntity()
            value.planId = plan.id
            value.featureId = feature.id
            value.valueType = featureValue.valueType
            value.valueBoolean = featureValue.valueBoolean
            value.valueInt = featureValue.valueInt
            featureValueDAO.save(value)
          }
      }
    }
  }

  private fun createFeature(product: ProductEntity, featureName: FeatureName): FeatureEntity {
    val feature = FeatureEntity()
    feature.productId = product.id
    feature.name = featureName.name
    return featureDAO.save(feature)
  }

  private fun asIntFeature(value: Int): FeatureValueEntity {
    val feature = FeatureValueEntity()
    feature.valueType = FeatureValueType.number
    feature.valueInt = value
    return feature
  }

  private fun asBoolFeature(value: Boolean): FeatureValueEntity {
    val feature = FeatureValueEntity()
    feature.valueType = FeatureValueType.bool
    feature.valueBoolean = value
    return feature
  }

}
