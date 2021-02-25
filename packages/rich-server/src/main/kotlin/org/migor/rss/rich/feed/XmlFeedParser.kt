package org.migor.rss.rich.feed

import com.rometools.rome.io.SyndFeedInput
import org.migor.rss.rich.FeedUtil
import org.migor.rss.rich.XmlUtil
import org.migor.rss.rich.harvest.HarvestResponse
import org.migor.rss.rich.harvest.RichFeed
import org.slf4j.LoggerFactory
import java.io.StringReader


class XmlFeedParser : FeedParser {

  private val log = LoggerFactory.getLogger(XmlFeedParser::class.simpleName)

  override fun canProcess(feedTypeAndContentType: Pair<FeedType, String>): Boolean {
    return arrayOf(FeedType.RSS, FeedType.ATOM, FeedType.XML).indexOf(feedTypeAndContentType.first) > -1
  }

  override fun process(response: HarvestResponse): RichFeed {
    // parse rss/atom/rdf/opml
    val (feedType) = FeedUtil.detectFeedType(response.response)
    return when (feedType) {
      FeedType.RSS -> parseXml(response)
      FeedType.ATOM -> parseXml(response)
      FeedType.XML -> parseXml(response)
      else -> throw RuntimeException("Not implemented")
    }
  }

  private fun parseXml(harvestResponse: HarvestResponse): RichFeed {
    val input = SyndFeedInput()
    input.xmlHealerOn = true
    input.isAllowDoctypes = true
    val responseBody = XmlUtil.explicitCloseTags(harvestResponse.response.responseBody!!)
    val feed = try {
      input.build(StringReader(responseBody))
    } catch (e: Exception) {
      log.warn("Cannot parse feed ${harvestResponse.url} ${e.message}, trying BrokenXmlParser")
      input.build(StringReader(BrokenXmlParser.parse(responseBody)))
    }

    return RichFeed(feed)
  }

}

enum class FeedType {
  RSS, ATOM, JSON, XML, NONE
}
