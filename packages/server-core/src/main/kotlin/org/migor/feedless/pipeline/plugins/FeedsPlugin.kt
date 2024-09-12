package org.migor.feedless.pipeline.plugins

import org.jsoup.nodes.Document
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.actions.ExecuteActionEntity
import org.migor.feedless.api.toDto
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.feed.FeedParserService
import org.migor.feedless.feed.asRemoteNativeFeed
import org.migor.feedless.feed.discovery.GenericFeedLocator
import org.migor.feedless.feed.discovery.NativeFeedLocator
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.ScrapedFeeds
import org.migor.feedless.pipeline.FragmentOutput
import org.migor.feedless.pipeline.FragmentTransformerPlugin
import org.migor.feedless.scrape.GenericFeedParserOptions
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.util.HtmlUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets

@Service
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
    corrId: String,
    action: ExecuteActionEntity,
    data: HttpResponse,
    logger: LogCollector,
  ): FragmentOutput {
    log.debug("[$corrId] transformFragment")

    val feedMimeTypes = arrayOf(
      "text/xml",
      "text/rss+xml",
      "application/rss+xml",
      "application/rdf+xml",
      "application/rss+xml",
      "application/atom+xml",
      "application/xml"
    )

    val mimeType = data.contentType.lowercase()
    logger.log("Found mimeType=$mimeType")
    return if (feedMimeTypes.any { mimeType.startsWith(it) }) {
      logger.log("Parsing native feed")
      val jsonFeed = feedParserService.parseFeed(corrId, data)
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
        extractFeeds(corrId, document, data.url, logger)
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
    corrId: String,
    document: Document,
    url: String,
    logger: LogCollector,
  ): FragmentOutput {
    val parserOptions = GenericFeedParserOptions()
    val nativeFeeds = nativeFeedLocator.locateInDocument(corrId, document, url)
    logger.log("found ${nativeFeeds.size} native feeds $nativeFeeds")
    val genericFeeds = genericFeedLocator.locateInDocument(corrId, document, url, parserOptions)
    logger.log("found ${genericFeeds.size} generic feeds")
    log.info("[$corrId] Found feedRules=${genericFeeds.size} nativeFeeds=${nativeFeeds.size}")

    return FragmentOutput(
      fragmentName = "feeds",
      feeds = ScrapedFeeds(
        genericFeeds = genericFeeds.map { it.toDto() },
        nativeFeeds = nativeFeeds.map { it.toDto() }
      )
    )
  }

}
