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
 * Schützt den angemeldeten Zugriff auf einen Report, also den GraphQL-Pfad.
 *
 * Die Links in Report-Mails laufen bewusst nicht hier durch: ihre Empfänger
 * sind in aller Regel nicht angemeldet, und dort ist der Besitz des signierten
 * Tokens der Nachweis. Siehe ReportController.
 *
 * Vorher warfen alle drei Methoden NotImplementedError, wodurch schon das
 * Löschen eines Reports zur Laufzeit scheiterte.
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

    // Ein anonym angelegter Report hat keinen Eigentümer. Er ist über den Link
    // in seiner Mail erreichbar, nicht über eine angemeldete Sitzung - sonst
    // könnte jeder Angemeldete fremde Reports abbestellen.
    val ownerId = report.userId
      ?: throw AccessDeniedException("Report $id has no owner, use the link from its mail")

    if (ownerId != userId) {
      throw AccessDeniedException("Report $id belongs to someone else")
    }
    report
  }
}
