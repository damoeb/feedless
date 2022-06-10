package org.migor.rich.rss.exporter

import com.google.gson.GsonBuilder
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import org.migor.rich.rss.api.dto.ArticleJsonDto
import org.migor.rich.rss.api.dto.EnclosureDto
import org.migor.rich.rss.api.dto.FeedJsonDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class JsonFeedExporter {
  private val FORMAT_RFC3339 = "yyyy-MM-dd'T'HH:mm:ss-Z"

  private val log = LoggerFactory.getLogger(JsonFeedExporter::class.simpleName)

  //  http://underpop.online.fr/j/java/help/modules-with-rome-xml-java.html.gz
  private val gson = GsonBuilder()
    .setDateFormat(FORMAT_RFC3339) // https://tools.ietf.org/html/rfc3339
    .create()

  private fun toJsonFeedUrlForPage(feed: FeedJsonDto, page: Int? = null): String {
    return toFeedUrlForPage(feed, "json", page)
  }

  private fun toFeedUrlForPage(feed: FeedJsonDto, type: String, page: Int? = null): String {
    return Optional.ofNullable(page).map { actualPage -> "${feed.feed_url}/${type}?page=${actualPage}" }
      .orElse(feed.feed_url)
  }

  fun toJson(corrId: String, syndFeed: SyndFeed): String = toJson(corrId, toJson(syndFeed))

  fun toJson(corrId: String, feed: FeedJsonDto): String {
    log.info("[${corrId}] to json")

    feed.selfPage?.let {
      if (feed.lastPage != feed.selfPage) {
        feed.next_url = toJsonFeedUrlForPage(feed, feed.selfPage + 1)
      }
      if (feed.selfPage != 0) {
        feed.previous_url = toJsonFeedUrlForPage(feed, feed.selfPage - 1)
      }
    }
    feed.last_url = toJsonFeedUrlForPage(feed, feed.lastPage)
    return gson.toJson(feed)
  }

  private fun toJson(syndFeed: SyndFeed): FeedJsonDto {
    return FeedJsonDto(
      id = syndFeed.link,
      title = syndFeed.title,
      description = syndFeed.description,
      home_page_url = syndFeed.link,
      icon = syndFeed.image?.url,
      date_published = syndFeed.publishedDate,
      items = syndFeed.entries.mapNotNull { toJson(it) },
      feed_url = "feedUrl", // todo should be the actual url
      tags = syndFeed.categories.map { category -> category.name },
      feedType = syndFeed.feedType,
    )
  }

  private fun toJson(syndEntry: SyndEntry): ArticleJsonDto? {
    return try {
      val text = if (syndEntry.description == null) {
        syndEntry.contents.filter { syndContent -> syndContent.type.contains("text") }
          .map { syndContent -> syndContent.value }
          .firstOrNull()
          .toString()
      } else {
        syndEntry.description.value
      }

      val rawContent = syndEntry.contents.filter { syndContent -> syndContent.type.contains("html") }
      ArticleJsonDto(
        id = syndEntry.uri,
        title = syndEntry.title!!,
        tags = syndEntry.categories?.map { syndCategory -> syndCategory.name },
        content_text = Optional.ofNullable(text).orElse(""),
        content_raw = rawContent
          .map { syndContent -> syndContent.value }
          .firstOrNull(),
        content_raw_mime = rawContent
          .map { syndContent -> syndContent.type }
          .firstOrNull(),
        url = syndEntry.link,
        author = syndEntry.author,
        enclosures = syndEntry.enclosures.map { e -> EnclosureDto(url = e.url, type = e.type, length = e.length) },
//        modules = syndEntry.modules,
        date_published = Optional.ofNullable(syndEntry.publishedDate).orElse(Date()),
        main_image_url = syndEntry.enclosures.find { e -> e.type === "image" }?.url, // toodo mag find image enclosure
      )
    } catch (e: Exception) {
      this.log.error(e.message)
      null
    }
  }

}
