package org.migor.feedless.data.jpa.report

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.report.Report
import org.migor.feedless.report.ReportId
import org.migor.feedless.report.ReportRepository
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@Component
@Profile("${AppProfiles.report} & ${AppLayer.repository}")
class ReportJpaRepository(private val reportDAO: ReportDAO) : ReportRepository {
  override fun save(report: Report): Report {
    return reportDAO.save(report.toEntity()).toDomain()
  }

  override fun deleteById(reportId: ReportId) {
    reportDAO.deleteById(reportId.uuid)
  }

  override fun findById(reportId: ReportId): Report? {
    return reportDAO.findById(reportId.uuid).getOrNull()?.toDomain()
  }

  override fun findAllPendingBatched(now: LocalDateTime): List<Report> {
    val pageable = PageRequest.of(0, 100)
    return reportDAO.findAllEnabledPendingBatched(now, pageable).map { it.toDomain() }
  }
}
