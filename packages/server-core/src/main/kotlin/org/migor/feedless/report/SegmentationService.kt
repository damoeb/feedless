package org.migor.feedless.report

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.report} & ${AppLayer.service}")
class SegmentationService(
  val segmentationRepository: SegmentationRepository,
) {

  @Deprecated("use repository")
  suspend fun saveSegmentation(segmentation: Segmentation): Segmentation = withContext(Dispatchers.IO) {
    segmentationRepository.save(segmentation)
  }
}
