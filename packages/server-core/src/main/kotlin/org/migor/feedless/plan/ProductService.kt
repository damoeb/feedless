package org.migor.feedless.plan

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppProfiles
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.migor.feedless.data.jpa.enums.fromDto
import org.migor.feedless.generated.types.ProductsWhereInput
import org.migor.feedless.subscription.PlanDAO
import org.migor.feedless.subscription.PlanEntity
import org.migor.feedless.user.UserEntity
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile("!test")
@Transactional
class ProductService {

  private val log = LoggerFactory.getLogger(ProductService::class.simpleName)

  @Autowired
  private lateinit var propertyService: PropertyService

  @Autowired
  private lateinit var productDAO: ProductDAO

  @Autowired
  private lateinit var planDAO: PlanDAO

  @Autowired
  private lateinit var pricedProductDAO: PricedProductDAO

  @Autowired
  private lateinit var environment: Environment

  fun getDomain(product: ProductCategory): String {
    return when (product) {
      ProductCategory.visualDiff -> "visualdiff.com"
      ProductCategory.pageChangeTracker -> "pagechangetracker.com"
      ProductCategory.rssProxy -> "rssproxy.migor.org"
      ProductCategory.feedless -> "feedless.org"
      ProductCategory.untoldNotes -> "notes.feedless.org"
      ProductCategory.upcoming -> "upcoming.feedless.org"
      ProductCategory.reader -> "reader.feedless.org"
      ProductCategory.feedDump -> "dump.feedless.org"
      else -> throw IllegalArgumentException("$product not supported")
    }
  }

  fun getAppUrl(product: ProductCategory): String {
    return if (isSelfHosted()) {
      propertyService.appHost
    } else {
      "https://${getDomain(product)}"
    }
  }

  fun getGatewayUrl(product: ProductCategory): String {
    return if (isSelfHosted() || isDev()) {
      propertyService.apiGatewayUrl
    } else {
      "https://api.${getDomain(product)}"
    }
  }

  fun isSelfHosted() = environment.acceptsProfiles(Profiles.of(AppProfiles.selfHosted))
  private fun isDev() = environment.acceptsProfiles(Profiles.of(AppProfiles.dev))

  suspend fun findAll(data: ProductsWhereInput): List<ProductEntity> {
    return withContext(Dispatchers.IO) {
      data.id?.equals?.let {
        listOf(productDAO.findById(UUID.fromString(it)).orElseThrow())
      } ?: productDAO.findAllByPartOfOrPartOfIsNull(data.category!!.fromDto())
    }
  }

  suspend fun resolvePriceForProduct(productId: UUID, existingUserId: UUID?): Double {
    val product = withContext(Dispatchers.IO) {
      productDAO.findById(productId).orElseThrow()
    }
    val prices = product.prices.filter { it.validTo?.let { it.time > System.currentTimeMillis() } ?: true }
      .filter { it.validFrom?.let { it.time < System.currentTimeMillis() } ?: true }

    return if (prices.size == 1) {
      prices[0].price
    } else {
      prices[0].price
      // todo Not yet implemented"
    }
  }

  suspend fun enableCloudProduct(corrId: String, product: ProductEntity, user: UserEntity, order: OrderEntity? = null) {

    val prices = withContext(Dispatchers.IO) {
      pricedProductDAO.findAllByProductId(product.id)
    }
    val isFree = { prices.any { it.price == 0.0 } }
    val isBought = { order?.isPaid == true }

    if (isFree() || isBought()) {

      // terminate existing plan
      val existingPlan = withContext(Dispatchers.IO) {
        planDAO.findActiveByUserAndProductIn(user.id, listOf(product.partOf!!))
      }

      existingPlan?.let {
        log.info("[$corrId] terminate existing plan")
        it.terminatedAt = Date()
        planDAO.save(it)
      }

      val plan = PlanEntity()
      plan.productId = product.id
      plan.userId = user.id
      plan.startedAt = Date()

      withContext(Dispatchers.IO) {
        planDAO.save(plan)
      }
    }
  }
}
