package org.migor.feedless.report

import org.migor.feedless.geo.LatLonPoint
import org.migor.feedless.repository.RepositoryId
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class Segmentation(
  val id: SegmentationId = SegmentationId(),
  val size: Int,
  val timeSegmentStartingAt: LocalDateTime,
  val timeInterval: ChronoUnit,
  val repositoryId: RepositoryId,
  val contentSegmentLatLon: LatLonPoint? = null,
  val contentSegmentLatLonDistance: Double? = null,
  val createdAt: LocalDateTime = LocalDateTime.now(),
)

