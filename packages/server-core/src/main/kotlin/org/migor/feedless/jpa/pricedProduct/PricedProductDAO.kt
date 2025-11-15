package org.migor.feedless.jpa.pricedProduct

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Profile("${AppProfiles.plan} & ${AppLayer.repository}")
interface PricedProductDAO : JpaRepository<PricedProductEntity, UUID> {
  fun findAllByProductId(id: UUID): List<PricedProductEntity>

  @Transactional
  fun deleteAllByProductId(id: UUID)

}
