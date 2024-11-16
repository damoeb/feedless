package org.migor.feedless.report

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.locationtech.jts.geom.Point
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.feed.parser.json.JsonPoint
import org.migor.feedless.generated.types.GeoPointInput
import org.migor.feedless.generated.types.IntervalUnit
import org.migor.feedless.generated.types.SegmentInput
import org.migor.feedless.user.UserEntity
import org.migor.feedless.util.JtsUtil
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@Service
@Profile("${AppProfiles.report} & ${AppLayer.service}")
class ReportService(
  val reportDAO: ReportDAO,
  val segmentationDAO: SegmentationDAO,
) {

  suspend fun createReport(repositoryId: String, segment: SegmentInput, currentUserId: UUID?): ReportEntity {
    val report = ReportEntity()

    currentUserId?.let {
      report.userId = it
    }

    val segmentation = SegmentationEntity()
    segmentation.repositoryId = UUID.fromString(repositoryId)
    val startingAt = LocalDateTime.from(Instant.ofEpochMilli(segment.time.startingAt))
    segmentation.timeSegmentStartingAt = startingAt
    segment.filter.latLng?.let {
      segmentation.contentSegmentLatLon = it.near.toPoint()
      segmentation.contentSegmentLatLonDistance = it.distanceKm
    }
    val interval = when(segment.time.interval) {
      IntervalUnit.DAY -> ChronoUnit.DAYS
      IntervalUnit.MONTH -> ChronoUnit.MONTHS
      IntervalUnit.WEEK -> ChronoUnit.WEEKS
    }
    segmentation.timeInterval = interval
    report.nextReportedAt = startingAt.plus(1, interval)

    return withContext(Dispatchers.IO) {
      report.segmentId = segmentationDAO.save(segmentation).id

      reportDAO.save(report)
    }
  }

  suspend fun deleteReport(reportId: String, currentUser: UserEntity) {
    withContext(Dispatchers.IO) {
      reportDAO.deleteById(UUID.fromString(reportId))
    }
  }
}

private fun GeoPointInput.toPoint(): Point {
  return JtsUtil.createPoint(lat, lon)
}
