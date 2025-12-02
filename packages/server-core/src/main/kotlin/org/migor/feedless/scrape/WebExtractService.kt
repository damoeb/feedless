package org.migor.feedless.scrape

import org.jsoup.nodes.Element
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.feed.DateClaimer
import org.migor.feedless.generated.types.DOMElementByXPath
import org.migor.feedless.generated.types.DOMExtract
import org.migor.feedless.generated.types.MimeData
import org.migor.feedless.generated.types.ScrapeEmit
import org.migor.feedless.generated.types.ScrapeExtractFragment
import org.migor.feedless.generated.types.ScrapeExtractFragmentPart
import org.migor.feedless.generated.types.ScrapeExtractResponse
import org.migor.feedless.generated.types.TextData
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import us.codecraft.xsoup.Xsoup
import java.util.*

@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.service}")
class WebExtractService(private val dateClaimer: DateClaimer) {
  private val log = LoggerFactory.getLogger(WebExtractService::class.simpleName)

  companion object {
    const val MIME_DATE = "text/x-date"
    const val MIME_URL = "text/x-uri"
  }

  private fun fixXpath(xpath: DOMElementByXPath): String {
    return xpath.value.replaceFirst("/html/body", "/").replaceFirst("./", "//")
  }

  suspend fun extract(
    extract: DOMExtract,
    element: Element,
    locale: Locale,
    logger: LogCollector,
    level: Int = 0
  ): ScrapeExtractResponse {
    val fragments = if (extract.xpath.value == "./") {
      listOf(element)
    } else {
      Xsoup.compile(fixXpath(extract.xpath)).evaluate(element).elements
        .filterIndexed { index, _ -> index < (extract.max ?: Integer.MAX_VALUE) }
    }
    // expected
    // found //main/div[2]/div/div/div/div/div[3]/div/div/div/div[2]/table/tbody/tr/td
    // found //main/div[2]/div/div/div/div/div[3]/div/div/div/div[2]/table/tbody/tr/td
    logger.log("extracting @level $level ${extract.xpath} -> ${fragments.size} elements (max ${extract.max})")

    return ScrapeExtractResponse(
      fragmentName = extract.fragmentName,
      fragments = fragments.map { fragment ->
        ScrapeExtractFragment(
          extracts = extract.extract?.map { extract(it, fragment, locale, logger, level + 1) },
          html = if (extract.emit.contains(ScrapeEmit.html)) {
            TextData(fragment.html())
          } else {
            null
          },
          text = if (extract.emit.contains(ScrapeEmit.text)) {
            TextData(fragment.text())
          } else {
            null
          },
          uniqueBy = ScrapeExtractFragmentPart.html, // todo check this
          data = if (extract.emit.contains(ScrapeEmit.date)) {
            dateClaimer.claimDatesFromString(fragment.text(), locale, logger)?.let { date ->
              MimeData(
                mimeType = MIME_DATE,
                data = date.toMillis().toString()
              )
            }
          } else {
            if (fragment.hasAttr("href")) {
              MimeData(
                mimeType = MIME_URL,
                data = fragment.attr("href")
              )
            } else {
              null
            }
          }
        )
      }
    )
  }

}
