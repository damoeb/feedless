package org.migor.feedless.product

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.Vertical
import org.migor.feedless.api.fromDto
import org.migor.feedless.generated.types.ProductsWhereInput
import org.migor.feedless.order.Order
import org.migor.feedless.plan.Plan
import org.migor.feedless.plan.PlanRepository
import org.migor.feedless.pricedProduct.PricedProductRepository
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.migor.feedless.user.groupId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
@Profile("${AppProfiles.plan} & ${AppLayer.service}")
class ProductUseCaseImpl(
  private var productRepository: ProductRepository,
  private var userRepository: UserRepository,
  private var planRepository: PlanRepository,
  private var pricedProductRepository: PricedProductRepository
) : ProductUseCase {

  private val log = LoggerFactory.getLogger(ProductUseCaseImpl::class.simpleName)

  suspend fun findAll(data: ProductsWhereInput): List<Product> = withContext(Dispatchers.IO) {
    log.info("findAll")
    val products = data.id?.eq?.let {
      listOf(productRepository.findById(ProductId(it))!!)
    } ?: data.id?.`in`?.let { ids ->
      productRepository.findAllByIdIn(ids.map { ProductId(it) })
    } ?: data.vertical?.let {
      productRepository.findAllByPartOfOrPartOfIsNullAndAvailableTrue(data.vertical!!.fromDto())
    } ?: throw IllegalArgumentException("Insufficient filter params")

    products
  }

  override suspend fun resolvePriceForProduct(productId: ProductId, userId: UserId?): Double =
    withContext(Dispatchers.IO) {
      log.info("resolvePriceForProduct productId=$productId userId=$userId")
      val product = productRepository.findById(productId)!!

      val prices = product.prices(pricedProductRepository).filter { it.validTo?.isAfter(LocalDateTime.now()) ?: true }
        .filter { it.validFrom?.isBefore(LocalDateTime.now()) ?: true }

      if (prices.size == 1) {
        prices[0].price
      } else {
        prices[0].price
        // todo Not yet implemented"
      }
    }

  override suspend fun enableSaasProduct(
    product: Product,
    user: User,
    order: Order?
  ) = withContext(Dispatchers.IO) {

    log.info("enableSaasProduct ${product.name} (${product.id}) for user ${user.id}")
    val prices = pricedProductRepository.findAllByProductId(product.id)

    val isFree = { prices.any { it.price == 0.0 } }
    val isBought = { order?.isPaid == true }

    if (isFree() || isBought()) {

      // terminate existing plan
      val now = LocalDateTime.now()
      val existingPlan = planRepository.findActiveByUserAndProductIn(user.id, listOf(product.partOf!!), now)

      existingPlan?.let {
        log.info("terminate existing plan")
        planRepository.save(existingPlan.copy(terminatedAt = now))
      }

      log.info("enabling plan for product ${product.name} for user ${user.id}")
      val plan = Plan(
        productId = product.id,
        userId = user.id,
        startedAt = now,
        groupId = currentCoroutineContext().groupId()
      )
      planRepository.save(plan)
    }
  }

  override suspend fun enableDefaultSaasProduct(vertical: Vertical, userId: UserId) = withContext(Dispatchers.IO) {
    log.info("enableDefaultSaasProduct vertical=$vertical userId=$userId")
    val product = productRepository.findByPartOfAndBaseProductIsTrue(vertical)!!
    val user = userRepository.findById(userId)

    enableSaasProduct(product, user!!)
  }

  override suspend fun findAllByProductId(productId: ProductId): List<PricedProduct> = withContext(Dispatchers.IO) {
    log.info("findAllByProductId productId=$productId")
    pricedProductRepository.findAllByProductId(productId)
  }

  override suspend fun findById(productId: ProductId): Product? = withContext(Dispatchers.IO) {
    log.info("findById productId=$productId")
    productRepository.findById(productId)
  }
}

private fun Product.prices(pricedProductRepository: PricedProductRepository): List<PricedProduct> {
  return pricedProductRepository.findAllByProductId(id)
}
