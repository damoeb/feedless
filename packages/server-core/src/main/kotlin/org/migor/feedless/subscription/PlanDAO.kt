package org.migor.feedless.subscription

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile(AppProfiles.database)
interface PlanDAO : JpaRepository<PlanEntity, UUID> {

  @Query(
    """
    select PL from PlanEntity PL
    inner join ProductEntity P on P.id = PL.productId
    where PL.userId = :userId and P.partOf IN :products AND (PL.terminatedAt IS NULL OR PL.terminatedAt <= NOW())
  """
  )
  fun findActiveByUserAndProductIn(@Param("userId") userId: UUID, @Param("products") products: List<ProductCategory>): PlanEntity?
}
