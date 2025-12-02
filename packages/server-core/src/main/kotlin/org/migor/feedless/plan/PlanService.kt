package org.migor.feedless.plan

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.feature.FeatureGroupRepository
import org.migor.feedless.user.UserId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.plan} & ${AppLayer.service}")
class PlanService {
  private val log = LoggerFactory.getLogger(PlanService::class.simpleName)

  @Autowired
  private lateinit var featureGroupRepository: FeatureGroupRepository

  @Autowired
  private lateinit var planRepository: PlanRepository

  @Transactional(readOnly = true)
  suspend fun findById(id: PlanId): Plan? {
    return planRepository.findById(id)
  }

  @Transactional(readOnly = true)
  suspend fun findAllByUser(userId: UserId): List<Plan> {
    return planRepository.findAllByUser(userId)
  }
}
