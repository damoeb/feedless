package org.migor.feedless.data.jpa.pricedProduct

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
  override fun findAllByProductId(id: ProductId): List<PricedProduct> {
    return pricedProductDAO.findAllByProductId(id.uuid).map { it.toDomain() }
  }

  override fun deleteAllByProductId(id: ProductId) {
    pricedProductDAO.deleteAllByProductId(id.uuid)
  }

  override fun saveAll(list: List<PricedProduct>) {
    pricedProductDAO.saveAll(list.map { it.toEntity() })
  }

}
