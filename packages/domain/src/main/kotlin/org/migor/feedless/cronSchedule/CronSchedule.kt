package org.migor.feedless.cronSchedule

import java.time.LocalDateTime

data class CronSchedule(
  val id: CronScheduleId = CronScheduleId(),
  val cronExpression: String,
  val scheduledNextAt: LocalDateTime? = null,
  val createdAt: LocalDateTime = LocalDateTime.now(),
)

