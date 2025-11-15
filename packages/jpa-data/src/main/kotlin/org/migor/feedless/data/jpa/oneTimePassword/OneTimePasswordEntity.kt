package org.migor.feedless.data.jpa.oneTimePassword

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.user.UserEntity
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "t_otp")
open class OneTimePasswordEntity : EntityWithUUID() {

  @Column(nullable = false, name = "password")
  open lateinit var password: String

  @Column(nullable = false, name = "valid_until")
  open lateinit var validUntil: LocalDateTime

  @OneToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.userId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_otp__to__user")
  )
  open var user: UserEntity? = null

  @Column(name = StandardJpaFields.userId, nullable = false)
  open lateinit var userId: UUID

  @Column(name = "attempts_left", nullable = false)
  open var attemptsLeft: Int = 3

}

