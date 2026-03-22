package org.migor.feedless.report

import java.time.LocalDateTime

interface ReportRepository {
  fun save(report: Report): Report
  fun deleteById(reportId: ReportId)
  fun findById(reportId: ReportId): Report?
  fun findAllPendingBatched(now: LocalDateTime): List<Report>
}
