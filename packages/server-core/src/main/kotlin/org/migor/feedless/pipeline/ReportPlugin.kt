package org.migor.feedless.pipeline

import org.migor.feedless.actions.PluginExecutionJson
import org.migor.feedless.document.Document
import org.migor.feedless.pipeline.plugins.EventsReportPluginParams
import org.migor.feedless.repository.Repository
import org.migor.feedless.scrape.LogCollector

interface ReportPlugin<T> : Plugin {

  suspend fun report(
    documents: List<Document>,
    repository: Repository,
    params: T,
    logCollector: LogCollector
  )

  suspend fun report(
    documents: List<Document>,
    repository: Repository,
    params: PluginExecutionJson,
    logCollector: LogCollector
  )

  suspend fun tryParseParams(jsonParams: String): EventsReportPluginParams
}
