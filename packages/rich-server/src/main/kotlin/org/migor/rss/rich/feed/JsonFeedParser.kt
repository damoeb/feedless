package org.migor.rss.rich.feed

import com.rometools.rome.feed.synd.*
import org.migor.rss.rich.harvest.HarvestResponse
import org.migor.rss.rich.harvest.RichFeed
import org.slf4j.LoggerFactory
import software.tinlion.pertwee.Author
import software.tinlion.pertwee.Feed
import software.tinlion.pertwee.Item
import software.tinlion.pertwee.feed.DefaultFeed

class JsonFeedParser: FeedParser {

  private val log = LoggerFactory.getLogger(JsonFeedParser::class.simpleName)

  override fun canProcess(feedTypeAndContentType: Pair<FeedType, String>): Boolean {
    return feedTypeAndContentType.first == FeedType.JSON
  }

  override fun process(response: HarvestResponse): RichFeed {
    // todo mag check the body
    val feed = DefaultFeed.fromString(response.response.responseBody)
    return RichFeed(toSyndFeed(feed))
  }

  private fun toSyndFeed(json: Feed): SyndFeed {
    val feed = SyndFeedImpl()
    feed.authors = json.authors().map { author: Author -> asPerson(author) }
    feed.description = json.description()
    feed.title = json.title()
    feed.entries = json.items().map { item: Item -> asEntry(item) }
    feed.language = json.language()

    return feed
  }

  private fun asPerson(author: Author): SyndPerson {
    val p = SyndPersonImpl()
    p.name = author.name()
    p.uri = author.url()
    return p
  }

  private fun asEntry(item: Item): SyndEntry {
    val e = SyndEntryImpl()
    e.uri = item.url()
    e.title = item.title()
//  todo mag e.description = item.summary()
    return e
  }

}
