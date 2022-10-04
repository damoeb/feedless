package org.migor.rich.rss.harvest.feedparser

import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.api.dto.RichEnclosure
import org.migor.rich.rss.api.dto.RichFeed
import org.migor.rich.rss.harvest.HarvestResponse
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
  private val FORMAT_RFC3339 = "yyyy-MM-dd'T'HH:mm:ss-Z"
  private var formatter = SimpleDateFormat(FORMAT_RFC3339)

  override fun priority(): Int {
    return 1
  }

  override fun canProcess(feedType: FeedType, mimeType: MimeType?): Boolean {
    return feedType == FeedType.JSON
  }

  override fun process(corrId: String, response: HarvestResponse): RichFeed {
    val feed = DefaultFeed.fromString(patchResponse(response))
    return toRichFeed(corrId, feed)
  }

  private fun patchResponse(harvestResponse: HarvestResponse): String {
    val responseBody = String(harvestResponse.response.responseBody).trim()
    return if (responseBody.startsWith("[")) {
      "{\"items\": $responseBody}"
    } else {
      responseBody
    }
  }

  private fun toRichFeed(corrId: String, json: Feed): RichFeed {
    val items = json.items().map { item: Item -> toArticle(corrId, item) }
    return RichFeed(
      id = json.feedUrl(),
      author = json.authors().map { author: Author -> author.name() }.firstOrNull(),
      description = json.description(),
      title = json.title(),
      items = items,
      language = json.language(),
      home_page_url = json.homePageUrl(),
      feed_url = json.feedUrl(),
      expired = json.hasExpired(),
      date_published = items.maxOfOrNull { it.publishedAt }
    )
  }

  private fun toArticle(corrId: String, item: Item): RichArticle {
    val publishedDate = runCatching {
      formatter.parse(item.datePublished())
    }.recover {
      run {
        log.warn("[${corrId}] Cannot parse date ${item.datePublished()}")
        Date()
      }
    }.getOrDefault(Date())

    val contentText = listOf(item.contentText(), item.summary())
      .mapNotNull { StringUtils.trimToNull(it) }.maxByOrNull { it.length }
    return RichArticle(
      id = item.id(),
      title = item.title(),
      tags = item.tags(),
      contentText = StringUtils.trimToEmpty(contentText),
      contentRaw = item.contentHtml(),
      contentRawMime = "text/html",
      imageUrl = item.image(),
      url = item.url(),
      enclosures = toEnclusures(item),
      author = item.author()?.name(),
      publishedAt = publishedDate,
    )
  }

  private fun toEnclusures(item: Item): Collection<RichEnclosure>? {
    return if (item.hasAttachments()) {
      item.attachments().map { a -> RichEnclosure(
        length = a.sizeInBytes(),
        type = a.mimeType(),
        url = a.url()
      ) }
    } else {
      null
    }
  }
}
