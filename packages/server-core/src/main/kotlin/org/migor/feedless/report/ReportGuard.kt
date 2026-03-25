package org.migor.feedless.report

import kotlinx.coroutines.currentCoroutineContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.guard.ResourceGuard
import org.migor.feedless.user.userIdMaybe
import org.springframework.context.annotation.Profile
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component

@Component
@Profile("${AppProfiles.report} & ${AppLayer.service}")
class ReportGuard(private val reportRepository: ReportRepository) : ResourceGuard<ReportId, Report> {
  override suspend fun requireRead(id: ReportId): Report {
    TODO("Not yet implemented")
  }

  override suspend fun requireWrite(id: ReportId): Report {
    val report = reportRepository.findById(id) ?: throw AccessDeniedException("report not found")
    val ctxUserId = currentCoroutineContext().userIdMaybe()
      ?: throw AccessDeniedException("no user")
    if (report.userId != null && report.userId != ctxUserId) {
      throw AccessDeniedException("not report owner")
    }
    return report
  }

  override suspend fun requireExecute(id: ReportId): Report {
    TODO("Not yet implemented")
  }

}
