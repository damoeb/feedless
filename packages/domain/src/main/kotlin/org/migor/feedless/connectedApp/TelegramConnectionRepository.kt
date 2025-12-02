package org.migor.feedless.connectedApp

import org.migor.feedless.user.UserId

interface TelegramConnectionRepository {
  suspend fun findByChatId(chatId: Long): TelegramConnection?
  suspend fun findByUserIdAndAuthorized(ownerId: UserId): TelegramConnection?
  suspend fun findAllAuthorized(): List<TelegramConnection>
  suspend fun save(connection: TelegramConnection): TelegramConnection
}
