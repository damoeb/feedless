package org.migor.rss.rich.transform

import com.rometools.rome.feed.synd.SyndEntry
import org.migor.rss.rich.harvest.RichFeed
import org.migor.rss.rich.model.Entry
import org.migor.rss.rich.model.SourceType

class NullTransform : EntryTransform {
  override fun canHandle(sourceType: SourceType): Boolean {
    return true
  }

  override fun applyTransform(entry: Entry, syndEntry: SyndEntry, feeds: List<RichFeed>): Entry {
    return entry
  }

}
