package org.migor.rich.rss.data.jpa.models

import jakarta.persistence.Basic
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.migor.rich.rss.data.jpa.EntityWithUUID
import java.sql.Timestamp
import java.util.*

@Entity
@Table(name = "t_secret_key")
open class SecretKeyEntity : EntityWithUUID() {

  @Basic
  @Column(nullable = false)
  open lateinit var value: String

  @Basic
  @Column(nullable = false)
  open lateinit var validUntil: Date

  @Basic
  @Column
  open var lastUsedAt: Timestamp? = null

  @Basic
  @Column(name = "owner_id", insertable = false, updatable = false, nullable = false)
  open var ownerId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
  @JoinColumn(name = "owner_id", referencedColumnName = "id")
  open var owner: UserEntity? = null
}

