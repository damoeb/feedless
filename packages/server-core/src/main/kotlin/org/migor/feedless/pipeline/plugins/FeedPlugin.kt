package org.migor.feedless.pipeline.plugins

import org.migor.feedless.AppProfiles
import org.migor.feedless.api.fromDto
import org.migor.feedless.feed.asRemoteNativeFeed
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.PluginExecution
import org.migor.feedless.generated.types.RemoteNativeFeed
import org.migor.feedless.generated.types.ScrapedElement
import org.migor.feedless.pipeline.FragmentTransformerPlugin
import org.migor.feedless.util.HtmlUtil
import org.migor.feedless.util.JsonUtil
import org.migor.feedless.web.WebToFeedTransformer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.net.URL

@Service
@Profile(AppProfiles.scrape)
class FeedPlugin : FragmentTransformerPlugin {

  private val log = LoggerFactory.getLogger(FeedPlugin::class.simpleName)

  @Autowired
  lateinit var webToFeedTransformer: WebToFeedTransformer

  override fun id(): String = FeedlessPlugins.org_feedless_feed.name
  override fun listed() = true

  override fun transformFragment(
    corrId: String,
    element: ScrapedElement,
    plugin: PluginExecution,
    url: String
  ): RemoteNativeFeed {
    log.debug("[$corrId] transformFragment using selectors ${JsonUtil.gson.toJson(plugin.params.org_feedless_feed)}")
    val feed = webToFeedTransformer.getFeedBySelectors(
      corrId, plugin.params.org_feedless_feed.fromDto(),
      HtmlUtil.parseHtml(element.selector.html.data, url), URL(url)
    )
      .asRemoteNativeFeed()
    log.debug("[$corrId] transformed to feed with ${feed.items.size} items")
    return feed

  }

  override fun name(): String = "Feed"


}
