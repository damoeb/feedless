package org.migor.rss.rich

import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.Dsl

object HttpUtil {
  private val builderConfig = Dsl.config()
    .setConnectTimeout(60000)
    .setReadTimeout(60000)
    .setFollowRedirect(true)
    .setMaxRedirects(5)
    .build()

  val client: AsyncHttpClient = Dsl.asyncHttpClient(builderConfig)

}
