package org.migor.feedless.plan

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.user.userId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.plan} & ${AppLayer.service}")
class PlanUseCase(private val planRepository: PlanRepository) {
  private val log = LoggerFactory.getLogger(PlanUseCase::class.simpleName)

  suspend fun findById(id: PlanId): Plan? = withContext(Dispatchers.IO) {
    log.info("findById id=$id")
    val plan = planRepository.findById(id)

    plan?.let {
      if (plan.userId != coroutineContext.userId()) {
        throw PermissionDeniedException("must be owner")
      }
    }

    plan
  }

  suspend fun findAllByUser(): List<Plan> = withContext(Dispatchers.IO) {
    log.info("findAllByUser")
    planRepository.findAllByUser(coroutineContext.userId())
  }
}
