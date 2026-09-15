package org.migor.feedless.scrape

import org.migor.feedless.feed.parser.json.JsonItem

data class ScrapeResult(val actionCount: Int, val lastFragment: ScrapedFragmentOutput?)

data class ScrapedFragmentOutput(val items: List<JsonItem>? = null, val fragments: List<ScrapedFragment>? = null)

data class ScrapedFragment(
  val html: String? = null,
  val text: String? = null,
  val data: ScrapedData? = null,
  val uniqueBy: ScrapedFragmentPart,
)

data class ScrapedData(val mimeType: String, val data: String)

enum class ScrapedFragmentPart { html, text, data }
