package org.migor.feedless.feed.parser

import com.rometools.modules.itunes.EntryInformationImpl
import com.rometools.modules.itunes.FeedInformationImpl
import com.rometools.rome.feed.synd.SyndEnclosure
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import org.migor.feedless.api.dto.RichArticle
import org.migor.feedless.api.dto.RichEnclosure
import org.migor.feedless.api.dto.RichFeed
import org.migor.feedless.harvest.HarvestResponse
import org.migor.feedless.util.FeedUtil
import org.slf4j.LoggerFactory
import java.io.StringReader
import java.util.*

class XmlFeedParser : FeedBodyParser {

  private val log = LoggerFactory.getLogger(XmlFeedParser::class.simpleName)

  override fun priority(): Int {
    return 1
  }

  override fun canProcess(feedType: FeedType): Boolean {
    return arrayOf(FeedType.RSS, FeedType.ATOM, FeedType.XML).indexOf(feedType) > -1
  }

  override fun process(corrId: String, response: HarvestResponse): RichFeed {
    // parse rss/atom/rdf/opml
    val (feedType, _) = FeedUtil.detectFeedTypeForResponse(corrId, response.response)
    return when (feedType) {
      FeedType.RSS, FeedType.ATOM, FeedType.XML -> fromSyndFeed(parseXml(response), response.url)
      else -> throw IllegalArgumentException("Not implemented ($corrId)")
    }
  }

  private fun parseXml(harvestResponse: HarvestResponse): SyndFeed {
    return runCatching { parseXml(harvestResponse.response.responseBody) }
      .onFailure { log.warn("Cannot parse feed ${harvestResponse.url} ${it.message}, trying BrokenXmlParser") }
      .getOrThrow()
  }

  fun parseXml(body: ByteArray): SyndFeed {
    val markup = runCatching {
      // pseudo fix namespaces
      val doc = Jsoup.parse(String(body), "", Parser.xmlParser())
      val head = doc.firstElementChild()!!

      val actualNamespaces = head.attributes().asList()
        .map { it.key }
        .filter { it.lowercase().startsWith("xmlns:") }
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
        log.error("Cannot patch xml namespaces: ${it.message}")
      }
      .getOrElse { String(body) }

    val input = SyndFeedInput()
    input.xmlHealerOn = true
    input.isAllowDoctypes = false
    return try {
      input.build(StringReader(markup))
    } catch (e: Exception) {
      log.warn("Cannot parse feed ${e.message}, trying BrokenXmlParser")
      input.build(StringReader(BrokenXmlParser.parse(markup)))
    }
  }

  private fun fromSyndFeed(feed: SyndFeed, feedUrl: String): RichFeed {

    val feedInformation = feed.modules.find { it is FeedInformationImpl } as FeedInformationImpl?
    val imageUrl = feedInformation?.imageUri ?: feed.image?.url
    val richFeed = RichFeed()
    richFeed.id = feed.uri ?: FeedUtil.toURI("native", feed.link)
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

  private fun fromSyndEntry(entry: SyndEntry): RichArticle {
    val content = entry.contents.firstOrNull()
    val contentText = Optional.ofNullable(entry.description?.value)
      .orElse("")

    val entryInformation = entry.modules.find { it is EntryInformationImpl } as EntryInformationImpl?
    val imageUrl = entryInformation?.imageUri
    val richArticle = RichArticle()
    richArticle.id = entry.uri
    richArticle.title = entry.title
    richArticle.tags = entry.categories.map { it.name }
    richArticle.contentText = contentText
    richArticle.contentRawBase64 = content?.value
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

  private fun fromSyndEnclosure(syndEnclosure: SyndEnclosure, entryInformation: EntryInformationImpl? = null) = RichEnclosure(
    length = syndEnclosure.length,
    type = syndEnclosure.type,
    url = syndEnclosure.url,
    duration = entryInformation?.duration?.milliseconds?.let { it/1000 }
  )

}

enum class FeedType {
  RSS, ATOM, JSON, XML, NONE
}
