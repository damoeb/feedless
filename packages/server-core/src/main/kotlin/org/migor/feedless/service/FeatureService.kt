package org.migor.feedless.service

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.models.FeatureEntity
import org.migor.feedless.data.jpa.repositories.FeatureDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile(AppProfiles.database)
class FeatureService {
  private val log = LoggerFactory.getLogger(FeatureService::class.simpleName)

  @Autowired
  lateinit var featureDAO: FeatureDAO

  fun findAllByPlanId(id: UUID): List<FeatureEntity> {
    return this.featureDAO.findAllByPlanId(id)
  }

}
