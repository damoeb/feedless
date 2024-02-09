package org.migor.feedless.data.jpa.models

import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import java.util.*

@Entity
@Table(name = "t_mail_forward")
open class MailForwardEntity : EntityWithUUID() {

  @Basic
  @Column(nullable = false)
  open lateinit var email: String

  @Basic
  @Column(nullable = false)
  open var authorized: Boolean = false

  @Temporal(TemporalType.TIMESTAMP)
  @Column
  open var authorizedAt: Date? = null

  @Basic
  @Column(name = StandardJpaFields.subscriptionId, nullable = false)
  open lateinit var subscriptionId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = StandardJpaFields.subscriptionId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false, foreignKey = ForeignKey(name = "fk_subscription__mail_forward"))
  open var subscription: SourceSubscriptionEntity? = null
}
