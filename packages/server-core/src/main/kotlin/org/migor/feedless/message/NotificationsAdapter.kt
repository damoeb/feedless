package org.migor.feedless.message

import org.migor.feedless.AppLayer
import org.migor.feedless.transport.TelegramBotService
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*
import kotlin.jvm.optionals.getOrNull

// Unconditional, unlike @ConditionalOnBean TelegramBotService: without a bot every call is a no-op.
@Service
@Profile(AppLayer.service)
class NotificationsAdapter(
  @Lazy
  private val telegramBotServiceMaybe: Optional<TelegramBotService>,
) : Notifications {

  override fun showOptionsForKnownUser(chatId: Long) {
    telegramBotServiceMaybe.getOrNull()?.showOptionsForKnownUser(chatId)
  }

  override fun sendMessage(chatId: Long, message: String) {
    telegramBotServiceMaybe.getOrNull()?.sendMessage(chatId, message)
  }
}
