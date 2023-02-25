package org.migor.rich.rss.util

import com.rometools.modules.itunes.EntryInformationImpl
import com.rometools.modules.itunes.FeedInformationImpl
import com.rometools.rome.feed.synd.SyndCategory
import com.rometools.rome.feed.synd.SyndCategoryImpl
import com.rometools.rome.feed.synd.SyndContent
import com.rometools.rome.feed.synd.SyndContentImpl
import com.rometools.rome.feed.synd.SyndEnclosure
import com.rometools.rome.feed.synd.SyndEnclosureImpl
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndEntryImpl
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.feed.synd.SyndFeedImpl
import com.rometools.rome.feed.synd.SyndImage
import com.rometools.rome.feed.synd.SyndImageImpl
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.api.dto.RichEnclosure
import org.migor.rich.rss.api.dto.RichFeed
import org.migor.rich.rss.harvest.feedparser.FeedType
import org.migor.rich.rss.harvest.feedparser.json.JsonAttachment
import org.migor.rich.rss.service.HttpResponse
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

object FeedUtil {

  private val uriDateFormatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")

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

  fun cleanMetatags(value: String): String {
    return value.removePrefix("<![CDATA[").removeSuffix("]]")
  }

  fun detectFeedTypeForResponse(response: HttpResponse): Pair<FeedType, String> {
    val mimeType = response.contentType
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

    val entryInformation = entry.modules.find { it is EntryInformationImpl }
    val imageUrl = Optional.ofNullable(entryInformation)
      .map { it as EntryInformationImpl }
      .map { it.imageUri }
      .orElse(null)

    val richArticle = RichArticle()
    richArticle.id = entry.uri
    richArticle.title = entry.title
    richArticle.tags = entry.categories.map { it.name }
    richArticle.contentText = contentText
    richArticle.contentRaw = content?.value
    richArticle.contentRawMime = content?.type
    richArticle.imageUrl = imageUrl
    richArticle.url = Optional.ofNullable(entry.link).orElse(entry.uri)
//    richArticle.author = entry.author
    richArticle.attachments = entry.enclosures.map { fromSyndEnclosure(it) }
    richArticle.publishedAt = Optional.ofNullable(entry.publishedDate).orElse(Date())
    return richArticle
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

  fun fromSyndFeed(feed: SyndFeed, feedUrl: String): RichFeed {

    val feedInformation = feed.modules.find { it is FeedInformationImpl }
    val imageUrl = Optional.ofNullable(feedInformation)
      .map { it as FeedInformationImpl }
      .map { it.imageUri }
      .orElse(feed.image?.url)
    val richFeed = RichFeed()
    richFeed.id = Optional.ofNullable(feed.uri).orElse(toURI("native", feed.link))
    richFeed.title = feed.title
    richFeed.description = feed.description
//      icon_url = "",
//    richFeed.author = feed.author
    richFeed.imageUrl = imageUrl
    richFeed.websiteUrl = feed.link
    richFeed.language = feed.language
    richFeed.expired = false
    richFeed.publishedAt = Optional.ofNullable(feed.publishedDate).orElse(Date())
    richFeed.items = feed.entries.map { this.fromSyndEntry(it) }
    richFeed.feedUrl = feedUrl
    return richFeed
  }

  fun toSyndFeed(richFeed: RichFeed): SyndFeed {
    val feed = SyndFeedImpl()
    feed.uri = toURI("feed", richFeed.id)
    feed.feedType = "atom_1.0"
    feed.title = richFeed.title
    feed.description = richFeed.description
//    if (StringUtils.isNoneBlank(richFeed.author)) {
//      feed.author = richFeed.author
//    }
    feed.image = Optional.ofNullable(richFeed.imageUrl).map { toSyndImage(it) }.orElse(null)
    feed.link = richFeed.websiteUrl
    feed.language = richFeed.language
    feed.publishedDate = feed.publishedDate
    feed.entries = richFeed.items.map { toSyndEntry(it) }
    return feed
  }

  private fun toSyndEntry(article: RichArticle): SyndEntry {
    val entry = SyndEntryImpl()

    entry.uri = toURI("article", article.url)
    entry.title = article.title
    entry.categories = Optional.ofNullable(article.tags).orElse(emptyList()).map { toSyndCategory(it) }
    entry.contents = toSyndContents(article)
//    entry.enclosures = listOf() // it.imageUrl = imageUrl
    entry.link = article.url
//    if (StringUtils.isNoneBlank(article.author)) {
//      entry.author = article.author
//    }
    entry.enclosures = Optional.ofNullable(article.attachments).orElse(emptyList()).map { toSyndEnclosure(it) }
    entry.publishedDate = article.publishedAt

    return entry
  }

  private fun toSyndEnclosure(it: JsonAttachment): SyndEnclosure {
    val e = SyndEnclosureImpl()
    e.url = it.url
    e.type = it.type
    return e
  }

  private fun toSyndCategory(it: String): SyndCategory {
    val c = SyndCategoryImpl()
    c.name = it
    return c
  }

  private fun toSyndContents(it: RichArticle): List<SyndContent> {
    val contents = mutableListOf<SyndContent>()
    if (StringUtils.isNoneBlank(it.contentRaw)) {
      val other = SyndContentImpl()
      other.value = it.contentRaw
      other.type = it.contentRawMime
      contents.add(other)
    }

    val plain = SyndContentImpl()
    plain.value = it.contentText
    plain.type = "text/plain"
    contents.add(plain)

    return contents
  }

  private fun toSyndImage(imageUrl: String): SyndImage {
    val image = SyndImageImpl()
    image.url = imageUrl
    return image
  }
}
