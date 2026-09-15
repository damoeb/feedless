package org.migor.feedless.data.jpa.report

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.report.Report
import org.migor.feedless.report.ReportId
import org.migor.feedless.report.ReportRepository
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
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

  /**
   * The mapping reads the lazily loaded segment relation and must therefore
   * run inside the transaction. Without it, confirming and unsubscribing via
   * the mail link failed: the suspend controllers switch threads via
   * withContext, and the session bound to the request thread is gone there.
   */
  @Transactional(readOnly = true)
  override fun findById(reportId: ReportId): Report? {
    return reportDAO.findById(reportId.uuid).getOrNull()?.toDomain()
  }

  override fun findAllPendingBatched(now: LocalDateTime): List<Report> {
    val pageable = PageRequest.of(0, 100)
    return reportDAO.findAllEnabledPendingBatched(now, pageable).map { it.toDomain() }
  }

  @Transactional
  override fun disableAllByRecipientEmail(email: String, now: LocalDateTime): Int =
    reportDAO.disableAllByRecipientEmail(email, now)
}
