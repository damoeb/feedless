package org.migor.feedless.data.jpa.report

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.user.UserEntity
import org.migor.feedless.report.Report
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "t_report")
open class ReportEntity : EntityWithUUID() {

  @Email
  @Column(nullable = false, name = "recipient_email")
  open lateinit var recipientEmail: String

  @Size(min = 3, max = 50)
  @Column(nullable = false, name = "recipient_name")
//  @Convert(converter = EncryptionConverter::class)
  open lateinit var recipientName: String

  @Column(nullable = false, name = "authorized")
  open var authorized: Boolean = false

  @Column(name = "authorization_attempt", nullable = false)
  open var authorizationAttempt: Int = 0

  @Column(name = "last_requested_authorization")
  open var lastRequestedAuthorization: LocalDateTime? = null

  @Column(name = "authorized_at")
  open var authorizedAt: LocalDateTime? = null

  @Column(name = "last_reported_at")
  open var lastReportedAt: LocalDateTime? = null

  @Column(name = "is_disabled")
  open var disabled: Boolean = false

  @Column(name = "disabled_at")
  open var disabledAt: LocalDateTime? = null

  @Column(name = "next_reported_at", nullable = false)
  open lateinit var nextReportedAt: LocalDateTime

  @Column(name = StandardJpaFields.segmentationId, nullable = false)
  open lateinit var segmentId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.segmentationId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_report__to__segment")
  )
  open var segment: SegmentationEntity? = null

  @Column(name = StandardJpaFields.userId)
  open var userId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.userId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_report__to__user")
  )
  open var user: UserEntity? = null
}

fun ReportEntity.toDomain(): Report {
  return ReportMapper.INSTANCE.toDomain(this)
}

fun Report.toEntity(): ReportEntity {
  return ReportMapper.INSTANCE.toEntity(this)
}
