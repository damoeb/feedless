package org.migor.rss.rich.harvest.feedparser

import com.rometools.rome.feed.synd.SyndEntry
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.Feed
import org.migor.rss.rich.harvest.FeedData
import org.migor.rss.rich.harvest.HarvestContext
import org.springframework.stereotype.Service

@Service
class NativeFeedResolver : FeedContextResolver {

  override fun priority(): Int {
    return 0
  }

  override fun canHarvest(feed: Feed): Boolean {
    return true
  }

  override fun getHarvestContexts(corrId: String, feed: Feed): List<HarvestContext> {
    return listOf(
      HarvestContext(feed.feedUrl!!),
    )
  }

  override fun mergeFeeds(feedData: List<FeedData>): List<Pair<SyndEntry, Article>> {
    return feedData.first().feed.entries.map { syndEntry -> Pair(syndEntry, Article()) }
  }
}
