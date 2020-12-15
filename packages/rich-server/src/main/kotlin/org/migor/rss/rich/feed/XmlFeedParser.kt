package org.migor.rss.rich.feed

import com.guseyn.broken_xml.ParsedXML
import com.guseyn.broken_xml.XmlDocument
import com.rometools.rome.io.SyndFeedInput
import org.migor.rss.rich.FeedUtil
import org.migor.rss.rich.harvest.HarvestResponse
import org.migor.rss.rich.harvest.RichFeed
import java.io.StringReader


class XmlContent : FeedParser {
  override fun canProcess(response: HarvestResponse): Boolean {
    val feedType = FeedUtil.detectFeedType(response.response)
    return arrayOf(FeedType.RSS, FeedType.ATOM).indexOf(feedType) > -1
  }

  override fun process(response: HarvestResponse): RichFeed {
    // parse rss/atom/rdf/opml
    val feedType = FeedUtil.detectFeedType(response.response)
    return when (feedType) {
      FeedType.RSS -> parseXml(response, feedType)
      FeedType.ATOM -> parseXml(response, feedType)
      FeedType.XML -> parseXml(response, feedType)
      else -> throw RuntimeException("Not implemented")
    }
  }

  private fun serializeXmlDocument(document: XmlDocument?): String {
    document.toString()
    TODO("Not yet implemented")

  }

  private fun parseXml(harvestResponse: HarvestResponse, feedType: FeedType): RichFeed {
    val input = SyndFeedInput()
    input.xmlHealerOn = true
    input.isAllowDoctypes = true
    val responseBody = harvestResponse.response.responseBody!!
    val feed = try {
      input.build(StringReader(responseBody))
    } catch (e: Exception) {
      val document = ParsedXML(responseBody).document()
      input.build(StringReader(serializeXmlDocument(document)))
    }

    return RichFeed(feed, feedType)
  }

}

enum class FeedType {
  RSS, ATOM, JSON, XML, NONE
}
