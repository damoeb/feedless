package org.migor.feedless.feed.parser

import com.rometools.modules.georss.GeoRSSModule
import com.rometools.modules.itunes.EntryInformationImpl
import com.rometools.modules.itunes.FeedInformationImpl
import com.rometools.modules.mediarss.MediaEntryModule
import com.rometools.modules.mediarss.types.PlayerReference
import com.rometools.modules.mediarss.types.UrlReference
import com.rometools.rome.feed.synd.SyndEnclosure
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.feed.synd.SyndPerson
import com.rometools.rome.io.SyndFeedInput
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.feed.exporter.FeedlessModuleImpl
import org.migor.feedless.feed.exporter.castToFeedlessModule
import org.migor.feedless.feed.parser.json.JsonAttachment
import org.migor.feedless.feed.parser.json.JsonAuthor
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.feed.parser.json.JsonPoint
import org.migor.feedless.util.FeedUtil
import org.migor.feedless.util.HtmlUtil
import org.migor.feedless.util.JsonUtil
import org.migor.feedless.util.toLocalDateTime
import org.slf4j.LoggerFactory
import java.io.StringReader
import java.time.LocalDateTime
import java.util.*

class XmlFeedParser : FeedBodyParser {

  private val log = LoggerFactory.getLogger(XmlFeedParser::class.simpleName)

  override fun priority(): Int {
    return 1
  }

  override fun canProcess(feedType: FeedType): Boolean {
    return arrayOf(FeedType.RSS, FeedType.ATOM, FeedType.XML).indexOf(feedType) > -1
  }

  override suspend fun process(corrId: String, response: HttpResponse): JsonFeed {
    // parse rss/atom/rdf/opml
    val (feedType, _) = FeedUtil.detectFeedTypeForResponse(corrId, response)
    return when (feedType) {
      FeedType.RSS, FeedType.ATOM, FeedType.XML -> fromSyndFeed(parseXml(response), response.url)
      else -> throw IllegalArgumentException("Not implemented ($corrId)")
    }
  }

  private fun parseXml(response: HttpResponse): SyndFeed {
    return runCatching { parseXml(response.responseBody) }
      .onFailure { log.warn("Cannot parse feed ${response.url} ${it.message}, trying BrokenXmlParser") }
      .getOrThrow()
  }

  private fun parseXml(body: ByteArray): SyndFeed {
    val markup = runCatching {
      // pseudo fix namespaces
      val doc = Jsoup.parse(String(body), "", Parser.xmlParser())
      val head = doc.firstElementChild()!!

      val actualNamespaces = head.attributes().asList()
        .map { it.key }
        .filterTo(ArrayList()) { it: String -> it.lowercase().startsWith("xmlns:") }
        .map { it.split(":")[1] }

      head.select("*")
        .map { it.tagName() }.distinct()
        .filter { it.contains(":") }
        .map { it.split(":")[0] }.distinct()
        .filter { namespace -> !actualNamespaces.contains(namespace) }
        .forEach {
          head.attr("xmlns:$it", "http://purl.org/rss/1.0/modules/$it/")
        }
      doc.html()
    }
      .onFailure {
        log.error("parseXml failed. Cannot patch xml namespaces: ${it.message}", it)
      }
      .getOrElse { String(body) }

    val input = SyndFeedInput()
    input.xmlHealerOn = true
    input.isAllowDoctypes = false
    return try {
      input.build(StringReader(markup))
    } catch (e: Exception) {
      log.warn("xml healing failed: Cannot parse feed ${e.message}")
      input.build(StringReader(BrokenXmlParser.parse(markup)))
    }
  }

  private fun fromSyndFeed(feed: SyndFeed, feedUrl: String): JsonFeed {
    // todo mag add/parse geo-spatial information to feed
    val feedInformation = feed.modules.find { it is FeedInformationImpl } as FeedInformationImpl?
    val imageUrl = feedInformation?.imageUri ?: feed.image?.url
    val jsonFeed = JsonFeed()
    jsonFeed.id = feed.uri ?: FeedUtil.toURI("native", feed.link)
    jsonFeed.title = feed.title
//    richFeed.link = feed.link
    jsonFeed.description = feed.description
//      icon_url = "",
//    richFeed.author = feed.author
    jsonFeed.imageUrl = imageUrl
    jsonFeed.websiteUrl = feed.link
    jsonFeed.language = feed.language
    jsonFeed.expired = false

    val feedlessModule = feed.modules.find { it.uri == FeedlessModuleImpl.URI }?.castToFeedlessModule()
    feedlessModule?.let {
      jsonFeed.page = it.getPage()!!
    }

//    jsonFeed.tags = feed.categories?.map { it.name }
    jsonFeed.publishedAt = feed.publishedDate?.toLocalDateTime() ?: LocalDateTime.now()
    jsonFeed.items = feed.entries.map { this.fromSyndEntry(it) }
    jsonFeed.feedUrl = feedUrl
    val tags = mutableListOf<String>()
    feedInformation?.let {
      if (StringUtils.isNotBlank(it.author)) {
        jsonFeed.authors = listOf(JsonAuthor(name = it.author))
      }
      it.keywords?.toList()?.let {
        tags.addAll(it)
      }
    }
    feed.categories?.let {
      tags.addAll(it.map { it.name })
    }
    jsonFeed.tags = tags
    jsonFeed.links = feed.links.filter { it.rel == "next" }.map { it.href }

    return jsonFeed
  }

  private fun fromSyndEntry(entry: SyndEntry): JsonItem {
    val contentHtml = entry.contents.firstOrNull { it.type.lowercase().contains("html") }
    val contentText = Optional.ofNullable(entry.description?.value)
      .orElse("").trimMargin()

    val entryInformationModule = entry.modules.find { it is EntryInformationImpl } as EntryInformationImpl?
    val mediaEntryModule = entry.modules.find { it is MediaEntryModule } as MediaEntryModule?
    val geoModule = entry.modules.find { it is GeoRSSModule } as GeoRSSModule?
    val feedlessModule = entry.modules.find { it.uri == FeedlessModuleImpl.URI }?.castToFeedlessModule()

    val article = JsonItem()
    article.id = entry.uri
    article.title = entry.title

    feedlessModule?.let {
      feedlessModule.getLatLng()?.let {
        article.latLng = JsonUtil.gson.fromJson(it, JsonPoint::class.java)
      }
      article.startingAt = feedlessModule.getStartingAt()?.toLocalDateTime()
      article.contentRawBase64 = feedlessModule.getData()
      article.contentRawMime = feedlessModule.getDataType()
    }

    geoModule?.let {
      val point = JsonPoint()
      point.x = it.position.latitude
      point.y = it.position.longitude
      article.latLng = point
    }

    val tags = mutableListOf<String>()
    tags.addAll(entry.categories.map { it.name })
    mediaEntryModule?.let {
      tags.addAll(it.metadata.keywords)
      tags.addAll(it.metadata.categories.map { it.label })
    }

    article.tags = tags

    val assignHtmlContent = {
//      article.contentRawMime = "text/html"
      article.contentHtml = contentHtml!!.value
    }
    val assignOtherContent = {
      entry.contents.firstOrNull { !it.type.lowercase().contains("html") }?.let {
        article.contentRawMime = it.type
        article.contentRawBase64 = it.value
      }
    }
    val assignTextContent = { text: String ->
      article.contentText = text
    }

    // content
    val hasText = StringUtils.isNotBlank(contentText)
    val hasHtml = contentHtml != null
    if (hasText || hasHtml) {
      if (hasText && hasHtml) {
        assignTextContent(contentText)
        assignHtmlContent()
      } else {
        if (hasText) {
          assignTextContent(contentText)
        } else {
          assignHtmlContent()
          try {
            val body = HtmlUtil.parseHtml(contentText, "").body()
            assignTextContent(body.text())
          } catch (e: Exception) {
            // ignore
          }
        }
      }
    }
    assignOtherContent()

    article.url = entry.link ?: entry.uri

    val authors = mutableListOf<JsonAuthor>()
    (entryInformationModule?.author ?: entry.author)?.let {
      authors.add(JsonAuthor(name = it))
    }
    authors.addAll(entry.authors.map { it.toJsonAuthor() })
    article.authors = authors

    val attachments = arrayListOf<JsonAttachment>()
    if (entry.enclosures.size == 1) {
      attachments.add(fromSyndEnclosure(entry.enclosures.first(), entryInformationModule))
    } else {
      attachments.addAll(entry.enclosures.map { fromSyndEnclosure(it) })
    }
    mediaEntryModule?.let {
      it.mediaContents.filter { it.reference is UrlReference || it.reference is PlayerReference }
        .forEach { mediaContent ->
          run {
            val url = when (mediaContent.reference) {
              is UrlReference -> (mediaContent.reference as UrlReference).url
              is PlayerReference -> (mediaContent.reference as PlayerReference).url
              else -> throw IllegalArgumentException("no supported")
            }
            attachments.add(
              JsonAttachment(
                length = 0,
                type = mediaContent.type ?: mediaContent.medium ?: "unknown",
                url = url.toString(),
                duration = null
              )
            )
          }
        }
    }
    article.attachments = attachments

    val imageUrl = entryInformationModule?.imageUri ?: attachments.filterTo(ArrayList()) { it: JsonAttachment ->
      StringUtils.isNotBlank(
        it.type
      )
    }
      .firstOrNull { it.type.lowercase().startsWith("image") }?.url
    article.imageUrl = imageUrl

    article.publishedAt = entry.publishedDate?.toLocalDateTime() ?: LocalDateTime.now()

//    entryInformation?.let {
//      it.duration
//      it.episodeType
//    }

    return article
  }

  private fun fromSyndEnclosure(syndEnclosure: SyndEnclosure, entryInformation: EntryInformationImpl? = null) =
    JsonAttachment(
      length = syndEnclosure.length,
      type = syndEnclosure.type,
      url = syndEnclosure.url,
      duration = entryInformation?.duration?.milliseconds?.let { it / 1000 }
    )

}

private fun SyndPerson.toJsonAuthor(): JsonAuthor {
  return JsonAuthor(name = name, url = uri?.toString(), email = email)
}

enum class FeedType {
  RSS, ATOM, JSON, XML, NONE
}
