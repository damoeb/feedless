package org.migor.rich.rss.exporter

import org.migor.rich.rss.api.dto.RichtFeed
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.util.*
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Service
class FeedExporter {
  private val log = LoggerFactory.getLogger(FeedExporter::class.simpleName)

  @Autowired
  lateinit var jsonFeedExporter: JsonFeedExporter

  @Autowired
  lateinit var atomFeedExporter: AtomFeedExporter

  fun resolveResponseType(
    corrId: String,
    responseType: String?
  ): Pair<String, (RichtFeed, Duration?) -> ResponseEntity<String>> {
    return when (responseType?.lowercase()) {
      "atom" -> "atom" to { feed, maxAge -> ok("application/atom+xml; charset=utf-8", maxAge, atomFeedExporter.toAtom(corrId, feed)) }
      else -> "json" to { feed, maxAge -> ok("application/json; charset=utf-8", maxAge, jsonFeedExporter.toJson(corrId, feed)) }
    }
  }

  fun to(corrId: String, responseType: String?, feed: RichtFeed, maxAge: Duration? = null): ResponseEntity<String> {
    return resolveResponseType(corrId, responseType).second(feed, maxAge)
  }

  private fun fallbackCacheControl(retryAfter: Duration?): String =
    Optional.ofNullable(retryAfter).orElse(5.toLong().toDuration(DurationUnit.MINUTES)).inWholeSeconds.toString()

  private fun fallbackRetryAfter(retryAfter: Duration?) =
    Optional.ofNullable(retryAfter).orElse(5.toLong().toDuration(DurationUnit.MINUTES)).inWholeSeconds.toString()

  private fun ok(mime: String, maxAge: Duration?, body: String?): ResponseEntity<String> {
    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_TYPE, mime)
      .header(HttpHeaders.RETRY_AFTER, fallbackRetryAfter(maxAge))
      .header(HttpHeaders.CACHE_CONTROL, fallbackCacheControl(maxAge))
      .body(body)
  }
}
