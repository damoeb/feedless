package org.migor.feedless.data.jpa.connectedApp

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.connectedApp.TelegramConnection
import org.migor.feedless.connectedApp.TelegramConnectionRepository
import org.migor.feedless.user.UserId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(propagation = Propagation.MANDATORY)
@Profile("${AppProfiles.user} & ${AppLayer.repository}")
class TelegramConnectionJpaRepository(private val telegramConnectionDAO: TelegramConnectionDAO) :
  TelegramConnectionRepository {
  override fun findByChatId(chatId: Long): TelegramConnection? {
    return telegramConnectionDAO.findByChatId(chatId)?.toDomain()
  }

  override fun findByUserIdAndAuthorized(ownerId: UserId): TelegramConnection? {
    return telegramConnectionDAO.findByUserIdAndAuthorizedIsTrue(ownerId.uuid)?.toDomain()
  }

  override fun findAllAuthorized(): List<TelegramConnection> {
    return telegramConnectionDAO.findAllByAuthorizedIsTrue().map { it.toDomain() }
  }

  override fun save(connection: TelegramConnection): TelegramConnection {
    val entity = connection.toEntity()
    return if (entity is TelegramConnectionEntity) {
      telegramConnectionDAO.save(entity).toDomain()
    } else {
      connection
    }
  }
}
