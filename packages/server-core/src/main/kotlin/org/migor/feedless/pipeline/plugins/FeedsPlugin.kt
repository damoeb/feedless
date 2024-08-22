package org.migor.feedless.pipeline.plugins

import org.jsoup.nodes.Document
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
import org.migor.feedless.util.HtmlUtil
import org.migor.feedless.web.GenericFeedParserOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets

@Service
@Profile(AppProfiles.scrape)
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
  override fun transformFragment(
    corrId: String,
    action: ExecuteActionEntity,
    data: HttpResponse,
    logger: (String) -> Unit,
  ): FragmentOutput {
    log.debug("[$corrId] transformFragment")

    val feedMimeTypes = arrayOf("text/xml",
      "text/rss+xml",
      "application/rss+xml",
      "application/rdf+xml",
      "application/rss+xml",
      "application/atom+xml",
      "application/xml")

    val mimeType = data.contentType.lowercase()
    logger("Found mimeType=$mimeType")
    return if (feedMimeTypes.any { mimeType.startsWith(it) }) {
      logger("Parsing native feed")
      val jsonFeed = feedParserService.parseFeed(corrId, data)
      FragmentOutput(
        fragmentName = "",
        feeds = ScrapedFeeds(
          genericFeeds = emptyList(),
          nativeFeeds = listOf(jsonFeed.asRemoteNativeFeed())
        )
      )
    } else {
      if (mimeType.contains("text/html")) {
        logger("extracting feeds")
        val document = HtmlUtil.parseHtml(data.responseBody.toString(StandardCharsets.UTF_8), data.url)
        log.debug("[$corrId] extracting feeds")
        extractFeeds(corrId, document, data.url, logger)
      } else {
        logger("unsupported mimeType")
        log.warn("[$corrId] unsupported mimeType $mimeType")
        FragmentOutput(
          fragmentName = "",
          feeds = ScrapedFeeds(
            genericFeeds = emptyList(),
            nativeFeeds = emptyList()
          )
        )
      }
    }
  }

  override fun name(): String = "Feeds"

  private fun extractFeeds(
    corrId: String,
    document: Document,
    url: String,
    logger: (String) -> Unit,
  ): FragmentOutput {
    val parserOptions = GenericFeedParserOptions()
    val nativeFeeds = nativeFeedLocator.locateInDocument(corrId, document, url)
    logger("found ${nativeFeeds.size} native feeds $nativeFeeds")
    val genericFeeds = genericFeedLocator.locateInDocument(corrId, document, url, parserOptions)
    logger("found ${genericFeeds.size} generic feeds")
    log.info("[$corrId] Found feedRules=${genericFeeds.size} nativeFeeds=${nativeFeeds.size}")

    return FragmentOutput(
      fragmentName = "",
      feeds = ScrapedFeeds(
        genericFeeds = genericFeeds.map { it.toDto() },
        nativeFeeds = nativeFeeds.map { it.toDto() }
      )
    )
  }

}
