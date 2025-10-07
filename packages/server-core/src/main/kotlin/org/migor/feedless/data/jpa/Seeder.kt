package org.migor.feedless.data.jpa

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.migor.feedless.AppProfiles
import org.migor.feedless.BadRequestException
import org.migor.feedless.Vertical
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.document.DocumentDAO
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.feature.FeatureGroupDAO
import org.migor.feedless.feature.FeatureGroupEntity
import org.migor.feedless.feature.FeatureName
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.feature.FeatureValueEntity
import org.migor.feedless.feature.FeatureValueType
import org.migor.feedless.feed.StandaloneFeedService
import org.migor.feedless.group.GroupDAO
import org.migor.feedless.group.GroupEntity
import org.migor.feedless.group.RoleInGroup
import org.migor.feedless.group.UserGroupAssignmentDAO
import org.migor.feedless.group.UserGroupAssignmentEntity
import org.migor.feedless.plan.PricedProductDAO
import org.migor.feedless.plan.PricedProductEntity
import org.migor.feedless.plan.ProductDAO
import org.migor.feedless.plan.ProductEntity
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.secrets.UserSecretDAO
import org.migor.feedless.secrets.UserSecretEntity
import org.migor.feedless.secrets.UserSecretType
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserEntity
import org.migor.feedless.util.CryptUtil
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
import java.util.*

@Service
@Order(1)
@Profile(AppProfiles.seed)
class Seeder(
  private val featureGroupDAO: FeatureGroupDAO,
  private val featureService: FeatureService,
  private val documentDAO: DocumentDAO,
  private val environment: Environment,
  private val propertyService: PropertyService,
  private val productDAO: ProductDAO,
  private val pricedProductDAO: PricedProductDAO,
  private val userSecretDAO: UserSecretDAO,
  private val repositoryDAO: RepositoryDAO,
  private val standaloneFeedService: StandaloneFeedService,
  private val userDAO: UserDAO,
  private val groupDAO: GroupDAO,
  private val userGroupAssignmentDAO: UserGroupAssignmentDAO,
) {

  private val log = LoggerFactory.getLogger(Seeder::class.simpleName)

  @PostConstruct
  @Transactional(propagation = Propagation.REQUIRED)
  fun onInit() {
    val root = seedRootUser()
    seedGroups(root)
    seedProducts(root)
    seedUsers()
  }

  private fun seedGroups(root: UserEntity) {
    val adminGroup = resolveGroup("")

    val ugLink = userGroupAssignmentDAO.findByUserIdAndGroupId(root.id, adminGroup.id) ?: UserGroupAssignmentEntity()
    ugLink.groupId = adminGroup.id
    ugLink.userId = root.id
    ugLink.role = RoleInGroup.owner

    userGroupAssignmentDAO.save(ugLink)
  }

  private fun resolveGroup(groupName: String): GroupEntity {
    return groupDAO.findByName(groupName) ?: createGroup(groupName)
  }

  private fun createGroup(groupName: String): GroupEntity {
    val adminGroup = GroupEntity()
    adminGroup.name = groupName
    return groupDAO.save(adminGroup)
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

    prepareStandaloneNotification(root)
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

  private fun prepareStandaloneNotification(root: UserEntity) {
    val standaloneFeedNotificationRepo = resolveStandaloneNotificationsRepo(root)

    if (environment.acceptsProfiles(Profiles.of(AppProfiles.saas))) {
      val deprecationMessageTitle = "SERVICE ANNOUNCEMENT: RSS-Proxy Feeds Deprecation Warning"
      val repositoryId = standaloneFeedNotificationRepo.id

      val title = "SERVICE ANNOUNCEMENT: About this feed"
      val notification =
        documentDAO.findByTitleInAndRepositoryId(listOf(title, deprecationMessageTitle), repositoryId) ?: run {
          val d = DocumentEntity()
          d.repositoryId = repositoryId
          d
        }

      val url = "https://github.com/damoeb/feedless/wiki/Messages-in-your-Feed#standalone-feed-urls"
      notification.url = url
      notification.title = title
      notification.status = ReleaseStatus.released
      notification.contentHash = CryptUtil.sha1(url)
      notification.text =
        "Dear user, this standalone feed URL is generated per request and comes with some limitations like fetch frequency and others."
      notification.html =
        "Dear user, this standalone feed URL is generated per request and <a href=\"https://github.com/damoeb/feedless/wiki/Messages-in-your-Feed#standalone-feed-urls\">comes with some limitations</a> like fetch frequency and others."
      notification.publishedAt = LocalDateTime.now() // force to top on most readers
      notification.updatedAt = LocalDateTime.now()

      documentDAO.save(notification)
    }
  }

  private fun resolveStandaloneNotificationsRepo(root: UserEntity): RepositoryEntity {
    return resolveOpsNotificationsRepo(standaloneFeedService.getRepoTitleForStandaloneFeedNotifications(), root)
  }

  private fun resolveOpsNotificationsRepo(repoTitle: String, root: UserEntity): RepositoryEntity {
    val repo = RepositoryEntity()
    repo.title = repoTitle
    repo.description = ""
    repo.shareKey = ""
    repo.ownerId = root.id
    repo.visibility = EntityVisibility.isPrivate
    repo.product = Vertical.feedless
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
    return userDAO.save(user)
  }

  private fun seedProducts(root: UserEntity) {
    val baseFeatureGroup = featureGroupDAO.findByParentFeatureGroupIdIsNull() ?: run {
      val group = FeatureGroupEntity()
      group.name = "feedless"
      featureGroupDAO.save(group)
    }

    runBlocking {
      featureService.assignFeatureValues(
        baseFeatureGroup, features = mapOf(
          FeatureName.requestPerMinuteUpperLimitInt to asIntFeature(40),
          FeatureName.refreshRateInMinutesLowerLimitInt to asIntFeature(5),
          FeatureName.publicRepositoryBool to asBoolFeature(false),
          FeatureName.pluginsBool to asBoolFeature(false),
          FeatureName.legacyApiBool to asBoolFeature(true),

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
    }

    if (isSelfHosted()) {
      runBlocking {
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
      }
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
        parentFeatureGroupId = baseFeatureGroup!!.id,
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
          FeatureName.scrapeRequestActionMaxCountInt to asIntFeature(10), // todo check
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
//          FeatureName.scrapeRequestActionMaxCountInt to asIntFeature(10), // todo check
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
    parentFeatureGroupId: UUID,
    features: Map<FeatureName, FeatureValueEntity>
  ): FeatureGroupEntity {
    log.info("resolveFeatureGroup $name")
    val group = featureGroupDAO.findByNameEqualsIgnoreCase(name) ?: run {
      val group = FeatureGroupEntity()
      group.name = name
      group.parentFeatureGroupId = parentFeatureGroupId
      featureGroupDAO.save(group)
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

  private fun createProduct(
    name: String,
    description: String,
    group: Vertical? = null,
    prices: List<PricedProductEntity>,
    parentFeatureGroupId: UUID,
    features: Map<FeatureName, FeatureValueEntity>,
    available: Boolean = true,
    saas: Boolean = false,
    isBaseProduct: Boolean = false,
    selfHostingIndividual: Boolean = false,
    selfHostingEnterprise: Boolean = false,
    selfHostingOther: Boolean = false,
  ): ProductEntity {

    val product = productDAO.findByNameEqualsIgnoreCase(name) ?: ProductEntity()

    product.name = name
    product.saas = saas
    product.available = available
    product.selfHostingIndividual = selfHostingIndividual
    product.selfHostingOther = selfHostingOther
    product.selfHostingEnterprise = selfHostingEnterprise
    product.description = description
    product.baseProduct = isBaseProduct
    product.partOf = group

    productDAO.save(product)

    if (saas) {
      product.featureGroupId = parentFeatureGroupId

      if (features.isNotEmpty()) {
        val featureGroup = resolveFeatureGroup(name, parentFeatureGroupId, features)
        product.featureGroupId = featureGroup.id
      }
      productDAO.save(product)
    }


    pricedProductDAO.deleteAllByProductId(product.id)
    pricedProductDAO.saveAll(prices.map {
      it.productId = product.id
      it
    })

    return product
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
