package org.migor.feedless.report

interface ReportRepository {
  suspend fun save(report: Report): Report
  suspend fun deleteById(reportId: ReportId)
  suspend fun findById(reportId: ReportId): Report?
}
