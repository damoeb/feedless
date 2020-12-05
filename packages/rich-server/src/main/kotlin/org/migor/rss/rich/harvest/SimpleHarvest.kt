package org.migor.rss.rich.harvest

import org.migor.rss.rich.model.Subscription

class SimpleHarvest: HarvestStrategy {
  override fun canHarvest(subscription: Subscription): Boolean = subscription.isFeedUrl!!

  override fun url(subscription: Subscription): String {
    TODO("Not yet implemented")
  }

}
