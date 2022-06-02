package org.migor.rich.rss.service

import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.BoundRequestBuilder
import org.asynchttpclient.Dsl
import org.asynchttpclient.Response
import org.migor.rich.rss.harvest.HarvestException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.ConnectException
import java.net.URL
import java.util.*
import javax.annotation.PostConstruct

@Service
class HttpService {

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

//  private fun askJoinProxyRing() {
//    val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
//    val token = ThreadLocalRandom.current()
//      .ints(TOKEN_LENGTH.toLong(), 0, charPool.size)
//      .asSequence()
//      .map(charPool::get)
//      .joinToString("")
//
//    httpPost("${propertyService.host}/api/http/join?token=$token")
//  }
//  fun httpPost(url: String, body: String? = null, useProxy: Boolean = false): Response {
//    val preparePost = client.preparePost(url)
//    body?.let {
//      preparePost.setBody(body)
//    }
//    val request = preparePost.execute()
//
//    return try {
//      request.get()
//    } catch (e: ConnectException) {
//      throw HarvestException("Cannot connect to $url cause ${e.message}")
//    }
//  }
//  fun joinProxyRing(token: String) {
//  }

  fun prepareGet(url: String): BoundRequestBuilder {
    return client.prepareGet(url)
  }

  fun preparePost(url: String): BoundRequestBuilder {
    return client.preparePost(url)
  }

  fun getContentTypeForUrl(corrId: String, url: String): String? {
    return runCatching {
      val response = execute(corrId, client.prepareHead(url), 200)
      val contentType = response.getHeader("content-type").lowercase()
      log.info("[${corrId} contentType=${contentType}")
      contentType.replace(Regex(";.*"), "")
    }.getOrNull()
  }

  fun executeRequest(corrId: String, request: BoundRequestBuilder, expectedStatusCode: Int): Response {
    return this.execute(corrId, request, expectedStatusCode)
  }

  fun httpGet(corrId: String, url: String, expectedHttpStatus: Int): Response {
    log.info("[$corrId] GET $url")
    return execute(corrId, client.prepareGet(url), expectedHttpStatus)
  }

  private fun execute(corrId: String, request: BoundRequestBuilder, expectedStatusCode: Int): Response {
    return try {
      val response = request.execute().get()

      if (response.statusCode != expectedStatusCode) {
        log.error("[$corrId] -> ${response.statusCode}")
        throw HarvestException("Expected $expectedStatusCode received ${response.statusCode}")
      } else {
        log.info("[$corrId] -> ${response.statusCode}")
      }
      response
    } catch (e: ConnectException) {
      throw HarvestException("Cannot connect cause ${e.message}")
    }
  }

  fun guardedHttpResource(corrId: String, url: String, statusCode: Int, contentTypes: List<String>) {
    val req = client.prepareHead(url)
    val response = req.execute().get()
    assert(response.statusCode == statusCode)
    assert(contentTypes.stream().anyMatch { response.contentType.startsWith(it) })
  }

  fun parseUrl(urlParam: String): String {
    return if (urlParam.startsWith("https://") || urlParam.startsWith("http://")) {
      val url = URL(urlParam)
      rewriteUrl(url)
    } else {
      parseUrl("https://$urlParam")
    }
  }

  private fun rewriteUrl(url: URL): String {
    val hosts = arrayOf("twitter.com" to "nitter.net")
    val match = hosts.firstOrNull { url.host === it.first }
    return Optional.ofNullable(match).map {
      url.toString().replaceFirst(it.first, it.second)
    }.orElse(url.toString())
  }
}
