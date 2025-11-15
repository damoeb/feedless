package org.migor.feedless.plan

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.Vertical
import org.migor.feedless.api.fromDto
import org.migor.feedless.data.jpa.order.OrderEntity
import org.migor.feedless.data.jpa.plan.PlanDAO
import org.migor.feedless.data.jpa.plan.PlanEntity
import org.migor.feedless.data.jpa.pricedProduct.PricedProductDAO
import org.migor.feedless.data.jpa.pricedProduct.PricedProductEntity
import org.migor.feedless.data.jpa.product.ProductDAO
import org.migor.feedless.data.jpa.product.ProductEntity
import org.migor.feedless.data.jpa.user.UserDAO
import org.migor.feedless.data.jpa.user.UserEntity
import org.migor.feedless.generated.types.ProductsWhereInput
import org.migor.feedless.user.UserId
import org.migor.feedless.user.corrId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import kotlin.coroutines.coroutineContext

data class ProductId(val value: UUID) {
  constructor(value: String) : this(UUID.fromString(value))
}

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.plan} & ${AppLayer.service}")
class ProductService(
  private var productDAO: ProductDAO,
  private var userDAO: UserDAO,
  private var planDAO: PlanDAO,
  private var pricedProductDAO: PricedProductDAO
) {

  private val log = LoggerFactory.getLogger(ProductService::class.simpleName)

  @Transactional(readOnly = true)
  suspend fun findAll(data: ProductsWhereInput): List<ProductEntity> {
    return withContext(Dispatchers.IO) {
      data.id?.eq?.let {
        listOf(productDAO.findById(UUID.fromString(it)).orElseThrow())
      } ?: data.id?.`in`?.let { ids ->
        productDAO.findAllByIdIn(ids.map { UUID.fromString(it) })
      } ?: data.vertical?.let {
        productDAO.findAllByPartOfOrPartOfIsNullAndAvailableTrue(data.vertical!!.fromDto())
      } ?: throw IllegalArgumentException("Insufficient filter params")
    }
  }

  @Transactional(readOnly = true)
  suspend fun resolvePriceForProduct(productId: ProductId, existingUserId: UserId?): Double {
    val product = withContext(Dispatchers.IO) {
      productDAO.findById(productId.value).orElseThrow()
    }
    val prices = product.prices.filter { it.validTo?.isAfter(LocalDateTime.now()) ?: true }
      .filter { it.validFrom?.isBefore(LocalDateTime.now()) ?: true }

    return if (prices.size == 1) {
      prices[0].price
    } else {
      prices[0].price
      // todo Not yet implemented"
    }
  }

  @Transactional
  // todo thats bad transactional code
  suspend fun enableSaasProduct(product: ProductEntity, user: UserEntity, order: OrderEntity? = null) {

    log.error("enableSaasProduct WAAAAAAAAAAAAAAAAAAAAAAAAAA")
    val prices = withContext(Dispatchers.IO) {
      pricedProductDAO.findAllByProductId(product.id)
    }
    val isFree = { prices.any { it.price == 0.0 } }
    val isBought = { order?.isPaid == true }

    if (isFree() || isBought()) {

      // terminate existing plan
      val now = LocalDateTime.now()
      val existingPlan = withContext(Dispatchers.IO) {
        planDAO.findActiveByUserAndProductIn(user.id, listOf(product.partOf!!), now)
      }

      existingPlan?.let {
        log.info("[${coroutineContext.corrId()}] terminate existing plan")
        it.terminatedAt = now
        planDAO.save(it)
      }

      log.info("[${coroutineContext.corrId()}] enabling plan for product ${product.name} for user ${user.id}")
      val plan = PlanEntity()
      plan.productId = product.id
      plan.userId = user.id
      plan.startedAt = now

      withContext(Dispatchers.IO) {
        planDAO.save(plan)
      }
    }
  }

  @Transactional
  suspend fun enableDefaultSaasProduct(vertical: Vertical, userId: UserId) {
    val product = withContext(Dispatchers.IO) { productDAO.findByPartOfAndBaseProductIsTrue(vertical)!! }
    val user = withContext(Dispatchers.IO) { userDAO.findById(userId.value).orElseThrow() }

    enableSaasProduct(product, user)
  }

  @Transactional(readOnly = true)
  suspend fun findAllByProductId(productId: ProductId): List<PricedProductEntity> {
    return withContext(Dispatchers.IO) {
      pricedProductDAO.findAllByProductId(productId.value)
    }
  }
}
