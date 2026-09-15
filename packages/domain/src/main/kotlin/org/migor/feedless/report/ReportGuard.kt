package org.migor.feedless.report

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.NotFoundException
import org.migor.feedless.guard.ResourceGuard
import org.migor.feedless.user.userIdMaybe
import org.springframework.context.annotation.Profile
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component

/**
 * Guards the authenticated access path to a report, i.e. the GraphQL path.
 *
 * Links in report mails deliberately bypass this: their recipients are
 * usually not logged in, and possession of the signed token is the proof
 * there. See ReportController.
 */
@Component
@Profile("${AppProfiles.report} & ${AppLayer.service}")
class ReportGuard(private val reportRepository: ReportRepository) : ResourceGuard<ReportId, Report> {

  override suspend fun requireRead(id: ReportId): Report = requireOwnership(id)

  override suspend fun requireWrite(id: ReportId): Report = requireOwnership(id)

  override suspend fun requireExecute(id: ReportId): Report = requireOwnership(id)

  private suspend fun requireOwnership(id: ReportId): Report = withContext(Dispatchers.IO) {
    val report = reportRepository.findById(id) ?: throw NotFoundException("Report $id not found")
    val userId = coroutineContext.userIdMaybe()
      ?: throw AccessDeniedException("Report $id belongs to someone, you are not logged in")

    // An anonymously created report has no owner. It's reachable via the
    // link in its mail, not via a logged-in session - otherwise any logged-in
    // user could unsubscribe someone else's reports.
    val ownerId = report.userId
      ?: throw AccessDeniedException("Report $id has no owner, use the link from its mail")

    if (ownerId != userId) {
      throw AccessDeniedException("Report $id belongs to someone else")
    }
    report
  }
}
