package org.migor.rss.rich.transform

import com.rometools.rome.feed.synd.SyndEntry
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.Feed
import org.migor.rss.rich.harvest.FeedData

interface ArticleTransform {
  fun canHandle(feed: Feed): Boolean
  fun applyTransform(feed: Feed, article: Article, syndEntry: SyndEntry, feedData: List<FeedData>): Article
}
