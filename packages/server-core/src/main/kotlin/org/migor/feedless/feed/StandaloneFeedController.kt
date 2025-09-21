package org.migor.feedless.feed

import io.micrometer.core.annotation.Timed
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.analytics.AnalyticsService
import org.migor.feedless.analytics.toFullUrlString
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.feed.exporter.FeedExporter
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.session.createRequestContext
import org.migor.feedless.source.SourceId
import org.migor.feedless.util.toLocalDateTime
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import kotlin.time.DurationUnit
import kotlin.time.toDuration

// https://rssproxy.migor.org/api/web-to-feed?version=0.1&url=https%3A%2F%2Fheise.de&linkXPath=.%2Fa%5B1%5D&extendContext=&contextXPath=%2F%2Fdiv%5B5%5D%2Fdiv%5B1%5D%2Fdiv%2Fsection%5B1%5D%2Fsection%5B1%5D%2Farticle
// v2 https://rssproxy.migor.org/api/w2f?v=0.1&url=https%3A%2F%2Fheise.de&link=.%2Fa%5B1%5D&context=%2F%2Fdiv%5B3%5D%2Fdiv%2Fdiv%5B1%5D%2Fsection%5B1%5D%2Farticle&re=none&q=contains(%23any%2C%20%22EM%22)&out=atom
// v2 https://rssproxy.migor.org/api/tf?url=https%3A%2F%2Fwww.telepolis.de%2Fnews-atom.xml&re=none&q=not(contains(%23any%2C%20%22Politik%22))&out=atom
// v1 https://rssproxy.migor.org/api/feed?url=https%3A%2F%2Fwww.heise.de&pContext=%2F%2Fbody%2Fdiv%5B3%5D%2Fdiv%2Fdiv%5B1%5D%2Fsection%5B1%5D%2Farticle&pLink=.%2Fa%5B1%5D
// v1 https://rssproxy.migor.org/api/feed?url=http%3A%2F%2Fheise.de&pContext=%2F%2Fbody%2Fdiv%5B3%5D%2Fdiv%2Fdiv%5B1%5D%2Fsection%5B1%5D%2Farticle&pLink=.%2Fa%5B1%5D&x=s

/**
 * To support old versions of rss-proxy
 */
@Controller
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppLayer.api} & ${AppProfiles.standaloneFeeds}")
class StandaloneFeedController(
  val feedExporter: FeedExporter,
  val standaloneFeedService: StandaloneFeedService,
  val meterRegistry: MeterRegistry,
  val analyticsService: AnalyticsService
) {


  @GetMapping(
    "/stream/bucket/{repositoryId}",
    "/bucket/{repositoryId}",
    "/bucket:{repositoryId}",
    "/stream/bucket/{repositoryId}/atom",
    "/bucket/{repositoryId}/atom",
    "/bucket:{repositoryId}/atom",
  )
  fun bucketFeedWithFormat(
    @PathVariable("repositoryId") repositoryId: String,
  ): ResponseEntity<String> {
    runBlocking {
      analyticsService.track()
    }
    meterRegistry.counter(AppMetrics.standalonePull, listOf(Tag.of("type", "repositoryId"))).increment()
    return standaloneFeedService.getRepository(repositoryId)
  }

  @GetMapping(
    "/stream/feed/{feedId}/atom",
    "/feed/{feedId}/atom",
    "/feed:{feedId}/atom",
    "/stream/feed/{feedId}",
    "/feed/{feedId}",
    "/feed:{feedId}",
  )
  suspend fun legacyEntities(
    @PathVariable("feedId") feedId: String,
    request: HttpServletRequest
  ): ResponseEntity<String> = withContext(createRequestContext()) {
    analyticsService.track()
    meterRegistry.counter(AppMetrics.standalonePull, listOf(Tag.of("type", "feedId"))).increment()
    val feedUrl = toFullUrlString(request)
    val feed = resolveFeedCatching(feedUrl) {
      standaloneFeedService.getFeed(
        SourceId(feedId),
        feedUrl
      )
    }
    feed.export("atom")
  }

  @GetMapping(
    "/api/feed",
  )
  suspend fun web2Feedv1(request: HttpServletRequest): ResponseEntity<String> = withContext(createRequestContext()) {
    analyticsService.track()
    meterRegistry.counter(AppMetrics.standalonePull, listOf(Tag.of("type", "v1"))).increment()
    val feedUrl = toFullUrlString(request)
    val feed = resolveFeedCatching(feedUrl)
    {
      standaloneFeedService.webToFeed(
        request.param("url"),
        request.param("pLink"),
        "",
        request.param("pContext").replace("//body/", "//"),
        null,
        false,
        null,
        request.paramOptional("ts")?.toLong()?.toLocalDateTime(),
        feedUrl
      )
    }
    feed.export(request.param("out", "atom"))
  }

  @Throttled
  @Timed
  @GetMapping("/api/web-to-feed", ApiUrls.webToFeed)
  suspend fun web2Feedv2(request: HttpServletRequest): ResponseEntity<String> = withContext(createRequestContext()) {
    analyticsService.track()
    val feedUrl = toFullUrlString(request)
    meterRegistry.counter(AppMetrics.standalonePull, listOf(Tag.of("type", "v2"))).increment()
    val feed = resolveFeedCatching(feedUrl) {
      standaloneFeedService.webToFeed(
        request.param("url"),
        request.firstParam("link", "linkXPath"),
        request.firstParamOptional("x", "extendContext") ?: "",
        request.firstParam("context", "contextXPath"),
        request.paramOptional("date"),
        request.paramBool("pp"),
        request.paramOptional("q"),
        request.paramOptional("ts")?.toLong()?.toLocalDateTime(),
        feedUrl
      )
    }
    feed.export(request.param("out", "atom"))
  }

  @Throttled
  @Timed
  @GetMapping(
    "/api/feeds/transform",
    ApiUrls.transformFeed
  )
  suspend fun transformFeed(request: HttpServletRequest): ResponseEntity<String> = withContext(createRequestContext()) {
    analyticsService.track()
    meterRegistry.counter(AppMetrics.standalonePull, listOf(Tag.of("type", "transform"))).increment()
    val feedUrl = toFullUrlString(request)
    val feed = resolveFeedCatching(feedUrl) {
      standaloneFeedService.transformFeed(
        request.param("url"),
        request.paramOptional("q"),
        request.paramOptional("ts")?.toLong()?.toLocalDateTime(),
        feedUrl
      )
    }
    feed.export(request.param("out", "atom"))
  }

  private suspend fun resolveFeedCatching(
    feedUrl: String,
    feedProvider: suspend () -> JsonFeed
  ): JsonFeed {
    return try {
      feedProvider()
    } catch (t: Throwable) {
      standaloneFeedService.createErrorFeed(feedUrl, t)
    }
  }


  private fun JsonFeed.export(responseType: String?): ResponseEntity<String> {
    val (_, convert) = feedExporter.resolveResponseType(StringUtils.trimToNull(responseType) ?: "atom")
    return convert(
      this,
      HttpStatus.OK,
      1.toDuration(DurationUnit.DAYS)
    )
  }

  @GetMapping("/api/legacy/auth", "/api/legacy/settings")
  fun settings(): ResponseEntity<String> {
    return ResponseEntity.status(HttpStatus.GONE).build()
  }

}

private fun HttpServletRequest.firstParam(vararg names: String): String {
  return firstParamOptional(*names)
    ?: throw IllegalArgumentException("Expected one of these query parameters [${names.joinToString(",")}]")
}

private fun HttpServletRequest.firstParamOptional(vararg names: String): String? {
  return names.firstNotNullOfOrNull { paramOptional(it) }
}

private fun HttpServletRequest.paramBool(name: String): Boolean {
  return BooleanUtils.toBoolean(this.param(name, "false"))
}

private fun HttpServletRequest.paramOptional(name: String): String? {
  return try {
    getParameter(name)
  } catch (e: Exception) {
    null
  }
}

private fun HttpServletRequest.param(name: String, fallback: String? = null): String {
  return try {
    getParameter(name)
  } catch (e: Exception) {
    fallback ?: throw IllegalArgumentException("Expected query parameter '$name' not found")
  }
}
