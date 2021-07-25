package org.migor.rss.rich.transform

import com.rometools.rome.feed.synd.SyndEntry
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.Feed
import org.migor.rss.rich.harvest.FeedData

open class BaseTransform : EntryTransform {
  override fun canHandle(feed: Feed): Boolean = true

  override fun applyTransform(feed: Feed, article: Article, syndEntry: SyndEntry, feedData: List<FeedData>): Article {
//    if (source.withFulltext && source.sourceType != SourceType.TWITTER) {
//      val url = entry.link!!
//      val analysis = analyze(url)

//      val content = HashMap<String, Any>()
//      content.putAll(analysis)
//      entry.content?.let { content.putAll(it) }
//      entry.content = content
//    }
//    entry.score = calculateScore(entry)
    return article
  }

//  private fun calculateScore(entry: SourceEntry): Double {
//    // todo mag implement score
//    return 0.0
//  }

}
