package org.migor.feedless.service

import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeVisitor
import org.migor.feedless.generated.types.EmittedScrapeData
import org.migor.feedless.generated.types.ScrapeDebugResponse
import org.migor.feedless.generated.types.ScrapeEmitType
import org.migor.feedless.generated.types.ScrapeRequest
import org.migor.feedless.generated.types.ScrapeResponse
import org.migor.feedless.generated.types.ScrapedElement
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import us.codecraft.xsoup.Xsoup


@Service
class ScrapeService {

  private val log = LoggerFactory.getLogger(HttpService::class.simpleName)

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var puppeteerService: PuppeteerService

  fun scrape(corrId: String, scrapeRequest: ScrapeRequest): Mono<ScrapeResponse> {
    val prerender =
      scrapeRequest.emit?.contains(ScrapeEmitType.pixel) == true || scrapeRequest.page.prerender != null

    return if (prerender) {
      log.info("[$corrId] prerender")
      puppeteerService.prerender(corrId, scrapeRequest)
    } else {
      log.info("[$corrId] static")
      val url = scrapeRequest.page.url
      httpService.guardedHttpResource(
        corrId,
        url,
        200,
        listOf("text/", "application/xml", "application/json", "application/rss", "application/atom", "application/rdf")
      )
      val response = httpService.httpGetCaching(corrId, url, 200)
      val document = Jsoup.parse(String(response.responseBody))

      val elements = scrapeRequest.elements.map { xpath ->
        run {
          val fragment = StringUtils.trimToNull(xpath)?.let {
            Xsoup.compile(xpath).evaluate(document).elements.firstOrNull()
              ?: throw IllegalArgumentException("xpath ${xpath} cannot be resolved")
          } ?: document

          val scrapedData = scrapeRequest.emit.map {
            when (it) {
              ScrapeEmitType.text -> run {
                val texts = mutableListOf<String>()
                fragment.traverse(textElements(texts))
                toEmittedData(it, text = texts.joinToString("\n"))
              }

              ScrapeEmitType.markup -> toEmittedData(it, markup = fragment.html())
              else -> throw IllegalArgumentException("pixel cannot be extracted in static mode")
            }
          }

          ScrapedElement.newBuilder()
            .xpath(xpath)
            .data(scrapedData)
            .build()
        }
      }

      Mono.just(
        ScrapeResponse.newBuilder()
          .url(url)
          .failed(false)
          .elements(elements)
          .debug(
            ScrapeDebugResponse.newBuilder()
              .console(emptyList())
              .corrId(corrId)
              .statusCode(response.statusCode)
              .contentType(response.contentType)
              .cookies(emptyList())
              .network(emptyList())
              .build()
          )
          .build()
      )
    }
  }

  private fun toEmittedData(scrapeEmitType: ScrapeEmitType, text: String? = null, markup: String? = null): EmittedScrapeData {
    return EmittedScrapeData.newBuilder()
      .type(scrapeEmitType)
      .text(text)
      .markup(markup)
      .build()
  }

  private fun textElements(texts: MutableList<String>): NodeVisitor {
    return object : NodeVisitor {
      override fun head(node: Node, depth: Int) {
        if (node is TextNode) {
          texts.add(node.text())
        }
      }

      override fun tail(node: Node, depth: Int) {
      }
    }
  }
}
