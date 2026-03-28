package org.migor.feedless.report

import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.cronSchedule.CronSchedule
import org.migor.feedless.cronSchedule.CronScheduleRepository
import org.migor.feedless.generated.types.IntervalUnit
import org.migor.feedless.generated.types.SegmentInput
import org.migor.feedless.geo.LatLonPoint
import org.migor.feedless.mail.MailService
import org.migor.feedless.mail.OutgoingMail
import org.migor.feedless.pipeline.PluginService
import org.migor.feedless.pipeline.SinkPlugin
import org.migor.feedless.pipelineJob.PluginExecution
import org.migor.feedless.repository.RepositoryGuard
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.repository.nextCronDate
import org.migor.feedless.repository.toParams
import org.migor.feedless.template.MailTemplateReportCreated
import org.migor.feedless.template.ReportCreatedParams
import org.migor.feedless.template.TemplateService
import org.migor.feedless.user.UserId
import org.migor.feedless.user.userId
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
  private val cronScheduleRepository: CronScheduleRepository,
  private val repositoryRepository: RepositoryRepository,
  private val segmentationRepository: SegmentationRepository,
  private val meterRegistry: MeterRegistry,
  private val repositoryGuard: RepositoryGuard,
  private val templateService: TemplateService,
  private val pluginService: PluginService,
  private val mailService: MailService,
  private val reportGuard: ReportGuard,
  private val reportDeactivationLinkFactory: ReportDeactivationLinkFactory,
) {

  private val log = LoggerFactory.getLogger(ReportUseCase::class.simpleName)

  suspend fun createReport(repositoryId: RepositoryId, segment: SegmentInput): Report = withContext(Dispatchers.IO) {
    log.info("createReport repositoryId=$repositoryId")

    repositoryGuard.requireWrite(repositoryId)

    val email = segment.recipient.email.email

//      val isOwner = repository.ownerId == user?.id || repository.ownerId == resolveUserId()?.uuid
    // todo enable this
//    if (repository.visibility == EntityVisibility.isPrivate && !isOwner) {
//      throw IllegalArgumentException() // obscured access denied
//    }
    val startingAt = segment.`when`.scheduled.startingAt.toLocalDateTime()

    val interval = when (segment.`when`.scheduled.interval) {
      IntervalUnit.MONTH -> Pair(ChronoUnit.MONTHS, "0 8 L * *")
      IntervalUnit.WEEK -> Pair(ChronoUnit.WEEKS, "0 8 * * 0")
    }

    var segmentation = Segmentation(
      size = 200,
      repositoryId = repositoryId,
      timeSegmentStartingAt = startingAt,
      timeInterval = interval.first
    )

    segmentation = segment.what.latLng?.let {
      it.near?.let {
        segmentation.copy(
          contentSegmentLatLon = LatLonPoint(it.point.lat, it.point.lng),
          contentSegmentLatLonDistance = it.distanceKm
        )
      } ?: segmentation
    } ?: segmentation

    segmentationRepository.save(segmentation)

    val nextReportedAt = if (interval.first == ChronoUnit.MONTHS) {
      startingAt.with(TemporalAdjusters.lastDayOfMonth())
    } else {
      startingAt.with(TemporalAdjusters.next(DayOfWeek.FRIDAY))
    }

    val cronSchedule = CronSchedule(
      cronExpression = "",
      scheduledNextAt = nextReportedAt
    )

    cronScheduleRepository.save(cronSchedule)

    val reporterPlugin = segment.report.plugin

    val plugin = pluginService.resolveById<SinkPlugin>(reporterPlugin.pluginId)!!
//      plugin.tryParseParams("{}") // validate
//      plugin.tryParseParams(reporterPlugin.params.toParams().paramsJsonString!!) // validate


    val report = Report(
      recipientName = segment.recipient.email.name,
      recipientEmail = email,

      // send authorization mail
      authorizationAttempt = 1,
      lastRequestedAuthorization = LocalDateTime.now(),
      segmentId = segmentation.id,
      reporterPlugin = PluginExecution(
        id = reporterPlugin.pluginId,
        params = reporterPlugin.params.toParams()
      ),
      cronScheduleId = cronSchedule.id,
      userId = coroutineContext.userId()
    )

    meterRegistry.counter(AppMetrics.createReport)
    val saved = reportRepository.save(report)
    sendReportCreatedMail(segment, saved)
    saved
  }

  /**
   * Same as [createReport] but for a **public** shared repository (subscriber is not the repo owner).
   * Used after Stripe subscription checkout (trial) for products like auction-alert.
   */
  suspend fun createReportForPublicRepository(
    repositoryId: RepositoryId,
    segment: SegmentInput,
    subscriberUserId: UserId,
  ): Report = withContext(Dispatchers.IO) {
    log.info("createReportForPublicRepository repositoryId=$repositoryId user=$subscriberUserId")

    repositoryGuard.requirePublicRepositoryForAlertSubscription(repositoryId)

    val email = segment.recipient.email.email

    val startingAt = segment.`when`.scheduled.startingAt.toLocalDateTime()

    val interval = when (segment.`when`.scheduled.interval) {
      IntervalUnit.MONTH -> Pair(ChronoUnit.MONTHS, "0 8 L * *")
      IntervalUnit.WEEK -> Pair(ChronoUnit.WEEKS, "0 8 * * 0")
    }

    var segmentation = Segmentation(
      size = 200,
      repositoryId = repositoryId,
      timeSegmentStartingAt = startingAt,
      timeInterval = interval.first
    )

    segmentation = segment.what.latLng?.let {
      it.near?.let {
        segmentation.copy(
          contentSegmentLatLon = LatLonPoint(it.point.lat, it.point.lng),
          contentSegmentLatLonDistance = it.distanceKm
        )
      } ?: segmentation
    } ?: segmentation

    segmentationRepository.save(segmentation)

    val nextReportedAt = if (interval.first == ChronoUnit.MONTHS) {
      startingAt.with(TemporalAdjusters.lastDayOfMonth())
    } else {
      startingAt.with(TemporalAdjusters.next(DayOfWeek.FRIDAY))
    }

    val cronSchedule = CronSchedule(
      cronExpression = "",
      scheduledNextAt = nextReportedAt
    )

    cronScheduleRepository.save(cronSchedule)

    val reporterPlugin = segment.report.plugin

    pluginService.resolveById<SinkPlugin>(reporterPlugin.pluginId)!!

    val report = Report(
      recipientName = segment.recipient.email.name,
      recipientEmail = email,

      authorizationAttempt = 1,
      lastRequestedAuthorization = LocalDateTime.now(),
      segmentId = segmentation.id,
      reporterPlugin = PluginExecution(
        id = reporterPlugin.pluginId,
        params = reporterPlugin.params.toParams()
      ),
      cronScheduleId = cronSchedule.id,
      userId = subscriberUserId
    )

    meterRegistry.counter(AppMetrics.createReport)
    val saved = reportRepository.save(report)
    sendReportCreatedMail(segment, saved)
    saved
  }

  private suspend fun sendReportCreatedMail(segment: SegmentInput, report: Report) {
    val params = ReportCreatedParams(
      language = "de",
      deactivationLink = runCatching { reportDeactivationLinkFactory.createLink(report) }.fold(
        onSuccess = { it ?: "" },
        onFailure = { "" },
      ),
      reportName = "",
      cronExpression = "",
      nextScheduledAt = "",
    )
    val body = templateService.renderTemplate(MailTemplateReportCreated(params))
    val mail = OutgoingMail(
      from = "no-reply@feedless.org",
      to = listOf(segment.recipient.email.email),
      subject = "Reporter erstellt",
      htmlContent = body
    )
    mailService.send(mail)
  }

  suspend fun deleteReport(reportId: ReportId) = withContext(Dispatchers.IO) {
    log.info("deleteReport reportId=$reportId")
    reportGuard.requireWrite(reportId)
    reportRepository.deleteById(reportId)
  }

  suspend fun updateReportById(reportId: ReportId, authorize: Boolean) = withContext(Dispatchers.IO) {
    log.info("updateReportById reportId=$reportId authorize=$authorize")
    val report = reportGuard.requireWrite(reportId)
    reportRepository.save(
      report.copy(
        authorized = authorize,
        authorizedAt = LocalDateTime.now()
      )
    )
  }

  suspend fun processReportJobs() {
    val reports = withContext(Dispatchers.IO) {
      reportRepository.findAllPendingBatched(LocalDateTime.now())
    }

    reports.forEach { report ->
      val cron = report.cronSchedule!!
      val now = LocalDateTime.now()
      try {
        resolveReporterPlugin(report.reporterPlugin)
          .report(report)

      } catch (e: Exception) {
        log.error("Failed to process report job {}: {}", report.id, e.message, e)
        val next = if (cron.cronExpression.isNotBlank()) {
          nextCronDate(cron.cronExpression, cron.scheduledNextAt ?: now)
        } else {
          (cron.scheduledNextAt ?: now).plusWeeks(1)
        }
        withContext(Dispatchers.IO) {
          cronScheduleRepository.save(
            cron.copy(
              scheduledNextAt = next,
              executedLastAt = now
            )
          )
        }
      }
    }
  }

  private suspend fun resolveReporterPlugin(plugin: PluginExecution): SinkPlugin =
    pluginService.resolveById<SinkPlugin>(plugin.id)!!
}
