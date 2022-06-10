package org.migor.rich.rss.harvest.feedparser

import com.rometools.rome.feed.synd.SyndContentImpl
import com.rometools.rome.feed.synd.SyndEntryImpl
import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.api.dto.ArticleJsonDto
import org.migor.rich.rss.api.dto.FeedJsonDto
import org.migor.rich.rss.harvest.HarvestResponse
import org.migor.rich.rss.util.SafeGuards
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

  override fun process(corrId: String, response: HarvestResponse): FeedJsonDto {
    val feed = DefaultFeed.fromString(patchResponse(response))
    return toSyndFeed(corrId, feed)
  }

  private fun patchResponse(response: HarvestResponse): String? {
    val responseBody = SafeGuards.guardedToString(response.response.responseBodyAsStream).trim()
    return if (responseBody.startsWith("[")) {
      "{\"items\": $responseBody}"
    } else {
      responseBody
    }
  }

  private fun toSyndFeed(corrId: String, json: Feed): FeedJsonDto {
    val items = json.items().map { item: Item -> asEntry(corrId, item) }
    return FeedJsonDto(
      id = json.feedUrl(),
      author = json.authors().map { author: Author -> author.name() }.firstOrNull(),
      description = json.description(),
      title = json.title(),
      items = items,
      language = json.language(),
      home_page_url = json.homePageUrl(),
      feed_url = json.feedUrl(),
      date_published = items.map { it.date_published }.maxOrNull()
    )
  }

  private fun asEntry(corrId: String, item: Item): ArticleJsonDto {
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
    val publishedDate = runCatching {
      formatter.parse(item.datePublished())
    }.recover {
      run {
        log.warn("[${corrId}] Cannot parse date ${item.datePublished()}")
        Date()
      }
    }.getOrNull()


//  todo mag e.description = item.summary()
    return ArticleJsonDto(
      id = item.id(),
      title = item.title(),
      tags = item.tags(),
      content_text = item.contentText(),
      content_raw = item.contentHtml(),
      content_raw_mime = "text/html",
      main_image_url = item.image(),
      url = item.url(),
      author = item.author()?.name(),
//    val enclosures: Collection<EnclosureDto>? = null,
      date_published = Optional.ofNullable(publishedDate).orElse(Date()),
    )
  }
}
