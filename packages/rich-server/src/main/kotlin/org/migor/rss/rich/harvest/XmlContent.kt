package org.migor.rss.rich.harvest

import com.guseyn.broken_xml.ParsedXML
import com.guseyn.broken_xml.XmlDocument
import com.rometools.rome.io.SyndFeedInput
import java.io.StringReader


class XmlContent : ContentStrategy {
  override fun canProcess(harvestResponse: HarvestResponse): Boolean {
    val contentType = simpleContentType(harvestResponse)
    return contentType.contains("xml")
  }

  private fun simpleContentType(harvestResponse: HarvestResponse): String {
    return harvestResponse.contentType!!.split(";")[0]
  }

  override fun process(harvestResponse: HarvestResponse): RichFeed {
    // parse rss/atom/rdf/opml
    val feedType = detectFeedType(harvestResponse)
    return when (feedType) {
      FeedType.RSS -> parseXml(harvestResponse, feedType)
      FeedType.ATOM -> parseXml(harvestResponse, feedType)
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
    val feed = try {
      input.build(StringReader(harvestResponse.responseBody!!))
    } catch (e: Exception) {
      val document = ParsedXML(harvestResponse.responseBody).document()
      input.build(StringReader(serializeXmlDocument(document)))
    }

    return RichFeed(feed, feedType)
  }

  private fun detectFeedType(harvestResponse: HarvestResponse): FeedType {
    return when(simpleContentType(harvestResponse)) {
//      "application/json" -> FeedType.JSON
      "application/rss+xml" -> FeedType.RSS
      "application/atom+xml" -> FeedType.ATOM
//      "application/xml" -> FeedType.OPML
      else -> throw IllegalArgumentException("Feed contentType ${harvestResponse.contentType} is not supported")
    }
  }

}

enum class FeedType {
  RSS, ATOM, RDF, OPML, JSON
}
