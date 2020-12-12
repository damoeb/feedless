package org.migor.rss.rich.harvest

import com.guseyn.broken_xml.ParsedXML
import com.guseyn.broken_xml.XmlDocument
import com.rometools.rome.io.SyndFeedInput
import java.io.StringReader


class XmlContent : ContentStrategy {
  override fun canProcess(response: HarvestResponse): Boolean {
    val contentType = simpleContentType(response)
    return contentType.contains("xml") || response.response.responseBody.startsWith("<?xml version=\"1.0\"")
  }

  private fun simpleContentType(harvestResponse: HarvestResponse): String {
    return harvestResponse.response.contentType!!.split(";")[0]
  }

  override fun process(response: HarvestResponse): RichFeed {
    // parse rss/atom/rdf/opml
    val feedType = detectFeedType(response)
    return when (feedType) {
      FeedType.RSS -> parseXml(response, feedType)
      FeedType.ATOM -> parseXml(response, feedType)
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

  private fun detectFeedType(harvestResponse: HarvestResponse): FeedType {
    return when(simpleContentType(harvestResponse)) {
//      "application/json" -> FeedType.JSON
      "application/rss+xml" -> FeedType.RSS
      "application/atom+xml" -> FeedType.ATOM
      else -> guessFeedType(harvestResponse)
    }
  }

  private fun guessFeedType(harvestResponse: HarvestResponse): FeedType {
    if (harvestResponse.response.responseBody.trimStart().startsWith("<?xml ")) {
      return FeedType.RSS
    }
    throw IllegalArgumentException("Feed contentType ${harvestResponse.response.contentType} is not supported")
  }

}

enum class FeedType {
  RSS, ATOM, JSON
}
