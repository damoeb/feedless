package org.migor.feedless.feed.discovery

import org.apache.commons.lang3.StringUtils
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.util.FeedUtil
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.net.URI

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.scrape} & ${AppLayer.service}")
class NativeFeedLocator {
  suspend fun locateInDocument(document: Document, url: String): List<RemoteNativeFeedRef> {
    return document.select("link[rel=alternate][type], link[rel=feed][type]")
      .mapIndexedNotNull { index, element -> toFeedReference(index, element, url) }
      .distinctBy { it.url }
  }

  private suspend fun toFeedReference(index: Int, element: Element, url: String): RemoteNativeFeedRef? {
    return try {
      RemoteNativeFeedRef(
        absUrl(url, element.attr("href")),
        FeedUtil.detectFeedType(element.attr("type"))!!,
        StringUtils.trimToNull(element.attr("title")) ?: "Native Feed #${index + 1}"
      )
    } catch (e: Exception) {
      null
    }
  }

  private suspend fun absUrl(baseUrl: String, relativeUrl: String): String {
    return try {
      val baseUri = URI(baseUrl)
      val resolvedUri = baseUri.resolve(relativeUrl)
      resolvedUri.toString()
    } catch (e: Exception) {
      relativeUrl
    }
  }

}
