package org.migor.feedless.report

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.report} & ${AppLayer.service}")
class SegmentationService(
  val segmentationDAO: SegmentationDAO,
) {

  @Transactional
  suspend fun saveSegmentation(segmentation: SegmentationEntity): SegmentationEntity {
    return withContext(Dispatchers.IO) {
      segmentationDAO.save(segmentation)
    }
  }
}
