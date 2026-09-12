package org.migor.feedless.message

import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import org.migor.feedless.transport.TelegramBotService
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.util.*

class NotificationsAdapterTest {

  @Test
  fun `without a telegram bot every call is a no-op`() {
    val notifications = NotificationsAdapter(Optional.empty())

    assertThatCode {
      notifications.showOptionsForKnownUser(1234)
      notifications.sendMessage(1234, "Disconnected")
    }.doesNotThrowAnyException()
  }

  @Test
  fun `with a telegram bot every call is delegated`() {
    val telegramBotService = mock(TelegramBotService::class.java)
    val notifications = NotificationsAdapter(Optional.of(telegramBotService))

    notifications.showOptionsForKnownUser(1234)
    notifications.sendMessage(5678, "Disconnected")

    verify(telegramBotService).showOptionsForKnownUser(1234)
    verify(telegramBotService).sendMessage(5678, "Disconnected")
  }
}
