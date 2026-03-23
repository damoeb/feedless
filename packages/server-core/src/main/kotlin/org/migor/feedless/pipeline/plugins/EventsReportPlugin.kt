package org.migor.feedless.pipeline.plugins

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
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
  @SerializedName("language") val language: String,
  @SerializedName("deactivationLink") val deactivationLink: String,
)

data class EventCalendarMailParams(
  val events: List<Document>,
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
    logCollector.log("report ${documents.size}")

    val params = EventCalendarMailParams(
      events = documents
    )
    val eventCalendarMail = templateService.renderTemplate(MailTemplateEventCalendar(params))
    mailService.send(
      OutgoingMail(
        from = "",
        to = listOf(),
        subject = "",
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
    logCollector.log("report ${documents.size}")
    report(documents, repository, EventsReportPluginParams(language = "de", deactivationLink = ""), logCollector)
//    report(documents, repository, tryParseParams(params), logCollector)
  }

  override suspend fun tryParseParams(jsonParams: String): EventsReportPluginParams {
    return Gson().fromJson(jsonParams, EventsReportPluginParams::class.java)
  }

}
