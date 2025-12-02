package org.migor.feedless.plan

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.user.UserId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.plan} & ${AppLayer.service}")
class PlanUseCase(private var planRepository: PlanRepository) {
  private val log = LoggerFactory.getLogger(PlanUseCase::class.simpleName)

  suspend fun findById(id: PlanId): Plan? = withContext(Dispatchers.IO) {
    planRepository.findById(id)
  }

  suspend fun findAllByUser(userId: UserId): List<Plan> = withContext(Dispatchers.IO) {
    planRepository.findAllByUser(userId)
  }
}
