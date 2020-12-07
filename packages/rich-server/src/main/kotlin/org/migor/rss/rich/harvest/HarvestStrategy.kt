package org.migor.rss.rich.harvest

import com.rometools.rome.feed.synd.SyndEntry
import org.migor.rss.rich.model.Entry
import org.migor.rss.rich.model.Subscription

interface HarvestStrategy {
  fun canHarvest(subscription: Subscription): Boolean
  fun applyPostTransforms(syndEntry: Pair<Entry, SyndEntry>): Entry
  fun url(subscription: Subscription): String
}
