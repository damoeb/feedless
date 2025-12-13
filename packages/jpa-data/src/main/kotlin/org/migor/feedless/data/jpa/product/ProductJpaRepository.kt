package org.migor.feedless.data.jpa.product

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.Vertical
import org.migor.feedless.product.Product
import org.migor.feedless.product.ProductId
import org.migor.feedless.product.ProductRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrNull

@Component
@Profile("${AppProfiles.plan} & ${AppLayer.repository}")
class ProductJpaRepository(private val productDAO: ProductDAO) : ProductRepository {
  override fun findByNameEqualsIgnoreCase(name: String): Product? {
    return productDAO.findByNameEqualsIgnoreCase(name)?.toDomain()
  }

  override fun findByPartOfAndBaseProductIsTrue(name: Vertical): Product? {
    return productDAO.findByPartOfAndBaseProductIsTrue(name)?.toDomain()
  }

  override fun findAllByPartOfOrPartOfIsNullAndAvailableTrue(category: Vertical): List<Product> {
    return productDAO.findAllByPartOfOrPartOfIsNullAndAvailableTrue(category).map { it.toDomain() }
  }

  override fun findAllByIdIn(ids: List<ProductId>): List<Product> {
    return productDAO.findAllByIdIn(ids.map { it.uuid }).map { it.toDomain() }
  }

  override fun findById(productId: ProductId): Product? {
    return productDAO.findById(productId.uuid).getOrNull()?.toDomain()
  }

  override fun save(product: Product): Product {
    return productDAO.save(product.toEntity()).toDomain()
  }
}
