package org.migor.feedless.data.jpa.report

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.report.ReportRecipient

@Entity
@Table(name = "t_report_recipient")
open class ReportRecipientEntity : EntityWithUUID() {

  @Column(nullable = false, unique = true, name = "email")
  open lateinit var email: String

  @Column(nullable = false, name = "opt_in_required")
  open var optInRequired: Boolean = false
}

fun ReportRecipientEntity.toDomain(): ReportRecipient = ReportRecipientMapper.INSTANCE.toDomain(this)

fun ReportRecipient.toEntity(): ReportRecipientEntity = ReportRecipientMapper.INSTANCE.toEntity(this)
