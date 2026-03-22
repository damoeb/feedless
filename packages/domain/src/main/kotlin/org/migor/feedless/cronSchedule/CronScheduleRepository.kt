package org.migor.feedless.cronSchedule

import org.migor.feedless.report.ReportId

interface CronScheduleRepository {
  fun save(cronSchedule: CronSchedule)
  fun findByReportId(id: ReportId): CronSchedule
}
