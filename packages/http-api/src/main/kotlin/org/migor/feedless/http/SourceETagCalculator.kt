package org.migor.feedless.http

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.HexFormat
import org.migor.feedless.http.api.model.Source as HttpSource

/**
 * The single place that turns a source's HTTP representation into a strong ETag — a quoted,
 * lowercase-hex SHA-256 of the same JSON the `Source` endpoints serialize. `SourceHttpController`
 * calls this from both `getSource` (to set the response header) and `updateSource` (to compare
 * against `If-Match`, and to set the new header on success), so the two can never disagree about
 * what a source's ETag is.
 */
@Component
class SourceETagCalculator {

  // jacksonObjectMapper() only registers the Kotlin module — without JavaTimeModule, a source
  // with a non-null lastRefreshedAt (an OffsetDateTime) throws InvalidDefinitionException instead
  // of hashing.
  private val json = jacksonObjectMapper()
    .registerModule(JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

  /** A strong ETag, e.g. `"3f2504e0…"` — quotes included, ready for the `ETag`/`If-Match` headers. */
  fun compute(source: HttpSource): String {
    val bytes = json.writeValueAsBytes(source)
    val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
    return "\"${HexFormat.of().formatHex(digest)}\""
  }
}
