package org.migor.feedless.pipeline.plugins

import org.apache.commons.lang3.BooleanUtils
import org.jsoup.nodes.Document
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.actions.ExecuteActionEntity
import org.migor.feedless.api.isHtml
import org.migor.feedless.api.toDto
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.feed.FeedParserService
import org.migor.feedless.feed.discovery.GenericFeedLocator
import org.migor.feedless.feed.discovery.NativeFeedLocator
import org.migor.feedless.feed.parser.json.JsonAttachment
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.generated.types.Attachment
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.GeoPoint
import org.migor.feedless.generated.types.Record
import org.migor.feedless.generated.types.RemoteNativeFeed
import org.migor.feedless.generated.types.ScrapedFeeds
import org.migor.feedless.pipeline.FragmentOutput
import org.migor.feedless.pipeline.FragmentTransformerPlugin
import org.migor.feedless.scrape.GenericFeedParserOptions
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.user.corrId
import org.migor.feedless.util.FeedUtil
import org.migor.feedless.util.HtmlUtil
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.*
import kotlin.coroutines.coroutineContext

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.scrape} & ${AppLayer.service}")
class FeedsPlugin : FragmentTransformerPlugin {

  private val log = LoggerFactory.getLogger(FeedsPlugin::class.simpleName)

  @Autowired
  private lateinit var nativeFeedLocator: NativeFeedLocator

  @Autowired
  private lateinit var genericFeedLocator: GenericFeedLocator

  @Autowired
  @Lazy
  private lateinit var feedParserService: FeedParserService

  override fun id(): String = FeedlessPlugins.org_feedless_feeds.name
  override fun listed() = false
  override suspend fun transformFragment(
    action: ExecuteActionEntity,
    data: HttpResponse,
    logger: LogCollector,
  ): FragmentOutput {
    val corrId = coroutineContext.corrId()
    log.debug("[$corrId] transformFragment")

    val mimeType = data.contentType.lowercase()
    logger.log("Found mimeType=$mimeType")
    return if (FeedUtil.isFeed(mimeType)) {
      logger.log("Parsing native feed")
      val jsonFeed = feedParserService.parseFeed(data)
      FragmentOutput(
        fragmentName = "native",
        feeds = ScrapedFeeds(
          genericFeeds = emptyList(),
          nativeFeeds = listOf(jsonFeed.asRemoteNativeFeed())
        )
      )
    } else {
      if (mimeType.contains("text/html")) {
        logger.log("extracting feeds")
        val document = HtmlUtil.parseHtml(data.responseBody.toString(StandardCharsets.UTF_8), data.url)
        log.debug("[$corrId] extracting feeds")
        extractFeeds(document, data.url, logger)
      } else {
        logger.log("unsupported mimeType")
        log.warn("[$corrId] unsupported mimeType $mimeType")
        FragmentOutput(
          fragmentName = "generic",
          feeds = ScrapedFeeds(
            genericFeeds = emptyList(),
            nativeFeeds = emptyList()
          )
        )
      }
    }
  }

  override fun name(): String = "Feeds"

  private suspend fun extractFeeds(
    document: Document,
    url: String,
    logger: LogCollector,
  ): FragmentOutput {
    val parserOptions = GenericFeedParserOptions()
    val nativeFeeds = nativeFeedLocator.locateInDocument(document, url)
    logger.log("found ${nativeFeeds.size} native feeds $nativeFeeds")
    val genericFeeds = genericFeedLocator.locateInDocument(document, url, parserOptions)
    logger.log("found ${genericFeeds.size} generic feeds")
    val corrId = coroutineContext.corrId()
    log.debug("[$corrId] Found feedRules=${genericFeeds.size} nativeFeeds=${nativeFeeds.size}")

    return FragmentOutput(
      fragmentName = "feeds",
      feeds = ScrapedFeeds(
        genericFeeds = genericFeeds.map { it.toDto() },
        nativeFeeds = nativeFeeds.map { it.toDto() }
      )
    )
  }
}

private fun JsonFeed.asRemoteNativeFeed(): RemoteNativeFeed {

  return RemoteNativeFeed(
    description = description,
    title = title,
    feedUrl = feedUrl,
    websiteUrl = websiteUrl,
    language = language,
    publishedAt = publishedAt.toMillis(),
    tags = tags,
    nextPageUrls = links,
    expired = BooleanUtils.isTrue(expired),
    items = items.map {
      var html: String? = null
      var rawBase64: String? = null
      var rawMimeType: String? = null
      if (isHtml(it.rawMimeType)) {
        try {
          html = Base64.getDecoder().decode(it.rawBase64).toString(StandardCharsets.UTF_8)
        } catch (e: Exception) {
          html = it.rawBase64
        }
      } else {
        rawBase64 = it.rawBase64
        rawMimeType = it.rawMimeType
      }

      Record(
        id = it.url,
        tags = it.tags,
        title = it.title,
        text = it.text,
        publishedAt = it.publishedAt.toMillis(),
        updatedAt = it.publishedAt.toMillis(),
        startingAt = it.startingAt?.toMillis(),
        latLng = it.latLng?.let { GeoPoint(lat = it.x, lng = it.y) },
        createdAt = LocalDateTime.now().toMillis(),
        url = it.url,
        attachments = it.attachments.map { it.toDto() },
        imageUrl = it.imageUrl,
        html = html,
        rawMimeType = rawMimeType,
        rawBase64 = rawBase64,
      )
    }
  )

}

private fun JsonAttachment.toDto(): Attachment = Attachment(
  url = url,
  type = type,
  size = length,
  duration = duration,
)
