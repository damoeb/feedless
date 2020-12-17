package org.migor.rss.rich.transform

import com.rometools.rome.feed.synd.SyndEntry
import org.migor.rss.rich.harvest.RichFeed
import org.migor.rss.rich.model.Entry
import org.migor.rss.rich.model.SourceType
import org.migor.rss.rich.model.Subscription

interface EntryTransform {
  fun canHandle(sourceType: SourceType): Boolean
  fun applyTransform(subscription: Subscription, entry: Entry, syndEntry: SyndEntry, feeds: List<RichFeed>): Entry
}
