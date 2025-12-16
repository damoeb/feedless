package org.migor.feedless.pipeline

import org.migor.feedless.document.Document
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.report.Report
import org.migor.feedless.repository.Repository
import org.migor.feedless.scrape.LogCollector

interface ReportPlugin : Plugin {

  suspend fun report(
    documents: List<Document>,
    repository: Repository,
    params: PluginExecutionParamsInput,
    logCollector: LogCollector
  )

  suspend fun askForAuthorization(report: Report): Report
}
