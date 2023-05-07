package org.migor.rich.rss.feed.exporter

import org.migor.rich.rss.api.dto.RichFeed
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Service
class FeedExporter {
  private val log = LoggerFactory.getLogger(FeedExporter::class.simpleName)

  @Autowired
  lateinit var jsonFeedExporter: JsonFeedExporter

  @Autowired
  lateinit var atomFeedExporter: SyndAtomFeedExporter

  fun resolveResponseType(
    corrId: String,
    responseType: String?
  ): Pair<String, (RichFeed, HttpStatus, Duration?) -> ResponseEntity<String>> {
    return when (responseType?.lowercase()) {
      "atom" -> "atom" to { feed, status, maxAge ->
        ok(
          status,
          "application/atom+xml; charset=utf-8",
          maxAge,
          atomFeedExporter.toAtom(corrId, feed)
        )
      }

      else -> "json" to { feed, status, maxAge ->
        ok(
          status,
          "application/json; charset=utf-8",
          maxAge,
          jsonFeedExporter.toJson(corrId, feed)
        )
      }
    }
  }

  fun to(
    corrId: String,
    status: HttpStatus,
    responseType: String?,
    feed: RichFeed,
    maxAge: Duration? = null
  ): ResponseEntity<String> {
    return resolveResponseType(corrId, responseType).second(feed, status, maxAge)
  }

  private fun fallbackCacheControl(retryAfter: Duration?): String =
    (retryAfter ?: 5.toLong().toDuration(DurationUnit.MINUTES)).inWholeSeconds.toString()

  private fun fallbackRetryAfter(retryAfter: Duration?) =
    (retryAfter ?: 5.toLong().toDuration(DurationUnit.MINUTES)).inWholeSeconds.toString()

  private fun ok(status: HttpStatus, mime: String, maxAge: Duration?, body: String?): ResponseEntity<String> {
    return ResponseEntity.status(status)
      .header(HttpHeaders.CONTENT_TYPE, mime)
      .header(HttpHeaders.RETRY_AFTER, fallbackRetryAfter(maxAge))
      .header(HttpHeaders.CACHE_CONTROL, fallbackCacheControl(maxAge))
      .body(body)
  }
}
