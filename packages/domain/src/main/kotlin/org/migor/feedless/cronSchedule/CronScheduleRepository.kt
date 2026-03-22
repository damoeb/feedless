package org.migor.feedless.cronSchedule

interface CronScheduleRepository {
  fun save(cronSchedule: CronSchedule)
}
