package org.migor.feedless.report

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.guard.ResourceGuard
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("${AppProfiles.report} & ${AppLayer.service}")
class ReportGuard(private val reportRepository: ReportRepository) : ResourceGuard<ReportId, Report> {
  override suspend fun requireRead(id: ReportId): Report {
    TODO("Not yet implemented")
  }

  override suspend fun requireWrite(id: ReportId): Report {
    TODO("Not yet implemented")
  }

  override suspend fun requireExecute(id: ReportId): Report {
    TODO("Not yet implemented")
  }

}
