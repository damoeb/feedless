package org.migor.feedless.pipeline

import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.jpa.document.DocumentEntity
import org.migor.feedless.jpa.report.ReportEntity
import org.migor.feedless.jpa.repository.RepositoryEntity
import org.migor.feedless.scrape.LogCollector

interface ReportPlugin : FeedlessPlugin {

  suspend fun report(
    documents: List<DocumentEntity>,
    repository: RepositoryEntity,
    params: PluginExecutionParamsInput,
    logCollector: LogCollector
  )

  suspend fun askForAuthorization(report: ReportEntity): ReportEntity
}
