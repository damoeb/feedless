package org.migor.feedless.pipeline.plugins

import com.google.gson.Gson
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.actions.PluginExecutionJson
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.mail.MailService
import org.migor.feedless.mail.OutgoingMail
import org.migor.feedless.pipeline.AbstractSegmentedSinkPlugin
import org.migor.feedless.report.Report
import org.migor.feedless.report.ReportDeactivationLinkFactory
import org.migor.feedless.template.FreemarkerTemplate
import org.migor.feedless.template.TemplateService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

data class EventsReportPluginParams(
  val language: String
)

data class EventCalendarMailParams(
  val language: String,
  val events: List<Document>,
  val deactivationLink: String,
)

data class MailTemplateEventCalendar(override val params: EventCalendarMailParams) :
  FreemarkerTemplate<EventCalendarMailParams>("mail-event-calendar")


@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.service}")
class EventsSegmentedSinkPlugin(
  documentRepository: DocumentRepository,
  private val mailService: MailService,
  private val templateService: TemplateService,
  private val reportDeactivationLinkFactory: ReportDeactivationLinkFactory,
) : AbstractSegmentedSinkPlugin(documentRepository) {

  private val log = LoggerFactory.getLogger(EventsSegmentedSinkPlugin::class.simpleName)

  override fun id(): String = FeedlessPlugins.org_feedless_event_report.name
  override fun name(): String = ""
  override fun listed() = false

  override suspend fun report(report: Report) {

    val segment = report.segment!!
    val documents = getSegmentOfDocuments(segment)
    val params = fromPluginExecutionJson(report.reporterPlugin.params)

//    val repoitory = repositoryRepository.findById(report.segment!!.repositoryId)

    val templateParams = EventCalendarMailParams(
      language = params.language,
      events = documents,
      deactivationLink = reportDeactivationLinkFactory.createLink(report),
    )
    val eventCalendarMail = templateService.renderTemplate(MailTemplateEventCalendar(templateParams))
    mailService.send(
      OutgoingMail(
        from = "no-reply@lokale.events",
        to = listOf(report.recipientEmail),
        subject = "Events Report für Thalwil Woche 12",
        htmlContent = eventCalendarMail
      )
    )
  }

  private fun fromPluginExecutionJson(params: PluginExecutionJson): EventsReportPluginParams {
    return StringUtils.trimToNull(params.paramsJsonString)
      ?.let { Gson().fromJson(params.paramsJsonString, EventsReportPluginParams::class.java) }
      ?: EventsReportPluginParams(language = "de")
  }
}
