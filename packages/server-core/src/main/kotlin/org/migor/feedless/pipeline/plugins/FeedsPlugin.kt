package org.migor.feedless.pipeline.plugins

import org.jsoup.nodes.Document
import org.migor.feedless.AppProfiles
import org.migor.feedless.actions.ExecuteActionEntity
import org.migor.feedless.api.toDto
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.feed.discovery.GenericFeedLocator
import org.migor.feedless.feed.discovery.NativeFeedLocator
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.PluginExecutionData
import org.migor.feedless.generated.types.ScrapedFeeds
import org.migor.feedless.pipeline.FragmentTransformerPlugin
import org.migor.feedless.util.HtmlUtil
import org.migor.feedless.web.GenericFeedParserOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
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

  override fun id(): String = FeedlessPlugins.org_feedless_feeds.name
  override fun listed() = false

  override fun transformFragment(
    corrId: String,
    action: ExecuteActionEntity,
    data: HttpResponse,
  ): PluginExecutionData {
    log.debug("[$corrId] transformFragment")

    val document = HtmlUtil.parseHtml(data.responseBody.toString(StandardCharsets.UTF_8), data.url)
    log.debug("[$corrId] extracting feeds")
    return extractFeeds(corrId, document, data.url)
  }

  override fun name(): String = "Feeds"

  private fun extractFeeds(
    corrId: String,
    document: Document,
    url: String,
  ): PluginExecutionData {
    val parserOptions = GenericFeedParserOptions()
    val nativeFeeds = nativeFeedLocator.locateInDocument(corrId, document, url)
    val genericFeeds = genericFeedLocator.locateInDocument(corrId, document, url, parserOptions)
    log.info("[$corrId] Found feedRules=${genericFeeds.size} nativeFeeds=${nativeFeeds.size}")

    return PluginExecutionData(org_feedless_feeds = ScrapedFeeds(
      genericFeeds = genericFeeds.map { it.toDto() },
      nativeFeeds = nativeFeeds.map { it.toDto() }
    )
    )
  }

}
