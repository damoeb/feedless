package org.migor.feedless.product

import org.migor.feedless.Vertical
import java.util.*

interface ProductRepository {
  suspend fun findByNameEqualsIgnoreCase(name: String): Product?
  suspend fun findByPartOfAndBaseProductIsTrue(name: Vertical): Product?
  suspend fun findAllByPartOfOrPartOfIsNullAndAvailableTrue(category: Vertical): List<Product>
  suspend fun findAllByIdIn(ids: List<UUID>): List<Product>
  suspend fun findById(productId: ProductId): Product?
}
