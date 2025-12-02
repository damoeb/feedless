package org.migor.feedless.report

import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.generated.types.IntervalUnit
import org.migor.feedless.generated.types.SegmentInput
import org.migor.feedless.geo.LatLonPoint
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.repository.fromDto
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.migor.feedless.util.toLocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters


@Service
@Profile("${AppProfiles.report} & ${AppLayer.service}")
class ReportUseCase(
  private val reportRepository: ReportRepository,
  private val repositoryRepository: RepositoryRepository,
  private val userRepository: UserRepository,
  private val segmentationService: SegmentationService,
  private val meterRegistry: MeterRegistry,
) {

  private val log = LoggerFactory.getLogger(ReportUseCase::class.simpleName)

  suspend fun createReport(repositoryId: RepositoryId, segment: SegmentInput, currentUserId: UserId?): Report =
    withContext(Dispatchers.IO) {
      log.debug("createReport for $repositoryId")

      val repository = repositoryRepository.findById(repositoryId)!!

      val email = segment.recipient.email.email
      val user = userRepository.findByEmail(email)

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

      reportRepository.save(report)
    }


  private suspend fun sendAuthorizationMail(segment: SegmentInput) {
    // todo implement
  }

  suspend fun deleteReport(reportId: ReportId, currentUser: User) = withContext(Dispatchers.IO) {
    // todo validate user
    reportRepository.deleteById(reportId)
  }

  suspend fun updateReportById(reportId: ReportId, authorize: Boolean) = withContext(Dispatchers.IO) {
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
