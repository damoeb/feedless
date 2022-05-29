package org.migor.rich.rss.service

import org.apache.commons.lang3.StringUtils
import org.asynchttpclient.Dsl
import org.asynchttpclient.ListenableFuture
import org.asynchttpclient.Response
import org.migor.rich.rss.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.RestController
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@RestController
class PuppeteerService {

  private val log = LoggerFactory.getLogger(PuppeteerService::class.simpleName)

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var bypassConsentService: BypassConsentService

  @Autowired
  lateinit var environment: Environment

  @Autowired
  lateinit var propertyService: PropertyService

  fun prerender(
    corrId: String,
    url: String,
    script: String?,
  ): PuppeteerScreenshotResponse {
    log.info("[$corrId] prerender url=$url, script=$script")
    return try {
      val puppeteerUrl = "${propertyService.puppeteerHost}/api/intern/prerender/?url=${
        URLEncoder.encode(
          url,
          StandardCharsets.UTF_8
        )
      }&corrId=${corrId}&script=${
        URLEncoder.encode(
          StringUtils.trimToEmpty(script),
          StandardCharsets.UTF_8
        )
      }&optimize=false"

      val request = prepareRequest(corrId, puppeteerUrl)
      log.info("[$corrId] GET $puppeteerUrl")
      val response = request.get()
      log.info("[$corrId] -> ${response.statusCode}")

      if (response.statusCode == 200) {
        JsonUtil.gson.fromJson(response.responseBody, PuppeteerScreenshotResponse::class.java)
      } else {
        throw RuntimeException("Invalid statusCode ${response.statusCode} expected 200")
      }
    } catch (e: Exception) {
      log.error("[$corrId] Unable to discover feeds: ${e.message}")
      // todo mag return error code
      PuppeteerScreenshotResponse(screenshot = "", isError = true, errorMessage = e.message)
    }
  }

  private fun prepareRequest(corrId: String, url: String): ListenableFuture<Response> {
    val builderConfig = Dsl.config()
      .setConnectTimeout(500)
      .setConnectionTtl(2000)
      .setFollowRedirect(true)
      .setMaxRedirects(5)
      .build()

    val client = Dsl.asyncHttpClient(builderConfig)

    val request = client.prepareGet(url)
    bypassConsentService.tryBypassConsent(corrId, request, url)
    return request.execute()
  }
}

data class PuppeteerScreenshotResponse(
  val screenshot: String? = null,
  val html: String? = null,
  val isError: Boolean,
  val errorMessage: String? = null
)
