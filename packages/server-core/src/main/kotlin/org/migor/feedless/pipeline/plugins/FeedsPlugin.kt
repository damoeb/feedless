package org.migor.feedless.pipeline.plugins

import org.jsoup.nodes.Document
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.toDto
import org.migor.feedless.feed.discovery.GenericFeedLocator
import org.migor.feedless.feed.discovery.NativeFeedLocator
import org.migor.feedless.feed.discovery.RemoteNativeFeedRef
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.PluginExecution
import org.migor.feedless.generated.types.ScrapedElement
import org.migor.feedless.generated.types.ScrapedFeeds
import org.migor.feedless.pipeline.FragmentTransformerPlugin
import org.migor.feedless.util.HtmlUtil
import org.migor.feedless.web.GenericFeedParserOptions
import org.migor.feedless.web.GenericFeedRule
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile(AppProfiles.scrape)
class FeedsPlugin : FragmentTransformerPlugin {

  private val log = LoggerFactory.getLogger(FeedsPlugin::class.simpleName)

  @Autowired
  lateinit var nativeFeedLocator: NativeFeedLocator

  @Autowired
  lateinit var genericFeedLocator: GenericFeedLocator

  override fun id(): String = FeedlessPlugins.org_feedless_feeds.name
  override fun listed() = false

  override fun transformFragment(
    corrId: String,
    element: ScrapedElement,
    plugin: PluginExecution,
    url: String
  ): ScrapedFeeds {
    log.debug("[$corrId] transformFragment")

    val document = HtmlUtil.parseHtml(element.selector.html.data, url)
    log.debug("[$corrId] extracting feeds")
    val (nativeFeeds, genericFeeds) = extractFeeds(corrId, document, url, false)
    return ScrapedFeeds.newBuilder()
      .genericFeeds(genericFeeds.map { it.toDto() })
      .nativeFeeds(nativeFeeds.map { it.toDto() })
      .build()
  }

  override fun name(): String = "Feeds"

  private fun extractFeeds(
    corrId: String,
    document: Document,
    url: String,
    strictMode: Boolean
  ): Pair<List<RemoteNativeFeedRef>, List<GenericFeedRule>> {
    val parserOptions = GenericFeedParserOptions(
      strictMode = strictMode
    )
    val nativeFeeds = nativeFeedLocator.locateInDocument(corrId, document, url)
    val genericFeedRules = genericFeedLocator.locateInDocument(corrId, document, url, parserOptions)
    log.info("[$corrId] Found feedRules=${genericFeedRules.size} nativeFeeds=${nativeFeeds.size}")
    return Pair(nativeFeeds, genericFeedRules)
  }

}
