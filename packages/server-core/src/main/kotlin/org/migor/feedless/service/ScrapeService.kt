package org.migor.feedless.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeVisitor
import org.migor.feedless.AppMetrics
import org.migor.feedless.BadRequestException
import org.migor.feedless.api.dto.RichFeed
import org.migor.feedless.api.graphql.asRemoteNativeFeed
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.feed.parser.FeedType
import org.migor.feedless.generated.types.CompositeFilterParamsInput
import org.migor.feedless.generated.types.DOMElementByXPath
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.PluginExecution
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.RemoteNativeFeed
import org.migor.feedless.generated.types.ScrapeDebugResponse
import org.migor.feedless.generated.types.ScrapeDebugTimes
import org.migor.feedless.generated.types.ScrapeRequest
import org.migor.feedless.generated.types.ScrapeResponse
import org.migor.feedless.generated.types.ScrapedBySelector
import org.migor.feedless.generated.types.ScrapedElement
import org.migor.feedless.generated.types.ScrapedField
import org.migor.feedless.generated.types.ScrapedFieldValue
import org.migor.feedless.generated.types.ScrapedSingleFieldValue
import org.migor.feedless.generated.types.TextData
import org.migor.feedless.generated.types.WebDocument
import org.migor.feedless.harvest.HarvestResponse
import org.migor.feedless.plugins.CompositeFilterPlugin
import org.migor.feedless.util.CryptUtil
import org.migor.feedless.util.FeedUtil
import org.migor.feedless.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import us.codecraft.xsoup.Xsoup
import java.nio.charset.Charset
import java.time.Duration
import java.util.*


@Service
class ScrapeService {

  private val log = LoggerFactory.getLogger(ScrapeService::class.simpleName)

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var agentService: AgentService

  @Autowired
  lateinit var filterPlugin: CompositeFilterPlugin

  @Autowired
  lateinit var feedParserService: FeedParserService

  @Autowired
  lateinit var pluginService: PluginService

  @Autowired
  lateinit var meterRegistry: MeterRegistry

  fun scrape(corrId: String, scrapeRequest: ScrapeRequest): Mono<ScrapeResponse> {
    val prerender = scrapeRequest.page.prerender != null

    log.info("[$corrId] scrape ${scrapeRequest.page.url}")

    if (!prerender && scrapeRequest.emit.any { scrapeEmit -> scrapeEmit.imageBased !== null }) {
      throw IllegalArgumentException("[${corrId}] emitting pixel requires prerendering ($corrId)")
    }

    meterRegistry.counter(
      AppMetrics.scrape, listOf(
        Tag.of("type", "scrape"),
        Tag.of("prerender", prerender.toString()),
      )
    ).increment()

    return if (prerender) {
      log.info("[$corrId] prerender")
      agentService.prerender(corrId, scrapeRequest)
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

      val headers = HashMap<String, String>()
      scrapeRequest.page.actions
        ?.mapNotNull { it.header }
        ?.forEach {
          log.info("[$corrId] add header ${it.value}")
          headers[it.name] = it.value
        }

      val staticResponse = httpService.httpGetCaching(corrId, url, 200, headers)

      val document = Jsoup.parse(staticResponse.responseBody.toString(Charset.defaultCharset()))
      scrapeRequest.page.actions
        ?.mapNotNull { it.purge }
        ?.forEach {
          val elements = Xsoup.compile(it.value).evaluate(document).elements
          log.info("[$corrId] purge element ${it.value} -> ${elements.size}")
          elements.remove()
        }

      val (feedType, mimeType) = FeedUtil.detectFeedTypeForResponse(corrId, staticResponse)

      val builder = ScrapeResponse.newBuilder()
        .failed(false)
        .url(scrapeRequest.page.url)
        .debug(
          ScrapeDebugResponse.newBuilder()
            .corrId(corrId)
            .console(emptyList())
            .html(document.html())
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
                throw IllegalArgumentException("fragment spec of type boundingBox requires prepredering ($corrId)")
              }

              val xpath = scrapeEmit.selectorBased.xpath.value

              val fragment = StringUtils.trimToNull(xpath)?.let {
                Xsoup.compile(xpath).evaluate(document).elements.firstOrNull()
                  ?: throw IllegalArgumentException("xpath $xpath cannot be resolved ($corrId)")
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

  fun scrapeFeedFromUrl(corrId: String, url: String): RichFeed {
    log.info("[$corrId] parseFeedFromUrl $url")
    httpService.guardedHttpResource(
      corrId,
      url,
      200,
      listOf("text/", "application/xml", "application/json", "application/rss", "application/atom", "application/rdf")
    )
    val request = httpService.prepareGet(url)
//    authHeader?.let {
//      request.setHeader("Authorization", it)
//    }
    val branchedCorrId = CryptUtil.newCorrId(parentCorrId = corrId)
    log.info("[$branchedCorrId] GET $url")
    val response = httpService.executeRequest(branchedCorrId, request, 200)
    return feedParserService.parseFeed(corrId, HarvestResponse(url, response))
  }


  fun scrapeFeedFromRequest(
    corrId: String,
    scrapeRequests: List<ScrapeRequest>,
    filters: List<CompositeFilterParamsInput>
  ): RemoteNativeFeed {
    val params = filters.toPluginExecutionParamsInput()

    val items = Flux.fromIterable(scrapeRequests)
      .flatMap { scrapeRequest -> scrape(corrId, scrapeRequest) }
      .map { response -> response.elements.firstOrNull()!!.selector.fields.find { it.name == FeedlessPlugins.org_feedless_feed.name }!! }
      .flatMap { field: ScrapedField ->
        Flux.fromIterable(
          JsonUtil.gson.fromJson(
            field.value.one.data,
            RemoteNativeFeed::class.java
          ).items
        )
      }
      .filter { item ->
        filterPlugin.filterEntity(
          corrId,
          item.asEntity(),
          params
        )
      }
      .collectList()
      .block(Duration.ofSeconds(30))!!

    val feed = RemoteNativeFeed()
    feed.items = items
    feed.expired = false
    feed.feedUrl = ""
    feed.publishedAt = Date().time
    feed.title = "Preview Feed"

    return feed
  }


  private fun toFields(feed: RichFeed): List<ScrapedField> {
    return listOf(createJsonField(FeedlessPlugins.org_feedless_feed.name, feed.asRemoteNativeFeed()))
  }

  private fun injectScrapeData(corrId: String, req: ScrapeRequest, res: ScrapeResponse): ScrapeResponse {
    val elements = if (res.debug.contentType.startsWith("text/")) {
      res.elements.mapIndexed { index, scrapedElement ->
        req.emit.get(index).selectorBased?.let {
          it.expose.transformers?.let {
            applyMarkupTransformers(
              corrId,
              it,
              res,
              scrapedElement
            )
          }
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

  private fun applyMarkupTransformers(
    corrId: String,
    transformers: List<PluginExecution>,
    res: ScrapeResponse,
    element: ScrapedElement
  ): ScrapedElement {
    val selector = element.selector
    return if (element.image !== null) {
      log.info("$corrId omit markup transformers")
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
    it: PluginExecution,
    element: ScrapedElement,
    url: String
  ): ScrapedField {

    log.info("[$corrId] applying plugin '${it.pluginId}'")

    val data = pluginService.resolveFragmentTransformerById(it.pluginId)
      ?.transformFragment(corrId, element, it, url)
      ?: throw BadRequestException("plugin '${it.pluginId}' does not exist ($corrId)")

    return createJsonField(it.pluginId, data)
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

private fun WebDocument.asEntity(): WebDocumentEntity {
  val e = WebDocumentEntity()
  e.contentTitle = contentTitle
//  if (StringUtils.isNotBlank(contentRawBase64)) {
//    e.contentRaw = Base64.getDecoder().decode(contentRawBase64)
//    e.contentRawMime = contentRawMime
//  }

  e.contentText = contentText
  e.status = ReleaseStatus.released
  e.releasedAt = Date(publishedAt)
//  e.updatedAt = Date()
  e.url = url
  return e
}

private fun <E : CompositeFilterParamsInput> List<E>.toPluginExecutionParamsInput(): PluginExecutionParamsInput {
  return PluginExecutionParamsInput.newBuilder()
    .org_feedless_filter(this)
    .build()
}
