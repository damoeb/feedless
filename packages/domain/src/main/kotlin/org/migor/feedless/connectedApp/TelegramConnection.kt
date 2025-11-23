package org.migor.feedless.connectedApp

import org.migor.feedless.user.UserId
import java.time.LocalDateTime

data class TelegramConnection(
  override val id: ConnectedAppId,
  override val authorized: Boolean,
  override val authorizedAt: LocalDateTime?,
  override val userId: UserId?,
  val chatId: String?,
  override val createdAt: LocalDateTime
) : ConnectedApp(
  id = id,
  authorized = authorized,
  authorizedAt = authorizedAt,
  userId = userId,
  createdAt = createdAt
)
