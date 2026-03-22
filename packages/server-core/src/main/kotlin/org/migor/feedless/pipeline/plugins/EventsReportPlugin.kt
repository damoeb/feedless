package org.migor.feedless.pipeline.plugins

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.document.Document
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.pipeline.ReportPlugin
import org.migor.feedless.repository.Repository
import org.migor.feedless.scrape.LogCollector
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

/**
 * Mail reports
 *
 * when:
 * - asap
 * - scheduled
 *
 * mails:
 * - welcome
 * - report
 * - good bye
 */

data class EventsReportPluginParams(
  @SerializedName("foo") val foo: Boolean,
)


@Service
@Profile("${AppProfiles.DEV_ONLY} & ${AppProfiles.scrape} & ${AppLayer.service}")
class EventsReportPlugin : ReportPlugin<EventsReportPluginParams> {

  private val log = LoggerFactory.getLogger(EventsReportPlugin::class.simpleName)

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
  }

  override suspend fun report(
    documents: List<Document>,
    repository: Repository,
    params: String,
    logCollector: LogCollector
  ) {
    logCollector.log("report ${documents.size}")
    report(documents, repository, tryParseParams(params), logCollector)
  }

  override suspend fun tryParseParams(jsonParams: String): EventsReportPluginParams {
    return Gson().fromJson(jsonParams, EventsReportPluginParams::class.java)
  }

}
