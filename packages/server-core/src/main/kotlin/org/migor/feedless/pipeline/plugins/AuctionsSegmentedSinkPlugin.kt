package org.migor.feedless.pipeline.plugins

import com.google.gson.Gson
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
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.template.FreemarkerTemplate
import org.migor.feedless.template.TemplateService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service


data class AuctionsReportPluginParams(
  val language: String,
  val from: String,
  val to: String,
  val subject: String,
)

data class AuctionsReportMailParams(
  val language: String,
  val documents: List<Document>,
  val deactivationLink: String,
)

data class MailTemplateAuctionsReport(override val params: AuctionsReportMailParams) :
  FreemarkerTemplate<AuctionsReportMailParams>("mail-auctions-report")


@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.service}")
class AuctionsSegmentedSinkPlugin(
  private val mailService: MailService,
  private val templateService: TemplateService,
  private val repositoryRepository: RepositoryRepository,
  documentRepository: DocumentRepository,
) : AbstractSegmentedSinkPlugin(documentRepository) {

  private val log = LoggerFactory.getLogger(AuctionsSegmentedSinkPlugin::class.simpleName)

  override fun id(): String = FeedlessPlugins.org_feedless_auctions_report.name
  override fun name(): String = ""

  override fun listed(): Boolean = false

  override suspend fun report(report: Report) {

    val segment = report.segment!!
    val documents = getSegmentOfDocuments(segment)
      .filter { true } // todo apply plugin specific filters

    val params = fromPluginExecutionJson(report.reporterPlugin.params)

//    val repoitory = repositoryRepository.findById(report.segment!!.repositoryId)

    val templateParams = AuctionsReportMailParams(
      language = params.language,
      documents = documents,
      deactivationLink = "",
    )
    val auctionsReportMail = templateService.renderTemplate(MailTemplateAuctionsReport(templateParams))
    mailService.send(
      OutgoingMail(
        from = "no-reply@lokale.events",
        to = listOf(report.recipientEmail),
        subject = "Auktionen {place} ",
        htmlContent = auctionsReportMail
      )
    )
  }

  private fun fromPluginExecutionJson(params: PluginExecutionJson): AuctionsReportPluginParams {
    return Gson().fromJson(params.paramsJsonString, AuctionsReportPluginParams::class.java)
  }
}
