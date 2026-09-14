package org.migor.feedless.report

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.EntityVisibility
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.common.AppConfig
import org.migor.feedless.session.TokenIssuer
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.actions.PluginExecutionJson
import org.migor.feedless.cronSchedule.CronSchedule
import org.migor.feedless.cronSchedule.CronScheduleRepository
import org.migor.feedless.PageableRequest
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentsFilter
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.document.DatesWhereInput
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
import org.migor.feedless.user.UserRepository
import org.migor.feedless.user.userIdMaybe
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


/** Ein Abmeldelink soll auch in einer alten Mail noch funktionieren. */
private const val LINK_VALID_FOR_DAYS = 365L

/**
 * Freitag 08:00. Sechs Felder, wie Springs CronExpression sie verlangt - der
 * früher gespeicherte Ausdruck "0 8 * * 0" hatte fünf, und jede Fortschreibung
 * des Termins warf.
 */
const val WEEKLY_REPORT_CRON = "0 0 8 * * FRI"

/** Letzter Tag des Monats, 08:00. */
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
  // Mit Vorgabewert: app.mail.sender steht nur in application-mail.yaml, und
  // ohne das mail-Profil startete sonst kein Kontext, der Reports enthält.
  @Value("\${app.mail.sender:feedless-sender@localhost}") private val mailSender: String,
  private val appConfig: AppConfig,
  private val userRepository: UserRepository,
  private val tokenIssuer: TokenIssuer,
  private val reportRecipientRepository: ReportRecipientRepository,
) {

  /** The token names the report; holding it is the proof, since recipients usually have no account. */
  private fun deactivationLink(report: Report): String {
    val token = tokenIssuer.createJwtForReport(report.id.uuid.toString(), LINK_VALID_FOR_DAYS)
    return "${appConfig.apiGatewayUrl}${ApiUrls.reportDelete}/${report.id.uuid}?token=${token.tokenValue}"
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

    // Lesen genügt: ein Abo auf ein öffentliches Repository ist der Normalfall,
    // und sein Abonnent ist nicht dessen Eigentümer. requireWrite verlangte
    // Eigentümerschaft und wies damit jeden anonymen Besucher ab. Private
    // Repositories schützt requireRead weiterhin.
    repositoryGuard.requireRead(repositoryId)

    val recipient = recipientFor(segment.recipientEmail)

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

    // Der erste Termin fällt auf den Takt des Ausdrucks. Vorher lag er auf dem
    // nächsten Freitag zur Uhrzeit des Anlegens, während der gespeicherte
    // Ausdruck Sonntag 08:00 meinte.
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

      // no opt-in step: active at once, and every mail carries a cancel link
      authorized = true,
      authorizedAt = LocalDateTime.now(),
      segmentId = segmentation.id,
      reporterPlugin = PluginExecution(
        id = reporterPluginId,
        params = PluginExecutionJson()
      ),
      cronScheduleId = cronSchedule.id,
      // Ein anonymes Token trägt eine frisch erfundene UserId, zu der keine
      // Zeile in t_user gehört. Gespeichert verletzt sie fk_report__to__user,
      // und genau das ist der Weg, den ein Abo ohne Konto nimmt.
      userId = coroutineContext.userIdMaybe()?.takeIf { userRepository.findById(it) != null }
    )

    meterRegistry.counter(AppMetrics.createReport)
    val saved = reportRepository.save(report)
    sendConfirmationMail(saved, recipient, nextReportedAt)
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
        // Auch nach einem erfolgreichen Versand fortschreiben. Vorher geschah
        // das nur im Fehlerfall, wodurch ein zugestellter Report beim nächsten
        // Lauf 60 Sekunden später erneut verschickt wurde. Scheitert die
        // Fortschreibung, darf das die übrigen Reports des Laufs nicht
        // mitreissen.
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
          // Das Backend kennt kein Produkt: es reicht den Variantennamen
          // durch, und die Vorlagenauflösung entscheidet, ob es dafür eine
          // eigene Vorlage gibt.
          templateVariant = repository.product.name,
          deactivationLink = deactivationLink(report),
          abuseLink = abuseLink(recipientFor(report.recipientEmail)),
        ).toPluginExecutionJson(), LogCollector()
      )
  }

  /**
   * Ein Abo auf ein Repository, das später privat wird, darf nicht weiter
   * dessen Inhalte an eine fremde Adresse schicken. Aus einem privaten
   * Repository bekommt nur sein Eigentümer Reports.
   */
  private fun mayReceive(report: Report, repository: Repository): Boolean =
    repository.visibility == EntityVisibility.isPublic || repository.ownerId == report.userId

  /**
   * Der nächste Termin, immer ab jetzt gerechnet: ab einem weit
   * zurückliegenden Termin gerechnet bliebe der Report fällig und ginge jede
   * Minute erneut raus, bis er aufgeholt hätte. Bestandszeilen tragen noch den
   * früher gespeicherten Leerstring - dann gilt der wöchentliche Standard,
   * statt dass die Fortschreibung wirft.
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
   * Übersetzt die [SegmentSpec] in eine Dokumentabfrage. Die Spec ist die
   * Stelle, an der später das Empfehlungsprofil andockt - hier wird nur noch
   * ausgeführt, was sie beschreibt.
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
