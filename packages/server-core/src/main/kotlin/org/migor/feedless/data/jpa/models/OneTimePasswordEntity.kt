package org.migor.feedless.data.jpa.models

import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
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
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "userId", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = ForeignKey(name = "fk_otp__user"))
  open var user: UserEntity? = null

  @Column(name = "userId", nullable = false)
  open var userId: UUID? = null

}

