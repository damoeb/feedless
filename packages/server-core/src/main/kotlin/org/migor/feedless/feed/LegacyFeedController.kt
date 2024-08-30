package org.migor.feedless.feed

import io.micrometer.core.annotation.Timed
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.analytics.Tracked
import org.migor.feedless.analytics.toFullUrlString
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.feed.exporter.FeedExporter
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.util.CryptUtil.newCorrId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import kotlin.time.DurationUnit
import kotlin.time.toDuration

// http://localhost:8080/api/web-to-feed?version=0.1&url=https%3A%2F%2Fheise.de&linkXPath=.%2Fa%5B1%5D&extendContext=&contextXPath=%2F%2Fdiv%5B5%5D%2Fdiv%5B1%5D%2Fdiv%2Fsection%5B1%5D%2Fsection%5B1%5D%2Farticle
// v2 http://localhost:8080/api/w2f?v=0.1&url=https%3A%2F%2Fheise.de&link=.%2Fa%5B1%5D&context=%2F%2Fdiv%5B3%5D%2Fdiv%2Fdiv%5B1%5D%2Fsection%5B1%5D%2Farticle&re=none&q=contains(%23any%2C%20%22EM%22)&out=atom
// v2 http://localhost:8080/api/tf?url=https%3A%2F%2Fwww.telepolis.de%2Fnews-atom.xml&re=none&q=not(contains(%23any%2C%20%22Politik%22))&out=atom
// v1 http://localhost:8080/api/feed?url=https%3A%2F%2Fwww.heise.de&pContext=%2F%2Fbody%2Fdiv%5B3%5D%2Fdiv%2Fdiv%5B1%5D%2Fsection%5B1%5D%2Farticle&pLink=.%2Fa%5B1%5D
// v1 http://localhost:8080/api/feed?url=http%3A%2F%2Fheise.de&pContext=%2F%2Fbody%2Fdiv%5B3%5D%2Fdiv%2Fdiv%5B1%5D%2Fsection%5B1%5D%2Farticle&pLink=.%2Fa%5B1%5D&x=s

/**
 * To support old versions of rss-proxy
 */
@Controller
@Profile("${AppProfiles.feed} & ${AppProfiles.api} & ${AppProfiles.legacyFeeds}")
class LegacyFeedController {

  @Autowired
  private lateinit var feedExporter: FeedExporter

  @Autowired
  private lateinit var legacyFeedService: LegacyFeedService


  @Tracked
  @GetMapping(
    "/stream/bucket/{repositoryId}",
    "/bucket/{repositoryId}",
    "/bucket:{repositoryId}",
  )
  fun bucketFeedWithFormat(
    @PathVariable("repositoryId") repositoryId: String,
  ): ResponseEntity<String> {
    return legacyFeedService.getRepository(repositoryId)
  }

  @Tracked
  @GetMapping(
    "/stream/feed/{feedId}",
    "/feed/{feedId}",
    "/feed:{feedId}",
  )
  fun legacyEntities(@PathVariable("feedId") feedId: String, request: HttpServletRequest): ResponseEntity<String> {
    val corrId = newCorrId()
    val feedUrl = toFullUrlString(request)
    val feed = resolveFeedCatching(corrId, feedUrl) {
      legacyFeedService.getFeed(
        corrId,
        feedId,
        feedUrl
      )
    }
    return feed.export("atom")
  }

  @Tracked
  @GetMapping(
    "/api/feed",
  )
  fun web2Feedv1(request: HttpServletRequest): ResponseEntity<String> {
    val corrId = newCorrId()

    val feedUrl = toFullUrlString(request)
    val feed = resolveFeedCatching(corrId, feedUrl)
    {
      legacyFeedService.webToFeed(
        corrId,
        request.param("url"),
        request.param("pLink"),
        "",
        request.param("pContext").replace("//body/", "//"),
        null,
        false,
        null,
        feedUrl
      )
    }
    return feed.export(request.param("out", "atom"))
  }

  @Tracked
  @Throttled
  @Timed
  @GetMapping("/api/web-to-feed", ApiUrls.webToFeed)
  fun web2Feedv2(request: HttpServletRequest): ResponseEntity<String> {
    val corrId = newCorrId()
    val feedUrl = toFullUrlString(request)
    val feed = resolveFeedCatching(corrId, feedUrl) {
      legacyFeedService.webToFeed(
        corrId,
        request.param("url"),
        request.firstParam("link", "linkXPath"),
        request.firstParamOptional("x", "extendContext") ?: "",
        request.firstParam("context", "contextXPath"),
        request.paramOptional("date"),
        request.paramBool("pp"),
        request.paramOptional("q"),
        toFullUrlString(request)
      )
    }
    return feed.export(request.param("out", "atom"))
  }

  fun JsonFeed.serialize() {

  }

  @Tracked
  @Throttled
  @Timed
  @GetMapping(
    "/api/feeds/transform",
    ApiUrls.transformFeed
  )
  fun transformFeed(request: HttpServletRequest): ResponseEntity<String> {
    val corrId = newCorrId()
    val feedUrl = toFullUrlString(request)
    val feed = resolveFeedCatching(corrId, feedUrl) {
      legacyFeedService.transformFeed(
        corrId,
        request.param("url"),
        request.paramOptional("q"),
        feedUrl
      )
    }
    return feed.export(request.param("out", "atom"))
  }

  private fun resolveFeedCatching(corrId: String, feedUrl: String, feedProvider: () -> JsonFeed): JsonFeed {
    return try {
      val f = feedProvider()
      f
    } catch (t: Throwable) {
      legacyFeedService.createErrorFeed(corrId, feedUrl, t)
    }
  }


  private fun JsonFeed.export(responseType: String?): ResponseEntity<String> {
    val (_, convert) = feedExporter.resolveResponseType(newCorrId(), StringUtils.trimToNull(responseType) ?: "atom")
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
  return names.firstNotNullOfOrNull { paramOptional(it) }
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
