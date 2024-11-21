package org.migor.feedless.report

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.locationtech.jts.geom.Point
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.generated.types.GeoPointInput
import org.migor.feedless.generated.types.IntervalUnit
import org.migor.feedless.generated.types.SegmentInput
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserEntity
import org.migor.feedless.util.JtsUtil
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.report} & ${AppLayer.service}")
class ReportService(
  val reportDAO: ReportDAO,
  val userDAO: UserDAO,
  val segmentationDAO: SegmentationDAO,
) {

  @Transactional
  suspend fun createReport(repositoryId: String, segment: SegmentInput, currentUserId: UUID?): ReportEntity {
    val report = ReportEntity()

    currentUserId?.let {
      report.userId = it
    }

    val segmentation = SegmentationEntity()
    segmentation.repositoryId = UUID.fromString(repositoryId)
    val startingAt = LocalDateTime.from(Instant.ofEpochMilli(segment.`when`.scheduled.startingAt))
    segmentation.timeSegmentStartingAt = startingAt
    segment.what.latLng?.let {
      segmentation.contentSegmentLatLon = it.near.toPoint()
      segmentation.contentSegmentLatLonDistance = it.distanceKm
    }
    val interval = when (segment.`when`.scheduled.interval) {
      IntervalUnit.DAY -> ChronoUnit.DAYS
      IntervalUnit.MONTH -> ChronoUnit.MONTHS
      IntervalUnit.WEEK -> ChronoUnit.WEEKS
    }
    segmentation.timeInterval = interval
    report.nextReportedAt = startingAt.plus(1, interval)
    report.recipientName = segment.recipient.email.name
    val email = segment.recipient.email.email
    report.recipientEmail = email

    val userWithEmilExists = withContext(Dispatchers.IO) {
      userDAO.existsByEmail(email)
    }

    if (currentUserId == null && userWithEmilExists) {
      throw IllegalArgumentException("Please login")
    } else {
      // send authorization mail
      report.authorizationAttempt = 1
      report.lastRequestedAuthorization = LocalDateTime.now()
      sendAuthorizationMail(segment)
    }

    return withContext(Dispatchers.IO) {
      report.segmentId = segmentationDAO.save(segmentation).id

      reportDAO.save(report)
    }
  }

  private suspend fun sendAuthorizationMail(segment: SegmentInput) {
    TODO("Not yet implemented")
  }

  @Transactional
  suspend fun deleteReport(reportId: String, currentUser: UserEntity) {
    withContext(Dispatchers.IO) {
      reportDAO.deleteById(UUID.fromString(reportId))
    }
  }

  @Transactional
  suspend fun updateReportById(reportId: UUID, authorize: Boolean) {
    withContext(Dispatchers.IO) {
      reportDAO.findById(reportId).orElseThrow()?.let {
        it.authorized = authorize
        it.authorizedAt = LocalDateTime.now()
        reportDAO.save(it)
      }
    }
  }
}

private fun GeoPointInput.toPoint(): Point {
  return JtsUtil.createPoint(lat, lon)
}
