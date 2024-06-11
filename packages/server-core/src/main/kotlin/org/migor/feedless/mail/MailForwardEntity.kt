package org.migor.feedless.mail

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
import org.migor.feedless.repository.RepositoryEntity
import java.util.*

@Entity
@Table(name = "t_mail_forward")
open class MailForwardEntity : EntityWithUUID() {

  @Column(nullable = false)
  open lateinit var email: String

  @Column(nullable = false)
  open var authorized: Boolean = false

  @Temporal(TemporalType.TIMESTAMP)
  @Column
  open var authorizedAt: Date? = null

  @Column(name = StandardJpaFields.repositoryId, nullable = false)
  open lateinit var repositoryId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.repositoryId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_mail__to__repository")
  )
  open var repository: RepositoryEntity? = null
}
