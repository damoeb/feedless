package org.migor.feedless.report

import io.micrometer.core.instrument.MeterRegistry
import org.locationtech.jts.geom.Point
import org.migor.feedless.AppLayer
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.JtsUtil
import org.migor.feedless.generated.types.GeoPointInput
import org.migor.feedless.generated.types.IntervalUnit
import org.migor.feedless.generated.types.SegmentInput
import org.migor.feedless.geo.LatLonPoint
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryService
import org.migor.feedless.repository.fromDto
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserService
import org.migor.feedless.util.toLocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters


@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.report} & ${AppLayer.service}")
class ReportService(
  val reportRepository: ReportRepository,
  val repositoryService: RepositoryService,
  val userService: UserService,
  val segmentationService: SegmentationService,
  val meterRegistry: MeterRegistry,
  val context: ApplicationContext
) {

  private val log = LoggerFactory.getLogger(ReportService::class.simpleName)

  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun createReport(repositoryId: RepositoryId, segment: SegmentInput, currentUserId: UserId?): Report {
    log.debug("createReport for $repositoryId")

    val repository = repositoryService.findById(repositoryId)!!

    val email = segment.recipient.email.email
    val user = userService.findByEmail(email)

    if (currentUserId == null && user != null) {
      throw IllegalArgumentException() // obscured login request
    }

    val isOwner = repository.ownerId == user?.id || repository.ownerId == currentUserId?.uuid
    // todo enable this
//    if (repository.visibility == EntityVisibility.isPrivate && !isOwner) {
//      throw IllegalArgumentException() // obscured access denied
//    }
    val startingAt = segment.`when`.scheduled.startingAt.toLocalDateTime()

    val interval = when (segment.`when`.scheduled.interval) {
      IntervalUnit.MONTH -> ChronoUnit.MONTHS
      IntervalUnit.WEEK -> ChronoUnit.WEEKS
    }

    var segmentation = Segmentation(
      size = 200,
      repositoryId = repositoryId,
      timeSegmentStartingAt = startingAt,
      timeInterval = interval,
      reportPlugin = segment.report.plugin.fromDto()
    )

    segmentation = segment.what.latLng?.let {
      it.near?.let {
        segmentation.copy(
          contentSegmentLatLon = LatLonPoint(it.point.lat, it.point.lng),
          contentSegmentLatLonDistance = it.distanceKm
        )
      } ?: segmentation
    } ?: segmentation

    segmentationService.saveSegmentation(segmentation)


    val nextReportedAt = if (interval == ChronoUnit.MONTHS) {
      startingAt.with(TemporalAdjusters.lastDayOfMonth())
    } else {
      startingAt.with(TemporalAdjusters.next(DayOfWeek.FRIDAY))
    }

    val report = Report(
      recipientName = segment.recipient.email.name,
      recipientEmail = email,

      // send authorization mail
      authorizationAttempt = 1,
      lastRequestedAuthorization = LocalDateTime.now(),
      segmentId = segmentation.id,
      nextReportedAt = nextReportedAt,
      userId = currentUserId
    )

    meterRegistry.counter(AppMetrics.createReport)
    sendAuthorizationMail(segment)

    return context.getBean(ReportService::class.java).saveReport(report)
  }

  @Transactional
  suspend fun saveReport(report: Report): Report {
    return reportRepository.save(report)
  }

  private suspend fun sendAuthorizationMail(segment: SegmentInput) {
    // todo implement
  }

  @Transactional
  suspend fun deleteReport(reportId: ReportId, currentUser: User) {
    reportRepository.deleteById(reportId)
  }

  @Transactional
  suspend fun updateReportById(reportId: ReportId, authorize: Boolean) {
    reportRepository.findById(reportId)?.let {
      reportRepository.save(
        it.copy(
          authorized = authorize,
          authorizedAt = LocalDateTime.now()
        )
      )
    }
  }
}

private fun GeoPointInput.toPoint(): Point {
  return JtsUtil.createPoint(lat, lng)
}
