package org.migor.feedless.pricedProduct

import org.migor.feedless.product.PricedProduct
import org.migor.feedless.product.ProductId

interface PricedProductRepository {
  suspend fun findAllByProductId(id: ProductId): List<PricedProduct>
  suspend fun deleteAllByProductId(id: ProductId)
  suspend fun saveAll(list: List<PricedProduct>)
}
