package org.migor.feedless.feed.discovery

import org.jsoup.nodes.Document
import org.migor.feedless.api.graphql.DtoResolver.toDto
import org.migor.feedless.feed.parser.FeedType
import org.migor.feedless.generated.types.EmittedScrapeData
import org.migor.feedless.generated.types.ScrapeDebugResponse
import org.migor.feedless.generated.types.ScrapeEmitType
import org.migor.feedless.generated.types.ScrapeRequest
import org.migor.feedless.generated.types.ScrapeResponse
import org.migor.feedless.generated.types.ScrapedElement
import org.migor.feedless.generated.types.ScrapedFeeds
import org.migor.feedless.harvest.HarvestResponse
import org.migor.feedless.service.FeedParserService
import org.migor.feedless.service.HttpService
import org.migor.feedless.service.PropertyService
import org.migor.feedless.service.ScrapeService
import org.migor.feedless.util.FeedUtil
import org.migor.feedless.util.GenericFeedUtil
import org.migor.feedless.util.HtmlUtil
import org.migor.feedless.web.GenericFeedParserOptions
import org.migor.feedless.web.GenericFeedRule
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.charset.Charset

@Service
class FeedDiscoveryService {
  private val log = LoggerFactory.getLogger(FeedDiscoveryService::class.simpleName)

  @Autowired
  lateinit var nativeFeedLocator: NativeFeedLocator

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var genericFeedLocator: GenericFeedLocator

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var scrapeService: ScrapeService

  @Autowired
  lateinit var feedParserService: FeedParserService

  fun discoverFeeds(
    corrId: String,
    scrapeRequest: ScrapeRequest
  ): ScrapeResponse {
    val homepageUrl = scrapeRequest.page.url
    log.info("[$corrId] feeds/discover url=$homepageUrl")
    return runCatching {
      val url = rewriteUrl(corrId, httpService.prefixUrl(homepageUrl.trim()))
      val staticResponse = httpService.httpGetCaching(corrId, url, 200)
      val (feedType, mimeType) = FeedUtil.detectFeedTypeForResponse(staticResponse)

      val builder = ScrapeResponse.newBuilder()
        .failed(false)
        .url(scrapeRequest.page.url)
        .debug(
          ScrapeDebugResponse.newBuilder()
            .corrId(corrId)
            .console(emptyList())
            .body(staticResponse.responseBody.toString(Charset.defaultCharset()))
            .contentType(mimeType)
            .statusCode(staticResponse.statusCode)
            .cookies(emptyList())
            .network(emptyList())
            .build()
        )

      if (feedType !== FeedType.NONE) {
        val feed = feedParserService.parseFeed(corrId, HarvestResponse(url, staticResponse))
        log.debug("[$corrId] is native-feed")

        builder
          .elements(
            listOf(
              ScrapedElement.newBuilder()
                .xpath("/")
                .data(
                  listOf(
                    EmittedScrapeData.newBuilder()
                      .type(ScrapeEmitType.markup)
                      .markup(staticResponse.responseBody.toString())
                      .build(),
                    EmittedScrapeData.newBuilder()
                      .type(ScrapeEmitType.feeds)
                      .feeds(
                        ScrapedFeeds.newBuilder()
                          .genericFeeds(emptyList())
                          .nativeFeeds(
                            listOf(
                              toDto(
                                TransientOrExistingNativeFeed(
                                  transient = TransientNativeFeed(
                                    url = url,
                                    type = feedType,
                                    title = feed.title,
                                    description = feed.description
                                  )
                                )
                              )
                            )
                          )
                          .build()
                      )
                      .build()
                  )
                )
                .build()
            )
          )
          .build()
      } else {
        val response = scrapeService.scrape(corrId, scrapeRequest).block()!!
        val element = response.getRootElement() // todo handle all elements

        val markup = element.getEmittedData(ScrapeEmitType.markup).markup
        val document = HtmlUtil.parseHtml(markup, url)
        val (nativeFeeds, genericFeeds) = extractFeeds(corrId, document, url, false)
        builder
          .elements(
            listOf(
              ScrapedElement.newBuilder()
                .xpath("/")
                .data(
                  listOf(
                    EmittedScrapeData.newBuilder()
                      .type(ScrapeEmitType.markup)
                      .markup(markup)
                      .build(),
                    EmittedScrapeData.newBuilder()
                      .type(ScrapeEmitType.feeds)
                      .feeds(
                        ScrapedFeeds.newBuilder()
                        .genericFeeds(genericFeeds.map { GenericFeedUtil.toDto(it) })
                        .nativeFeeds(nativeFeeds.map { toDto(it) })
                        .build()
                      )
                      .build()
                  )
                )
                .build()
            )
          )
          .build()
      }
    }.getOrElse {
      log.error("[$corrId] Unable to discover feeds: ${it.message}")
      // todo mag return error code
//      it.printStackTrace()
      ScrapeResponse.newBuilder()
        .url(homepageUrl)
        .failed(true)
        .errorMessage(it.message)
        .elements(emptyList())
        .debug(
          ScrapeDebugResponse.newBuilder()
            .console(emptyList())
            .statusCode(400)
            .cookies(emptyList())
            .network(emptyList())
            .build()
        )
        .build()
    }
  }

//  private fun toDiscoveryDocument(
//    inspection: PageInspection,
//    body: String,
//    url: String,
//    mimeType: String,
//    statusCode: Int
//  ): FeedDiscoveryDocument = FeedDiscoveryDocument(
//    body = body,
//    mimeType = mimeType,
//    url = url,
//    title = inspection.valueOf("title"),
//    description = inspection.valueOf("description"),
//    language = inspection.valueOf("language"),
//    imageUrl = inspection.valueOf("imageUrl"),
//    statusCode = statusCode
//  )

  private fun rewriteUrl(corrId: String, url: String): String {
    val rewrite = url.replace("https://twitter.com", propertyService.nitterHost)
    if (rewrite != url) {
      log.info("[$corrId] rewrote url $url -> $rewrite")
    }
    return rewrite
  }


  private fun extractFeeds(
    corrId: String,
    document: Document,
    url: String,
    strictMode: Boolean
  ): Pair<List<TransientOrExistingNativeFeed>, List<GenericFeedRule>> {
    val parserOptions = GenericFeedParserOptions(
      strictMode = strictMode
    )
    val genericFeedRules = genericFeedLocator.locateInDocument(corrId, document, url, parserOptions)
    val nativeFeeds = nativeFeedLocator.locateInDocument(document, url)
    log.info("[$corrId] Found feedRules=${genericFeedRules.size} nativeFeeds=${nativeFeeds.size}")
    return Pair(nativeFeeds, genericFeedRules)
  }
}

private fun ScrapedElement.getEmittedData(type: ScrapeEmitType): EmittedScrapeData {
  return this.data.find { it.type == type }!!
}

fun ScrapeResponse.getRootElement(): ScrapedElement {
  return this.elements.minByOrNull { it.xpath.length } ?: throw IllegalArgumentException("no root element present")
}
