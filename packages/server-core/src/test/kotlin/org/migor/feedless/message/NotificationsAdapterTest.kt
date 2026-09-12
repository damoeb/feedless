package org.migor.feedless.message

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import org.migor.feedless.connectedApp.TelegramConnection
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.transport.TelegramBotService
import org.migor.feedless.user.UserId
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import java.util.*

class NotificationsAdapterTest {

  private val messageService = mock(MessageService::class.java)

  @Test
  fun `without a telegram bot every call is a no-op`() = runTest {
    val notifications = NotificationsAdapter(Optional.empty(), messageService)

    assertThatCode {
      notifications.showOptionsForKnownUser(1234)
      notifications.sendMessage(1234, "Disconnected")
    }.doesNotThrowAnyException()
    notifications.pushToOwner(UserId(), listOf(JsonItem()))
    verifyNoInteractions(messageService)
  }

  @Test
  fun `with a telegram bot every call is delegated`() {
    val telegramBotService = mock(TelegramBotService::class.java)
    val notifications = NotificationsAdapter(Optional.of(telegramBotService), messageService)

    notifications.showOptionsForKnownUser(1234)
    notifications.sendMessage(5678, "Disconnected")

    verify(telegramBotService).showOptionsForKnownUser(1234)
    verify(telegramBotService).sendMessage(5678, "Disconnected")
  }

  @Test
  fun `pushes every item to the owner's authorized telegram chat`() = runTest {
    val owner = UserId()
    val telegramBotService = mock(TelegramBotService::class.java)
    val telegramLink = mock(TelegramConnection::class.java)
    `when`(telegramLink.chatId).thenReturn(12345)
    `when`(telegramBotService.findByUserIdAndAuthorizedIsTrue(owner)).thenReturn(telegramLink)
    val first = JsonItem()
    val second = JsonItem()

    NotificationsAdapter(Optional.of(telegramBotService), messageService).pushToOwner(owner, listOf(first, second))

    verify(messageService).publishMessage(TelegramBotService.toTopic(12345), first)
    verify(messageService).publishMessage(TelegramBotService.toTopic(12345), second)
  }

  @Test
  fun `an owner without an authorized telegram chat gets no push`() = runTest {
    val telegramBotService = mock(TelegramBotService::class.java)

    NotificationsAdapter(Optional.of(telegramBotService), messageService).pushToOwner(UserId(), listOf(JsonItem()))

    verifyNoInteractions(messageService)
  }
}
