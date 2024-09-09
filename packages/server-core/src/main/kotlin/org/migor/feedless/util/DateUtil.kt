package org.migor.feedless.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

//fun toDate(dt: TemporalAccessor): Date {
//  return Date(Instant.from(dt).toEpochMilli())
//}

fun toDate(dt: LocalDateTime): Date {
  return Date.from(dt.atZone(ZoneId.systemDefault()).toInstant())
}

fun LocalDateTime.toLegacyDate(): Date {
  return Date.from(this.atZone(ZoneId.systemDefault()).toInstant())
}

fun Date.toLocalDateTime(): LocalDateTime {
  return this.toInstant()
    .atZone(ZoneId.systemDefault())
    .toLocalDateTime()
}

fun LocalDateTime.toMillis(): Long {
  return this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun Long.toLocalDateTime(): LocalDateTime {
  return LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
}
