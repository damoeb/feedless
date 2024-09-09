package org.migor.feedless.feed

import org.apache.commons.lang3.StringUtils
import org.locationtech.jts.geom.Point
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
import org.migor.feedless.service.LogCollector
import org.migor.feedless.service.ScrapeActionOutput
import org.migor.feedless.service.ScrapeOutput
import org.migor.feedless.service.ScrapeService
import org.migor.feedless.source.SourceEntity
import org.migor.feedless.util.CryptUtil
import org.migor.feedless.util.FeedUtil
import org.migor.feedless.util.JtsUtil
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.LocalDateTime

private fun ScrapeOutput.lastOutput(): ScrapeActionOutput {
  return this.outputs.last()
}

@Service
@Profile(AppProfiles.scrape)
class FeedParserService {

  private val log = LoggerFactory.getLogger(FeedParserService::class.simpleName)

  @Autowired
  private lateinit var propertyService: PropertyService

  @Autowired
  private lateinit var filterPlugin: CompositeFilterPlugin

  @Autowired
  private lateinit var conditionalTagPlugin: ConditionalTagPlugin

  @Autowired
  private lateinit var httpService: HttpService

  @Autowired
  private lateinit var scrapeService: ScrapeService

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

  suspend fun parseFeed(corrId: String, response: HttpResponse): JsonFeed {
    log.debug("[$corrId] Parsing feed")
    val (feedType, _) = FeedUtil.detectFeedTypeForResponse(
      corrId, response
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

  suspend fun parseFeedFromUrl(corrId: String, url: String): JsonFeed {
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
    val branchedCorrId = CryptUtil.newCorrId(parentCorrId = corrId)
    log.debug("[$branchedCorrId] GET $url")
    val response = httpService.executeRequest(branchedCorrId, request, 200)
    return parseFeed(corrId, response)
  }


  @Deprecated("obsolete")
  suspend fun parseFeedFromRequest(
    corrId: String,
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
      .map { source -> scrapeService.scrape(corrId, source, logCollector) }
      .flatMap { response -> response.lastOutput().fragment!!.items!! }
      .filterIndexed { index, item ->
        filterPlugin.filterEntity(
          corrId,
          item,
          params,
          index,
          LogCollector()
        )
      }
      .map {
        conditionalTagPlugin.mapEntity(corrId, it.asEntity(dummyRepository), dummyRepository, conditionalTagsParams, logCollector)
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
  e.contentTitle = title
//  if (StringUtils.isNotBlank(contentRawBase64)) {
//    e.contentRaw = Base64.getDecoder().decode(contentRawBase64)
//    e.contentRawMime = contentRawMime
//  }
  e.repositoryId = repository.id
  e.latLon = this.latLng?.toPoint()
  e.contentText = StringUtils.trimToEmpty(contentText)
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
