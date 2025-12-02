package org.migor.feedless.report

interface SegmentationRepository {
  suspend fun save(segmentation: Segmentation): Segmentation

}
