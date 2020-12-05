package org.migor.rss.rich.harvest

interface ContentStrategy {
  fun canProcess(harvestResponse: HarvestResponse): Boolean
  fun process(harvestResponse: HarvestResponse): HarvestContent
}
