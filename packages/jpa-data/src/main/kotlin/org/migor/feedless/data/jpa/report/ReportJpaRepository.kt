package org.migor.feedless.data.jpa.report

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.report.Report
import org.migor.feedless.report.ReportId
import org.migor.feedless.report.ReportRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrNull

@Component
@Profile("${AppProfiles.report} & ${AppLayer.repository}")
class ReportJpaRepository(private val reportDAO: ReportDAO) : ReportRepository {
  override suspend fun save(report: Report): Report {
    return withContext(Dispatchers.IO) {
      reportDAO.save(report.toEntity()).toDomain()
    }
  }

  override suspend fun deleteById(reportId: ReportId) {
    withContext(Dispatchers.IO) {
      reportDAO.deleteById(reportId.uuid)
    }
  }

  override suspend fun findById(reportId: ReportId): Report? {
    return withContext(Dispatchers.IO) {
      reportDAO.findById(reportId.uuid).getOrNull()?.toDomain()
    }
  }
}
