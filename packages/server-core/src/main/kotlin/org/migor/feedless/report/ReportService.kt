package org.migor.feedless.report

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.locationtech.jts.geom.Point
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.generated.types.GeoPointInput
import org.migor.feedless.generated.types.IntervalUnit
import org.migor.feedless.generated.types.SegmentInput
import org.migor.feedless.repository.RepositoryService
import org.migor.feedless.repository.fromDto
import org.migor.feedless.user.UserEntity
import org.migor.feedless.user.UserService
import org.migor.feedless.util.JtsUtil
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
import java.util.*

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.report} & ${AppLayer.service}")
class ReportService(
  val reportDAO: ReportDAO,
  val repositoryService: RepositoryService,
  val userService: UserService,
  val segmentationService: SegmentationService,
  val context: ApplicationContext
) {

  private val log = LoggerFactory.getLogger(ReportService::class.simpleName)

  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun createReport(repositoryId: UUID, segment: SegmentInput, currentUserId: UUID?): ReportEntity {
    log.debug("createReport for $repositoryId")
    val report = ReportEntity()

    currentUserId?.let {
      report.userId = it
    }

    val repository = repositoryService.findById(repositoryId).orElseThrow()

    val email = segment.recipient.email.email
    val user = userService.findByEmail(email)

    if (currentUserId == null && user != null) {
      throw IllegalArgumentException() // obscured login request
    }

    val isOwner = repository.ownerId == user?.id || repository.ownerId == currentUserId
    // todo enable this
//    if (repository.visibility == EntityVisibility.isPrivate && !isOwner) {
//      throw IllegalArgumentException() // obscured access denied
//    }

    val segmentation = SegmentationEntity()
    segmentation.size = 200
    segmentation.repositoryId = repositoryId
    val startingAt = segment.`when`.scheduled.startingAt.toLocalDateTime()
    segmentation.timeSegmentStartingAt = startingAt
    segment.what.latLng?.let {
      segmentation.contentSegmentLatLon = it.near.toPoint()
      segmentation.contentSegmentLatLonDistance = it.distanceKm
    }
    val interval = when (segment.`when`.scheduled.interval) {
      IntervalUnit.MONTH -> ChronoUnit.MONTHS
      IntervalUnit.WEEK -> ChronoUnit.WEEKS
    }
    segmentation.timeInterval = interval
    segmentation.reportPlugin = segment.report.plugin.fromDto()

    report.segmentId = segmentationService.saveSegmentation(segmentation).id

    if (interval == ChronoUnit.MONTHS) {
      report.nextReportedAt = startingAt.with(TemporalAdjusters.lastDayOfMonth())
    } else {
      report.nextReportedAt = startingAt.with(TemporalAdjusters.next(DayOfWeek.FRIDAY))
    }
    report.recipientName = segment.recipient.email.name
    report.recipientEmail = email

    // send authorization mail
    report.authorizationAttempt = 1
    report.lastRequestedAuthorization = LocalDateTime.now()

    sendAuthorizationMail(segment)

    return context.getBean(ReportService::class.java).saveReport(report)
  }

  @Transactional
  suspend fun saveReport(report: ReportEntity): ReportEntity {
    return withContext(Dispatchers.IO) {
      reportDAO.save(report)
    }
  }

  private suspend fun sendAuthorizationMail(segment: SegmentInput) {
    // todo implement
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
