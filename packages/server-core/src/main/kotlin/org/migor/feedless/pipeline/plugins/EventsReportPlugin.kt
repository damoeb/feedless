package org.migor.feedless.pipeline.plugins

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.jpa.document.DocumentEntity
import org.migor.feedless.jpa.report.ReportEntity
import org.migor.feedless.jpa.repository.RepositoryEntity
import org.migor.feedless.pipeline.ReportPlugin
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.user.corrId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import kotlin.coroutines.coroutineContext

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.DEV_ONLY} & ${AppProfiles.scrape} & ${AppLayer.service}")
class EventsReportPlugin : ReportPlugin {

  private val log = LoggerFactory.getLogger(EventsReportPlugin::class.simpleName)

  override fun id(): String = FeedlessPlugins.org_feedless_event_report.name
  override fun name(): String = ""
  override fun listed() = false

  override suspend fun report(
    documents: List<DocumentEntity>,
    repository: RepositoryEntity,
    params: PluginExecutionParamsInput,
    logCollector: LogCollector
  ) {
    val corrId = coroutineContext.corrId()
    logCollector.log("[$corrId] report ${documents.size}")

  }

  override suspend fun askForAuthorization(report: ReportEntity): ReportEntity {
    TODO("Not yet implemented")
  }

}
