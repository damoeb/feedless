package org.migor.feedless.http

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.HexFormat

/** The one ETag source for GET and PATCH, so they never disagree: a quoted SHA-256 of the endpoint's JSON. */
@Component
class ETagCalculator {

  // Needs JavaTimeModule, or an OffsetDateTime field throws instead of hashing.
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
