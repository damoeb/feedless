package org.migor.feedless.feed

import org.apache.commons.lang3.StringUtils
import org.locationtech.jts.geom.Point
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.common.HttpService
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.document.toDto
import org.migor.feedless.feed.parser.FeedBodyParser
import org.migor.feedless.feed.parser.JsonFeedParser
import org.migor.feedless.feed.parser.NullFeedParser
import org.migor.feedless.feed.parser.XmlFeedParser
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.feed.parser.json.JsonPoint
import org.migor.feedless.generated.types.ConditionalTagInput
import org.migor.feedless.generated.types.FeedPreview
import org.migor.feedless.generated.types.ItemFilterParamsInput
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.RemoteNativeFeed
import org.migor.feedless.pipeline.plugins.CompositeFilterPlugin
import org.migor.feedless.pipeline.plugins.ConditionalTagPlugin
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.scrape.ScrapeActionOutput
import org.migor.feedless.scrape.ScrapeOutput
import org.migor.feedless.scrape.ScrapeService
import org.migor.feedless.source.SourceEntity
import org.migor.feedless.user.corrId
import org.migor.feedless.util.FeedUtil
import org.migor.feedless.util.JtsUtil
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.coroutines.coroutineContext

private fun ScrapeOutput.lastOutput(): ScrapeActionOutput {
  return this.outputs.last()
}

@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.service}")
class FeedParserService(
  private val propertyService: PropertyService,
  private val filterPlugin: CompositeFilterPlugin,
  private val conditionalTagPlugin: ConditionalTagPlugin,
  private val httpService: HttpService,
  private val scrapeService: ScrapeService
) {

  private val log = LoggerFactory.getLogger(FeedParserService::class.simpleName)

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

  suspend fun parseFeed(response: HttpResponse): JsonFeed {
    val corrId = coroutineContext.corrId()
    log.debug("[$corrId] Parsing feed")
    val (feedType, _) = FeedUtil.detectFeedTypeForResponse(response)
    log.debug("[$corrId] Parse feedType=$feedType")
    val bodyParser = feedBodyParsers.first { bodyParser ->
      bodyParser.canProcess(feedType)
    }
    return runCatching {
      bodyParser.process(response)
    }.onFailure {
      log.info("[${corrId}] bodyParser ${bodyParser::class.simpleName} failed with ${it.message}")
    }.getOrThrow()
  }

  suspend fun parseFeedFromUrl(url: String): JsonFeed {
    val corrId = coroutineContext.corrId()
    log.debug("[$corrId] parseFeedFromUrl $url")
//    httpService.guardedHttpResource(
//      corrId,
//      url,
//      200,
//      listOf("text/", "application/xml", "application/json", "application/rss", "application/atom", "application/rdf")
//    )
    val request = httpService.prepareGet(url)
//    authHeader?.let {
//      request.setHeader("Authorization", it)
//    }
    log.debug("[$corrId] GET $url")
    val response = httpService.executeRequest(request, 200)
    return parseFeed(response)
  }


  @Deprecated("obsolete")
  suspend fun parseFeedFromRequest(
    sources: List<SourceEntity>,
    filters: List<ItemFilterParamsInput>,
    tags: List<ConditionalTagInput>
  ): FeedPreview {
    val params = filters.toPluginExecutionParamsInput()

    val dummyRepository = RepositoryEntity()
    val conditionalTagsParams = PluginExecutionParamsInput(
      org_feedless_conditional_tag = tags
    )

    val logCollector = LogCollector()

    val items = sources
      .map { source -> scrapeService.scrape(source, logCollector) }
      .flatMap { response -> response.lastOutput().fragment!!.items!! }
      .filterIndexed { index, item ->
        filterPlugin.filterEntity(
          item,
          params,
          index,
          LogCollector()
        )
      }
      .map {
        conditionalTagPlugin.mapEntity(
          it.asEntity(dummyRepository),
          dummyRepository,
          conditionalTagsParams,
          logCollector
        )
          .toDto(propertyService)
      }

    val feed = RemoteNativeFeed(
      items = items,
      expired = false,
      feedUrl = "",
      publishedAt = LocalDateTime.now().toMillis(),
      title = "Preview Feed"
    )

    return FeedPreview(
      logs = logCollector.logs,
      feed = feed
    )
  }

}

private fun <E : ItemFilterParamsInput> List<E>.toPluginExecutionParamsInput(): PluginExecutionParamsInput {
  return PluginExecutionParamsInput(
    org_feedless_filter = this
  )
}

private fun JsonItem.asEntity(repository: RepositoryEntity): DocumentEntity {
  val e = DocumentEntity()
  e.title = title
//  if (StringUtils.isNotBlank(rawBase64)) {
//    e.raw = Base64.getDecoder().decode(rawBase64)
//    e.rawMimeType = rawMimeType
//  }
  e.repositoryId = repository.id
  e.latLon = this.latLng?.toPoint()
  e.text = StringUtils.trimToEmpty(text)
  e.status = ReleaseStatus.released
  e.publishedAt = publishedAt
  startingAt?.let {
    e.startingAt = startingAt
  }
  e.updatedAt = publishedAt
  e.url = url
  return e
}

fun JsonPoint.toPoint(): Point {
  return JtsUtil.createPoint(x, y)
}
