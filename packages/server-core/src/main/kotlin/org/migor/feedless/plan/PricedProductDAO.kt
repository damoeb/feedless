package org.migor.feedless.plan

import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Profile(AppProfiles.database)
interface PricedProductDAO : JpaRepository<PricedProductEntity, UUID> {
  fun findAllByProductId(id: UUID): List<PricedProductEntity>

  @Transactional
  fun deleteAllByProductId(id: UUID)

}
