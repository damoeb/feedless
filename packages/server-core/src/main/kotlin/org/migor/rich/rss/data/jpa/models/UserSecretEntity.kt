package org.migor.rich.rss.data.jpa.models

import jakarta.persistence.Basic
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.migor.rich.rss.data.jpa.EntityWithUUID
import java.sql.Timestamp
import java.util.*

@Entity
@Table(name = "t_user_secret")
open class UserSecretEntity : EntityWithUUID() {

  @Basic
  @Column(nullable = false, length = 400)
  open lateinit var value: String

  @Basic
  @Column(nullable = false)
  open lateinit var validUntil: Date

  @Basic
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  open lateinit var type: UserSecretType

  @Basic
  @Column
  open var lastUsedAt: Timestamp? = null

  @Basic
  @Column(name = "owner_id", nullable = false)
  open var ownerId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
  @JoinColumn(name = "owner_id", referencedColumnName = "id", insertable = false, updatable = false)
  open var owner: UserEntity? = null
}

