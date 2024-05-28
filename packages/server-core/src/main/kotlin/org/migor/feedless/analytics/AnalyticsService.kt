package org.migor.feedless.analytics

import jakarta.annotation.PostConstruct
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.asynchttpclient.AsyncCompletionHandlerBase
import org.asynchttpclient.AsyncHandler
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.Dsl
import org.asynchttpclient.HttpResponseStatus
import org.migor.feedless.AppProfiles
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

data class PlausibleEvent(val name: String, val url: String, val domain: String)

@Aspect
@Service
@Profile(AppProfiles.analytics)
@ConfigurationProperties("app.analytics")
class AnalyticsService {

  private val log = LoggerFactory.getLogger(AnalyticsService::class.simpleName)

  lateinit var plausibleUrl: String
  lateinit var plausibleSite: String

  private lateinit var httpClient: AsyncHttpClient

  @PostConstruct
  fun postConstruct() {
    log.info("plausibleUrl: $plausibleUrl")
    if (this.plausibleUrl.isBlank()) {
      log.error("plausibleUrl is empty")
    }
    log.info("plausibleSite: $plausibleSite")
    if (this.plausibleSite.isBlank()) {
      log.error("plausibleSite is empty")
    }
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
      val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
      request.getHeader(HttpHeaders.USER_AGENT)
      request.getHeader(HttpHeaders.CONTENT_TYPE)
      request.requestURI

      // https://plausible.io/docs/events-api
      val event = PlausibleEvent(name = "pageview", url = request.requestURI, domain = plausibleSite)
      val expectedStatusCode = 202

      // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-For
      val forwardedForHeader = "X-Forwarded-For"
      httpClient.preparePost("$plausibleUrl/api/event")
        .addHeader(HttpHeaders.USER_AGENT, request.getHeader(HttpHeaders.USER_AGENT))
        .addHeader(HttpHeaders.CONTENT_TYPE, request.getHeader(HttpHeaders.CONTENT_TYPE))
        .addHeader(forwardedForHeader, request.getHeader(forwardedForHeader))
        .setBody(Json.encodeToString(event))
        .execute(CompletionHandlerBase(expectedStatusCode))

    } catch (e: Exception) {
      log.error(e.message)
    }
  }
}

class CompletionHandlerBase(val expectedStatusCode: Int) : AsyncCompletionHandlerBase() {
  private val log = LoggerFactory.getLogger(AnalyticsService::class.simpleName)

  override fun onStatusReceived(status: HttpResponseStatus?): AsyncHandler.State {
    val actualStatusCode = status?.statusCode
    if (actualStatusCode != expectedStatusCode) {
      log.warn("Received httpStatus $actualStatusCode expected $expectedStatusCode")
    }
    return super.onStatusReceived(status)
  }
}
