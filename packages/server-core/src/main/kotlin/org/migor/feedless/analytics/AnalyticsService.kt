package org.migor.feedless.analytics

import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.future.await
import org.apache.commons.lang3.StringUtils
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.asynchttpclient.AsyncCompletionHandlerBase
import org.asynchttpclient.AsyncHandler
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.Dsl
import org.asynchttpclient.HttpResponseStatus
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.*
import java.util.concurrent.TimeUnit

data class PlausibleEvent(val name: String, val url: String, val domain: String)
data class PlausibleStatsResults(val results: List<PlausibleStatsResult>)
data class PlausibleStatsResult(val date: String /* 2020-09-01 */, val visitors: Int)

fun toFullUrlString(request: HttpServletRequest): String {
  return if (StringUtils.isBlank(request.queryString)) {
    request.requestURL.toString()
  } else {
    request.requestURL.toString() + "?" + request.queryString
  }
}

@Aspect
@Service
@Profile("${AppProfiles.analytics} & ${AppLayer.service}")
@ConfigurationProperties("app.analytics")
class AnalyticsService {

  private val log = LoggerFactory.getLogger(AnalyticsService::class.simpleName)

  lateinit var plausibleUrl: String
  lateinit var plausibleSite: String
  lateinit var plausibleApiKey: String

  private lateinit var httpClient: AsyncHttpClient
  private var canPush: Boolean = true
  private var canPull: Boolean = true

  @PostConstruct
  fun postConstruct() {

    log.info("plausibleUrl: $plausibleUrl")
    val hasUrl = plausibleUrl.isBlank()
    if (hasUrl) {
      log.error("plausibleUrl is empty")
    }
    log.info("plausibleSite: $plausibleSite")
    val hasSite = plausibleSite.isBlank()
    if (hasSite) {
      log.error("plausibleSite is empty")
    }
    val hasKey = plausibleApiKey.isBlank()
    if (hasKey) {
      log.error("plausibleApiKey is empty")
    }

    canPush = hasUrl && hasSite
    canPull = canPush && hasKey

    val builderConfig = Dsl.config()
      .setConnectTimeout(60000)
      .setReadTimeout(60000)
      .setFollowRedirect(true)
      .setMaxRedirects(8)
      .build()

    httpClient = Dsl.asyncHttpClient(builderConfig)
  }

  @Before("@annotation(org.migor.feedless.analytics.Tracked)")
  fun track(joinPoint: JoinPoint) {
    try {
      if (canPush) {
        val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request

        val url = toFullUrlString(request)
        log.debug("track url $url")
        // https://plausible.io/docs/events-api
        val event = PlausibleEvent(name = "pageview", url = url, domain = plausibleSite)
        val expectedStatusCode = 202

        val getHeader = { header: String ->
          StringUtils.trimToEmpty(request.getHeader(header))
        }

        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-For
        val forwardedForHeader = "X-Forwarded-For"
        httpClient.preparePost("$plausibleUrl/api/event")
          .addHeader(HttpHeaders.USER_AGENT, getHeader(HttpHeaders.USER_AGENT))
          .addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
          .addHeader(HttpHeaders.REFERER, getHeader(HttpHeaders.REFERER))
          .addHeader(forwardedForHeader, getHeader(forwardedForHeader))
          .setBody(JsonUtil.gson.toJson(event))
          .execute(CompletionHandlerBase(expectedStatusCode))
      }
    } catch (e: Exception) {
      log.error("track failed: ${e.message}", e)
      canPush = false
    }
  }

  suspend fun getUniquePageViewsForRepository(repoId: UUID): Int {
//    curl "https://plausible.io/api/v1/stats/timeseries?site_id=$SITE_ID&period=6mo&filters=visit:source%3D%3DGoogle" \
//    -H "Authorization: Bearer ${TOKEN}"
    val response = httpClient.prepareGet("$plausibleUrl/api/event")
      .addHeader(HttpHeaders.AUTHORIZATION, "Bearer $plausibleApiKey")
      .execute()
      .toCompletableFuture()
      .orTimeout(5, TimeUnit.SECONDS)
      .await()
//      .get(3, TimeUnit.SECONDS)

    return if (response.statusCode == 200) {
      parseStatsResponse(response.responseBody)
    } else {
      0
    }
  }

  suspend fun parseStatsResponse(responseBody: String): Int {
    return JsonUtil.gson.fromJson(responseBody, PlausibleStatsResults::class.java).results.sumOf { it.visitors }
  }

  fun canPullEvents(): Boolean {
    return canPull
  }
}

class CompletionHandlerBase(val expectedStatusCode: Int) : AsyncCompletionHandlerBase() {
  private val log = LoggerFactory.getLogger(AnalyticsService::class.simpleName)

  override fun onStatusReceived(status: HttpResponseStatus?): AsyncHandler.State {
    val actualStatusCode = status?.statusCode
    if (actualStatusCode != expectedStatusCode) {
      log.debug("Received httpStatus $actualStatusCode expected $expectedStatusCode")
    }
    return super.onStatusReceived(status)
  }
}
