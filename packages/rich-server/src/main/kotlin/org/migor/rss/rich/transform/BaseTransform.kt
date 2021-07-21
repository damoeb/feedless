package org.migor.rss.rich.transform

import com.rometools.rome.feed.synd.SyndEntry
import org.migor.rss.rich.harvest.RichFeed
import org.migor.rss.rich.database.model.Source
import org.migor.rss.rich.database.model.SourceEntry

open class BaseTransform : EntryTransform {
  override fun canHandle(source: Source): Boolean = true

  override fun applyTransform(source: Source, entry: SourceEntry, syndEntry: SyndEntry, feeds: List<RichFeed>): SourceEntry {
//    if (source.withFulltext && source.sourceType != SourceType.TWITTER) {
//      val url = entry.link!!
//      val analysis = analyze(url)

//      val content = HashMap<String, Any>()
//      content.putAll(analysis)
//      entry.content?.let { content.putAll(it) }
//      entry.content = content
//    }
//    entry.score = calculateScore(entry)
    return entry
  }

//  private fun calculateScore(entry: SourceEntry): Double {
//    // todo mag implement score
//    return 0.0
//  }

}
