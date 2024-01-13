package org.migor.feedless.data.jpa.models

import jakarta.persistence.Basic
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
import java.util.*

@Entity
@Table(name = "t_agent")
open class AgentEntity : EntityWithUUID() {

  @Basic
  @Column(nullable = false)
  open lateinit var connectionId: String

  @Basic
  @Column(nullable = false)
  open lateinit var version: String

  @Basic
  @Column(nullable = false)
  open var openInstance: Boolean = false

  @Basic
  @Column(name = "secret_id", nullable = false)
  open var secretKeyId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "secret_id", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = ForeignKey(name = "fk_agent__user_secrets"))
  open var secretKey: UserSecretEntity? = null

  @Basic
  @Column(name = "owner_id", nullable = false)
  open var ownerId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "owner_id", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = ForeignKey(name = "fk_user_secrets__user"))
  open var owner: UserEntity? = null
}
