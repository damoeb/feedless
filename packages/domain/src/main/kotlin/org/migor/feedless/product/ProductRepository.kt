package org.migor.feedless.product

import org.migor.feedless.Vertical

interface ProductRepository {
  suspend fun findByNameEqualsIgnoreCase(name: String): Product?
  suspend fun findByPartOfAndBaseProductIsTrue(name: Vertical): Product?
  suspend fun findAllByPartOfOrPartOfIsNullAndAvailableTrue(category: Vertical): List<Product>
  suspend fun findAllByIdIn(ids: List<ProductId>): List<Product>
  suspend fun findById(productId: ProductId): Product?
  suspend fun save(product: Product): Product
}
