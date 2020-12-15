package org.migor.rss.rich.transform

import com.rometools.rome.feed.synd.SyndEntry
import org.migor.rss.rich.harvest.RichFeed
import org.migor.rss.rich.model.Entry
import org.migor.rss.rich.model.EntryStatus
import org.migor.rss.rich.model.SourceType
import org.migor.rss.rich.model.Subscription

open class BaseTransform : EntryTransform {
  override fun canHandle(sourceType: SourceType): Boolean = true

  override fun applyTransform(subscription: Subscription, entry: Entry, syndEntry: SyndEntry, feeds: List<RichFeed>): Entry {
    if (subscription.withFulltext && subscription.sourceType != SourceType.TWITTER) {
      // todo mag
    }
    entry.score = 1.0 // todo mag calculate a quality score
    entry.status = EntryStatus.TRANSFORMED
    return entry
  }

}
