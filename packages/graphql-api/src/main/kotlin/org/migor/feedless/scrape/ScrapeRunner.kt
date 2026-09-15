package org.migor.feedless.scrape

import org.migor.feedless.source.Source

// Lives here, not in domain: ScrapeOutput carries generated GraphQL types.
interface ScrapeRunner {
  suspend fun scrape(source: Source, logCollector: LogCollector): ScrapeOutput
}
