package org.migor.rss.rich.harvest

import org.migor.rss.rich.model.Subscription

interface HarvestStrategy {
  fun canHarvest(subscription: Subscription): Boolean
  fun url(subscription: Subscription): String
}
