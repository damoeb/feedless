package org.migor.feedless.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.migor.feedless.AppProfiles
import org.migor.feedless.BadRequestException
import org.migor.feedless.Vertical
import org.migor.feedless.common.PropertyService
import org.migor.feedless.feature.FeatureGroup
import org.migor.feedless.feature.FeatureGroupId
import org.migor.feedless.feature.FeatureGroupRepository
import org.migor.feedless.feature.FeatureId
import org.migor.feedless.feature.FeatureName
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.feature.FeatureValue
import org.migor.feedless.feature.FeatureValueType
import org.migor.feedless.group.Group
import org.migor.feedless.group.GroupRepository
import org.migor.feedless.pricedProduct.PricedProductRepository
import org.migor.feedless.product.PricedProduct
import org.migor.feedless.product.Product
import org.migor.feedless.product.ProductRepository
import org.migor.feedless.user.User
import org.migor.feedless.user.UserRepository
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.migor.feedless.userSecret.UserSecret
import org.migor.feedless.userSecret.UserSecretRepository
import org.migor.feedless.userSecret.UserSecretType
import org.slf4j.LoggerFactory
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
class Seeder(
  private val featureGroupRepository: FeatureGroupRepository,
  private val featureService: FeatureService,
  private val environment: Environment,
  private val propertyService: PropertyService,
  private val productRepository: ProductRepository,
  private val pricedProductRepository: PricedProductRepository,
  private val userSecretRepository: UserSecretRepository,
  private val userRepository: UserRepository,
  private val groupRepository: GroupRepository,
  private val userGroupAssignmentRepository: UserGroupAssignmentRepository,
) {

  private val log = LoggerFactory.getLogger(Seeder::class.simpleName)

  @OptIn(ExperimentalCoroutinesApi::class)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun seed() {
    runCatching {
      val root = seedRootUser()
      seedGroups(root)
      runBlocking {
        seedProducts(root)
      }
      seedUsers()
    }.onFailure { log.error("Seed failed with ${it.message}", it) }
  }

  private fun seedGroups(root: User) {
    val adminGroup = resolveGroup("", root)

    userGroupAssignmentRepository.findByUserIdAndGroupId(root.id, adminGroup.id) ?: userGroupAssignmentRepository.save(
      UserGroupAssignment(
        groupId = adminGroup.id,
        userId = root.id,
        role = RoleInGroup.owner
      )
    )

  }

  private fun resolveGroup(groupName: String, owner: User): Group {
    return groupRepository.findByName(groupName) ?: createGroup(groupName, owner)
  }

  private fun createGroup(groupName: String, owner: User): Group {
    return groupRepository.save(
      Group(
        name = groupName,
        ownerId = owner.id
      )
    )
  }

  private fun seedUsers() {
    userRepository.findByEmail(propertyService.anonymousEmail) ?: createAnonymousUser()
  }

  private fun seedRootUser(): User {
    val root = userRepository.findFirstByAdminIsTrue() ?: createUser(
      propertyService.rootEmail,
      isRoot = true,
    )
    if (root.email != propertyService.rootEmail) {
      log.info("Updated rootEmail")
      userRepository.save(
        root.copy(
          email = propertyService.rootEmail
        )
      )
    }

    if (!userSecretRepository.existsByValueAndOwnerId(propertyService.rootSecretKey, root.id)) {
      log.info("created secretKey for root")
      userSecretRepository.save(
        UserSecret(
          ownerId = root.id,
          value = propertyService.rootSecretKey,
          type = UserSecretType.SecretKey,
          validUntil = LocalDateTime.now().plus(Duration.ofDays(356))
        )
      )
    }

//    pushFeedlessOpsNotifications(root)

    return root
  }

//  private fun pushFeedlessOpsNotifications(root: User) {
//    val feedlessOpsNotificationRepo = resolveFeedlessOpsNotificationsRepo(root)
//
//    if (environment.acceptsProfiles(Profiles.of(AppProfiles.saas))) {
//      val title = "Important Note: feedless 1 is online!"
//      val repositoryId = feedlessOpsNotificationRepo.id
//
//      val notification = documentDAO.findByTitleAndRepositoryId(title, repositoryId) ?: run {
//        val n = Document()
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

//  private fun prepareStandaloneNotification(root: User) {
//    val standaloneFeedNotificationRepo = resolveStandaloneNotificationsRepo(root)
//
//    if (environment.acceptsProfiles(Profiles.of(AppProfiles.saas))) {
//      val deprecationMessageTitle = "SERVICE ANNOUNCEMENT: RSS-Proxy Feeds Deprecation Warning"
//      val repositoryId = standaloneFeedNotificationRepo.id
//
//      val title = "SERVICE ANNOUNCEMENT: About this feed"
//      val notification =
//        documentDAO.findByTitleInAndRepositoryId(listOf(title, deprecationMessageTitle), repositoryId) ?: run {
//          val d = Document()
//          d.repositoryId = repositoryId
//          d
//        }
//
//      val url = "https://github.com/damoeb/feedless/wiki/Messages-in-your-Feed#standalone-feed-urls"
//      notification.url = url
//      notification.title = title
//      notification.status = ReleaseStatus.released
//      notification.contentHash = CryptUtil.sha1(url)
//      notification.text =
//        "Dear user, this standalone feed URL is generated per request and comes with some limitations like fetch frequency and others."
//      notification.html =
//        "Dear user, this standalone feed URL is generated per request and <a href=\"https://github.com/damoeb/feedless/wiki/Messages-in-your-Feed#standalone-feed-urls\">comes with some limitations</a> like fetch frequency and others."
//      notification.publishedAt = LocalDateTime.now() // force to top on most readers
//      notification.updatedAt = LocalDateTime.now()
//
//      documentDAO.save(notification)
//    }
//  }

  private fun createAnonymousUser() = createUser(
    propertyService.anonymousEmail,
    isAnonymous = true,
  )

  private fun createUser(
    email: String,
    isRoot: Boolean = false,
    isAnonymous: Boolean = false
  ): User {
    if (userRepository.existsByEmail(email)) {
      throw BadRequestException("user already exists")
    }
    log.info("create internal user $email")
    val user = User(
      email = email,
      admin = isRoot,
      anonymous = isAnonymous,
      hasAcceptedTerms = isRoot || isAnonymous,
      lastLogin = LocalDateTime.now(),
    )
//    user.planId = planDAO.findByNameAndProduct(plan, ProductName.system)!!.id
    return userRepository.save(user)
  }

  private suspend fun seedProducts(root: User) = withContext(Dispatchers.IO) {
    val baseFeatureGroup = featureGroupRepository.findByParentFeatureGroupIdIsNull() ?: featureGroupRepository.save(
      FeatureGroup(
        name = "feedless"
      )
    )

    featureService.assignFeatureValues(
      baseFeatureGroup, features = mapOf(
        FeatureName.requestPerMinuteUpperLimitInt to asIntFeature(40),
        FeatureName.refreshRateInMinutesLowerLimitInt to asIntFeature(5),
        FeatureName.publicRepositoryBool to asBoolFeature(false),
        FeatureName.pluginsBool to asBoolFeature(false),
        FeatureName.legacyFeedApiBool to asBoolFeature(true),

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
        group = Vertical.feedless,
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
        parentFeatureGroupId = baseFeatureGroup.id,
        features = mapOf(
//          FeatureName.requestPerMinuteUpperLimitInt to asIntFeature(40),
//          FeatureName.refreshRateInMinutesLowerLimitInt to asIntFeature(120),
//          FeatureName.publicRepositoryBool to asBoolFeature(false),
          FeatureName.pluginsBool to asBoolFeature(true),

          FeatureName.repositoryCapacityLowerLimitInt to asIntFeature(2),
          FeatureName.repositoryCapacityUpperLimitInt to asIntFeature(300),
          FeatureName.repositoryRetentionMaxDaysLowerLimitInt to asIntFeature(7),

          FeatureName.scrapeRequestTimeoutMsecInt to asIntFeature(30000),
          FeatureName.repositoriesMaxCountTotalInt to asIntFeature(500),
          FeatureName.repositoriesMaxCountActiveInt to asIntFeature(500),
          FeatureName.scrapeRequestActionMaxCountInt to asIntFeature(10),
          FeatureName.sourceMaxCountPerRepositoryInt to asIntFeature(10),

//          FeatureName.hasWaitList to asBoolFeature(false),
          FeatureName.canActivatePlan to asBoolFeature(true),
//          FeatureName.canLoginWithCredentials to asBoolFeature(true),
//          FeatureName.signUpRequiresInvitationKeyOnly to asBoolFeature(true),

//          FeatureName.itemEmailForwardBool to asBoolFeature(false),
//          FeatureName.itemWebhookForwardBool to asBoolFeature(false),
        )
      )

//      val feedlessSupporter = createProduct(
//        "feedless Supporter",
//        "Getting Started",
//        group = Vertical.feedless,
//        isBaseProduct = false,
//        saas = true,
//        prices = listOf(
//          createPricedProduct(
//            unit = "Per Month",
//            recurringInterval = ChronoUnit.MONTHS,
//            price = 4.9
//          ),
//          createPricedProduct(
//            unit = "Per Year",
//            recurringInterval = ChronoUnit.YEARS,
//            price = 49.0
//          )
//        ),
//        parentFeatureGroupId = feedlessFree.featureGroupId,
//        features = emptyMap()
//      )


      createProduct(
        "feedless Pro",
        "Getting serious",
        group = Vertical.feedless,
        saas = true,
        available = false,
        selfHostingIndividual = true,
        selfHostingEnterprise = true,
        selfHostingOther = true,
        parentFeatureGroupId = feedlessFree.featureGroupId,
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
          FeatureName.publicRepositoryBool to asBoolFeature(true),
//          FeatureName.pluginsBool to asBoolFeature(true),
//
//          FeatureName.repositoryCapacityLowerLimitInt to asIntFeature(2),
          FeatureName.repositoryCapacityUpperLimitInt to asIntFeature(10000),
//          FeatureName.repositoryRetentionMaxDaysLowerLimitInt to asIntFeature(7),

//          FeatureName.scrapeRequestTimeoutMsecInt to asIntFeature(30000),
//          FeatureName.repositoriesMaxCountTotalInt to asIntFeature(500),
//          FeatureName.repositoriesMaxCountActiveInt to asIntFeature(500),
//          FeatureName.scrapeRequestActionMaxCountInt to asIntFeature(10),
          FeatureName.sourceMaxCountPerRepositoryInt to asIntFeature(1000),

//          FeatureName.hasWaitList to asBoolFeature(false),
//          FeatureName.canActivatePlan to asBoolFeature(true),
//          FeatureName.canLoginWithCredentials to asBoolFeature(true),
//          FeatureName.signUpRequiresInvitationKeyOnly to asBoolFeature(true),
        )
      )

    }
  }

  private fun resolveFeatureGroup(
    name: String,
    parentFeatureGroupId: FeatureGroupId?,
    features: Map<FeatureName, FeatureValue>
  ): FeatureGroup {
    log.info("resolveFeatureGroup $name")
    val group = featureGroupRepository.findByNameEqualsIgnoreCase(name) ?: featureGroupRepository.save(
      FeatureGroup(
        name = name,
        parentFeatureGroupId = parentFeatureGroupId
      )
    )

    runBlocking {
      featureService.assignFeatureValues(group, features)
    }
    return group
  }

  private fun createPricedProduct(
    inStock: Int? = null,
    unit: String,
    price: Double,
    recurringInterval: ChronoUnit
  ): PricedProduct {
    return PricedProduct(
      inStock = inStock,
      price = price,
      unit = unit,
      recurringInterval = recurringInterval
    )
  }

  private fun createProduct(
    name: String,
    description: String,
    group: Vertical? = null,
    prices: List<PricedProduct>,
    parentFeatureGroupId: FeatureGroupId?,
    features: Map<FeatureName, FeatureValue>,
    available: Boolean = true,
    saas: Boolean = false,
    isBaseProduct: Boolean = false,
    selfHostingIndividual: Boolean = false,
    selfHostingEnterprise: Boolean = false,
    selfHostingOther: Boolean = false,
  ): Product {

    val product = productRepository.findByNameEqualsIgnoreCase(name) ?: Product(
      name = name,
      saas = saas,
      available = available,
      selfHostingIndividual = selfHostingIndividual,
      selfHostingOther = selfHostingOther,
      selfHostingEnterprise = selfHostingEnterprise,
      description = description,
      baseProduct = isBaseProduct,
      partOf = group
    )

    if (saas) {
      productRepository.save(
        if (features.isNotEmpty()) {
          val featureGroup = resolveFeatureGroup(name, parentFeatureGroupId, features)
          product.copy(featureGroupId = featureGroup.id)
        } else {
          product.copy(featureGroupId = parentFeatureGroupId)
        }
      )
    } else {
      productRepository.save(product)
    }

    pricedProductRepository.deleteAllByProductId(product.id)
    pricedProductRepository.saveAll(prices.map {
      it.copy(productId = product.id)
    })

    return product
  }

  private fun isSelfHosted() = environment.acceptsProfiles(Profiles.of(AppProfiles.selfHosted))

  //

  private fun asIntFeature(value: Long?): FeatureValue {
    return FeatureValue(
      valueType = FeatureValueType.number,
      valueInt = value,
      featureGroupId = FeatureGroupId(),
      featureId = FeatureId(),
    )
  }

  private fun asBoolFeature(value: Boolean): FeatureValue {
    return FeatureValue(
      valueType = FeatureValueType.bool,
      valueBoolean = value,
      featureGroupId = FeatureGroupId(),
      featureId = FeatureId(),
    )
  }

}
