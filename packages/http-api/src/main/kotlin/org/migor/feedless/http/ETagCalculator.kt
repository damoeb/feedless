package org.migor.feedless.http

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.HexFormat

/**
 * The single place that turns any HTTP model object's representation into a strong ETag — a
 * quoted, lowercase-hex SHA-256 of the same JSON the endpoint serializes. `SourceHttpController`,
 * `RepositoryHttpController`, and `RecordHttpController` all call this from both their `get*`
 * (to set the response header) and `update*` (to compare against `If-Match`, and to set the new
 * header on success) methods, so a resource's GET and PATCH can never disagree about its ETag.
 */
@Component
class ETagCalculator {

  // jacksonObjectMapper() only registers the Kotlin module — without JavaTimeModule, a source
  // with a non-null lastRefreshedAt (an OffsetDateTime) throws InvalidDefinitionException instead
  // of hashing.
  private val json = jacksonObjectMapper()
    .registerModule(JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

  /** A strong ETag, e.g. `"3f2504e0…"` — quotes included, ready for the `ETag`/`If-Match` headers. */
  fun compute(value: Any): String {
    val bytes = json.writeValueAsBytes(value)
    val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
    return "\"${HexFormat.of().formatHex(digest)}\""
  }
}
