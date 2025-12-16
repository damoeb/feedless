package org.migor.feedless.scrape

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
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
import org.migor.feedless.pipeline.transformer.Transformer
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import us.codecraft.xsoup.Xsoup
import java.util.*
import kotlin.reflect.KClass

data class WebExtractServiceParameters(
  val extract: DOMExtract,
  val locale: Locale,
  val logger: LogCollector,
)

data class MarkupData(
  val element: Element
)

@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.service}")
class WebExtractService(private val dateClaimer: DateClaimer) :
  Transformer<MarkupData, ScrapeExtractResponse, WebExtractServiceParameters> {
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
    val input = flowOf(MarkupData(element))
    val params = WebExtractServiceParameters(
      extract,
      locale,
      logger,
    )
    return transform(input, params).first()
  }

  override fun consumes(): KClass<MarkupData> {
    return MarkupData::class
  }

  override fun produces(): KClass<ScrapeExtractResponse> {
    return ScrapeExtractResponse::class
  }

  override suspend fun transform(
    input: Flow<MarkupData>,
    parameters: WebExtractServiceParameters
  ): Flow<ScrapeExtractResponse> {
    return input.map {
      val extract = parameters.extract
      val logger = parameters.logger
      val locale = parameters.locale
      val element = it.element
      val level = 0

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

      ScrapeExtractResponse(
        fragmentName = extract.fragmentName,
        fragments = fragments.asFlow().map { fragment ->
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
        }.toList()
      )
    }
  }

}
