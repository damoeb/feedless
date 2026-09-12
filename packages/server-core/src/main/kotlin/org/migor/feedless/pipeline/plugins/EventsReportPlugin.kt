package org.migor.feedless.pipeline.plugins

import com.google.gson.Gson
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.actions.PluginExecutionJson
import org.migor.feedless.document.Document
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.mail.MailService
import org.migor.feedless.mail.OutgoingMail
import org.migor.feedless.pipeline.ReportPlugin
import org.migor.feedless.repository.Repository
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.template.FreemarkerTemplate
import org.migor.feedless.template.TemplateService
import org.migor.feedless.template.TemplateVariant
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter
import java.util.Locale

data class EventsReportPluginParams(
  val language: String,
  val from: String,
  val to: String,
  val subject: String,
  /**
   * Name der Vorlagenvariante, üblicherweise das Produkt des Repositories.
   * Der Versandpfad kennt kein Produkt - er reicht den Namen nur durch, und
   * die Vorlagenauflösung entscheidet, ob es dafür eine eigene Vorlage gibt.
   */
  val templateVariant: String? = null,
  /** Der Abmeldelink dieses Reports. Ohne ihn ging jede Mail mit href="" raus. */
  val deactivationLink: String? = null,
)

fun EventsReportPluginParams.toPluginExecutionJson(): PluginExecutionJson {
  return PluginExecutionJson(
    paramsJsonString = Gson().toJson(this)
  )
}


/**
 * Was eine Report-Mail von einem Event zeigt, bereits formatiert.
 *
 * Vorformatiert, weil Freemarker ein LocalDateTime als Zeichenkette einpackt:
 * startingAt?string("...") scheiterte daran, das Plugin warf, und der Report
 * ging nie raus - sobald er auch nur ein Event enthielt. Nur das Datum, keine
 * Uhrzeit: im Bestand ist sie überwiegend ein Default der Pipeline.
 */
data class ReportEventItem(
  val title: String,
  val url: String,
  val date: String,
)

internal fun Document.toReportEventItem(locale: Locale): ReportEventItem = ReportEventItem(
  title = title.orEmpty(),
  url = url.orEmpty(),
  date = startingAt?.format(DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy", locale)).orEmpty(),
)

data class EventCalendarMailParams(
  val language: String,
  val events: List<ReportEventItem>,
  val deactivationLink: String,
)

data class MailTemplateEventCalendar(override val params: EventCalendarMailParams) :
  FreemarkerTemplate<EventCalendarMailParams>("mail-event-calendar")


@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.service}")
class EventsReportPlugin() : ReportPlugin<EventsReportPluginParams> {

  private val log = LoggerFactory.getLogger(EventsReportPlugin::class.simpleName)

  @Autowired
  private lateinit var mailService: MailService

  @Autowired
  private lateinit var templateService: TemplateService

  override fun id(): String = FeedlessPlugins.org_feedless_event_report.name
  override fun name(): String = ""
  override fun listed() = false

  override suspend fun report(
    documents: List<Document>,
    repository: Repository,
    params: EventsReportPluginParams,
    logCollector: LogCollector
  ) {
    logCollector.log("event-report ${documents.size}")

    val templateParams = EventCalendarMailParams(
      language = params.language,
      events = documents.map { it.toReportEventItem(Locale.forLanguageTag(params.language)) },
      deactivationLink = params.deactivationLink ?: "",
    )
    val eventCalendarMail = templateService.renderTemplate(
      MailTemplateEventCalendar(templateParams),
      params.templateVariant?.let { TemplateVariant(it) },
    )
    mailService.send(
      OutgoingMail(
        from = params.from,
        to = listOf(params.to),
        subject = params.subject,
        htmlContent = eventCalendarMail
      )
    )
  }

  override suspend fun report(
    documents: List<Document>,
    repository: Repository,
    params: PluginExecutionJson,
    logCollector: LogCollector
  ) {
    return report(documents, repository, fromPluginExecutionJson(params), logCollector)
  }

  private fun fromPluginExecutionJson(params: PluginExecutionJson): EventsReportPluginParams {
    return Gson().fromJson(params.paramsJsonString, EventsReportPluginParams::class.java)
  }

}
