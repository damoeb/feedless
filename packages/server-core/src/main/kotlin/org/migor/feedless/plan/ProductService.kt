package org.migor.feedless.plan

import org.migor.feedless.AppProfiles
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.migor.feedless.data.jpa.enums.fromDto
import org.migor.feedless.generated.types.ProductsWhereInput
import org.migor.feedless.subscription.PlanDAO
import org.migor.feedless.subscription.PlanEntity
import org.migor.feedless.user.UserEntity
import org.migor.feedless.user.UserDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("!test")
class ProductService {

  private val log = LoggerFactory.getLogger(ProductService::class.simpleName)

  @Autowired
  private lateinit var propertyService: PropertyService

  @Autowired
  private lateinit var productDAO: ProductDAO

  @Autowired
  private lateinit var planDAO: PlanDAO

  @Autowired
  private lateinit var userDAO: UserDAO

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
    return if (isSelfHosted()) {
      propertyService.apiGatewayUrl
    } else {
      "https://api.${getDomain(product)}"
    }
  }

  fun isSelfHosted() = environment.acceptsProfiles(Profiles.of(AppProfiles.selfHosted))

  fun findAll(data: ProductsWhereInput): List<ProductEntity> {
    return data.id?.equals?.let {
      listOf(productDAO.findById(UUID.fromString(it)).orElseThrow())
    } ?: productDAO.findAllByPartOfOrPartOfIsNull(data.category!!.fromDto())
  }

  fun resolvePriceForProduct(productId: UUID, existingUserId: UUID?): Double {
    val product = productDAO.findById(productId).orElseThrow()
    val prices = product.prices.filter { it.validTo?.let { it.time > System.currentTimeMillis() } ?: true }
      .filter { it.validFrom?.let { it.time < System.currentTimeMillis() } ?: true }

    return if (prices.size == 1) {
      prices[0].price
    } else {
      prices[0].price
      // todo Not yet implemented"
    }
  }

  fun enableCloudProduct(corrId: String, product: ProductEntity, user: UserEntity, order: OrderEntity? = null) {
    val isFree = { product.prices.any { it.price == 0.0 } }
    val isBought = { order?.isPaid == true }

    if (isFree() || isBought() ) {

      // terminate existing plan
      planDAO.findActiveByUserAndProduct(user.id, product.partOf!!)?.let {
        log.info("[$corrId]")
        it.terminatedAt = Date()
        planDAO.save(it)
      } ?: log.info("[$corrId]")

      val plan = PlanEntity()
      plan.productId = product.id
      plan.userId = user.id
      plan.startedAt = Date()

      planDAO.save(plan)
    }
  }
}
