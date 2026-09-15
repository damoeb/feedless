package org.migor.feedless.data.jpa.report

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.report.ReportRecipient
import org.migor.feedless.report.ReportRecipientId
import org.migor.feedless.report.ReportRecipientRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrNull

@Component
@Profile("${AppProfiles.report} & ${AppLayer.repository}")
class ReportRecipientJpaRepository(private val reportRecipientDAO: ReportRecipientDAO) : ReportRecipientRepository {
  override fun findById(id: ReportRecipientId): ReportRecipient? =
    reportRecipientDAO.findById(id.uuid).getOrNull()?.toDomain()

  override fun findByEmail(email: String): ReportRecipient? =
    reportRecipientDAO.findByEmail(email)?.toDomain()

  override fun save(recipient: ReportRecipient): ReportRecipient =
    reportRecipientDAO.save(recipient.toEntity()).toDomain()
}
