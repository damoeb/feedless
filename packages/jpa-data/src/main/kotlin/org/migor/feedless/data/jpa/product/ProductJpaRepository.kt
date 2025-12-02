package org.migor.feedless.data.jpa.product

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
  override suspend fun findByNameEqualsIgnoreCase(name: String): Product? {
    return withContext(Dispatchers.IO) {
      productDAO.findByNameEqualsIgnoreCase(name)?.toDomain()
    }
  }

  override suspend fun findByPartOfAndBaseProductIsTrue(name: Vertical): Product? {
    return withContext(Dispatchers.IO) {
      productDAO.findByPartOfAndBaseProductIsTrue(name)?.toDomain()
    }
  }

  override suspend fun findAllByPartOfOrPartOfIsNullAndAvailableTrue(category: Vertical): List<Product> {
    return withContext(Dispatchers.IO) {
      productDAO.findAllByPartOfOrPartOfIsNullAndAvailableTrue(category).map { it.toDomain() }
    }
  }

  override suspend fun findAllByIdIn(ids: List<ProductId>): List<Product> {
    return withContext(Dispatchers.IO) {
      productDAO.findAllByIdIn(ids.map { it.uuid }).map { it.toDomain() }
    }
  }

  override suspend fun findById(productId: ProductId): Product? {
    return withContext(Dispatchers.IO) {
      productDAO.findById(productId.uuid).getOrNull()?.toDomain()
    }

  }

  override suspend fun save(product: Product): Product {
    return withContext(Dispatchers.IO) {
      productDAO.save(product.toEntity()).toDomain()
    }
  }
}
