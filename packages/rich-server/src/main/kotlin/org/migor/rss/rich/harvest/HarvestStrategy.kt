package org.migor.rss.rich.harvest

import com.rometools.rome.feed.synd.SyndEntry
import org.migor.rss.rich.model.Subscription

interface HarvestStrategy {
  fun canHarvest(subscription: Subscription): Boolean
  fun applyTransforms(syndEntry: SyndEntry): SyndEntry
  fun url(subscription: Subscription): String
}
