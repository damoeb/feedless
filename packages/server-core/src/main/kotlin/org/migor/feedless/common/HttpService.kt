package org.migor.feedless.common

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.BoundRequestBuilder
import org.asynchttpclient.Dsl
import org.asynchttpclient.Response
import org.migor.feedless.FatalHarvestException
import org.migor.feedless.HostOverloadingException
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.SiteNotFoundException
import org.migor.feedless.TemporaryServerException
import org.migor.feedless.config.CacheNames
import org.migor.feedless.user.corrId
import org.migor.feedless.util.SafeGuards
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.web.util.UrlUtils
import org.springframework.stereotype.Service
import java.io.Serializable
import java.net.ConnectException
import java.net.MalformedURLException
import java.net.URI
import java.net.URL
import java.net.UnknownHostException
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.coroutines.coroutineContext


@Service
class HttpService(
  @Value("\${app.apiGatewayUrl}")
  private val apiGatewayUrl: String
) {

  private val log = LoggerFactory.getLogger(HttpService::class.simpleName)

  private lateinit var gatewayHost: String

  private val builderConfig = Dsl.config()
    .setConnectTimeout(Duration.ofSeconds(60))
    .setReadTimeout(Duration.ofSeconds(60))
//    .setMaxConnections(20)
//    .setProxyServerSelector({ uri -> proxyUrl(uri) })
    .setFollowRedirect(true)
    .setMaxRedirects(8)
    .build()

  private val cache: MutableMap<String, Bucket> = ConcurrentHashMap()

  private val client: AsyncHttpClient = Dsl.asyncHttpClient(builderConfig)

  @PostConstruct
  fun postConstruct() {
    gatewayHost = URI(apiGatewayUrl).toURL().host
  }

  suspend fun prepareGet(url: String): BoundRequestBuilder {
    val urlWithProtocol = addProtocol(url)
    assert(UrlUtils.isAbsoluteUrl(urlWithProtocol)) { "Provided url is not valid" }
    return client.prepareGet(urlWithProtocol)
  }

  private fun addProtocol(url: String): String {
    return if (!url.startsWith("http://", true) && !url.startsWith("https://", true)) {
      "https://$url"
    } else {
      url
    }
  }

  suspend fun executeRequest(request: BoundRequestBuilder, expectedStatusCode: Int): HttpResponse {
    return toHttpResponse(this.execute(request, expectedStatusCode))
  }

  @Cacheable(value = [CacheNames.HTTP_RESPONSE], key = "#url")
  suspend fun httpGetCaching(
    url: String,
    expectedHttpStatus: Int,
    headers: Map<String, String>? = null
  ): HttpResponse {
    log.debug("cache miss $url")
    return this.httpGet(url, expectedHttpStatus, headers)
  }

  suspend fun httpGet(
    url: String,
    expectedHttpStatus: Int,
    headers: Map<String, String>? = null
  ): HttpResponse {
    protectFromOverloading(url)
    log.debug("[${coroutineContext.corrId()}] GET $url")
    val request = prepareGet(url)
    headers?.let {
      headers.forEach {
        request.addHeader(it.key, it.value)
      }
    }
    return toHttpResponse(execute(request, expectedHttpStatus))
  }

  private suspend fun protectFromOverloading(url: String) {
    val actualUrl = try {
      URL(url)
    } catch (e: Exception) {
      throw MalformedURLException("bad url ${e.message} $url")
    }
    if (actualUrl.host != gatewayHost) {
      val probes =
        listOf(resolveHostBucket(actualUrl), resolveUrlBucket(actualUrl)).map { it.tryConsumeAndReturnRemaining(1) }
      if (probes.any { !it.isConsumed }) {
        val waitFor = Duration.ofNanos(probes.maxOf { it.nanosToWaitForRefill })
        if (waitFor.toMillis() < 1000) {
          delay(waitFor.toMillis())
        } else {
          throw HostOverloadingException(
            "host overloading ${actualUrl.host}",
            Duration.ofNanos(probes.maxOf { it.nanosToWaitForRefill })
          )
        }
      }
    }
  }

  suspend fun resolveHostBucket(url: URL): Bucket {
    val cacheKey = url.host
    return cache.computeIfAbsent(cacheKey) {
      Bucket.builder()
        .addLimit(Bandwidth.classic(50, Refill.intervally(50, Duration.ofSeconds(30))))
        .build()
    }
  }

  suspend fun resolveUrlBucket(url: URL): Bucket {
    val cacheKey = "${url.host}${url.path}"
    return cache.computeIfAbsent(cacheKey) {
      Bucket.builder()
        .addLimit(Bandwidth.classic(2, Refill.intervally(2, Duration.ofMinutes(1))))
        .build()
    }
  }

  private fun toHttpResponse(response: Response): HttpResponse = HttpResponse(
    contentType = response.contentType,
    url = response.uri.toUrl(),
    responseBody = SafeGuards.respectMaxSize(response.responseBodyAsStream),
    statusCode = response.statusCode
  )

  private suspend fun execute(request: BoundRequestBuilder, expectedStatusCode: Int): Response {
    val corrId = coroutineContext.corrId()!!
    return try {
      val response = withContext(Dispatchers.IO) {
        request.execute().get(30, TimeUnit.SECONDS)
      }
      log.debug("[$corrId] -> ${response.statusCode}")
      if (response.statusCode != expectedStatusCode) {
        when (response.statusCode) {
          // todo mag readjust bucket
          500 -> throw ResumableHarvestException("500 received", Duration.ofMinutes(5))
          429 -> throw HostOverloadingException("429 received", Duration.ofMinutes(5))
          400 -> throw TemporaryServerException("400 received", Duration.ofHours(1))
//          HttpStatus.SERVICE_UNAVAILABLE.value() -> throw ServiceUnavailableException(corrId)
          in 400..499 -> throw SiteNotFoundException(response.uri.toUrl())
          in 500..599 -> throw ResumableHarvestException(response.uri.toUrl(), Duration.ofHours(5))
          else -> throw FatalHarvestException("Expected $expectedStatusCode received ${response.statusCode}")
        }
      } else {
        log.debug("[$corrId] -> ${response.getHeader("content-type")}")
      }
      response
    } catch (e: Exception) {
      if (e is NullPointerException) {
        log.error(e.message, e)
      }
      if (e is UnknownHostException || e is ConnectException || e is TimeoutException || e is ExecutionException) {
        throw ResumableHarvestException("${e.message}", Duration.ofMinutes(5))
      } else {
        if (e is ResumableHarvestException || e is FatalHarvestException) {
          throw e
        } else {
          throw FatalHarvestException("${e.message}")
        }
      }
    }
  }

//  fun guardedHttpResource(corrId: String, url: String, statusCode: Int, contentTypes: List<String>) {
//    if (supportsHead(url)) {
//      val req = client.prepareHead(url)
//
//      val response = req.execute().get()
//      if (response.statusCode == 404) {
//        throw SiteNotFoundException(url)
//      }
//      if (response.statusCode != 405) {
//        if (response.statusCode != statusCode) {
//          throw IllegalArgumentException("bad status code expected ${statusCode}, actual ${response.statusCode} ($corrId)")
//        }
//        if (response.contentType == null) {
//          throw IllegalArgumentException("invalid contentType null, expected $contentTypes ($corrId)")
//        }
//        if (!contentTypes.stream().anyMatch { response.contentType.startsWith(it) }) {
//          throw IllegalArgumentException("invalid contentType ${response.contentType}, expected $contentTypes")
//        }
//      }
//    }
//  }

}

data class HttpResponse(
  val contentType: String,
  val url: String,
  val statusCode: Int,
  val responseBody: ByteArray,
) : Serializable
