package org.migor.rss.rich.transform

import com.rometools.rome.feed.synd.SyndEntry
import org.migor.rss.rich.harvest.RichFeed
import org.migor.rss.rich.model.Source
import org.migor.rss.rich.model.SourceEntry
import org.migor.rss.rich.model.SourceType

interface EntryTransform {
  fun canHandle(sourceType: SourceType): Boolean
  fun applyTransform(source: Source, entry: SourceEntry, syndEntry: SyndEntry, feeds: List<RichFeed>): SourceEntry
}
