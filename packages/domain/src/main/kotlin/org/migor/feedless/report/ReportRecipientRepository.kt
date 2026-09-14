package org.migor.feedless.report

interface ReportRecipientRepository {
  fun findById(id: ReportRecipientId): ReportRecipient?
  fun findByEmail(email: String): ReportRecipient?
  fun save(recipient: ReportRecipient): ReportRecipient
}
