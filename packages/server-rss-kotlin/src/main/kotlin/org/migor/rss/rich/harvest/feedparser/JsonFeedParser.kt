package org.migor.rss.rich.harvest.feedparser

import com.rometools.rome.feed.synd.*
import org.migor.rss.rich.harvest.FeedData
import org.migor.rss.rich.harvest.HarvestResponse
import org.slf4j.LoggerFactory
import org.springframework.util.MimeType
import software.tinlion.pertwee.Author
import software.tinlion.pertwee.Feed
import software.tinlion.pertwee.Item
import software.tinlion.pertwee.feed.DefaultFeed

class JsonFeedParser : FeedBodyParser {

  private val log = LoggerFactory.getLogger(JsonFeedParser::class.simpleName)

  override fun priority(): Int {
    return 1
  }

  override fun canProcess(feedType: FeedType, mimeType: MimeType?): Boolean {
    return feedType == FeedType.JSON
  }

  override fun process(response: HarvestResponse): FeedData {
    val feed = DefaultFeed.fromString(patchResponse(response))
    return FeedData(toSyndFeed(feed))
  }

  private fun patchResponse(response: HarvestResponse): String? {
    val responseBody = response.response.responseBody.trim()
    return if (responseBody.startsWith("[")) {
      "{\"items\": $responseBody}"
    } else {
      responseBody
    }
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
