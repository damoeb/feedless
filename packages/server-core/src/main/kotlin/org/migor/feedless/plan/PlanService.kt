package org.migor.feedless.plan

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.migor.feedless.feature.FeatureGroupDAO
import org.migor.feedless.feature.FeatureGroupEntity
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
@Profile("${AppProfiles.plan} & ${AppLayer.service}")
class PlanService {
  private val log = LoggerFactory.getLogger(PlanService::class.simpleName)

  @Autowired
  private lateinit var featureGroupDAO: FeatureGroupDAO

  @Autowired
  private lateinit var planDAO: PlanDAO

  suspend fun findById(id: String): Optional<FeatureGroupEntity> {
    return withContext(Dispatchers.IO) {
      featureGroupDAO.findById(UUID.fromString(id))
    }
  }

  suspend fun findActiveByUserAndProductIn(userId: UUID, products: List<ProductCategory>): PlanEntity? {
    return withContext(Dispatchers.IO) {
      planDAO.findActiveByUserAndProductIn(userId, products, LocalDateTime.now())
    }
  }
}
