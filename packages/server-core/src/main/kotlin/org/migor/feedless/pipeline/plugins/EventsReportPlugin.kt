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
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

data class EventsReportPluginParams(
  val language: String,
  val from: String,
  val to: String,
  val subject: String,
)

fun EventsReportPluginParams.toPluginExecutionJson(): PluginExecutionJson {
  return PluginExecutionJson(
    paramsJsonString = Gson().toJson(this)
  )
}


data class EventCalendarMailParams(
  val language: String,
  val events: List<Document>,
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
      events = documents,
      deactivationLink = "",
    )
    val eventCalendarMail = templateService.renderTemplate(MailTemplateEventCalendar(templateParams))
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
