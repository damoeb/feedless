package org.migor.feedless.data.jpa.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.user.UserEntity
import java.util.*

@Entity
@Table(name = "t_notification")
open class NotificationEntity : EntityWithUUID() {

  @Column(nullable = false, name = "message")
  open lateinit var message: String

  @Column(name = StandardJpaFields.ownerId, nullable = false)
  open var ownerId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.ownerId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
  )
  open var owner: UserEntity? = null
}
