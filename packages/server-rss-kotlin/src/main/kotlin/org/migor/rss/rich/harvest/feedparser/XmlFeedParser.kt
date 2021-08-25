package org.migor.rss.rich.harvest.feedparser

import com.rometools.rome.io.SyndFeedInput
import org.migor.rss.rich.harvest.FeedData
import org.migor.rss.rich.harvest.HarvestResponse
import org.migor.rss.rich.util.FeedUtil
import org.migor.rss.rich.util.XmlUtil
import org.slf4j.LoggerFactory
import java.io.StringReader


class XmlFeedParser : FeedParser {

  private val log = LoggerFactory.getLogger(XmlFeedParser::class.simpleName)

  override fun canProcess(feedType: FeedType): Boolean {
    return arrayOf(FeedType.RSS, FeedType.ATOM, FeedType.XML).indexOf(feedType) > -1
  }

  override fun process(response: HarvestResponse): FeedData {
    // parse rss/atom/rdf/opml
    return when (FeedUtil.detectFeedTypeForResponse(response.response)) {
      FeedType.RSS, FeedType.ATOM, FeedType.XML -> parseXml(response)
      else -> throw RuntimeException("Not implemented")
    }
  }

  private fun parseXml(harvestResponse: HarvestResponse): FeedData {
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

    return FeedData(feed)
  }

}

enum class FeedType {
  RSS, ATOM, JSON, XML, RSS_PROXY, NONE
}
