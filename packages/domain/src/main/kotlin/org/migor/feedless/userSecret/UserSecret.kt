package org.migor.feedless.userSecret

import org.migor.feedless.user.UserId
import java.time.LocalDateTime

data class UserSecret(
    val id: UserSecretId = UserSecretId(),
    val value: String,
    val validUntil: LocalDateTime,
    val type: UserSecretType,
    val ownerId: UserId,
    val lastUsedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

