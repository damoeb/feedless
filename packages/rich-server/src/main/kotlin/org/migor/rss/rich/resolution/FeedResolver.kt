package org.migor.rss.rich.resolution

import org.asynchttpclient.Response
import org.migor.rss.rich.HttpUtil
import org.migor.rss.rich.JsonUtil
import org.migor.rss.rich.harvest.HarvestException
import org.migor.rss.rich.model.SourceType
import org.migor.rss.rich.model.Subscription
import org.slf4j.LoggerFactory
import java.net.ConnectException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object FeedResolver {

  private val log = LoggerFactory.getLogger(FeedResolver::class.simpleName)

  fun resolve(subscription: Subscription): Pair<SourceType, String?> {
    val url = subscription.url!!

    val response = fetch(url)

    if (subscription.url!!.contains("twitter.com")) {
      return Pair<SourceType,String?>(SourceType.TWITTER, null)
    }

    val contentType = response.contentType!!.split(";")[0]

    if (contentType.contains("xml") || response.responseBody.startsWith("<?xml version=")) {
      subscription.sourceType = SourceType.NATIVE
      log.info("subscription ${subscription.id}")
      return Pair<SourceType,String?>(SourceType.NATIVE, null)
    }

    return Pair(SourceType.RSS_PROXY, getRssProxyUrl(url))
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
