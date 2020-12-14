package org.migor.rss.rich.harvest

import com.rometools.rome.feed.synd.SyndEntry
import org.migor.rss.rich.model.Entry
import org.migor.rss.rich.model.Subscription

open class RssHarvest : HarvestStrategy {
  override fun namespace() = "rss"

  override fun canHarvest(subscription: Subscription): Boolean {
    return true
  }

  override fun applyPostTransforms(subscription: Subscription, entry: Entry, syndEntry: SyndEntry, feeds: List<RichFeed>): Entry {
//    if (subscription.optionsByNamespace(this.namespace())) {
      // mark to harvest fulltext
//    }
    return entry
  }

  override fun urls(subscription: Subscription): List<HarvestUrl> {
    return listOf(
      HarvestUrl(subscription.url!!),
    )
  }

  override fun options(): List<HarvestStrategyOption<out Any>> {
    return listOf()
  }
}
