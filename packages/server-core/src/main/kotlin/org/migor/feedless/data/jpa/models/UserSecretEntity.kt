package org.migor.feedless.data.jpa.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.generated.types.UserSecret
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.sql.Timestamp
import java.util.*

@Entity
@Table(name = "t_user_secret")
open class UserSecretEntity : EntityWithUUID() {

  @Column(nullable = false, length = 400)
  open lateinit var value: String

  @Column(nullable = false)
  open lateinit var validUntil: Date

  @Column(nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  open lateinit var type: UserSecretType

  @Column
  open var lastUsedAt: Timestamp? = null

  @Column(name = "owner_id", nullable = false)
  open lateinit var ownerId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = "owner_id",
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_user_secrets__user")
  )
  open var owner: UserEntity? = null

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "id", orphanRemoval = true)
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  open var agents: MutableList<AgentEntity> = mutableListOf()

  @PrePersist
  fun prePersist() {
    val encoder = BCryptPasswordEncoder()
    val encoded = encoder.encode(value)
    println(encoded)
  }
}

fun UserSecretEntity.toDto(mask: Boolean = true): UserSecret {
  return UserSecret.newBuilder()
    .id(id.toString())
    .type(type.toDto())
    .value(if (mask) value.substring(0..4) + "****" else value)
    .valueMasked(mask)
    .validUntil(validUntil.time)
    .lastUsed(lastUsedAt?.time)
    .build()
}

private fun UserSecretType.toDto(): org.migor.feedless.generated.types.UserSecretType = when (this) {
  UserSecretType.JWT -> org.migor.feedless.generated.types.UserSecretType.Jwt
  UserSecretType.SecretKey -> org.migor.feedless.generated.types.UserSecretType.SecretKey
}
