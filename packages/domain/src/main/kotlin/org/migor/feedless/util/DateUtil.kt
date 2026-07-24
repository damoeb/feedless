package org.migor.feedless.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

fun LocalDateTime.toLegacyDate(): Date {
  return Date.from(this.atZone(ZoneOffset.UTC).toInstant())
}

fun Date.toLocalDateTime(): LocalDateTime {
  return this.toInstant()
    .atZone(ZoneOffset.UTC)
    .toLocalDateTime()
}

fun LocalDateTime.toMillis(): Long {
  return this.atZone(ZoneOffset.UTC).toInstant().toEpochMilli()
}

fun Long.toLocalDateTime(): LocalDateTime {
  return LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneOffset.UTC)
}

/**
 * Domain [LocalDateTime]s are UTC — see [toMillis], which has always interpreted them that way.
 * These two make that explicit on the wire so clients don't have to guess a zone.
 */
fun LocalDateTime.toOffsetDateTime(): OffsetDateTime {
  return this.atOffset(ZoneOffset.UTC)
}

fun OffsetDateTime.toLocalDateTimeUtc(): LocalDateTime {
  return this.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime()
}
