package org.migor.feedless.feed.exporter

import org.migor.feedless.feed.parser.json.JsonFeed
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Service
@Transactional(propagation = Propagation.NEVER)
class FeedExporter(
  private val jsonFeedExporter: JsonFeedExporter,
  private val atomFeedExporter: SyndAtomFeedExporter
) {
  private val log = LoggerFactory.getLogger(FeedExporter::class.simpleName)

  fun resolveResponseType(
    responseType: String?
  ): Pair<String, (JsonFeed, HttpStatus, Duration?) -> ResponseEntity<String>> {
    return when (responseType?.lowercase()) {
      "atom" -> "atom" to { feed, status, maxAge ->
        ok(
          status,
          "application/xml; charset=utf-8",
          maxAge,
          atomFeedExporter.toAtom(feed)
        )
      }

      else -> "json" to { feed, status, maxAge ->
        ok(
          status,
          "application/json; charset=utf-8",
          maxAge,
          jsonFeedExporter.toJson(feed)
        )
      }
    }
  }

  fun to(
    status: HttpStatus,
    responseType: String?,
    feed: JsonFeed,
    maxAge: Duration? = null
  ): ResponseEntity<String> {
    return resolveResponseType(responseType).second(feed, status, maxAge)
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
