package org.migor.feedless.service

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.BoundRequestBuilder
import org.asynchttpclient.Dsl
import org.asynchttpclient.Response
import org.migor.feedless.config.CacheNames
import org.migor.feedless.harvest.HarvestException
import org.migor.feedless.harvest.HostOverloadingException
import org.migor.feedless.harvest.MethodNotAllowedException
import org.migor.feedless.harvest.ResumableHarvestException
import org.migor.feedless.harvest.ServiceUnavailableException
import org.migor.feedless.harvest.SiteNotFoundException
import org.migor.feedless.harvest.TemporaryServerException
import org.migor.feedless.util.SafeGuards
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.io.Serializable
import java.net.ConnectException
import java.net.URL
import java.net.UnknownHostException
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit


@Service
class HttpService {

  private val log = LoggerFactory.getLogger(HttpService::class.simpleName)

  private val builderConfig = Dsl.config()
    .setConnectTimeout(60000)
    .setReadTimeout(60000)
//    .setProxyServerSelector({ uri -> proxyUrl(uri) })
    .setFollowRedirect(true)
    .setMaxRedirects(8)
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

  @Cacheable(value = [CacheNames.HTTP_RESPONSE], key = "#url")
  fun httpGetCaching(
    corrId: String,
    url: String,
    expectedHttpStatus: Int,
    headers: Map<String, Any>? = null
  ): HttpResponse {
    return this.httpGet(corrId, url, expectedHttpStatus, headers)
  }

  fun httpGet(corrId: String, url: String, expectedHttpStatus: Int, headers: Map<String, Any>? = null): HttpResponse {
    protectFromOverloading(url)
    log.debug("[$corrId] GET $url")
    val request = client.prepareGet(url)
    headers?.let {
      headers.forEach {
        request.addHeader(it.key, it.value)
      }
    }
    val response = execute(corrId, request, expectedHttpStatus)
    return toHttpResponse(response)
  }

  private fun protectFromOverloading(url: String) {
    val actualUrl = URL(url)
    val probes =
      listOf(resolveHostBucket(actualUrl), resolveUrlBucket(actualUrl)).map { it.tryConsumeAndReturnRemaining(1) }
    if (probes.any { !it.isConsumed }) {
      throw HostOverloadingException(
        "Canceled due to host overloading (${actualUrl.host}). See X-Rate-Limit-Retry-After-Seconds",
        Duration.ofNanos(probes.maxOf { it.nanosToWaitForRefill })
      )
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
    url = response.uri.toUrl(),
    responseBody = SafeGuards.respectMaxSize(response.responseBodyAsStream),
    statusCode = response.statusCode
  )

  private fun execute(corrId: String, request: BoundRequestBuilder, expectedStatusCode: Int): Response {
    return try {
      val start = System.nanoTime()
      val response = request.execute().get(30, TimeUnit.SECONDS)
      if (response.statusCode != expectedStatusCode) {
        log.info("[$corrId] -> ${response.statusCode}")
        when (response.statusCode) {
          // todo mag readjust bucket
          500 -> throw ResumableHarvestException("500 received", Duration.ofMinutes(5))
          429 -> throw HostOverloadingException("429 received", Duration.ofMinutes(5))
          400 -> throw TemporaryServerException("400 received", Duration.ofHours(1))
          HttpStatus.SERVICE_UNAVAILABLE.value() -> throw ServiceUnavailableException()
          404, 403 -> throw SiteNotFoundException()
          else -> throw HarvestException("Expected $expectedStatusCode received ${response.statusCode}")
        }
      } else {
        val duration = (System.nanoTime() - start) / 100000
        log.info("[$corrId] -> ${response.statusCode} ${response.getHeader("content-type")} [${duration}ms]")
      }
      response
    } catch (e: ConnectException) {
      throw HarvestException("Cannot connect cause ${e.message}")
    } catch (e: ExecutionException) {
      throw HarvestException("${e.message}")
    }
  }

  fun guardedHttpResource(corrId: String, url: String, statusCode: Int, contentTypes: List<String>) {
    if (supportsHead(url)) {
      try {
        val req = client.prepareHead(url)

        val response = req.execute().get()
        if (response.statusCode == 404) {
          throw SiteNotFoundException()
        }
        if (response.statusCode == 405) {
          throw MethodNotAllowedException()
        }
        if (response.statusCode != statusCode) {
          throw IllegalArgumentException("bad status code expected ${statusCode}, actual ${response.statusCode}")
        }
        if (response.contentType == null) {
          throw IllegalArgumentException("invalid contentType null, expected $contentTypes")
        }
        if (!contentTypes.stream().anyMatch { response.contentType.startsWith(it) }) {
          throw IllegalArgumentException("invalid contentType ${response.contentType}, expected $contentTypes")
        }
      } catch (e: UnknownHostException) {
        log.error("[$corrId] ${e.message}")
      } catch (e: SiteNotFoundException) {
        log.error("[$corrId] ${e.message}")
      } catch (e: MethodNotAllowedException) {
        log.error("[$corrId] ${e.message}")
      }
    }
  }

  private fun supportsHead(url: String): Boolean = !url.startsWith(propertyService.nitterHost)

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
  val url: String,
  val statusCode: Int,
  val responseBody: ByteArray,
) : Serializable
