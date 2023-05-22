package org.migor.feedless.feed.parser

import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import org.migor.feedless.api.dto.RichFeed
import org.migor.feedless.harvest.HarvestResponse
import org.migor.feedless.util.FeedUtil
import org.slf4j.LoggerFactory
import java.io.StringReader

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
    val (feedType, _) = FeedUtil.detectFeedTypeForResponse(response.response)
    return when (feedType) {
      FeedType.RSS, FeedType.ATOM, FeedType.XML -> FeedUtil.fromSyndFeed(parseXml(response), response.url)
      else -> throw RuntimeException("Not implemented")
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
}

enum class FeedType {
  RSS, ATOM, JSON, XML, NONE
}
