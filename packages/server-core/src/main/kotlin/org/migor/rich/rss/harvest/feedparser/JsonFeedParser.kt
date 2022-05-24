package org.migor.rich.rss.harvest.feedparser

import com.rometools.rome.feed.synd.SyndContentImpl
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndEntryImpl
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.feed.synd.SyndFeedImpl
import com.rometools.rome.feed.synd.SyndPerson
import com.rometools.rome.feed.synd.SyndPersonImpl
import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.harvest.HarvestResponse
import org.migor.rich.rss.util.FeedExporter
import org.slf4j.LoggerFactory
import org.springframework.util.MimeType
import software.tinlion.pertwee.Author
import software.tinlion.pertwee.Feed
import software.tinlion.pertwee.Item
import software.tinlion.pertwee.feed.DefaultFeed
import java.text.SimpleDateFormat
import java.util.*

class JsonFeedParser : FeedBodyParser {

  private val log = LoggerFactory.getLogger(JsonFeedParser::class.simpleName)
  private var formatter = SimpleDateFormat(FeedExporter.FORMAT_RFC3339)

  override fun priority(): Int {
    return 1
  }

  override fun canProcess(feedType: FeedType, mimeType: MimeType?): Boolean {
    return feedType == FeedType.JSON
  }

  override fun process(corrId: String, response: HarvestResponse): SyndFeed {
    val feed = DefaultFeed.fromString(patchResponse(response))
    return toSyndFeed(corrId, feed)
  }

  private fun patchResponse(response: HarvestResponse): String? {
    val responseBody = response.response.responseBody.trim()
    return if (responseBody.startsWith("[")) {
      "{\"items\": $responseBody}"
    } else {
      responseBody
    }
  }

  private fun toSyndFeed(corrId: String, json: Feed): SyndFeed {
    val feed = SyndFeedImpl()
    feed.authors = json.authors().map { author: Author -> asPerson(author) }
    feed.description = json.description()
    feed.title = json.title()
    feed.entries = json.items().map { item: Item -> asEntry(corrId, item) }
    feed.language = json.language()
    feed.link = json.homePageUrl()
    feed.publishedDate = feed.entries.map { entry -> entry.publishedDate }.maxOrNull()

    return feed
  }

  private fun asPerson(author: Author): SyndPerson {
    val p = SyndPersonImpl()
    p.name = author.name()
    p.uri = author.url()
    return p
  }

  private fun asEntry(corrId: String, item: Item): SyndEntry {
    val e = SyndEntryImpl()
    e.uri = item.url()
    e.link = item.url()
    e.title = item.title()
    if (StringUtils.isNotBlank(item.contentText())) {
      val content = SyndContentImpl()
      content.value = item.contentText()
      content.type = "text"
      e.description = content
    }
    e.publishedDate = runCatching {
      formatter.parse(item.datePublished())
    }.recover {
      run {
        log.warn("[${corrId}] Cannot parse date ${item.datePublished()}")
        Date()
      }
    }.getOrNull()
//  todo mag e.description = item.summary()
    return e
  }
}
