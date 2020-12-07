package org.migor.rss.rich.harvest

import com.rometools.rome.feed.synd.SyndEntry
import org.migor.rss.rich.model.Entry
import org.migor.rss.rich.model.Subscription

class SimpleHarvest: HarvestStrategy {
  override fun canHarvest(subscription: Subscription): Boolean = subscription.isFeedUrl!!

  override fun applyPostTransforms(syndEntry: Pair<Entry, SyndEntry>): Entry {
    TODO("Not yet implemented")
  }

  override fun url(subscription: Subscription): String {
    TODO("Not yet implemented")
  }

}
