package org.migor.rich.rss.service

import org.migor.rich.rss.data.jpa.models.FeatureEntity
import org.migor.rich.rss.data.jpa.repositories.FeatureDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class FeatureService {
  private val log = LoggerFactory.getLogger(FeatureService::class.simpleName)

  @Autowired
  lateinit var featureDAO: FeatureDAO

  fun findAllByPlanId(id: UUID): List<FeatureEntity> {
    return this.featureDAO.findAllByPlanId(id)
  }

}
