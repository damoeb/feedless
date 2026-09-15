package org.migor.feedless.scrape

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.generated.types.ScrapeExtractFragment
import org.migor.feedless.generated.types.ScrapeExtractFragmentPart
import org.migor.feedless.pipeline.FragmentOutput
import org.migor.feedless.source.Source
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

// ScrapeService can't implement Scraper itself: its scrape(source, logCollector) already returns ScrapeOutput.
@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.service}")
class ScraperAdapter(private val scrapeService: ScrapeService) : Scraper {

  override suspend fun scrape(source: Source, logCollector: LogCollector): ScrapeResult {
    val output = scrapeService.scrape(source, logCollector)
    return ScrapeResult(
      actionCount = output.outputs.size,
      lastFragment = output.outputs.lastOrNull()?.fragment?.toScrapedFragmentOutput(),
    )
  }

  override suspend fun fetch(source: Source, logCollector: LogCollector): HttpResponse =
    scrapeService.scrape(source, logCollector).outputs.find { it.fetch != null }!!.fetch!!.response
}

private fun FragmentOutput.toScrapedFragmentOutput(): ScrapedFragmentOutput =
  ScrapedFragmentOutput(
    items = items,
    fragments = fragments?.map { it.toScrapedFragment() },
  )

private fun ScrapeExtractFragment.toScrapedFragment(): ScrapedFragment =
  ScrapedFragment(
    html = html?.data,
    text = text?.data,
    data = data?.let { ScrapedData(mimeType = it.mimeType, data = it.data) },
    uniqueBy = when (uniqueBy) {
      ScrapeExtractFragmentPart.html -> ScrapedFragmentPart.html
      ScrapeExtractFragmentPart.text -> ScrapedFragmentPart.text
      ScrapeExtractFragmentPart.data -> ScrapedFragmentPart.data
    },
  )
