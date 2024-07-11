package org.migor.feedless.web

import org.jsoup.nodes.Element
import org.migor.feedless.feed.DateClaimer
import org.migor.feedless.generated.types.DOMExtract
import org.migor.feedless.generated.types.MimeData
import org.migor.feedless.generated.types.ScrapeEmit
import org.migor.feedless.generated.types.ScrapeExtractFragment
import org.migor.feedless.generated.types.ScrapeExtractResponse
import org.migor.feedless.generated.types.TextData
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import us.codecraft.xsoup.Xsoup

@Service
class WebExtractService {
  private val log = LoggerFactory.getLogger(WebExtractService::class.simpleName)

  @Autowired
  private lateinit var dateClaimer: DateClaimer

  fun extract(corrId: String, extract: DOMExtract, element: Element): ScrapeExtractResponse {
    val fragments = Xsoup.compile(extract.xpath.value.replaceFirst("./", "//")).evaluate(element).elements
      .filterIndexed { index, _ -> index < (extract.max ?: Integer.MAX_VALUE) }
    return ScrapeExtractResponse(
      fragmentName = extract.fragmentName,
      fragments = fragments.map { fragment ->
        ScrapeExtractFragment(
          extracts = extract.extract?.map { extract(corrId, it, fragment) },
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
          data = if (extract.emit.contains(ScrapeEmit.date)) {
            MimeData(
              mimeType = "text/plain",
              data = dateClaimer.claimDatesFromString(corrId, fragment.text(), null).toString()
            )
          } else {
            null
          }
        )
      }
    )
  }

}
