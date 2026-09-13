package org.migor.feedless.transport

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TelegramUpdatesResponseTest {

  @Test
  fun `parses a getUpdates payload into telegrambots updates`() {
    val payload = """
      {"ok":true,"result":[{"update_id":7,"message":{"message_id":1,"date":1700000000,
      "chat":{"id":42,"type":"private"},"text":"/start",
      "entities":[{"type":"bot_command","offset":0,"length":6}]}}]}
    """.trimIndent()

    val update = parseTelegramUpdates(com.fasterxml.jackson.databind.ObjectMapper(), payload).single()

    assertThat(update.updateId).isEqualTo(7)
    assertThat(update.message.chatId).isEqualTo(42L)
    assertThat(update.message.text).isEqualTo("/start")
    assertThat(update.message.isCommand).isTrue()
  }
}
