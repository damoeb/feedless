package org.migor.feedless.product

import org.migor.feedless.Vertical

interface ProductRepository {
  fun findByNameEqualsIgnoreCase(name: String): Product?
  fun findByPartOfAndBaseProductIsTrue(name: Vertical): Product?
  fun findAllByPartOfOrPartOfIsNullAndAvailableTrue(category: Vertical): List<Product>
  fun findAllByIdIn(ids: List<ProductId>): List<Product>
  fun findById(productId: ProductId): Product?
  fun save(product: Product): Product
}
