package org.migor.rich.rss.service

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import kotlinx.coroutines.coroutineScope
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.BoundRequestBuilder
import org.asynchttpclient.Dsl
import org.asynchttpclient.Response
import org.asynchttpclient.handler.MaxRedirectException
import org.migor.rich.rss.api.HostOverloadingException
import org.migor.rich.rss.api.TemporaryServerException
import org.migor.rich.rss.harvest.HarvestException
import org.migor.rich.rss.harvest.SiteNotFoundException
import org.migor.rich.rss.util.SafeGuards
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.io.Serializable
import java.net.ConnectException
import java.net.URL
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit


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

  private val cache: MutableMap<String, Bucket> = ConcurrentHashMap()

  val client: AsyncHttpClient = Dsl.asyncHttpClient(builderConfig)

  @Autowired
  lateinit var propertyService: PropertyService

  fun prepareGet(url: String): BoundRequestBuilder {
    return client.prepareGet(url)
  }

  fun getContentTypeForUrl(corrId: String, url: String): String {
    protectFromOverloading(url)
    val response = execute(corrId, client.prepareHead(url), 200)
    val contentType = response.getHeader("content-type").lowercase()
    return contentType.replace(Regex(";.*"), "")
  }

  fun executeRequest(corrId: String, request: BoundRequestBuilder, expectedStatusCode: Int): HttpResponse {
    return toHttpResponse(this.execute(corrId, request, expectedStatusCode))
  }

  @Cacheable(value = ["httpCache"], key = "#url")
  fun httpGet(corrId: String, url: String, expectedHttpStatus: Int): HttpResponse {
    protectFromOverloading(url)
    log.info("[$corrId] GET $url")
    val response = execute(corrId, client.prepareGet(url), expectedHttpStatus)
    return toHttpResponse(response)
  }

  private fun protectFromOverloading(url: String) {
    val actualUrl = URL(url)
    val probes =
      listOf(resolveHostBucket(actualUrl), resolveUrlBucket(actualUrl)).map { it.tryConsumeAndReturnRemaining(1) }
    if (probes.any { !it.isConsumed }) {
      throw HostOverloadingException(
        "Canceled due to host overloading (${actualUrl.host}). See X-Rate-Limit-Retry-After-Seconds",
        probes.maxOf { it.nanosToWaitForRefill })
    }
  }

  fun resolveHostBucket(url: URL): Bucket {
    val cacheKey = url.host
    return cache.computeIfAbsent(cacheKey) {
      Bucket.builder()
        .addLimit(Bandwidth.classic(50, Refill.intervally(50, Duration.ofSeconds(30))))
        .build()
    }
  }

  fun resolveUrlBucket(url: URL): Bucket {
    val cacheKey = "${url.host}${url.path}"
    return cache.computeIfAbsent(cacheKey) {
      Bucket.builder()
        .addLimit(Bandwidth.classic(2, Refill.intervally(2, Duration.ofMinutes(1))))
        .build()
    }
  }

  private fun toHttpResponse(response: Response): HttpResponse = HttpResponse(
    contentType = response.contentType,
    responseBody = SafeGuards.respectMaxSize(response.responseBodyAsStream)
  )

  private fun execute(corrId: String, request: BoundRequestBuilder, expectedStatusCode: Int): Response {
    return try {
      val response = request.execute().get(30, TimeUnit.SECONDS)
      if (response.statusCode != expectedStatusCode) {
        log.error("[$corrId] -> ${response.statusCode}")
        when (response.statusCode) {
          // todo mag readjust bucket
          429 -> throw HostOverloadingException("429 received", Duration.ofMinutes(5).seconds)
          400 -> throw TemporaryServerException("400 received", Duration.ofHours(1).seconds)
          404, 403 -> throw SiteNotFoundException()
          else -> throw HarvestException("Expected $expectedStatusCode received ${response.statusCode}")
        }
      } else {
        log.info("[$corrId] -> ${response.statusCode} ${response.getHeader("content-type")}")
      }
      response
    } catch (e: ConnectException) {
      throw HarvestException("Cannot connect cause ${e.message}")
    } catch (e: MaxRedirectException) {
      throw HarvestException("Max redirects ${e.message}")
    }
  }

  fun guardedHttpResource(corrId: String, url: String, statusCode: Int, contentTypes: List<String>) {
    val req = client.prepareHead(url)
    val response = req.execute().get()
    assert(response.statusCode == statusCode)
    assert(contentTypes.stream().anyMatch { response.contentType.startsWith(it) })
  }

  fun prefixUrl(urlParam: String): String {
    return if (urlParam.startsWith("https://") || urlParam.startsWith("http://")) {
      val url = URL(urlParam)
      rewriteUrl(url)
    } else {
      prefixUrl("https://$urlParam")
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

data class HttpResponse(
  val contentType: String,
  val responseBody: ByteArray,
) : Serializable
