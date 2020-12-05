package org.migor.rss.rich.harvest

import org.migor.rss.rich.model.Subscription

class TwitterHarvest : HarvestStrategy {
  override fun canHarvest(subscription: Subscription): Boolean {
    return subscription.url?.contains("twitter.com")!!
  }

  override fun url(subscription: Subscription): String {
    return subscription.url!!.replace("twitter.com", "nitter.net") + "/rss"
  }

}
