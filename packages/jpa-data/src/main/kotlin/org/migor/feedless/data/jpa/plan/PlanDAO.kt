package org.migor.feedless.data.jpa.plan

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.Vertical
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
@Profile("${AppProfiles.plan} & ${AppLayer.repository}")
interface PlanDAO : JpaRepository<PlanEntity, UUID> {

  @Query(
    """
    select PL from PlanEntity PL
    LEFT JOIN FETCH PL.product
    inner join ProductEntity P on P.id = PL.productId
    where PL.userId = :userId
  """
  )
  fun findAllByUser(
    @Param("userId") userId: UUID,
  ): List<PlanEntity>

  @Query(
    """
    select PL from PlanEntity PL
    LEFT JOIN FETCH PL.product
    inner join ProductEntity P on P.id = PL.productId
    where PL.userId = :userId and P.partOf IN :products AND (PL.terminatedAt IS NULL OR PL.terminatedAt > :now)
  """
  )
  fun findActiveByUserAndProductIn(
    @Param("userId") userId: UUID,
    @Param("products") products: List<Vertical>,
    @Param("now") date: LocalDateTime,
  ): PlanEntity?
}
