package org.migor.feedless.plan

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.jpa.featureGroup.FeatureGroupDAO
import org.migor.feedless.jpa.featureGroup.FeatureGroupEntity
import org.migor.feedless.jpa.plan.PlanDAO
import org.migor.feedless.jpa.plan.PlanEntity
import org.migor.feedless.user.UserId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

data class PlanId(val value: UUID)

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.plan} & ${AppLayer.service}")
class PlanService {
  private val log = LoggerFactory.getLogger(PlanService::class.simpleName)

  @Autowired
  private lateinit var featureGroupDAO: FeatureGroupDAO

  @Autowired
  private lateinit var planDAO: PlanDAO

  @Transactional(readOnly = true)
  suspend fun findById(id: PlanId): Optional<FeatureGroupEntity> {
    return withContext(Dispatchers.IO) {
      featureGroupDAO.findById(id.value)
    }
  }

  @Transactional(readOnly = true)
  suspend fun findAllByUser(userId: UserId): List<PlanEntity> {
    return withContext(Dispatchers.IO) {
      planDAO.findAllByUser(userId.value)
    }
  }
}
