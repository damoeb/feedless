package org.migor.feedless.data.jpa

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.migor.feedless.AppProfiles
import org.migor.feedless.BadRequestException
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.document.DocumentDAO
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.feature.FeatureGroupDAO
import org.migor.feedless.feature.FeatureGroupEntity
import org.migor.feedless.feature.FeatureName
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.feature.FeatureValueEntity
import org.migor.feedless.feature.FeatureValueType
import org.migor.feedless.feed.LegacyFeedService
import org.migor.feedless.plan.PricedProductDAO
import org.migor.feedless.plan.PricedProductEntity
import org.migor.feedless.plan.ProductDAO
import org.migor.feedless.plan.ProductEntity
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.repository.RepositoryService
import org.migor.feedless.secrets.UserSecretDAO
import org.migor.feedless.secrets.UserSecretEntity
import org.migor.feedless.secrets.UserSecretType
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserEntity
import org.migor.feedless.util.CryptUtil
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
import java.time.temporal.ChronoUnit

@Service
@Order(1)
@Profile(AppProfiles.seed)
class Seeder {

  private val log = LoggerFactory.getLogger(Seeder::class.simpleName)

  @Autowired
  private lateinit var featureGroupDAO: FeatureGroupDAO

  @Autowired
  private lateinit var featureService: FeatureService

  @Autowired
  private lateinit var documentDAO: DocumentDAO

  @Autowired
  private lateinit var environment: Environment

  @Autowired
  private lateinit var propertyService: PropertyService

  @Autowired
  private lateinit var productDAO: ProductDAO

  @Autowired
  private lateinit var pricedProductDAO: PricedProductDAO

  @Autowired
  private lateinit var userSecretDAO: UserSecretDAO

  @Autowired
  private lateinit var repositoryDAO: RepositoryDAO

  @Autowired
  private lateinit var repositoryService: RepositoryService

  @Autowired
  private lateinit var legacyFeedService: LegacyFeedService

  @Autowired
  private lateinit var userDAO: UserDAO

  @PostConstruct
  @Transactional(propagation = Propagation.REQUIRED)
  fun onInit() {
    val root = seedRootUser()
    runBlocking {
      coroutineScope {
        seedProducts(root)
      }
    }
    seedUsers()
  }

  private fun seedUsers() {
    userDAO.findByEmail(propertyService.anonymousEmail) ?: createAnonymousUser()
  }

  private fun seedRootUser(): UserEntity {
    val root =
      userDAO.findFirstByAdminIsTrue() ?: createUser(
        propertyService.rootEmail,
        isRoot = true,
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
      userSecret.validUntil = LocalDateTime.now().plus(Duration.ofDays(356))
      userSecretDAO.save(userSecret)
    }

    pushLegacyNotifications(root)
//    pushFeedlessOpsNotifications(root)

    return root
  }

//  private fun pushFeedlessOpsNotifications(root: UserEntity) {
//    val feedlessOpsNotificationRepo = resolveFeedlessOpsNotificationsRepo(root)
//
//    if (environment.acceptsProfiles(Profiles.of(AppProfiles.saas))) {
//      val title = "Important Note: feedless 1 is online!"
//      val repositoryId = feedlessOpsNotificationRepo.id
//
//      val notification = documentDAO.findByTitleAndRepositoryId(title, repositoryId) ?: run {
//        val n = DocumentEntity()
//        n.repositoryId = repositoryId
//        n
//      }
//
//      notification.url = propertyService.appHost
//      notification.title = title
//      notification.status = ReleaseStatus.released
//      notification.text =
//        "Hi, I released a new version of feedless, that gives you a lot of new features."
//      notification.updatedAt = LocalDateTime.now()
//
//      documentDAO.save(notification)
//    }
//  }

  private fun pushLegacyNotifications(root: UserEntity) {
    val legacyNotificationRepo = resolveLegacyNotificationsRepo(root)

    if (environment.acceptsProfiles(Profiles.of(AppProfiles.saas))) {
      val title = "SERVICE ANNOUNCEMENT: RSS-Proxy Feeds Deprecation Warning"
      val repositoryId = legacyNotificationRepo.id

      val notification = documentDAO.findByTitleAndRepositoryId(title, repositoryId) ?: run {
        val d = DocumentEntity()
        d.repositoryId = repositoryId
        d
      }

      val url = "https://github.com/damoeb/feedless/wiki/Messages-in-your-Feed#rss-proxy-feeds-deprecation-warning"
      notification.url = url
      notification.title = title
      notification.status = ReleaseStatus.released
      notification.contentHash = CryptUtil.sha1(url)
      notification.text =
        "Dear user, please note that RSS-proxy feeds are now deprecated and will be deactivated in the future. We recommend creating a new feed at https://feedless.org, where you can import your RSS-proxy feeds."
      notification.html =
        "Dear user, please note that RSS-proxy feeds are now deprecated and will be deactivated in the future. We recommend creating a new feed at <a href-\"https://feedless.org\">feedless.org</a>, where you can import your RSS-proxy feeds."
      notification.publishedAt = LocalDateTime.now() // force to top on most readers
      notification.updatedAt = LocalDateTime.now()

      documentDAO.save(notification)
    }
  }

  private fun resolveLegacyNotificationsRepo(root: UserEntity): RepositoryEntity {
    val repoTitleLegacyNotifications = legacyFeedService.getRepoTitleForLegacyFeedNotifications()
    return resolveOpsNotificationsRepo(repoTitleLegacyNotifications, root)
  }

  private fun resolveFeedlessOpsNotificationsRepo(root: UserEntity): RepositoryEntity {
    val repoTitleOpsNotifications = repositoryService.getRepoTitleForFeedlessOpsNotifications()
    return resolveOpsNotificationsRepo(repoTitleOpsNotifications, root)
  }

  private fun resolveOpsNotificationsRepo(repoTitle: String, root: UserEntity): RepositoryEntity {
    val repo = RepositoryEntity()
    repo.title = repoTitle
    repo.description = ""
    repo.shareKey = ""
    repo.ownerId = root.id
    repo.visibility = EntityVisibility.isPrivate
    repo.product = ProductCategory.feedless
    repo.sourcesSyncCron = ""

    return repositoryDAO.findByTitleAndOwnerId(repoTitle, root.id) ?: repositoryDAO.save(repo)
  }

  private fun createAnonymousUser() = createUser(
    propertyService.anonymousEmail,
    isAnonymous = true,
  )

  private fun createUser(
    email: String,
    isRoot: Boolean = false,
    isAnonymous: Boolean = false
  ): UserEntity {
    if (userDAO.existsByEmail(email)) {
      throw BadRequestException("user already exists")
    }
    log.info("create internal user $email")
    val user = UserEntity()
    user.email = email
    user.admin = isRoot
    user.anonymous = isAnonymous
    user.hasAcceptedTerms = isRoot || isAnonymous
//    user.planId = planDAO.findByNameAndProduct(plan, ProductName.system)!!.id
    return userDAO.saveAndFlush(user)
  }

  private suspend fun seedProducts(root: UserEntity) {
    val baseFeatureGroup = withContext(Dispatchers.IO) {
      featureGroupDAO.findByParentFeatureGroupIdIsNull() ?: run {
        val group = FeatureGroupEntity()
        group.name = "feedless"
        featureGroupDAO.save(group)
      }
    }

    featureService.assignFeatureValues(
      baseFeatureGroup, features = mapOf(
        FeatureName.requestPerMinuteUpperLimitInt to asIntFeature(40),
        FeatureName.refreshRateInMinutesLowerLimitInt to asIntFeature(5),
        FeatureName.publicRepositoryBool to asBoolFeature(false),
        FeatureName.pluginsBool to asBoolFeature(false),
        FeatureName.legacyApiBool to asBoolFeature(true),
        FeatureName.legacyAnonymousFeedSupportEolInt to asIntFeature(1718265907060),

        FeatureName.repositoryCapacityLowerLimitInt to asIntFeature(0),
        FeatureName.repositoryCapacityUpperLimitInt to asIntFeature(0),
        FeatureName.repositoryRetentionMaxDaysLowerLimitInt to asIntFeature(0),

        FeatureName.scrapeRequestTimeoutMsecInt to asIntFeature(0),
        FeatureName.repositoriesMaxCountTotalInt to asIntFeature(0),
        FeatureName.repositoriesMaxCountActiveInt to asIntFeature(0),
        FeatureName.scrapeRequestActionMaxCountInt to asIntFeature(0),
        FeatureName.sourceMaxCountPerRepositoryInt to asIntFeature(0),

        FeatureName.canJoinPlanWaitList to asBoolFeature(false),
        FeatureName.canActivatePlan to asBoolFeature(false),
        FeatureName.canLogin to asBoolFeature(true),
        FeatureName.canSignUp to asBoolFeature(true),
        FeatureName.canCreateUser to asBoolFeature(true),

        FeatureName.itemEmailForwardBool to asBoolFeature(false),
        FeatureName.itemWebhookForwardBool to asBoolFeature(false),
      )
    )

    if (isSelfHosted()) {
      featureService.assignFeatureValues(
        baseFeatureGroup, features = mapOf(
//          FeatureName.requestPerMinuteUpperLimitInt to asIntFeature(40),
//          FeatureName.refreshRateInMinutesLowerLimitInt to asIntFeature(120),
          FeatureName.publicRepositoryBool to asBoolFeature(true),
          FeatureName.pluginsBool to asBoolFeature(true),
          FeatureName.legacyAnonymousFeedSupportEolInt to asIntFeature(null),
//          FeatureName.legacyApiBool to asBoolFeature(true),

          FeatureName.repositoryCapacityLowerLimitInt to asIntFeature(2),
          FeatureName.repositoryCapacityUpperLimitInt to asIntFeature(10000),
          FeatureName.repositoryRetentionMaxDaysLowerLimitInt to asIntFeature(2),

          FeatureName.scrapeRequestTimeoutMsecInt to asIntFeature(30000),
          FeatureName.repositoriesMaxCountTotalInt to asIntFeature(10000),
          FeatureName.repositoriesMaxCountActiveInt to asIntFeature(10000),
          FeatureName.scrapeRequestActionMaxCountInt to asIntFeature(5),
          FeatureName.sourceMaxCountPerRepositoryInt to asIntFeature(2),

//          FeatureName.hasWaitList to asBoolFeature(false),
          FeatureName.canLogin to asBoolFeature(true),
          FeatureName.canSignUp to asBoolFeature(true),
          FeatureName.canCreateUser to asBoolFeature(true),

//          FeatureName.itemEmailForwardBool to asBoolFeature(false),
//          FeatureName.itemWebhookForwardBool to asBoolFeature(false),
        )
      )
    } else {
      val feedlessFree = createProduct(
        "feedless Free",
        "Getting started",
        group = ProductCategory.feedless,
        isBaseProduct = true,
        saas = true,
        prices = listOf(
          createPricedProduct(
            unit = "Per Month",
            recurringInterval = ChronoUnit.MONTHS,
            price = 0.0
          ),
          createPricedProduct(
            unit = "Per Year",
            recurringInterval = ChronoUnit.YEARS,
            price = 0.0
          )
        ),
        parentFeatureGroup = baseFeatureGroup,
        features = mapOf(
//          FeatureName.requestPerMinuteUpperLimitInt to asIntFeature(40),
//          FeatureName.refreshRateInMinutesLowerLimitInt to asIntFeature(120),
//          FeatureName.publicRepositoryBool to asBoolFeature(false),
          FeatureName.pluginsBool to asBoolFeature(true),

          FeatureName.repositoryCapacityLowerLimitInt to asIntFeature(2),
          FeatureName.repositoryCapacityUpperLimitInt to asIntFeature(1000),
          FeatureName.repositoryRetentionMaxDaysLowerLimitInt to asIntFeature(7),

          FeatureName.scrapeRequestTimeoutMsecInt to asIntFeature(30000),
          FeatureName.repositoriesMaxCountTotalInt to asIntFeature(500),
          FeatureName.repositoriesMaxCountActiveInt to asIntFeature(500),
          FeatureName.scrapeRequestActionMaxCountInt to asIntFeature(10), // todo check
          FeatureName.sourceMaxCountPerRepositoryInt to asIntFeature(10),

//          FeatureName.hasWaitList to asBoolFeature(false),
          FeatureName.canActivatePlan to asBoolFeature(true),

//          FeatureName.itemEmailForwardBool to asBoolFeature(false),
//          FeatureName.itemWebhookForwardBool to asBoolFeature(false),
        )
      )


      createProduct(
        "feedless Pro",
        "Getting serious",
        group = ProductCategory.feedless,
        saas = true,
        selfHostingIndividual = true,
        selfHostingEnterprise = true,
        selfHostingOther = true,
        parentFeatureGroup = feedlessFree.featureGroup!!,
        prices = listOf(
          createPricedProduct(
            unit = "Per Month",
            recurringInterval = ChronoUnit.MONTHS,
            price = 9.9
          ),
          createPricedProduct(
            unit = "1st Year",
            recurringInterval = ChronoUnit.YEARS,
            price = 99.0
          ),
          createPricedProduct(
            unit = "2nd Year",
            recurringInterval = ChronoUnit.YEARS,
            price = 69.0
          ),
          createPricedProduct(
            unit = "3rd Year onward",
            recurringInterval = ChronoUnit.YEARS,
            price = 49.0
          )
        ),
        features = mapOf(
//          FeatureName.requestPerMinuteUpperLimitInt to asIntFeature(40),
//          FeatureName.refreshRateInMinutesLowerLimitInt to asIntFeature(120),
//          FeatureName.publicRepositoryBool to asBoolFeature(false),
          FeatureName.pluginsBool to asBoolFeature(true),

          FeatureName.repositoryCapacityLowerLimitInt to asIntFeature(2),
          FeatureName.repositoryCapacityUpperLimitInt to asIntFeature(1000),
          FeatureName.repositoryRetentionMaxDaysLowerLimitInt to asIntFeature(7),

          FeatureName.scrapeRequestTimeoutMsecInt to asIntFeature(30000),
          FeatureName.repositoriesMaxCountTotalInt to asIntFeature(500),
          FeatureName.repositoriesMaxCountActiveInt to asIntFeature(500),
          FeatureName.scrapeRequestActionMaxCountInt to asIntFeature(10), // todo check
          FeatureName.sourceMaxCountPerRepositoryInt to asIntFeature(10),

//          FeatureName.hasWaitList to asBoolFeature(false),
          FeatureName.canActivatePlan to asBoolFeature(true),
        )
      )

    }
  }

  private suspend fun resolveFeatureGroup(
    name: String,
    parentFeatureGroup: FeatureGroupEntity?,
    features: Map<FeatureName, FeatureValueEntity>
  ): FeatureGroupEntity {
    val group = withContext(Dispatchers.IO) {
      featureGroupDAO.findByNameEqualsIgnoreCase(name) ?: run {
        val group = FeatureGroupEntity()
        group.name = name
        group.parentFeatureGroupId = parentFeatureGroup?.id
        featureGroupDAO.save(group)
      }
    }

    featureService.assignFeatureValues(group, features)
    return group
  }

  private fun createPricedProduct(
    inStock: Int? = null,
    unit: String,
    price: Double,
    recurringInterval: ChronoUnit
  ): PricedProductEntity {
    val priced = PricedProductEntity()
    priced.inStock = inStock
    priced.price = price
    priced.unit = unit
    priced.recurringInterval = recurringInterval

    return priced
  }

  private suspend fun createProduct(
    name: String,
    description: String,
    group: ProductCategory? = null,
    prices: List<PricedProductEntity>,
    parentFeatureGroup: FeatureGroupEntity? = null,
    features: Map<FeatureName, FeatureValueEntity>? = null,
    saas: Boolean = false,
    isBaseProduct: Boolean = false,
    selfHostingIndividual: Boolean = false,
    selfHostingEnterprise: Boolean = false,
    selfHostingOther: Boolean = false,
    ): ProductEntity {

    return withContext(Dispatchers.IO) {
      val product = productDAO.findByNameEqualsIgnoreCase(name) ?: run {
        val product = ProductEntity()
        product.name = name
        product.saas = saas
        product.selfHostingIndividual = selfHostingIndividual
        product.selfHostingOther = selfHostingOther
        product.selfHostingEnterprise = selfHostingEnterprise
        product.description = description
        product.baseProduct = isBaseProduct
        product.partOf = group

        productDAO.save(product)
      }

      features?.let {
        val featureGroup = resolveFeatureGroup(name, parentFeatureGroup, features)
        product.featureGroupId = featureGroup.id
        product.featureGroup = featureGroup
        if (!saas) {
          throw IllegalArgumentException("featureGroup is set, while saas is not")
        }
        productDAO.save(product)
      }

      pricedProductDAO.deleteAllByProductId(product.id)
      pricedProductDAO.saveAll(prices.map {
        it.productId = product.id
        it
      })

      product
    }
  }

  private fun isSelfHosted() = environment.acceptsProfiles(Profiles.of(AppProfiles.selfHosted))

  //

  private fun asIntFeature(value: Long?): FeatureValueEntity {
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
