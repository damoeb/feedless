package org.migor.feedless.data.jpa.connectedApp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.connectedApp.TelegramConnection
import org.migor.feedless.connectedApp.TelegramConnectionRepository
import org.migor.feedless.user.UserId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("${AppProfiles.user} & ${AppLayer.repository}")
class TelegramConnectionJpaRepository(private val telegramConnectionDAO: TelegramConnectionDAO) :
  TelegramConnectionRepository {
  override suspend fun findByChatId(chatId: Long): TelegramConnection? {
    return withContext(Dispatchers.IO) {
      telegramConnectionDAO.findByChatId(chatId)?.toDomain()
    }
  }

  override suspend fun findByUserIdAndAuthorized(ownerId: UserId): TelegramConnection? {
    return withContext(Dispatchers.IO) {
      telegramConnectionDAO.findByUserIdAndAuthorizedIsTrue(ownerId.uuid)?.toDomain()
    }
  }

  override suspend fun findAllAuthorized(): List<TelegramConnection> {
    return withContext(Dispatchers.IO) {
      telegramConnectionDAO.findAllByAuthorizedIsTrue().map { it.toDomain() }
    }
  }

  override suspend fun save(connection: TelegramConnection): TelegramConnection {
    return withContext(Dispatchers.IO) {
      val entity = connection.toEntity()
      if (entity is TelegramConnectionEntity) {
        telegramConnectionDAO.save(entity).toDomain()
      } else {
        connection
      }
    }
  }
}
