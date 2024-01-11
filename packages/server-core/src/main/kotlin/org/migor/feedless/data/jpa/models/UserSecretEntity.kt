package org.migor.feedless.data.jpa.models

import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.generated.types.UserSecret
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

  @ManyToOne(fetch = FetchType.LAZY, cascade = [])
  @JoinColumn(name = "owner_id", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = ForeignKey(name = "fk_user_secrets__user"))
  open var owner: UserEntity? = null
}

fun UserSecretEntity.toDto(mask: Boolean = true): UserSecret {
  return UserSecret.newBuilder()
    .id(this.id.toString())
    .type(this.type.toDto())
    .value(if (mask) this.value.substring(0..4) + "****" else this.value)
    .valueMasked(mask)
    .validUntil(this.validUntil.time)
    .lastUsed(this.lastUsedAt?.time)
    .build()
}

private fun UserSecretType.toDto(): org.migor.feedless.generated.types.UserSecretType = when(this) {
  UserSecretType.JWT -> org.migor.feedless.generated.types.UserSecretType.Jwt
  UserSecretType.SecretKey -> org.migor.feedless.generated.types.UserSecretType.SecretKey
}
