package org.migor.feedless.message

import org.migor.feedless.AppLayer
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.transport.TelegramBotService
import org.migor.feedless.user.UserId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*
import kotlin.jvm.optionals.getOrNull

// Unconditional, unlike @ConditionalOnBean TelegramBotService: without a bot every call is a no-op.
@Service
@Profile(AppLayer.service)
class NotificationsAdapter(
  private val telegramBotServiceMaybe: Optional<TelegramBotService>,
  private val messageService: MessageService,
) : Notifications {

  override fun showOptionsForKnownUser(chatId: Long) {
    telegramBotServiceMaybe.getOrNull()?.showOptionsForKnownUser(chatId)
  }

  override fun sendMessage(chatId: Long, message: String) {
    telegramBotServiceMaybe.getOrNull()?.sendMessage(chatId, message)
  }

  override suspend fun pushToOwner(ownerId: UserId, items: List<JsonItem>) {
    telegramBotServiceMaybe.getOrNull()?.let { telegramBot ->
      telegramBot.findByUserIdAndAuthorizedIsTrue(ownerId)?.let { telegramLink ->
        items.forEach {
          messageService.publishMessage(TelegramBotService.toTopic(telegramLink.chatId!!), it)
        }
      }
    }
  }
}
