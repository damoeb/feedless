package org.migor.feedless.feed

import io.micrometer.core.annotation.Timed
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.api.dto.RichFeed
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.feed.exporter.FeedExporter
import org.migor.feedless.util.CryptUtil.newCorrId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import kotlin.time.DurationUnit
import kotlin.time.toDuration

// http://localhost:8080/api/web-to-feed?version=0.1&url=https%3A%2F%2Fheise.de&linkXPath=.%2Fa%5B1%5D&extendContext=&contextXPath=%2F%2Fdiv%5B5%5D%2Fdiv%5B1%5D%2Fdiv%2Fsection%5B1%5D%2Fsection%5B1%5D%2Farticle
// v2 http://localhost:8080/api/w2f?v=0.1&url=https%3A%2F%2Fheise.de&link=.%2Fa%5B1%5D&context=%2F%2Fdiv%5B3%5D%2Fdiv%2Fdiv%5B1%5D%2Fsection%5B1%5D%2Farticle&re=none&q=contains(%23any%2C%20%22EM%22)&out=atom
// v2 http://localhost:8080/api/tf?url=https%3A%2F%2Fwww.telepolis.de%2Fnews-atom.xml&re=none&q=not(contains(%23any%2C%20%22Politik%22))&out=atom
// v1 http://localhost:8080/api/feed?url=https%3A%2F%2Fwww.heise.de&pContext=%2F%2Fbody%2Fdiv%5B3%5D%2Fdiv%2Fdiv%5B1%5D%2Fsection%5B1%5D%2Farticle&pLink=.%2Fa%5B1%5D


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

  @GetMapping(
    "/stream/feed/{feedId}",
    "/feed/{feedId}",
    "/feed:{feedId}",
  )
  fun legacyEntities(@PathVariable("feedId") feedId: String): ResponseEntity<String> {
    return serializeFeed(legacyFeedService.getFeed(feedId), "atom")
  }

  @Throttled
  @Timed
  @GetMapping("/api/web-to-feed", ApiUrls.webToFeed)
  fun handle(
    @RequestParam("url") url: String,
    @RequestParam("link") linkXPath: String,
    @RequestParam("x", defaultValue = "") extendContext: String,
    @RequestParam("context") contextXPath: String,
    @RequestParam("date", required = false) dateXPath: String?,
    @RequestParam("pp", required = false, defaultValue = "false") prerender: Boolean,
    @RequestParam("q") filter: String?,
    @RequestParam("v") version: String,
    @RequestParam("out", required = false) responseFormat: String?,
    request: HttpServletRequest
  ): ResponseEntity<String> {
    val corrId = newCorrId()
    return serializeFeed(
      legacyFeedService.webToFeed(
        corrId,
        url,
        linkXPath,
        extendContext,
        contextXPath,
        dateXPath,
        prerender,
        filter,
        request.requestURI
      ), responseFormat
    )
  }

  @Throttled
  @Timed
  @GetMapping(
    "/api/feeds/transform",
    ApiUrls.transformFeed
  )
  fun transformFeed(
    @RequestParam("url") feedUrl: String,
    @RequestParam("q", required = false) filter: String?,
    @RequestParam("out", required = false, defaultValue = "json") responseFormat: String,
    request: HttpServletRequest
  ): ResponseEntity<String> {
    val corrId = newCorrId()
    return serializeFeed(legacyFeedService.transformFeed(corrId, feedUrl, filter, request.requestURI), responseFormat)
  }

  private fun serializeFeed(feed: RichFeed, responseType: String?): ResponseEntity<String> {
    val (_, convert) = feedExporter.resolveResponseType(newCorrId(), StringUtils.trimToNull(responseType) ?: "atom")
    return convert(
      feed,
      HttpStatus.OK,
      1.toDuration(DurationUnit.DAYS)
    )
  }

  @GetMapping("/api/legacy/auth", "/api/legacy/settings")
  fun settings(): ResponseEntity<String> {
    return ResponseEntity.status(HttpStatus.GONE).build()
  }


}
