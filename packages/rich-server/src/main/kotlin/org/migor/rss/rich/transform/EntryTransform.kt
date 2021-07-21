package org.migor.rss.rich.transform

import com.rometools.rome.feed.synd.SyndEntry
import org.migor.rss.rich.harvest.RichFeed
import org.migor.rss.rich.database.model.Source
import org.migor.rss.rich.database.model.SourceEntry

interface EntryTransform {
  fun canHandle(source: Source): Boolean
  fun applyTransform(source: Source, entry: SourceEntry, syndEntry: SyndEntry, feeds: List<RichFeed>): SourceEntry
}
