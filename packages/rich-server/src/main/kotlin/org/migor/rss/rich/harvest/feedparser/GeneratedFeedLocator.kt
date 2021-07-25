package org.migor.rss.rich.harvest.feedparser

import org.asynchttpclient.Response
import org.jsoup.Jsoup
import org.migor.rss.rich.discovery.FeedReference
import org.migor.rss.rich.util.FeedUtil
import java.util.*

object GeneratedFeedLocator {

  fun locate(response: Response, url: String): Optional<FeedReference> {

    val doc = Jsoup.parse(response.responseBody);
    val feedType = FeedUtil.detectFeedTypeForResponse(response)

    if (url.contains("twitter.com")) {
      return Optional.of(FeedReference(url = url, title = doc.title(), type = FeedType.NITTER))
    }

    if (feedType.first == FeedType.NONE) {
      return Optional.of(FeedReference(url = url, title = doc.title(), type = FeedType.RSS_PROXY))
    }

    return Optional.empty<FeedReference>();
  }

//  private fun getRssProxyUrl(url: String): String {
//    val proxy = "http://localhost:3000/api/feed/live?url=${URLEncoder.encode(url, StandardCharsets.UTF_8)}&output=JSON"
//    val response = fetch(proxy)
//
//    val properties = JsonUtil.gson.fromJson<Map<String, Any>>(response.responseBody, Map::class.java)
//    // todo mag implement
//    return ""
//  }

}
