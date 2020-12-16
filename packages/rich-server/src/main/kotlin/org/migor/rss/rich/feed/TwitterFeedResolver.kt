package org.migor.rss.rich.feed

import org.migor.rss.rich.harvest.HarvestUrl
import org.migor.rss.rich.model.SourceType
import org.migor.rss.rich.model.Subscription
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class TwitterFeedResolver : FeedSourceResolver {
  override fun canHandle(sourceType: SourceType): Boolean {
    return sourceType == SourceType.TWITTER
  }

  override fun feedUrls(subscription: Subscription): List<HarvestUrl> {
    val url = subscription.url!!.replace("twitter.com", "nitter.net")
    val proxy = "http://localhost:3000/api/feed?url=${URLEncoder.encode(url, StandardCharsets.UTF_8)}&rule=DIV%3EDIV%3EDIV%3EDIV%3EDIV%3EDIV%3EDIV%3EDIV%3ESPAN%3EA&output=ATOM"
    return listOf(
      HarvestUrl("$url/rss"),
      HarvestUrl(proxy)
    )
  }
}
