package org.migor.feedless.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeVisitor
import org.migor.feedless.AppMetrics
import org.migor.feedless.api.dto.RichArticle
import org.migor.feedless.api.dto.RichFeed
import org.migor.feedless.api.graphql.DtoResolver
import org.migor.feedless.api.graphql.asRemoteNativeFeed
import org.migor.feedless.feed.discovery.GenericFeedLocator
import org.migor.feedless.feed.discovery.NativeFeedLocator
import org.migor.feedless.feed.discovery.RemoteOrExistingNativeFeed
import org.migor.feedless.feed.parser.FeedType
import org.migor.feedless.generated.types.DOMElementByXPath
import org.migor.feedless.generated.types.MarkupTransformer
import org.migor.feedless.generated.types.ScrapeDebugResponse
import org.migor.feedless.generated.types.ScrapeDebugTimes
import org.migor.feedless.generated.types.ScrapeRequest
import org.migor.feedless.generated.types.ScrapeResponse
import org.migor.feedless.generated.types.ScrapedBySelector
import org.migor.feedless.generated.types.ScrapedElement
import org.migor.feedless.generated.types.ScrapedFeeds
import org.migor.feedless.generated.types.ScrapedField
import org.migor.feedless.generated.types.ScrapedFieldValue
import org.migor.feedless.generated.types.ScrapedReadability
import org.migor.feedless.generated.types.ScrapedSingleFieldValue
import org.migor.feedless.generated.types.TextData
import org.migor.feedless.generated.types.TransformerInternalOrExternal
import org.migor.feedless.harvest.HarvestResponse
import org.migor.feedless.util.FeedUtil
import org.migor.feedless.util.GenericFeedUtil.fromDto
import org.migor.feedless.util.GenericFeedUtil.toDto
import org.migor.feedless.util.HtmlUtil.parseHtml
import org.migor.feedless.util.JsonUtil
import org.migor.feedless.web.GenericFeedParserOptions
import org.migor.feedless.web.GenericFeedRule
import org.migor.feedless.web.WebToArticleTransformer
import org.migor.feedless.web.WebToFeedTransformer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import us.codecraft.xsoup.Xsoup
import java.net.URL
import java.nio.charset.Charset


@Service
class ScrapeService {

  private val log = LoggerFactory.getLogger(HttpService::class.simpleName)

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var nativeFeedLocator: NativeFeedLocator

  @Autowired
  lateinit var genericFeedLocator: GenericFeedLocator

  @Autowired
  lateinit var webToArticleTransformer: WebToArticleTransformer

  @Autowired
  lateinit var webToFeedTransformer: WebToFeedTransformer

  @Autowired
  lateinit var puppeteerService: PuppeteerService

  @Autowired
  lateinit var feedParserService: FeedParserService

  @Autowired
  lateinit var meterRegistry: MeterRegistry

  fun scrape(corrId: String, scrapeRequest: ScrapeRequest): Mono<ScrapeResponse> {
    val prerender = scrapeRequest.page.prerender != null

//    if (scrapeRequest.emit.any { scrapeEmit -> scrapeEmit.fragment.xpath == null && scrapeEmit.fragment.boundingBox == null }) {
//      throw IllegalArgumentException("[${corrId}] fragment is underspecified.")
//    }

    if (!prerender && scrapeRequest.emit.any { scrapeEmit -> scrapeEmit.imageBased !== null }) {
      throw IllegalArgumentException("[${corrId}] emitting pixel requires preprendering")
    }

    meterRegistry.counter(
      AppMetrics.scrape, listOf(
        Tag.of("type", "scrape"),
        Tag.of("prerender", prerender.toString()),
      )
    ).increment()

    return if (prerender) {
      log.info("[$corrId] prerender")
      puppeteerService.prerender(corrId, scrapeRequest)
        .map { injectScrapeData(corrId, scrapeRequest, it) }
    } else {
      log.info("[$corrId] static")
      val startTime = System.nanoTime()
      val url = scrapeRequest.page.url
      httpService.guardedHttpResource(
        corrId,
        url,
        200,
        listOf("text/", "application/xml", "application/json", "application/rss", "application/atom", "application/rdf")
      )
      val staticResponse = httpService.httpGetCaching(corrId, url, 200)
      val document = Jsoup.parse(String(staticResponse.responseBody))
      val (feedType, mimeType) = FeedUtil.detectFeedTypeForResponse(staticResponse)

      val builder = ScrapeResponse.newBuilder()
        .failed(false)
        .url(scrapeRequest.page.url)
        .debug(
          ScrapeDebugResponse.newBuilder()
            .corrId(corrId)
            .console(emptyList())
            .html(staticResponse.responseBody.toString(Charset.defaultCharset()))
            .contentType(mimeType)
            .statusCode(staticResponse.statusCode)
            .cookies(emptyList())
            .network(emptyList())
            .metrics(
              ScrapeDebugTimes.newBuilder()
                .render(System.nanoTime().minus(startTime).div(1000000).toInt())
                .queue(0)
                .build()
            )
            .build()
        )

      if (feedType !== FeedType.NONE) {
        val feed = feedParserService.parseFeed(corrId, HarvestResponse(url, staticResponse))
        log.debug("[$corrId] is native-feed")

        builder
          .elements(
            listOf(
              ScrapedElement.newBuilder()
                .selector(
                  ScrapedBySelector.newBuilder()
                    .xpath(
                      DOMElementByXPath.newBuilder()
                        .value("/")
                        .build()
                    )
                    .fields(toFields(feed))
                    .build()
                )
                .build()
            )
          )
          .build()
      } else {
        val elements = scrapeRequest.emit
          .map { scrapeEmit ->
            run {

              scrapeEmit.imageBased?.boundingBox?.let {
                throw IllegalArgumentException("fragment spec of type boundingBox requires prepredering")
              }

              val xpath = scrapeEmit.selectorBased.xpath.value

              val fragment = StringUtils.trimToNull(xpath)?.let {
                Xsoup.compile(xpath).evaluate(document).elements.firstOrNull()
                  ?: throw IllegalArgumentException("xpath $xpath cannot be resolved")
              } ?: document

              val texts = mutableListOf<String>()
              fragment.traverse(textElements(texts))

              ScrapedElement.newBuilder()
                .selector(
                  ScrapedBySelector.newBuilder()
                    .xpath(
                      DOMElementByXPath.newBuilder()
                        .value("/")
                        .build()
                    )
                    .html(TextData.newBuilder().data(fragment.html()).build())
                    .text(TextData.newBuilder().data(texts.joinToString("\n")).build())
                    .build()

                )
                .build()

            }
          }
        builder.elements(elements)
      }

      Mono.just(builder.build()).map { injectScrapeData(corrId, scrapeRequest, it) }
    }
  }

  private fun toFields(feed: RichFeed): List<ScrapedField> {
//    return listOf(
//      field("title", feed.title),
//      field("link", feed.link),
//      field("description", feed.description),
//      field("imageUrl", feed.imageUrl),
//      field("iconUrl", feed.iconUrl),
//      field("favicon", feed.favicon),
//      field("websiteUrl", feed.websiteUrl),
//      field("feedUrl", feed.feedUrl),
//      ScrapedField.newBuilder()
//        .name("items")
//        .value(
//          ScrapedFieldValue.newBuilder()
//            .nested(feed.items.map { it.asScrapedField() })
//            .build()
//        )
//        .build()
//
//    )
    return listOf(createJsonField(MarkupTransformer.feed.name, feed.asRemoteNativeFeed()))
  }

  private fun injectScrapeData(corrId: String, req: ScrapeRequest, res: ScrapeResponse): ScrapeResponse {
    val elements = if (res.debug.contentType.startsWith("text/html")) {
      res.elements.mapIndexed { index, scrapedElement ->
        req.emit.get(index).selectorBased?.let {
          applyMarkupTransformers(
            corrId,
            it.expose.transformers,
            res,
            scrapedElement
          )
        } ?: scrapedElement
      }
    } else {
      res.elements
    }
    return ScrapeResponse.newBuilder()
      .failed(res.failed)
      .url(res.url)
      .errorMessage(res.errorMessage)
      .debug(res.debug)
      .elements(elements)
      .build()
  }

  private fun toScrapedFeeds(
    corrId: String,
    markup: String,
    url: String,
  ): ScrapedFeeds {
    val document = parseHtml(markup, url)
    val (nativeFeeds, genericFeeds) = extractFeeds(corrId, document, url, false)
    return ScrapedFeeds.newBuilder()
      .genericFeeds(genericFeeds.map { toDto(it) })
      .nativeFeeds(nativeFeeds.map { DtoResolver.toDto(it) })
      .build()
  }

  private fun toScrapedReadability(
    corrId: String,
    markup: String,
    url: String,
  ): ScrapedReadability {
    val article = webToArticleTransformer.fromHtml(markup, url)
    return ScrapedReadability.newBuilder()
      .date(article.date)
      .content(article.content)
      .url(article.url)
      .contentText(article.contentText)
      .contentMime(article.contentMime)
      .faviconUrl(article.faviconUrl)
      .imageUrl(article.imageUrl)
      .title(article.title)
      .build()
  }

  private fun applyMarkupTransformers(
    corrId: String,
    transformers: List<TransformerInternalOrExternal>,
    res: ScrapeResponse,
    element: ScrapedElement
  ): ScrapedElement {
    val selector = element.selector
    return if (element.image !== null) {
      element
    } else {
      return ScrapedElement.newBuilder()
        .selector(
          ScrapedBySelector.newBuilder()
            .xpath(selector.xpath)
            .html(selector.html)
            .text(selector.text)
            .pixel(selector.pixel)
            .fields(transformers.map { applyTransformer(corrId, it, element, res.url) }
              .plus(selector.fields ?: emptyList()))
            .build()
        )
        .build()
    }
  }

  private fun createJsonField(name: String, data: Any): ScrapedField {
    return ScrapedField.newBuilder()
      .name(name)
      .value(
        ScrapedFieldValue.newBuilder()
          .one(
            ScrapedSingleFieldValue.newBuilder()
              .mimeType("application/json")
              .data(JsonUtil.gson.toJson(data))
              .build()
          )
          .build()
      )
      .build()
  }


  private fun applyTransformer(
    corrId: String,
    transformer: TransformerInternalOrExternal,
    element: ScrapedElement,
    url: String
  ): ScrapedField {
    return transformer.internal?.let {

      fun createField(name: String, data: Any): ScrapedField {
        return createJsonField(name, data)
      }

      when (it.transformer) {
        MarkupTransformer.feeds -> createField(
          it.transformer.name,
          toScrapedFeeds(
            corrId, element.selector.html.data, url
          )
        )

        MarkupTransformer.readability -> createField(
          it.transformer.name,
          toScrapedReadability(corrId, element.selector.html.data, url)
        )

        MarkupTransformer.feed -> createField(
          it.transformer.name,
          webToFeedTransformer.getFeedBySelectors(corrId, fromDto(it.transformerData.genericFeed), parseHtml(element.selector.html.data, url), URL(url))
            .asRemoteNativeFeed()
        )

        else -> throw RuntimeException("transformer '${transformer.internal}' not supported")
      }
    } ?: throw RuntimeException("external transformer not implemented")
  }

  private fun extractFeeds(
    corrId: String,
    document: Document,
    url: String,
    strictMode: Boolean
  ): Pair<List<RemoteOrExistingNativeFeed>, List<GenericFeedRule>> {
    val parserOptions = GenericFeedParserOptions(
      strictMode = strictMode
    )
    val nativeFeeds = nativeFeedLocator.locateInDocument(document, url)
    val genericFeedRules = genericFeedLocator.locateInDocument(corrId, document, url, parserOptions)
    log.info("[$corrId] Found feedRules=${genericFeedRules.size} nativeFeeds=${nativeFeeds.size}")
    return Pair(nativeFeeds, genericFeedRules)
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

private fun RichArticle.asScrapedField(): ScrapedField {
  return ScrapedField.newBuilder()
    .name("item")
    .value(
      ScrapedFieldValue.newBuilder()
        .nested(
          listOf(
            field("id", this.id),
            field("url", this.url),
            field("title", this.title),
            field("imageUrl", this.imageUrl),
            field("url", this.url),
            field("publishedAt", this.publishedAt.toString(), "text/date-time"),
            field("contentRaw", this.contentRaw, this.contentRawMime!!),
            field("contentText", this.contentText),
          )
        )
        .build()
    )
    .build()
}

private fun field(name: String, value: String?, mime: String = "text/plain"): ScrapedField {
  return ScrapedField.newBuilder()
    .name(name)
    .value(
      ScrapedFieldValue.newBuilder()
        .one(
          ScrapedSingleFieldValue.newBuilder()
            .mimeType(mime)
            .data(value)
            .build()
        )
        .build()
    )
    .build()
}

fun ScrapeResponse.getRootElement(): ScrapedElement {
  return this.elements.minByOrNull { it.selector?.xpath?.value?.length ?: 0 }
    ?: throw IllegalArgumentException("no root element present")
}
