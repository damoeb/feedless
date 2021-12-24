package org.migor.rss.rich.util

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Period
import java.time.ZoneId
import java.util.*

object DateUtil {

  fun timeAgo(date: Date): String {
    val lastCreatedAt = date.toInstant()

    val period: Period = Period.between(LocalDate.now(), LocalDate.ofInstant(lastCreatedAt, ZoneId.systemDefault()))

    return if (period.years > 0) {
      "${period.years} y"
    } else if (period.months > 0) {
      "${period.months} m"
    } else if (period.days > 0) {
      "${period.days} d"
    } else {
      val duration =
        Duration.between(LocalTime.now(), LocalDateTime.ofInstant(lastCreatedAt, ZoneId.systemDefault()).toLocalTime())
      if (duration.toHours() > 0) {
        "${duration.toHours()} h"
      } else if (duration.toMinutes() > 0) {
        "${duration.toMinutes()} s"
      } else {
        "now"
      }
    }
  }
}
