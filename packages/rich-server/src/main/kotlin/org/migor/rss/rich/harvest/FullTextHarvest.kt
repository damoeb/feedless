package org.migor.rss.rich.harvest

import com.rometools.rome.feed.synd.SyndEntry
import org.migor.rss.rich.model.Entry
import org.migor.rss.rich.model.Subscription

open class FullTextHarvest : HarvestStrategy {

  override fun options(): List<HarvestStrategyOption<out Any>> {
    return listOf(
      HarvestStrategyOption("fulltext", true),
    )
  }

  override fun namespace() = "fulltext"

  override fun canHarvest(subscription: Subscription): Boolean {
    return true
  }

  override fun applyPostTransforms(subscription: Subscription, entry: Entry, syndEntry: SyndEntry, feeds: List<RichFeed>): Entry {
    if (subscription.withFullText) {
      // mark to harvest fulltext
    }
    return entry
  }

  override fun urls(subscription: Subscription): List<HarvestUrl> {
    return listOf(
      HarvestUrl(subscription.url!!),
    )
  }
}
