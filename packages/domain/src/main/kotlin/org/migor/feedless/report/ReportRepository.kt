package org.migor.feedless.report

interface ReportRepository {
  fun save(report: Report): Report
  fun deleteById(reportId: ReportId)
  fun findById(reportId: ReportId): Report?
}
