package org.migor.rss.rich.transform

import com.rometools.rome.feed.synd.SyndEntry
import org.migor.rss.rich.harvest.RichFeed
import org.migor.rss.rich.model.Source
import org.migor.rss.rich.model.SourceEntry
import org.migor.rss.rich.model.SourceType

open class BaseTransform : EntryTransform {
  override fun canHandle(sourceType: SourceType): Boolean = true

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
