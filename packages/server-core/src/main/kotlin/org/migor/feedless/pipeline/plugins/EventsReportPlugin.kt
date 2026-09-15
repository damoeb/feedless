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

/**
 * What a report mail shows for an event, already formatted.
 *
 * Pre-formatted because Freemarker wraps a LocalDateTime as a plain string:
 * startingAt?string("...") failed on that, the plugin threw, and the report
 * never went out as soon as it contained even one event. Date only, no time,
 * since in existing data the time is mostly just a pipeline default.
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
  val abuseLink: String,
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
      abuseLink = params.abuseLink ?: "",
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
