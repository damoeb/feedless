package org.migor.feedless.report

import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.NotFoundException
import org.migor.feedless.PageableRequest
import org.migor.feedless.actions.PluginExecutionJson
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.common.AppConfig
import org.migor.feedless.cronSchedule.CronSchedule
import org.migor.feedless.cronSchedule.CronScheduleRepository
import org.migor.feedless.document.DatesWhereInput
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.document.DocumentsFilter
import org.migor.feedless.document.GeoPointInput
import org.migor.feedless.document.GeoPointWhereInput
import org.migor.feedless.document.GeoPointWhereNearInput
import org.migor.feedless.document.RecordOrderBy
import org.migor.feedless.document.ReleaseStatus
import org.migor.feedless.document.SortOrder
import org.migor.feedless.document.StringFilter
import org.migor.feedless.mail.MailService
import org.migor.feedless.mail.OutgoingMail
import org.migor.feedless.pipeline.PipelinePlugins
import org.migor.feedless.pipeline.ReportPlugin
import org.migor.feedless.pipeline.plugins.EventsReportPluginParams
import org.migor.feedless.pipeline.plugins.toPluginExecutionJson
import org.migor.feedless.pipeline.resolveById
import org.migor.feedless.pipelineJob.PluginExecution
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryGuard
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.session.TokenIssuer
import org.migor.feedless.source.nextCronDate
import org.migor.feedless.template.MailTemplateReportConfirmRequest
import org.migor.feedless.template.MailTemplateReportCreated
import org.migor.feedless.template.ReportConfirmRequestParams
import org.migor.feedless.template.ReportCreatedParams
import org.migor.feedless.template.TemplateService
import org.migor.feedless.user.UserRepository
import org.migor.feedless.user.userIdMaybe
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


/** An unsubscribe link must still work from an old mail. */
private const val LINK_VALID_FOR_DAYS = 365L

/**
 * Friday 08:00, six fields as required by Spring's CronExpression - the
 * previously stored "0 8 * * 0" had five, and every schedule advance threw.
 */
const val WEEKLY_REPORT_CRON = "0 0 8 * * FRI"

/** Last day of the month, 08:00. */
const val MONTHLY_REPORT_CRON = "0 0 8 L * *"

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
  private val documentRepository: DocumentRepository,
  // Defaulted because app.mail.sender only exists in application-mail.yaml;
  // without it, a context with reports but without the mail profile wouldn't start.
  @param:Value("\${app.mail.sender:feedless-sender@localhost}") private val mailSender: String,
  private val appConfig: AppConfig,
  private val userRepository: UserRepository,
  private val tokenIssuer: TokenIssuer,
  private val reportRecipientRepository: ReportRecipientRepository,
  @Value("\${app.report.subscription-mode:opt-out}") subscriptionModeSetting: String,
) {

  private val subscriptionMode = ReportSubscriptionMode.parse(subscriptionModeSetting)

  /** The token names the report; holding it is the proof, since recipients usually have no account. */
  private fun deactivationLink(report: Report): String {
    val token = tokenIssuer.createJwtForReport(report.id.uuid.toString(), LINK_VALID_FOR_DAYS)
    return "${appConfig.apiGatewayUrl}${ApiUrls.reportDelete}/${report.id.uuid}?token=${token.tokenValue}"
  }

  private fun confirmationLink(report: Report): String {
    val token = tokenIssuer.createJwtForReport(report.id.uuid.toString(), LINK_VALID_FOR_DAYS)
    return "${appConfig.apiGatewayUrl}${ApiUrls.reportConfirm}/${report.id.uuid}?token=${token.tokenValue}"
  }

  private fun abuseLink(recipient: ReportRecipient): String {
    val token = tokenIssuer.createJwtForRecipient(recipient.id.uuid.toString(), LINK_VALID_FOR_DAYS)
    return "${appConfig.apiGatewayUrl}${ApiUrls.reportAbuse}/${recipient.id.uuid}?token=${token.tokenValue}"
  }

  /** A concurrent first subscription of the same address loses the insert and reads the winner. */
  private fun recipientFor(email: String): ReportRecipient {
    val normalized = normalizeEmail(email)
    return reportRecipientRepository.findByEmail(normalized)
      ?: runCatching { reportRecipientRepository.save(ReportRecipient(email = normalized)) }
        .getOrElse { reportRecipientRepository.findByEmail(normalized) ?: throw it }
  }

  private val log = LoggerFactory.getLogger(ReportUseCase::class.simpleName)

  suspend fun createReport(repositoryId: RepositoryId, segment: SegmentCreate): Report = withContext(Dispatchers.IO) {
    log.info("createReport repositoryId=$repositoryId")

    // Read access suffices: subscribing to a public repository is the normal
    // case, and the subscriber isn't its owner. requireWrite demanded
    // ownership and rejected every anonymous visitor. requireRead still
    // protects private repositories.
    repositoryGuard.requireRead(repositoryId)

    val recipient = recipientFor(segment.recipientEmail)

    // opt-out assumes no abuse; opt-in mode or an address whose owner reported abuse needs the owner's click
    val needsConfirmation = subscriptionMode == ReportSubscriptionMode.OPT_IN || recipient.optInRequired

    val startingAt = segment.startingAt

    val interval = when (segment.interval) {
      ChronoUnit.MONTHS -> Pair(ChronoUnit.MONTHS, MONTHLY_REPORT_CRON)
      ChronoUnit.WEEKS -> Pair(ChronoUnit.WEEKS, WEEKLY_REPORT_CRON)
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

    // The first run follows the cron expression's cadence rather than the
    // creation time, since the stored expression means Sunday 08:00.
    val nextReportedAt = nextCronDate(interval.second, startingAt)

    val cronSchedule = CronSchedule(
      cronExpression = interval.second,
      scheduledNextAt = nextReportedAt
    )

    cronScheduleRepository.save(cronSchedule)

    val reporterPluginId = segment.reporterPluginId

    val plugin = pipelinePlugins.resolveById<ReportPlugin<*>>(reporterPluginId)!!
//      plugin.tryParseParams("{}") // validate
//      plugin.tryParseParams(reporterPlugin.params.toParams().paramsJsonString!!) // validate


    val report = Report(
      recipientName = segment.recipientName,
      recipientEmail = recipient.email,

      authorized = !needsConfirmation,
      authorizedAt = if (needsConfirmation) null else LocalDateTime.now(),
      segmentId = segmentation.id,
      reporterPlugin = PluginExecution(
        id = reporterPluginId,
        params = PluginExecutionJson()
      ),
      cronScheduleId = cronSchedule.id,
      // An anonymous token carries a freshly invented UserId with no row in
      // t_user; stored as-is it would violate fk_report__to__user - and that
      // is exactly the path an accountless subscription takes.
      userId = coroutineContext.userIdMaybe()?.takeIf { userRepository.findById(it) != null }
    )

    meterRegistry.counter(AppMetrics.createReport)
    val saved = reportRepository.save(report)
    if (needsConfirmation) {
      sendConfirmationRequest(saved)
    } else {
      sendConfirmationMail(saved, recipient, nextReportedAt)
    }
    saved
  }

  private suspend fun sendConfirmationMail(report: Report, recipient: ReportRecipient, nextReportedAt: LocalDateTime) {
    val params = ReportCreatedParams(
      language = "de",
      deactivationLink = deactivationLink(report),
      abuseLink = abuseLink(recipient),
      reportName = report.recipientName,
      cronExpression = report.cronSchedule?.cronExpression ?: "",
      nextScheduledAt = nextReportedAt.toString(),
    )
    val body = templateService.renderTemplate(MailTemplateReportCreated(params))
    val mail = OutgoingMail(
      from = mailSender,
      to = listOf(report.recipientEmail),
      subject = "Dein Abo ist aktiv",
      htmlContent = body
    )
    mailService.send(mail)
  }

  private suspend fun sendConfirmationRequest(report: Report) {
    val body = templateService.renderTemplate(
      MailTemplateReportConfirmRequest(
        ReportConfirmRequestParams(
          language = "de",
          confirmationLink = confirmationLink(report),
        )
      )
    )
    mailService.send(
      OutgoingMail(
        from = mailSender,
        to = listOf(report.recipientEmail),
        subject = "Bitte bestätige dein Abo",
        htmlContent = body
      )
    )
  }

  /** From the confirmation link: the signed token is the proof, so this bypasses ReportGuard. */
  suspend fun confirmReportFromToken(reportId: ReportId) {
    withContext(Dispatchers.IO) {
      log.info("confirmReportFromToken reportId=$reportId")
      val report = reportRepository.findById(reportId) ?: throw NotFoundException("Report $reportId not found")
      // a report stopped by an abuse report stays stopped
      if (!report.authorized && !report.disabled) {
        reportRepository.save(report.copy(authorized = true, authorizedAt = LocalDateTime.now()))
      }
    }
  }

  /** From the abuse link: the address switches to opt-in and every report to it stops. */
  suspend fun reportAbuse(recipientId: ReportRecipientId) {
    withContext(Dispatchers.IO) {
      log.info("reportAbuse recipientId=$recipientId")
      val recipient = reportRecipientRepository.findById(recipientId)
        ?: throw NotFoundException("Recipient $recipientId not found")
      if (!recipient.optInRequired) {
        reportRecipientRepository.save(recipient.copy(optInRequired = true))
      }
      reportRepository.disableAllByRecipientEmail(recipient.email, LocalDateTime.now())
    }
  }

  /** For the cancel link in every mail: the signed token is the proof, so this bypasses ReportGuard. */
  suspend fun deleteReportFromToken(reportId: ReportId) = withContext(Dispatchers.IO) {
    log.info("deleteReportFromToken reportId=$reportId")
    reportRepository.deleteById(reportId)
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
        sendReport(report)
      } catch (e: Exception) {
        log.error("Failed to process report job {}: {}", report.id, e.message, e)
      } finally {
        // Advance the schedule after a successful send too - previously that
        // happened only on failure, so a delivered report was resent 60
        // seconds later on the next run. If advancing fails, it must not take
        // down the rest of this run's reports.
        try {
          withContext(Dispatchers.IO) {
            cronScheduleRepository.save(
              cron.copy(
                scheduledNextAt = nextRun(cron.cronExpression, now),
                executedLastAt = now
              )
            )
          }
        } catch (e: Exception) {
          log.error("Failed to advance the schedule of report {}: {}", report.id, e.message, e)
        }
      }
    }
  }

  private suspend fun sendReport(report: Report) {
    val (repository, documents) = resolveSegment(report.segment!!)
    if (!mayReceive(report, repository)) {
      log.info("skipping report {}: repository {} is private and not owned by its recipient", report.id, repository.id)
      return
    }

    resolveReporterPlugin(report.reporterPlugin)
      .report(
        documents, repository, EventsReportPluginParams(
          from = mailSender,
          to = report.recipientEmail,
          subject = repository.title,
          language = "de",
          // The backend knows no product: it just passes the variant name
          // through, and template resolution decides whether a dedicated
          // template exists for it.
          templateVariant = repository.product.name,
          deactivationLink = deactivationLink(report),
          abuseLink = abuseLink(recipientFor(report.recipientEmail)),
        ).toPluginExecutionJson(), LogCollector()
      )
  }

  /**
   * A subscription to a repository that later turns private must not keep
   * sending its content to a foreign address - only its owner receives
   * reports from a private repository.
   */
  private fun mayReceive(report: Report, repository: Repository): Boolean =
    repository.visibility == EntityVisibility.isPublic || repository.ownerId == report.userId

  /**
   * The next run, always computed from now: computed from a far-past run it
   * would stay due and resend every minute until it caught up. Legacy rows
   * still carry the previously stored empty string - that falls back to the
   * weekly default instead of making the advance throw.
   */
  private fun nextRun(cronExpression: String, now: LocalDateTime): LocalDateTime =
    runCatching { nextCronDate(cronExpression, now) }
      .getOrElse {
        log.warn("invalid cron expression '{}', falling back to {}", cronExpression, WEEKLY_REPORT_CRON)
        nextCronDate(WEEKLY_REPORT_CRON, now)
      }

  private suspend fun resolveReporterPlugin(plugin: PluginExecution): ReportPlugin<*> =
    pipelinePlugins.resolveById<ReportPlugin<*>>(plugin.id)!!

  /**
   * Translates the [SegmentSpec] into a document query. The spec is where a
   * future recommendation profile will hook in - this just executes what it
   * describes.
   */
  private fun resolveSegment(segment: Segmentation): Pair<Repository, List<Document>> {
    val repository = repositoryRepository.findById(segment.repositoryId)!!
    val spec = segment.toSpec(LocalDateTime.now())

    val documents = documentRepository.findAllFiltered(
      repositoryId = spec.repositoryId,
      filter = spec.toDocumentsFilter(),
      orderBy = RecordOrderBy(startedAt = SortOrder.ASC),
      status = ReleaseStatus.released,
      tags = spec.tags,
      pageable = PageableRequest(pageNumber = 0, pageSize = spec.maxSize),
    )

    return Pair(repository, documents)
  }

  private fun SegmentSpec.toDocumentsFilter(): DocumentsFilter = DocumentsFilter(
    repository = repositoryId,
    startedAt = DatesWhereInput(after = from, before = until),
    latLng = near?.let {
      GeoPointWhereInput(
        near = GeoPointWhereNearInput(
          point = GeoPointInput(lat = it.lat, lng = it.lng),
          distanceKm = it.distanceKm,
        ),
      )
    },
    tags = tags.takeIf { it.isNotEmpty() }?.let { StringFilter(`in` = it) },
  )
}
