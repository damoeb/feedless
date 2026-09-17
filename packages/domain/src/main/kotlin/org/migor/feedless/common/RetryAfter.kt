package org.migor.feedless.common

import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/** RFC 9110 §10.2.3: delta-seconds or an HTTP-date. */
object RetryAfter {
  val FALLBACK: Duration = Duration.ofMinutes(5)
  val CAP: Duration = Duration.ofHours(24)

  fun parse(header: String?, now: Instant = Instant.now()): Duration {
    val value = header?.trim().orEmpty()
    val parsed = value.toLongOrNull()?.let { Duration.ofSeconds(it) }
      ?: runCatching { Duration.between(now, ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant()) }.getOrNull()
    return when {
      parsed == null || parsed.isNegative || parsed.isZero -> FALLBACK
      parsed > CAP -> CAP
      else -> parsed
    }
  }
}
