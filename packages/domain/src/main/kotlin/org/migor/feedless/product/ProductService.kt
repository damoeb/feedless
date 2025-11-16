package org.migor.feedless.product

import org.migor.feedless.Vertical
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId

interface ProductService {
  suspend fun resolvePriceForProduct(productId: ProductId, userId: UserId?): Double
  suspend fun enableDefaultSaasProduct(vertical: Vertical, id: UserId)
  fun enableSaasProduct(
    product: Product,
    user: User
  )
}
