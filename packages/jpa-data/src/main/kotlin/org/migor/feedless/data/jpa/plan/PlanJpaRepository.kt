package org.migor.feedless.data.jpa.plan

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.Vertical
import org.migor.feedless.plan.Plan
import org.migor.feedless.plan.PlanId
import org.migor.feedless.plan.PlanRepository
import org.migor.feedless.user.UserId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@Component
@Profile("${AppProfiles.plan} & ${AppLayer.repository}")
class PlanJpaRepository(private val planDAO: PlanDAO) : PlanRepository {
  override suspend fun findAllByUser(userId: UserId): List<Plan> {
    return withContext(Dispatchers.IO) {
      planDAO.findAllByUser(userId.uuid).map { it.toDomain() }
    }
  }

  override suspend fun findActiveByUserAndProductIn(
    userId: UserId,
    products: List<Vertical>,
    date: LocalDateTime
  ): Plan? {
    return withContext(Dispatchers.IO) {
      planDAO.findActiveByUserAndProductIn(userId.uuid, products, date)?.toDomain()
    }
  }

  override suspend fun save(plan: Plan): Plan {
    return withContext(Dispatchers.IO) {
      planDAO.save(plan.toEntity()).toDomain()
    }
  }

  override suspend fun findById(id: PlanId): Plan? {
    return withContext(Dispatchers.IO) {
      planDAO.findById(id.uuid).getOrNull()?.toDomain()
    }
  }

}
