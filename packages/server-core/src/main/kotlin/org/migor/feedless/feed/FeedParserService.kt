package org.migor.feedless.feed

import org.migor.feedless.api.dto.RichFeed
import org.migor.feedless.common.HttpService
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.feed.parser.FeedBodyParser
import org.migor.feedless.feed.parser.JsonFeedParser
import org.migor.feedless.feed.parser.NullFeedParser
import org.migor.feedless.feed.parser.XmlFeedParser
import org.migor.feedless.generated.types.CompositeFilterParamsInput
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.RemoteNativeFeed
import org.migor.feedless.generated.types.ScrapeRequest
import org.migor.feedless.generated.types.ScrapedField
import org.migor.feedless.generated.types.WebDocument
import org.migor.feedless.harvest.HarvestResponse
import org.migor.feedless.pipeline.plugins.CompositeFilterPlugin
import org.migor.feedless.service.ScrapeService
import org.migor.feedless.util.CryptUtil
import org.migor.feedless.util.FeedUtil
import org.migor.feedless.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.time.Duration
import java.util.*

@Service
class FeedParserService {

  private val log = LoggerFactory.getLogger(FeedParserService::class.simpleName)

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var filterPlugin: CompositeFilterPlugin

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var scrapeService: ScrapeService

  private val feedBodyParsers: Array<FeedBodyParser> = arrayOf(
    XmlFeedParser(),
    JsonFeedParser(),
    NullFeedParser()
  )

  init {
    feedBodyParsers.sortByDescending { feedBodyParser -> feedBodyParser.priority() }
    log.debug(
      "Using bodyParsers ${
        feedBodyParsers.joinToString(", ") { contentStrategy -> "$contentStrategy priority: ${contentStrategy.priority()}" }
      }"
    )
  }

  fun parseFeed(corrId: String, response: HarvestResponse): RichFeed {
    log.debug("[$corrId] Parsing feed")
    val (feedType, _) = FeedUtil.detectFeedTypeForResponse(
      corrId, response.response
    )
    log.debug("[$corrId] Parse feedType=$feedType")
    val bodyParser = feedBodyParsers.first { bodyParser ->
      bodyParser.canProcess(feedType)
    }
    return runCatching {
      bodyParser.process(corrId, response)
    }.onFailure {
      log.info("[${corrId}] bodyParser ${bodyParser::class.simpleName} failed with ${it.message}")
    }.getOrThrow()
  }

  fun parseFeedFromUrl(corrId: String, url: String): RichFeed {
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
    return parseFeed(corrId, HarvestResponse(url, response))
  }


  fun parseFeedFromRequest(
    corrId: String,
    scrapeRequests: List<ScrapeRequest>,
    filters: List<CompositeFilterParamsInput>
  ): RemoteNativeFeed {
    val params = filters.toPluginExecutionParamsInput()

    val items = Flux.fromIterable(scrapeRequests)
      .flatMap { scrapeRequest -> scrapeService.scrape(corrId, scrapeRequest) }
      .map { response -> response.elements.firstOrNull()!!.selector.fields.find { it.name == FeedlessPlugins.org_feedless_feed.name }!! }
      .flatMap { field: ScrapedField ->
        Flux.fromIterable(
          JsonUtil.gson.fromJson(
            field.value.one.data,
            RemoteNativeFeed::class.java
          ).items
        )
      }
      .collectList()
      .block(Duration.ofSeconds(30))!!
      .filterIndexed { index, item ->
        filterPlugin.filterEntity(
          corrId,
          item.asEntity(),
          params,
          index
        )
      }

    val feed = RemoteNativeFeed()
    feed.items = items
    feed.expired = false
    feed.feedUrl = ""
    feed.publishedAt = Date().time
    feed.title = "Preview Feed"

    return feed
  }

}

private fun <E : CompositeFilterParamsInput> List<E>.toPluginExecutionParamsInput(): PluginExecutionParamsInput {
  return PluginExecutionParamsInput.newBuilder()
    .org_feedless_filter(this)
    .build()
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
