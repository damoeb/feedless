package org.migor.feedless.repository

import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.harvest.Harvest
import org.migor.feedless.harvest.HarvestRepository
import org.migor.feedless.harvest.HarvestStatus
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.scrape.ScrapeOutput
import org.migor.feedless.scrape.ScrapeService
import org.migor.feedless.source.Source
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * Dry runs: scrape a source to see what it would yield, without importing records and without
 * touching the source — no error state, no `lastRefreshedAt`. The user iterates on a flow this
 * way before saving it.
 */
@Service
@Profile("${AppProfiles.repository} & ${AppLayer.service} & ${AppLayer.scheduler}")
class SourceDryRunner(
  private val scrapeService: ScrapeService,
  private val harvestRepository: HarvestRepository,
) {

  private val log = LoggerFactory.getLogger(SourceDryRunner::class.simpleName)

  /**
   * Scrapes [source] — whose actions may be an unsaved override flow — and records on [harvest]
   * the scrape log plus a summary of the extracted items. `itemsAdded` is the number of items
   * extracted. `errornous` is set when the scrape failed or extracted no items — a flow that
   * yields nothing does not work. [harvest] is saved as [HarvestStatus.COMPLETED], also when the
   * scrape fails.
   */
  suspend fun dryRun(source: Source, harvest: Harvest): Harvest {
    val logCollector = LogCollector()
    var outcome = harvest
    var summary: String? = null
    try {
      val items = extractedItems(scrapeService.scrape(source, logCollector))
      if (items.isEmpty()) {
        // A broken selector usually yields nothing rather than an exception — that is what a
        // dry run must catch, so extracting nothing fails it.
        logCollector.log("dry run extracted no items")
      }
      outcome = outcome.copy(itemsAdded = items.size, errornous = items.isEmpty())
      summary = summarizeExtractedItems(items)
    } catch (e: Throwable) {
      log.info("dry run of source ${source.id} failed: ${e.message}")
      logCollector.log("scrape failed ${e.message}")
      outcome = outcome.copy(errornous = true)
    } finally {
      outcome = outcome.copy(
        status = HarvestStatus.COMPLETED,
        finishedAt = LocalDateTime.now(),
        logs = logCollector.toHarvestLog(summary),
      )
      harvestRepository.save(outcome)
    }
    return outcome
  }
}

internal data class ExtractedItem(val title: String?, val url: String?)

/** What a real run would import from [output]: the items of the last action, or else its fragments. */
internal fun extractedItems(output: ScrapeOutput): List<ExtractedItem> {
  val fragment = output.outputs.lastOrNull()?.fragment ?: return emptyList()
  val items = fragment.items.orEmpty()
  if (items.isNotEmpty()) {
    return items.map { ExtractedItem(title = it.title, url = it.url) }
  }
  return fragment.fragments.orEmpty().map {
    ExtractedItem(title = StringUtils.abbreviate(it.text?.data ?: it.html?.data, 120), url = null)
  }
}

internal fun summarizeExtractedItems(items: List<ExtractedItem>): String =
  buildString {
    append("dry run extracted ${items.size} item(s)")
    items.forEachIndexed { index, item ->
      append("\n${index + 1}. ${item.title?.ifBlank { null } ?: "(no title)"}  ${item.url?.ifBlank { null } ?: "(no url)"}")
    }
  }
