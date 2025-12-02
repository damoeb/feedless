package org.migor.feedless.data.jpa.report

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.report.Segmentation
import org.migor.feedless.report.SegmentationRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(propagation = Propagation.MANDATORY)
@Profile("${AppProfiles.report} & ${AppLayer.repository}")
class SegmentationJpaRepository(private val segmentationDAO: SegmentationDAO) : SegmentationRepository {
  override fun save(segmentation: Segmentation): Segmentation {
    return segmentationDAO.save(segmentation.toEntity()).toDomain()
  }

}
