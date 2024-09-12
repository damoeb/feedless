package org.migor.feedless.user

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "t_connected_app")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
  name = "app",
  discriminatorType = DiscriminatorType.STRING
)
open class ConnectedAppEntity : EntityWithUUID() {
  @Column(name = "is_authorized", nullable = false)
  open var authorized: Boolean = false

  @Column(name = "authorized_at")
  open var authorizedAt: LocalDateTime? = null

  @Column(name = StandardJpaFields.userId)
  open var userId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.userId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_connected_app__to__user")
  )
  open var user: UserEntity? = null
}
