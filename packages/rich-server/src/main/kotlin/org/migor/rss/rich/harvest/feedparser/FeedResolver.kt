package org.migor.rss.rich.harvest.feedparser

import org.asynchttpclient.Response
import org.migor.rss.rich.util.FeedUtil
import org.migor.rss.rich.util.HttpUtil
import org.migor.rss.rich.util.JsonUtil
import org.migor.rss.rich.harvest.HarvestException
import org.migor.rss.rich.database.model.Source
import org.migor.rss.rich.database.model.SourceType
import java.net.ConnectException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object FeedResolver {

  fun resolve(source: Source): Triple<SourceType, FeedType, String?> {
    val url = source.url!!

    if (source.url!!.contains("twitter.com")) {
      return Triple<SourceType, FeedType, String?>(SourceType.TWITTER, FeedType.ATOM, null)
    }

    val response = fetch(url)

    val (feedType) = FeedUtil.detectFeedType(response)

    if (feedType != FeedType.NONE) {
      return Triple<SourceType, FeedType, String?>(SourceType.NATIVE, feedType, null)
    }

    return Triple(SourceType.RSS_PROXY, FeedType.ATOM, getRssProxyUrl(url))
  }

  private fun getRssProxyUrl(url: String): String {
    val proxy = "http://localhost:3000/api/feed/live?url=${URLEncoder.encode(url, StandardCharsets.UTF_8)}&output=JSON"
    val response = fetch(proxy)

    val properties = JsonUtil.gson.fromJson<Map<String, Any>>(response.responseBody, Map::class.java)
    // todo mag implement
    return ""
  }

  private fun fetch(url: String): Response {
    val request = HttpUtil.client.prepareGet(url).execute()

    val response = try {
      request.get()
    } catch (e: ConnectException) {
      throw HarvestException("Cannot connect to $url cause ${e.message}")
    }
    if (response.statusCode != 200) {
      throw HarvestException("When fetching $url expected 200 but received ${response.statusCode}")
    }
    return response
  }


}
