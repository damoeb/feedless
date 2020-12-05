package org.migor.rss.rich.harvest

import com.rometools.rome.feed.synd.SyndEntry
import org.migor.rss.rich.model.Subscription

class TwitterHarvest : HarvestStrategy {
  override fun canHarvest(subscription: Subscription): Boolean {
    return subscription.url?.contains("twitter.com")!!
  }

  override fun applyTransforms(syndEntry: SyndEntry): SyndEntry {
    TODO("Not yet implemented")
  }

  override fun url(subscription: Subscription): String {
    return subscription.url!!.replace("twitter.com", "nitter.net") + "/rss"
  }

}
