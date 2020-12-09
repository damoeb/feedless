package org.migor.rss.rich.harvest

interface ContentStrategy {
  fun canProcess(response: HarvestResponse): Boolean
  fun process(response: HarvestResponse): RichFeed
}
