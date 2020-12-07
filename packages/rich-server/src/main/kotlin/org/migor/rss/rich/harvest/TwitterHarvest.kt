package org.migor.rss.rich.harvest

import com.rometools.rome.feed.synd.SyndEntry
import org.migor.rss.rich.model.Entry
import org.migor.rss.rich.model.Subscription
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class TwitterHarvest : HarvestStrategy {
  override fun canHarvest(subscription: Subscription): Boolean {
    return subscription.url?.contains("twitter.com")!!
  }

  override fun applyPostTransforms(group: Pair<Entry, SyndEntry>): Entry {
    // todo mag implement
    val entry = group.first
    group.second.contents

    return entry
  }

  override fun url(subscription: Subscription): String {
    val url = subscription.url!!.replace("twitter.com", "nitter.net")
    val proxiedUrl = "http://localhost:3000/api/feed?url=${URLEncoder.encode(url, StandardCharsets.UTF_8)}&rule=DIV%3EDIV%3EDIV%3EDIV%3EDIV%3EDIV%3EDIV%3EA&output=ATOM"
    return proxiedUrl
//    return subscription.url!!.replace("twitter.com", "nitter.net") + "/rss"
  }

}
