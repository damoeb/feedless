package org.migor.rich.rss.service

import io.micrometer.core.annotation.Timed
import io.micrometer.core.instrument.MeterRegistry
import org.apache.commons.lang3.StringUtils
import org.asynchttpclient.Dsl
import org.asynchttpclient.ListenableFuture
import org.asynchttpclient.Response
import org.migor.rich.rss.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.RestController
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import javax.annotation.PostConstruct

@RestController
class PuppeteerService {

  private var puppeteerHostFound: Boolean = false
  private val log = LoggerFactory.getLogger(PuppeteerService::class.simpleName)

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var meterRegistry: MeterRegistry

  @Autowired
  lateinit var environment: Environment

  @Autowired
  lateinit var httpService: HttpService

  @Value("\${app.puppeteerHost:#{null}}")
  lateinit var puppeteerHost: Optional<String>

  @Value("\${app.puppeteerTimeoutMs}")
  var puppeteerTimeout: Int = 10000

  fun canPrerender(): Boolean = puppeteerHostFound
  fun hasHost(): Boolean = puppeteerHost.map { StringUtils.isNoneBlank(it) }.orElse(false)

  private fun canConnect(corrId: String, host: String): Boolean {
    return runCatching {
      httpService.httpGet(corrId, "${host}/health", 200)
      true
    }.getOrElse {
      log.error("[${corrId}] Cannot connect to puppeteer ${puppeteerHost.get()}: ${it.message}")
      false
    }
  }

  @PostConstruct
  fun postConstruct() {
    puppeteerHostFound = puppeteerHost.map { canConnect("boot", it) }.orElse(false)

    if (puppeteerHostFound) {
      log.info("Prerendering using ${puppeteerHost.get()}")
    } else {
      log.warn("To use prerendering env PUPPETEER_HOST")
    }
  }

  @Timed
  fun prerender(
    corrId: String,
    url: String,
    script: String?,
    optimize: Boolean = false
  ): PuppeteerScreenshotResponse {
    meterRegistry.counter("prerender").increment()
    log.info("[$corrId] prerender url=$url, script=$script")
    return try {
      val puppeteerUrl = "${puppeteerHost.get()}/api/intern/prerender/?url=${
        URLEncoder.encode(
          url,
          StandardCharsets.UTF_8
        )
      }&corrId=${corrId}&timeout=${puppeteerTimeout}&script=${
        URLEncoder.encode(
          StringUtils.trimToEmpty(script),
          StandardCharsets.UTF_8
        )
      }&optimize=${optimize}"

      // todo use cache and overload protection
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

    return client.prepareGet(url).execute()
  }
}

data class PuppeteerScreenshotResponse(
  val screenshot: String? = null,
  val html: String? = null,
  val isError: Boolean,
  val errorMessage: String? = null
)
