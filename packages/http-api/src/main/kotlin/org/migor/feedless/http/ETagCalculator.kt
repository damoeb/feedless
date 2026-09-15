package org.migor.feedless.http

import org.springframework.stereotype.Component
import tools.jackson.databind.MapperFeature
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.module.kotlin.jacksonMapperBuilder
import java.security.MessageDigest
import java.util.HexFormat

/** The one ETag source for GET and PATCH, so they never disagree: a quoted SHA-256 of the endpoint's JSON. */
@Component
class ETagCalculator {

  // Declaration order and ISO dates, as under Jackson 2, so existing ETags stay valid.
  private val json = jacksonMapperBuilder()
    .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
    .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
    .build()

  /** A strong ETag, e.g. `"3f2504e0…"` — quotes included, ready for the `ETag`/`If-Match` headers. */
  fun compute(value: Any): String {
    val bytes = json.writeValueAsBytes(value)
    val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
    return "\"${HexFormat.of().formatHex(digest)}\""
  }
}
