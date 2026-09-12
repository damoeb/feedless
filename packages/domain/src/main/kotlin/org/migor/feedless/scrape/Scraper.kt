package org.migor.feedless.scrape

import org.migor.feedless.common.HttpResponse
import org.migor.feedless.source.Source

interface Scraper {
  /** Runs the source's flow; the harvester only needs the last action's fragment. */
  suspend fun scrape(source: Source, logCollector: LogCollector): ScrapeResult

  /** Runs the source's flow and returns the first fetch action's response. */
  suspend fun fetch(source: Source, logCollector: LogCollector): HttpResponse
}
