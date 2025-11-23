package org.migor.feedless.plan

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.featureGroup.FeatureGroupDAO
import org.migor.feedless.data.jpa.featureGroup.toDomain
import org.migor.feedless.data.jpa.plan.PlanDAO
import org.migor.feedless.data.jpa.plan.toDomain
import org.migor.feedless.feature.FeatureGroup
import org.migor.feedless.user.UserId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
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
    suspend fun findById(id: PlanId): Optional<FeatureGroup> {
        return withContext(Dispatchers.IO) {
            featureGroupDAO.findById(id.uuid).map { it.toDomain() }
        }
    }

    @Transactional(readOnly = true)
    suspend fun findAllByUser(userId: UserId): List<Plan> {
        return withContext(Dispatchers.IO) {
            planDAO.findAllByUser(userId.uuid).map { it.toDomain() }
        }
    }
}
