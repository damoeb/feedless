package org.migor.feedless.scrape

import org.migor.feedless.source.Source

interface Scraper {
  /** Runs the source's flow; the harvester only needs the last action's fragment. */
  suspend fun scrape(source: Source, logCollector: LogCollector): ScrapeResult
}
