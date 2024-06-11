package org.migor.feedless.agent

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.secrets.UserSecretEntity
import org.migor.feedless.user.UserEntity
import java.util.*

@Entity
@Table(name = "t_agent")
open class AgentEntity : EntityWithUUID() {

  @Column(nullable = false, name = "connection_id")
  open lateinit var connectionId: String

  @Column(nullable = false, name = "version")
  open lateinit var version: String

  @Column(nullable = false)
  open var openInstance: Boolean = false

  @Column(nullable = false)
  open lateinit var name: String

  @Column(nullable = false)
  open lateinit var lastSyncedAt: Date

  @Column(name = "secret_id", nullable = false)
  open var secretKeyId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = "secret_id",
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_agent__to__secret")
  )
  open var secretKey: UserSecretEntity? = null

  @Column(name = StandardJpaFields.ownerId, nullable = false)
  open var ownerId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.ownerId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_agent__to__user")
  )
  open var owner: UserEntity? = null
}
