package org.migor.feedless.pricedProduct

import org.migor.feedless.product.PricedProduct
import org.migor.feedless.product.ProductId

interface PricedProductRepository {
  fun findAllByProductId(id: ProductId): List<PricedProduct>
  fun deleteAllByProductId(id: ProductId)
  fun saveAll(list: List<PricedProduct>)
}
