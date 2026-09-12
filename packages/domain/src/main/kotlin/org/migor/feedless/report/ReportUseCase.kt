package org.migor.feedless.report

import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.actions.PluginExecutionJson
import org.migor.feedless.cronSchedule.CronSchedule
import org.migor.feedless.cronSchedule.CronScheduleRepository
import org.migor.feedless.document.Document
import org.migor.feedless.mail.MailService
import org.migor.feedless.mail.OutgoingMail
import org.migor.feedless.pipeline.PipelinePlugins
import org.migor.feedless.pipeline.ReportPlugin
import org.migor.feedless.pipeline.resolveById
import org.migor.feedless.pipeline.plugins.EventsReportPluginParams
import org.migor.feedless.pipeline.plugins.toPluginExecutionJson
import org.migor.feedless.pipelineJob.PluginExecution
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryGuard
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.repository.nextCronDate
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.template.MailTemplateReportCreated
import org.migor.feedless.template.ReportCreatedParams
import org.migor.feedless.template.TemplateService
import org.migor.feedless.user.userId
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
  private val pipelinePlugins: PipelinePlugins,
  private val mailService: MailService,
  private val reportGuard: ReportGuard,
) {

  private val log = LoggerFactory.getLogger(ReportUseCase::class.simpleName)

  suspend fun createReport(repositoryId: RepositoryId, segment: SegmentCreate): Report = withContext(Dispatchers.IO) {
    log.info("createReport repositoryId=$repositoryId")

    repositoryGuard.requireWrite(repositoryId)

    val email = segment.recipientEmail

//      val isOwner = repository.ownerId == user?.id || repository.ownerId == resolveUserId()?.uuid
    // todo enable this
//    if (repository.visibility == EntityVisibility.isPrivate && !isOwner) {
//      throw IllegalArgumentException() // obscured access denied
//    }
    val startingAt = segment.startingAt

    val interval = when (segment.interval) {
      ChronoUnit.MONTHS -> Pair(ChronoUnit.MONTHS, "0 8 L * *")
      ChronoUnit.WEEKS -> Pair(ChronoUnit.WEEKS, "0 8 * * 0")
      else -> throw IllegalArgumentException("interval ${segment.interval}")
    }

    var segmentation = Segmentation(
      size = 200,
      repositoryId = repositoryId,
      timeSegmentStartingAt = startingAt,
      timeInterval = interval.first
    )

    segmentation = segment.near?.let {
      segmentation.copy(
        contentSegmentLatLon = it,
        contentSegmentLatLonDistance = segment.nearDistanceKm
      )
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

    val reporterPluginId = segment.reporterPluginId

    val plugin = pipelinePlugins.resolveById<ReportPlugin<*>>(reporterPluginId)!!
//      plugin.tryParseParams("{}") // validate
//      plugin.tryParseParams(reporterPlugin.params.toParams().paramsJsonString!!) // validate


    val report = Report(
      recipientName = segment.recipientName,
      recipientEmail = email,

      // send authorization mail
      authorizationAttempt = 1,
      lastRequestedAuthorization = LocalDateTime.now(),
      segmentId = segmentation.id,
      reporterPlugin = PluginExecution(
        id = reporterPluginId,
        params = PluginExecutionJson()
      ),
      cronScheduleId = cronSchedule.id,
      userId = coroutineContext.userId()
    )

    meterRegistry.counter(AppMetrics.createReport)
    sendReportCreatedMail(segment)
    reportRepository.save(report)
  }

  private suspend fun sendReportCreatedMail(segment: SegmentCreate) {
    val params = ReportCreatedParams(
      language = "de",
      deactivationLink = "",
      reportName = "",
      cronExpression = "",
      nextScheduledAt = "",
    )
    val body = templateService.renderTemplate(MailTemplateReportCreated(params))
    val mail = OutgoingMail(
      from = "no-reply@feedless.org",
      to = listOf(segment.recipientEmail),
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

        val segment = report.segment!!
        val (repository, documents) = resolveSegment(segment)

        resolveReporterPlugin(report.reporterPlugin)
          .report(
            documents, repository, EventsReportPluginParams(
              from = "no-reply@lokale.events",
              to = report.recipientEmail,
              subject = "",
              language = "de",
            ).toPluginExecutionJson(), LogCollector()
          )

      } catch (e: Exception) {
        log.error("Failed to process report job {}: {}", report.id, e.message, e)
        val next = nextCronDate(cron.cronExpression, cron.scheduledNextAt ?: now)
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

  private suspend fun resolveReporterPlugin(plugin: PluginExecution): ReportPlugin<*> =
    pipelinePlugins.resolveById<ReportPlugin<*>>(plugin.id)!!

  private fun resolveSegment(segment: Segmentation): Pair<Repository, List<Document>> {
    val repository = repositoryRepository.findById(segment.repositoryId)!!

    return Pair(repository, emptyList())
  }
}
