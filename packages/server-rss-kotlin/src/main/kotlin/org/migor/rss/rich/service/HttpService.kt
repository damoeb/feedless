package org.migor.rss.rich.service

import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.BoundRequestBuilder
import org.asynchttpclient.Dsl
import org.asynchttpclient.Response
import org.migor.rss.rich.harvest.HarvestException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.ConnectException
import java.util.concurrent.ThreadLocalRandom
import javax.annotation.PostConstruct
import kotlin.streams.asSequence

@Service
class HttpService {

  private val TOKEN_LENGTH = 64
  private val log = LoggerFactory.getLogger(HttpService::class.simpleName)

  private val builderConfig = Dsl.config()
    .setConnectTimeout(60000)
    .setReadTimeout(60000)
//    .setProxyServerSelector({ uri -> proxyUrl(uri) })
    .setFollowRedirect(true)
    .setMaxRedirects(5)
    .build()

//  private fun proxyUrl(uri: Uri?): ProxyServer? {
//    // todo mag round robin
//    return ProxyServer.Builder(uri.toString(), 80).build()
//  }

  val client: AsyncHttpClient = Dsl.asyncHttpClient(builderConfig)

  @Autowired
  lateinit var propertyService: PropertyService

  @PostConstruct
  fun onInit() {
//    this.askJoinProxyRing()
  }

  private fun askJoinProxyRing() {
    val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    val token = ThreadLocalRandom.current()
      .ints(TOKEN_LENGTH.toLong(), 0, charPool.size)
      .asSequence()
      .map(charPool::get)
      .joinToString("")

    httpPost("${propertyService.publicHost()}/api/http/join?token=$token")
  }

  fun httpPost(url: String, body: String? = null, useProxy: Boolean = false): Response {
    val preparePost = client.preparePost(url)
    body?.let {
      preparePost.setBody(body)
    }
    val request = preparePost.execute()

    return try {
      request.get()
    } catch (e: ConnectException) {
      throw HarvestException("Cannot connect to $url cause ${e.message}")
    }
  }

  fun joinProxyRing(token: String) {
  }

  fun prepareGet(url: String): BoundRequestBuilder {
    return client.prepareGet(url)
  }

  fun executeRequest(request: BoundRequestBuilder): Response {
    return this.execute(request)
  }

  fun httpGet(url: String): Response {
    return execute(client.prepareGet(url))
  }

  private fun execute(request: BoundRequestBuilder): Response {
    return try {
      request.execute().get()
    } catch (e: ConnectException) {
      throw HarvestException("Cannot connect cause ${e.message}")
    }
  }
}
