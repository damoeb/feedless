package org.migor.rich.rss.data.jpa.models

import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import org.migor.rich.rss.data.jpa.EntityWithUUID
import java.sql.Timestamp
import java.util.*

@Entity
@Table(name = "t_otp")
open class OneTimePasswordEntity : EntityWithUUID() {

  @Basic
  @Column(nullable = false)
  open lateinit var password: String

//  @Basic
//  @Column(nullable = false)
//  open var attemptFailed: Boolean = false

  @Basic
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  open lateinit var validUntil: Timestamp

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "userId", referencedColumnName = "id", insertable = false, updatable = false)
  open var user: UserEntity? = null

  @Column(name = "userId", nullable = false)
  open var userId: UUID? = null

}

