package org.migor.feedless.product

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.Vertical
import org.migor.feedless.api.fromDto
import org.migor.feedless.data.jpa.plan.PlanDAO
import org.migor.feedless.data.jpa.plan.PlanEntity
import org.migor.feedless.data.jpa.pricedProduct.PricedProductDAO
import org.migor.feedless.data.jpa.product.ProductDAO
import org.migor.feedless.data.jpa.product.toDomain
import org.migor.feedless.data.jpa.user.UserDAO
import org.migor.feedless.data.jpa.user.toDomain
import org.migor.feedless.generated.types.ProductsWhereInput
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.plan} & ${AppLayer.service}")
class ProductServiceImpl(
    private var productDAO: ProductDAO,
    private var userDAO: UserDAO,
    private var planDAO: PlanDAO,
    private var pricedProductDAO: PricedProductDAO
) : ProductService {

    private val log = LoggerFactory.getLogger(ProductServiceImpl::class.simpleName)

    @Transactional(readOnly = true)
    suspend fun findAll(data: ProductsWhereInput): List<Product> {
        return withContext(Dispatchers.IO) {
            val products = data.id?.eq?.let {
                listOf(productDAO.findById(UUID.fromString(it)).orElseThrow())
            } ?: data.id?.`in`?.let { ids ->
                productDAO.findAllByIdIn(ids.map { UUID.fromString(it) })
            } ?: data.vertical?.let {
                productDAO.findAllByPartOfOrPartOfIsNullAndAvailableTrue(data.vertical!!.fromDto())
            } ?: throw IllegalArgumentException("Insufficient filter params")
            products.map { it.toDomain() }
        }
    }

    @Transactional(readOnly = true)
    override suspend fun resolvePriceForProduct(productId: ProductId, existingUserId: UserId?): Double {
        val product = withContext(Dispatchers.IO) {
            productDAO.findById(productId.uuid).orElseThrow()
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
    override suspend fun enableSaasProduct(
        product: Product,
        user: User,
        order: org.migor.feedless.order.Order?
    ) {

        log.error("enableSaasProduct")
        val prices = withContext(Dispatchers.IO) {
            pricedProductDAO.findAllByProductId(product.id.uuid)
        }
        val isFree = { prices.any { it.price == 0.0 } }
        val isBought = { order?.isPaid == true }

        if (isFree() || isBought()) {

            // terminate existing plan
            val now = LocalDateTime.now()
            val existingPlan = withContext(Dispatchers.IO) {
                planDAO.findActiveByUserAndProductIn(user.id.uuid, listOf(product.partOf!!), now)
            }

            existingPlan?.let {
                log.info("terminate existing plan")
                it.terminatedAt = now
                planDAO.save(it)
            }

            log.info("enabling plan for product ${product.name} for user ${user.id}")
            val plan = PlanEntity()
            plan.productId = product.id.uuid
            plan.userId = user.id.uuid
            plan.startedAt = now

            withContext(Dispatchers.IO) {
                planDAO.save(plan)
            }
        }
    }

    @Transactional
    override suspend fun enableDefaultSaasProduct(vertical: Vertical, userId: UserId) {
        val product = withContext(Dispatchers.IO) { productDAO.findByPartOfAndBaseProductIsTrue(vertical)!! }
        val user = withContext(Dispatchers.IO) { userDAO.findById(userId.uuid).orElseThrow() }

        enableSaasProduct(product.toDomain(), user.toDomain())
    }

    @Transactional(readOnly = true)
    override suspend fun findAllByProductId(productId: ProductId): List<PricedProduct> {
        return withContext(Dispatchers.IO) {
            pricedProductDAO.findAllByProductId(productId.uuid)
                .map { PricedProductMapper.INSTANCE.toDomain(it) }
        }
    }

    override suspend fun findById(productId: ProductId): Product? {
        return withContext(Dispatchers.IO) {
            productDAO.findById(productId.uuid).getOrNull()?.toDomain()
        }

    }
}
