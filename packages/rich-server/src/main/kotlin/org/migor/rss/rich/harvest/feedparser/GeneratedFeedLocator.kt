package org.migor.rss.rich.harvest.feedparser

import org.asynchttpclient.Response
import org.jsoup.Jsoup
import org.migor.rss.rich.discovery.FeedReference
import org.migor.rss.rich.util.FeedUtil
import java.util.*

object GeneratedFeedLocator {

  fun locate(response: Response, url: String): Optional<FeedReference> {
    fun reply(feedType: FeedType): Optional<FeedReference> {
      val doc = Jsoup.parse(response.responseBody);
      return Optional.of(FeedReference(url = url, title = doc.title(), type = feedType))
    }

    if (url.contains("twitter.com")) {
      return reply(FeedType.NITTER)
    }

    val feedType = FeedUtil.detectFeedTypeForResponse(response)
    if (feedType == FeedType.NONE) {
      // todo mag try if page is supported using rssProxy core lib and the response
      return reply(FeedType.RSS_PROXY)
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
