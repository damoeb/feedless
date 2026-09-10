package org.migor.feedless.report

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.NotFoundException
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.common.PropertyService
import org.migor.feedless.session.JwtTokenIssuer
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
import org.migor.feedless.generated.types.IntervalUnit
import org.migor.feedless.generated.types.SegmentInput
import org.migor.feedless.geo.LatLonPoint
import org.migor.feedless.mail.MailService
import org.migor.feedless.mail.OutgoingMail
import org.migor.feedless.pipeline.PluginService
import org.migor.feedless.pipeline.ReportPlugin
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
import org.migor.feedless.template.TemplateVariant
import org.migor.feedless.user.UserRepository
import org.migor.feedless.user.userIdMaybe
import org.migor.feedless.util.toLocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters


/** Ein Abmeldelink soll auch in einer alten Mail noch funktionieren. */
private const val LINK_VALID_FOR_DAYS = 365L

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
  private val documentRepository: DocumentRepository,
  @Value("\${app.mail.sender}") private val mailSender: String,
  private val propertyService: PropertyService,
  private val userRepository: UserRepository,
  private val jwtTokenIssuer: JwtTokenIssuer,
) {

  /**
   * Beide Links tragen dasselbe Token: es nennt den Report, und sein Besitz
   * ist der Nachweis. Ein Jahr Gültigkeit, damit ein Abmeldelink auch in einer
   * alten Mail noch funktioniert.
   */
  private fun confirmationLink(report: Report): String =
    reportLink(ApiUrls.reportConfirm, report)

  private fun deactivationLink(report: Report): String =
    reportLink(ApiUrls.reportDelete, report)

  private fun reportLink(path: String, report: Report): String {
    val token = jwtTokenIssuer.createJwtForReport(report.id.uuid.toString(), LINK_VALID_FOR_DAYS)
    return "${propertyService.apiGatewayUrl}$path/${report.id.uuid}?token=${token.tokenValue}"
  }

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
      cronExpression = interval.second,
      scheduledNextAt = nextReportedAt
    )

    cronScheduleRepository.save(cronSchedule)

    val reporterPlugin = segment.report.plugin

    val plugin = pluginService.resolveById<ReportPlugin<*>>(reporterPlugin.pluginId)!!
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
    sendAuthorizationMail(saved, nextReportedAt)
    saved
  }

  /**
   * Genau eine Anfrage, keine Erinnerungen. Wer nicht bestätigt, bekommt
   * nichts - der Report bleibt unbestätigt liegen.
   */
  private suspend fun sendAuthorizationMail(report: Report, nextReportedAt: LocalDateTime) {
    val params = ReportCreatedParams(
      language = "de",
      deactivationLink = deactivationLink(report),
      confirmationLink = confirmationLink(report),
      reportName = report.recipientName,
      cronExpression = report.cronSchedule?.cronExpression ?: "",
      nextScheduledAt = nextReportedAt.toString(),
    )
    val body = templateService.renderTemplate(MailTemplateReportCreated(params))
    val mail = OutgoingMail(
      from = mailSender,
      to = listOf(report.recipientEmail),
      subject = "Bitte bestätige dein Abo",
      htmlContent = body
    )
    mailService.send(mail)
  }

  /**
   * Für den Link aus der Mail. Der Besitz des signierten Tokens ist hier der
   * Nachweis - der Empfänger ist typischerweise nicht angemeldet, deshalb
   * läuft dieser Pfad bewusst nicht über den ReportGuard.
   */
  suspend fun confirmReportFromToken(reportId: ReportId) = withContext(Dispatchers.IO) {
    log.info("confirmReportFromToken reportId=$reportId")
    val report = reportRepository.findById(reportId) ?: throw NotFoundException("Report $reportId not found")
    if (!report.authorized) {
      reportRepository.save(
        report.copy(
          authorized = true,
          authorizedAt = LocalDateTime.now(),
        )
      )
    }
  }

  /** Wie [confirmReportFromToken]: der Link ist der Nachweis. */
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
        val segment = report.segment!!
        val (repository, documents) = resolveSegment(segment)

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
            ).toPluginExecutionJson(), LogCollector()
          )
      } catch (e: Exception) {
        log.error("Failed to process report job {}: {}", report.id, e.message, e)
      } finally {
        // Auch nach einem erfolgreichen Versand fortschreiben. Vorher geschah
        // das nur im Fehlerfall, wodurch ein zugestellter Report beim nächsten
        // Lauf 60 Sekunden später erneut verschickt wurde - endlos.
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
    pluginService.resolveById<ReportPlugin<*>>(plugin.id)!!

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
