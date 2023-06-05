package org.migor.feedless.util

import com.rometools.modules.atom.modules.AtomLinkModuleImpl
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
import com.rometools.rome.feed.synd.SyndLinkImpl
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.migor.feedless.api.dto.RichArticle
import org.migor.feedless.api.dto.RichEnclosure
import org.migor.feedless.api.dto.RichFeed
import org.migor.feedless.feed.parser.FeedType
import org.migor.feedless.feed.parser.json.JsonAttachment
import org.migor.feedless.service.HttpResponse
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import com.rometools.rome.feed.atom.Link as AtomLink
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

    val entryInformation = entry.modules.find { it is EntryInformationImpl } as EntryInformationImpl?
    val imageUrl = entryInformation?.imageUri
    val richArticle = RichArticle()
    richArticle.id = entry.uri
    richArticle.title = entry.title
    richArticle.tags = entry.categories.map { it.name }
    richArticle.contentText = contentText
    richArticle.contentRaw = content?.value
    richArticle.contentRawMime = content?.type
    richArticle.imageUrl = imageUrl
    richArticle.url = entry.link ?: entry.uri
//    richArticle.author = entry.author
    richArticle.attachments = if (entry.enclosures.size == 1) {
      listOf(fromSyndEnclosure(entry.enclosures.first(), entryInformation))
    } else {
      entry.enclosures.map { fromSyndEnclosure(it) }
    }
    richArticle.publishedAt = entry.publishedDate ?: Date()

//    entryInformation?.let {
//      it.duration
//      it.episodeType
//    }

    return richArticle
  }

  private fun toText(content: SyndContent): String {
    return if (content.type.lowercase().contains("html")) {
      HtmlUtil.html2text(content.value)
    } else {
      content.value
    }
  }

  private fun fromSyndEnclosure(syndEnclosure: SyndEnclosure, entryInformation: EntryInformationImpl? = null) = RichEnclosure(
    length = syndEnclosure.length,
    type = syndEnclosure.type,
    url = syndEnclosure.url,
    duration = entryInformation?.duration?.milliseconds?.let { it/1000 }
  )

  fun fromSyndFeed(feed: SyndFeed, feedUrl: String): RichFeed {

    val feedInformation = feed.modules.find { it is FeedInformationImpl } as FeedInformationImpl?
    val imageUrl = feedInformation?.imageUri ?: feed.image?.url
    val richFeed = RichFeed()
    richFeed.id = feed.uri ?: toURI("native", feed.link)
    richFeed.title = feed.title
    richFeed.link = feed.link
    richFeed.description = feed.description
//      icon_url = "",
//    richFeed.author = feed.author
    richFeed.imageUrl = imageUrl
    richFeed.websiteUrl = feed.link
    richFeed.language = feed.language
    richFeed.expired = false
    richFeed.publishedAt = feed.publishedDate ?: Date()
    richFeed.items = feed.entries.map { this.fromSyndEntry(it) }
    richFeed.feedUrl = feedUrl
    return richFeed
  }

  fun toSyndFeed(richFeed: RichFeed): SyndFeed {
    val feed = SyndFeedImpl()
//    val atomLinkModule = AtomLinkModuleImpl()
//    val atomLink = AtomLink()
//    atomLink.type = "self"
//    atomLink.href = richFeed.feedUrl
//    atomLink.type = "application/atom+xml"
//    atomLinkModule.link = atomLink
//    feed.modules = listOf(atomLinkModule)

    feed.uri = "https://feedless.org/feed/${richFeed.id}"
    feed.feedType = "atom_1.0"
    feed.title = richFeed.title
    feed.description = richFeed.description
//    if (StringUtils.isNoneBlank(richFeed.author)) {
//      feed.author = richFeed.author
//    }
    feed.image = richFeed.imageUrl?.let { toSyndImage(it) }
    richFeed.language?.let {
      feed.language = it
    }
    feed.publishedDate = richFeed.publishedAt
    val link = SyndLinkImpl()
    link.type = "self"
    link.href = richFeed.feedUrl
    link.type = "application/atom+xml"
    val website = SyndLinkImpl()
    website.type = "alternate"
    website.href = richFeed.websiteUrl
    website.type = "text/html"
    feed.links = listOf(link, website)

    feed.entries = richFeed.items.map { toSyndEntry(it) }
    return feed
  }

  private fun toSyndEntry(article: RichArticle): SyndEntry {
    val entry = SyndEntryImpl()

    entry.uri = "https://feedless.org/articles/${article.id}"
    entry.title = article.title
    entry.categories = (article.tags ?: emptyList()).map { toSyndCategory(it) }
    entry.contents = toSyndContents(article)
//    entry.enclosures = listOf() // it.imageUrl = imageUrl
    entry.link = article.url
//    if (StringUtils.isNoneBlank(article.author)) {
//      entry.author = article.author
//    }
    entry.author = URL(article.url).host
    entry.enclosures = article.attachments.map { toSyndEnclosure(it) }
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
