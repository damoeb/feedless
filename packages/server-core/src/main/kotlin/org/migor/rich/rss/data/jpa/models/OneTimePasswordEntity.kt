package org.migor.rich.rss.data.jpa.models

import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import org.migor.rich.rss.data.jpa.EntityWithUUID
import java.sql.Timestamp

@Entity
@Table(name = "t_otp")
open class OneTimePasswordEntity : EntityWithUUID() {

  @Basic
  @Column(nullable = false)
  open lateinit var password: String

  @Basic
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  open var validUntil: Timestamp? = null
}

