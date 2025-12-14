package org.migor.feedless.feed

import kotlinx.coroutines.currentCoroutineContext
import org.locationtech.jts.geom.Point
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.common.HttpService
import org.migor.feedless.data.jpa.JtsUtil
import org.migor.feedless.feed.parser.CalendarFeedParser
import org.migor.feedless.feed.parser.FeedBodyParser
import org.migor.feedless.feed.parser.JsonFeedParser
import org.migor.feedless.feed.parser.NullFeedParser
import org.migor.feedless.feed.parser.XmlFeedParser
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.feed.parser.json.JsonPoint
import org.migor.feedless.geo.LatLonPoint
import org.migor.feedless.user.corrId
import org.migor.feedless.util.FeedUtil
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.service}")
class FeedParserService(
  private val httpService: HttpService,
) {

  private val log = LoggerFactory.getLogger(FeedParserService::class.simpleName)

  private val feedBodyParsers: Array<FeedBodyParser> = arrayOf(
    XmlFeedParser(),
    JsonFeedParser(),
    CalendarFeedParser(),
    NullFeedParser()
  )

  init {
    feedBodyParsers.sortByDescending { feedBodyParser -> feedBodyParser.priority() }
    log.debug(
      "Using bodyParsers ${
        feedBodyParsers.joinToString(", ") { contentStrategy -> "$contentStrategy priority: ${contentStrategy.priority()}" }
      }"
    )
  }

  suspend fun parseFeed(response: HttpResponse): JsonFeed {
    val corrId = currentCoroutineContext().corrId()
    log.debug("[$corrId] Parsing feed")
    val feedType = FeedUtil.detectFeedTypeForResponse(response)!!
    log.debug("[$corrId] Parse feedType=$feedType")
    val bodyParser = feedBodyParsers.first { bodyParser ->
      bodyParser.canProcess(feedType)
    }
    return runCatching {
      bodyParser.process(response)
    }.onFailure {
      log.info("[${corrId}] bodyParser ${bodyParser::class.simpleName} failed with ${it.message}")
    }.getOrThrow()
  }

  suspend fun parseFeedFromUrl(url: String): JsonFeed {
    val corrId = currentCoroutineContext().corrId()
    log.debug("[$corrId] parseFeedFromUrl $url")
//    httpService.guardedHttpResource(
//      corrId,
//      url,
//      200,
//      listOf("text/", "application/xml", "application/json", "application/rss", "application/atom", "application/rdf")
//    )
    val request = httpService.prepareGet(url)
//    authHeader?.let {
//      request.setHeader("Authorization", it)
//    }
    log.debug("[$corrId] GET $url")
    val response = httpService.executeRequest(request, 200)
    return parseFeed(response)
  }
}

fun JsonPoint.toPoint(): Point {
  return JtsUtil.createPoint(x, y)
}


fun LatLonPoint.toPoint(): Point {
  return JtsUtil.createPoint(latitude, longitude)
}
