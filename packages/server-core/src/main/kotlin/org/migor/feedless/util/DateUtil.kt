package org.migor.feedless.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAccessor
import java.util.*

fun toDate(dt: TemporalAccessor): Date {
  return Date(Instant.from(dt).toEpochMilli())
}

fun toDate(dt: LocalDateTime): Date {
  return Date.from(dt.atZone(ZoneId.systemDefault()).toInstant())
}
