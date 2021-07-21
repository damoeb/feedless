package org.migor.rss.rich.discovery

import org.asynchttpclient.Dsl
import org.jsoup.Jsoup


object FeedLocator {

  private val feedContentTypes = arrayOf("application/atom+xml", "application/rss+xml", "application/json")

  fun locate(url: String): List<FeedReference> {
    val builderConfig = Dsl.config()
      .setConnectTimeout(500)
      .setConnectionTtl(2000)
      .setFollowRedirect(true)
      .setMaxRedirects(3)
      .build()

    val client = Dsl.asyncHttpClient(builderConfig)
    val request = client.prepareGet(url).execute()
    val response = request.get()

    val contentType = response.contentType.toLowerCase().split(";")[0]
    return if (feedContentTypes.indexOf(contentType) > -1) {
      listOf(FeedReference(url = url, type = contentType, title = "Feed"))
    } else {

      val document = Jsoup.parse(response.responseBody)

      DiscoveryLocator.locate(document) + LinkLocator.locate(document)
    }
  }
}
