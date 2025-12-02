package org.migor.feedless.report

interface SegmentationRepository {
  fun save(segmentation: Segmentation): Segmentation
}
