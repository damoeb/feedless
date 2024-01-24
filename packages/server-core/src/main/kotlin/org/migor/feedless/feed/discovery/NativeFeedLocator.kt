package org.migor.feedless.feed.discovery

import org.apache.commons.lang3.StringUtils
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.migor.feedless.util.FeedUtil
import org.springframework.stereotype.Service
import java.net.URL

@Service
class NativeFeedLocator {
  fun locateInDocument(corrId: String, document: Document, url: String): List<RemoteNativeFeedRef> {
    return document.select("link[rel=alternate][type], link[rel=feed][type]")
      .mapIndexedNotNull { index, element -> toFeedReference(corrId, index, element, url) }
      .distinctBy { it.url }
  }

  private fun toFeedReference(corrId: String, index: Int, element: Element, url: String): RemoteNativeFeedRef? {
    return try {
      RemoteNativeFeedRef(
        absUrl(url, element.attr("href")),
        FeedUtil.detectFeedType(corrId, element.attr("type")),
        StringUtils.trimToNull(element.attr("title")) ?: "Native Feed #${index+1}"
      )
    } catch (e: Exception) {
      null
    }
  }

  private fun absUrl(baseUrl: String, relativeUrl: String): String {
    return try {
      URL(URL(baseUrl), relativeUrl).toURI().toString()
    } catch (e: Exception) {
      relativeUrl
    }
  }

}
