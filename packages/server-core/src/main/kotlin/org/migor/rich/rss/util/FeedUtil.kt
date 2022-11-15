package org.migor.rich.rss.util

import com.rometools.rome.feed.synd.SyndContent
import com.rometools.rome.feed.synd.SyndEnclosure
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import org.jsoup.Jsoup
import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.api.dto.RichEnclosure
import org.migor.rich.rss.api.dto.RichFeed
import org.migor.rich.rss.harvest.feedparser.FeedType
import org.migor.rich.rss.service.HttpResponse
import org.springframework.util.MimeType
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

object FeedUtil {

  private val uriDateFormatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")

  // see https://stackoverflow.com/questions/15247742/rfc-822-date-time-format-in-rss-2-0-feeds-cet-not-accepted
  private val rfc822DateFormatter: SimpleDateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z")
  private val rfc3339DateFormatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")

  fun toURI(prefix: String, url: String, publishedAt: Date? = null): String {
    // example tag:diveintomark.org,2004-05-27:1192 from https://web.archive.org/web/20080701231200/http://diveintomark.org/archives/2004/05/28/howto-atom-id
    val basic = "tag:rich-rss,${prefix},${
      URLEncoder.encode(
        url,
        StandardCharsets.UTF_8
      )
    }"
    return Optional.ofNullable(publishedAt).map { basic + ":${uriDateFormatter.format(publishedAt)}" }.orElse(basic)
  }

  fun formatAsRFC822(date: Date): String {
    return rfc822DateFormatter.format(date)
  }

  fun formatAsRFC3339(date: Date): String {
    return rfc3339DateFormatter.format(date)
  }

  fun cleanMetatags(value: String): String {
    return value.removePrefix("<![CDATA[").removeSuffix("]]")
  }

  fun detectFeedTypeForResponse(response: HttpResponse): Pair<FeedType, MimeType?> {

    val mimeType = MimeType.valueOf(response.contentType)
    val contentType = simpleContentType(response)
    return try {
      Pair(detectFeedType(contentType), mimeType)
    } catch (e: RuntimeException) {
      Pair(guessFeedType(response), mimeType)
    }
  }

  fun detectFeedType(contentType: String): FeedType {
    return when (contentType) {
      "application/json" -> FeedType.JSON
      "application/rss+xml" -> FeedType.RSS
      "application/atom+xml" -> FeedType.ATOM
      "text/xml", "application/xml" -> FeedType.XML
      else -> throw RuntimeException("Cannot resolve feedType $contentType")
    }
  }

  private fun simpleContentType(harvestResponse: HttpResponse): String {
    return harvestResponse.contentType.split(";")[0]
  }

  private fun guessFeedType(harvestResponse: HttpResponse): FeedType {
    val html = String(harvestResponse.responseBody)
    return if (html.trimStart().startsWith("<?xml ")) {
      kotlin.runCatching {
        Jsoup.parse(html)
        FeedType.NONE
      }.getOrElse { FeedType.XML }
    } else {
      FeedType.NONE
    }
  }

  private fun fromSyndEntry(entry: SyndEntry): RichArticle {
    val content = entry.contents.firstOrNull()
    val contentText = Optional.ofNullable(entry.description?.value)
      .orElse(Optional.ofNullable(content).map { toText(it) }.orElse(""))
    return RichArticle(
      id = entry.uri,
      title = entry.title,
      tags = entry.categories.map { it.name },
      contentText = contentText,
      contentRaw = content?.value,
      contentRawMime = content?.type,
//      main_image_url: String? = null,
      url = Optional.ofNullable(entry.link).orElse(entry.uri),
      author = entry.author,
      enclosures = entry.enclosures.map { fromSyndEnclosure(it) },
      publishedAt = Optional.ofNullable(entry.publishedDate).orElse(Date()),
    )
  }

  private fun toText(content: SyndContent): String {
    return if (content.type.lowercase().contains("html")) {
      HtmlUtil.html2text(content.value)
    } else {
      content.value
    }
  }

  private fun fromSyndEnclosure(syndEnclosure: SyndEnclosure) = RichEnclosure(
    length = syndEnclosure.length,
    type = syndEnclosure.type,
    url = syndEnclosure.url
  )


  fun fromSyndFeed(feed: SyndFeed) = RichFeed(
    id = feed.uri,
    title = feed.title,
    description = feed.description,
    author = feed.author,
    home_page_url = feed.link,
    language = feed.language,
    expired = false,
    date_published = Optional.ofNullable(feed.publishedDate).orElse(Date()),
    items = feed.entries.map { this.fromSyndEntry(it) },
    feed_url = feed.uri,
    tags = null // todo feed.categories
  )

}
