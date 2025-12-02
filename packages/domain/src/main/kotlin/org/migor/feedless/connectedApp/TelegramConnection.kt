package org.migor.feedless.connectedApp

import org.migor.feedless.user.UserId
import java.time.LocalDateTime

data class TelegramConnection(
  override val id: ConnectedAppId = ConnectedAppId(),
  override val authorized: Boolean = false,
  override val authorizedAt: LocalDateTime? = null,
  override val userId: UserId?,
  val chatId: Long?,
  override val createdAt: LocalDateTime = LocalDateTime.now(),
) : ConnectedApp(
  id = id,
  authorized = authorized,
  authorizedAt = authorizedAt,
  userId = userId,
  createdAt = createdAt
)
