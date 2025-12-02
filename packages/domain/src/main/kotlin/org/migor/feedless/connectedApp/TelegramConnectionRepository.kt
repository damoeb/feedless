package org.migor.feedless.connectedApp

import org.migor.feedless.user.UserId

interface TelegramConnectionRepository {
  fun findByChatId(chatId: Long): TelegramConnection?
  fun findByUserIdAndAuthorized(ownerId: UserId): TelegramConnection?
  fun findAllAuthorized(): List<TelegramConnection>
  fun save(connection: TelegramConnection): TelegramConnection
}
