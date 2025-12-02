package org.migor.feedless.data.jpa.plan

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.Vertical
import org.migor.feedless.plan.Plan
import org.migor.feedless.plan.PlanId
import org.migor.feedless.plan.PlanRepository
import org.migor.feedless.user.UserId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@Component
@Transactional(propagation = Propagation.MANDATORY)
@Profile("${AppProfiles.plan} & ${AppLayer.repository}")
class PlanJpaRepository(private val planDAO: PlanDAO) : PlanRepository {
  override fun findAllByUser(userId: UserId): List<Plan> {
    return planDAO.findAllByUser(userId.uuid).map { it.toDomain() }
  }

  override fun findActiveByUserAndProductIn(
    userId: UserId,
    products: List<Vertical>,
    date: LocalDateTime
  ): Plan? {
    return planDAO.findActiveByUserAndProductIn(userId.uuid, products, date)?.toDomain()
  }

  override fun save(plan: Plan): Plan {
    return planDAO.save(plan.toEntity()).toDomain()
  }

  override fun findById(id: PlanId): Plan? {
    return planDAO.findById(id.uuid).getOrNull()?.toDomain()
  }

}
