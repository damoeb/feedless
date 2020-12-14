package org.migor.rss.rich.harvest

import com.rometools.rome.feed.synd.SyndEntry
import org.migor.rss.rich.model.Entry
import org.migor.rss.rich.model.Subscription

interface HarvestStrategy {
  fun canHarvest(subscription: Subscription): Boolean
  fun applyPostTransforms(subscription: Subscription, entry: Entry, syndEntry: SyndEntry, feeds: List<RichFeed>): Entry
  fun urls(subscription: Subscription): List<HarvestUrl>
  fun options(): List<HarvestStrategyOption<out Any>>
  fun namespace(): String
}
