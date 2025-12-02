package org.migor.feedless.data.jpa.pricedProduct

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.pricedProduct.PricedProductRepository
import org.migor.feedless.product.PricedProduct
import org.migor.feedless.product.ProductId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("${AppProfiles.plan} & ${AppLayer.repository}")
class PricedProductJpaRepository(private val pricedProductDAO: PricedProductDAO) : PricedProductRepository {
  override suspend fun findAllByProductId(id: ProductId): List<PricedProduct> {
    return withContext(Dispatchers.IO) {
      pricedProductDAO.findAllByProductId(id.uuid).map { it.toDomain() }
    }
  }

  override suspend fun deleteAllByProductId(id: ProductId) {
    withContext(Dispatchers.IO) {
      pricedProductDAO.deleteAllByProductId(id.uuid)
    }
  }

  override suspend fun saveAll(list: List<PricedProduct>) {
    withContext(Dispatchers.IO) {
      pricedProductDAO.saveAll(list.map { it.toEntity() })
    }
  }

}
