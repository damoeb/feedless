package org.migor.feedless.source

import java.time.LocalDateTime

object HarvestTimeLimits {
  /** Plan minimum and host cooldown are hard floors; the cron is only a target, so the floor wins over the cap. */
  fun coerce(
    requested: LocalDateTime,
    now: LocalDateTime,
    planFloor: LocalDateTime?,
    hostBlockedUntil: LocalDateTime?,
    cronCap: LocalDateTime?,
  ): LocalDateTime {
    val floor = listOfNotNull(now, planFloor, hostBlockedUntil).max()
    val afterFloor = maxOf(requested, floor)
    val capped = cronCap?.let { minOf(afterFloor, it) } ?: afterFloor
    return maxOf(capped, floor)
  }
}
