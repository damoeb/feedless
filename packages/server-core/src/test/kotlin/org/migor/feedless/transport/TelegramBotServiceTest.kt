package org.migor.feedless.transport

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.config.SystemSettingsDAO
import org.migor.feedless.config.SystemSettingsEntity
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.message.MessageService
import org.migor.feedless.repository.InboxService
import org.migor.feedless.repository.any
import org.migor.feedless.repository.argThat
import org.migor.feedless.repository.eq
import org.migor.feedless.user.TelegramConnectionDAO
import org.migor.feedless.user.TelegramConnectionEntity
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.core.env.Environment
import org.springframework.web.client.RestTemplate
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import reactor.test.scheduler.VirtualTimeScheduler
import java.net.URI
import java.time.Duration
import java.time.LocalDateTime
import java.util.*


@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TelegramBotServiceTest {

  lateinit var telegramLinkDAO: TelegramConnectionDAO
  lateinit var restTemplate: RestTemplate
  lateinit var messageService: MessageService
  lateinit var systemSettingsDAO: SystemSettingsDAO
  lateinit var telegramBotService: TelegramBotService
  lateinit var inboxService: InboxService
  val botToken = "MY_SECRET_TOKEN"
  val appHost = ""

  @BeforeEach
  fun setUp() {
    telegramLinkDAO = mock(TelegramConnectionDAO::class.java)
    restTemplate = mock(RestTemplate::class.java)
    messageService = mock(MessageService::class.java)
    systemSettingsDAO = mock(SystemSettingsDAO::class.java)
    inboxService = mock(InboxService::class.java)
    telegramBotService = TelegramBotService(
      botToken,
      appHost,
      telegramLinkDAO,
      mock(Environment::class.java),
      messageService,
      systemSettingsDAO,
      restTemplate,
      inboxService
    )


    `when`(systemSettingsDAO.save(any(SystemSettingsEntity::class.java))).thenAnswer { it.arguments[0] as SystemSettingsEntity }
    `when`(systemSettingsDAO.findByName(any(String::class.java))).thenReturn(null)
  }

  @Test
  fun `by default lastUpdateId is Int MAX_VALUE`() {
    assertThat(telegramBotService.lastUpdateId).isEqualTo(Int.MAX_VALUE)
  }

  @Test
  fun `given lastUpdateId is not stored, when initialized it gets set and saved`() {
    `when`(systemSettingsDAO.findByName(any(String::class.java))).thenReturn(null)

    telegramBotService.onInit()

    assertThat(telegramBotService.lastUpdateId).isEqualTo(Int.MAX_VALUE)
    verify(systemSettingsDAO, times(1)).save(any(SystemSettingsEntity::class.java))
  }

  @Test
  fun `given lastUpdateId is stored, when initialized it gets used`() {
    val mockSetting = SystemSettingsEntity()
    mockSetting.valueInt = 15243
    mockSetting.name = "tg-last-updated-id"
    `when`(systemSettingsDAO.findByName(any(String::class.java))).thenReturn(mockSetting)

    telegramBotService.onInit()

    assertThat(telegramBotService.lastUpdateId).isEqualTo(15243)
    verify(systemSettingsDAO, times(0)).save(any(SystemSettingsEntity::class.java))
  }

  @Test
  fun `pollUpdates invokes telegram api using a token`() {
    telegramBotService.pollUpdates()
    verify(restTemplate).getForObject(
      argThat<URI> {
        it.toURL().toString() == "https://api.telegram.org/botMY_SECRET_TOKEN/getUpdates"
      },
      eq(TelegramUpdatesResponse::class.java)
    )
  }

  @Test
  fun `pollUpdates will update lastUpdateId using the last message`() {
    val message = mock(Update::class.java)
    `when`(message.updateId).thenReturn(874112)
    val response = TelegramUpdatesResponse(
      ok = true,
      result = listOf(
        message
      )
    )
    `when`(
      restTemplate.getForObject(
        any(URI::class.java),
        eq(TelegramUpdatesResponse::class.java)
      )
    )
      .thenReturn(response)
    telegramBotService.onInit()
    reset(systemSettingsDAO)

    telegramBotService.pollUpdates()

    assertThat(telegramBotService.lastUpdateId).isEqualTo(874112)
    verify(systemSettingsDAO).save(argThat { it.valueInt == 874112 })
  }

  @Test
  fun `onInit will subscribe to all authorized chatIds`() {
    `when`(telegramLinkDAO.findAllByAuthorizedIsTrue())
      .thenReturn(
        listOf(
          newTelegramConnection(123),
          newTelegramConnection(567)
        )
      )

    `when`(messageService.subscribe(any(String::class.java))).thenReturn(Flux.empty())

    telegramBotService.onInit()

    verify(messageService).subscribe(argThat { it == TelegramBotService.toTopic(567) })
    verify(messageService).subscribe(argThat { it == TelegramBotService.toTopic(123) })
    verifyNoMoreInteractions(messageService)
  }

  @Test
  fun `pre chat messages are throttled to max 20 per minute`() {
    VirtualTimeScheduler.getOrSet()

    val messages = Flux.range(1, 100)
      .map { mock(JsonItem::class.java) }
      .delayElements(Duration.ofMillis(833))

    `when`(messageService.subscribe(eq(TelegramBotService.toTopic(496)))).thenReturn(messages)

    val throttledMessagedPerChat = telegramBotService.subscribeToChats(listOf(newTelegramConnection(496)))

    StepVerifier.withVirtualTime { throttledMessagedPerChat }
      .thenAwait(Duration.ofMinutes(1))
      .expectNextCount(20) // Expect exactly 20 elements in 1 minute
      .thenAwait(Duration.ofMinutes(1)) // Fast-forward 1 minute
      .expectNextCount(20)
      .thenCancel() // Cancel the subscription
      .verify()
  }

  @Test
  fun `messages to telegram are throttled to 30 per second`() {
    VirtualTimeScheduler.getOrSet()
    val messages = Flux.range(1, 100)
      .map { Pair<Long, JsonItem>(it.toLong(), mock(JsonItem::class.java)) }
      .delayElements(Duration.ofMillis(25))

    val telegramBotServiceSpy = spy(telegramBotService)
    val throttledMessaged = telegramBotServiceSpy.createTelegramPublisher(messages)

    StepVerifier.withVirtualTime { throttledMessaged }
      .thenAwait(Duration.ofSeconds(1))
      .expectNextCount(30)
      .thenAwait(Duration.ofSeconds(1))
      .expectNextCount(30)
      .thenCancel()
      .verify()
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "true, 'lorem ipsum\n\nvia foo-repository https://feedless.org/article/id'",
      "false, 'lorem ipsum\n\nvia foo-repository https://some-url'",
    ],
    quoteCharacter = '\''
  )
  fun `converts JsonItem to message`(isSaas: Boolean, exptected: String) {
    val jsonItem = JsonItem()
    jsonItem.id = "id"
    jsonItem.url = "https://some-url"
    jsonItem.publishedAt = LocalDateTime.now()
    jsonItem.text = "lorem ipsum"
    jsonItem.repositoryId = UUID.randomUUID()
    jsonItem.repositoryName = "foo-repository"

    assertThat(jsonItem.toTelegramMessage(isSaas)).isEqualTo(exptected)
  }

  private fun newTelegramConnection(chatId: Long, authorized: Boolean = true, userId: UUID? = null): TelegramConnectionEntity {
    val connection = TelegramConnectionEntity()
    connection.chatId = chatId
    connection.authorized = authorized
    connection.userId = userId
    return connection
  }

  @Test
  fun `valid updates from telegram will be appended to the inbox`() = runTest {
    val chatId: Long = 8273
    val message = mock(Message::class.java)
    `when`(message.chatId).thenReturn(chatId)
    `when`(message.isCommand).thenReturn(false)

    val update = mock(Update::class.java)
    `when`(update.updateId).thenReturn(1)
    `when`(update.message).thenReturn(message)
    `when`(update.hasMessage()).thenReturn(true)
    val response = TelegramUpdatesResponse(
      ok = true,
      result = listOf(
        update
      )
    )
    `when`(
      restTemplate.getForObject(
        any(URI::class.java),
        eq(TelegramUpdatesResponse::class.java)
      )
    )
      .thenReturn(response)

    `when`(telegramLinkDAO.findByChatId(eq(chatId)))
      .thenReturn(
          newTelegramConnection(chatId, true, UUID.randomUUID()),
      )
    telegramBotService.onInit()
    telegramBotService.pollUpdates()
    `when`(update.updateId).thenReturn(2)

    // when
    telegramBotService.pollUpdates()

    // then
    verify(inboxService).appendMessage(any(String::class.java), any(UUID::class.java), any(DocumentEntity::class.java))
  }

}
