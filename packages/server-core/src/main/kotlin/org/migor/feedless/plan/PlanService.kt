package org.migor.feedless.plan

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.feature.FeatureGroupDAO
import org.migor.feedless.feature.FeatureGroupEntity
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

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
  suspend fun findById(id: String): Optional<FeatureGroupEntity> {
    return withContext(Dispatchers.IO) {
      featureGroupDAO.findById(UUID.fromString(id))
    }
  }

  @Transactional(readOnly = true)
  suspend fun findAllByUser(userId: UUID): List<PlanEntity> {
    return withContext(Dispatchers.IO) {
      planDAO.findAllByUser(userId)
    }
  }
}
