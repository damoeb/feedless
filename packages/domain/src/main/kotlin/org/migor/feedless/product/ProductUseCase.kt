package org.migor.feedless.product

import org.migor.feedless.Vertical
import org.migor.feedless.order.Order
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId

interface ProductUseCase {
  suspend fun resolvePriceForProduct(productId: ProductId, userId: UserId?): Double
  suspend fun enableDefaultSaasProduct(vertical: Vertical, userId: UserId)
  suspend fun enableSaasProduct(
    product: Product,
    user: User,
    order: Order? = null
  )

  suspend fun findAllByProductId(productId: ProductId): List<PricedProduct>
  suspend fun findById(productId: ProductId): Product?
}
