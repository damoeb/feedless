package org.migor.feedless.transport

import org.migor.feedless.common.testPublicUrls
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.Mother.randomRepositoryId
import org.migor.feedless.any
import org.migor.feedless.any2
import org.migor.feedless.argThat
import org.migor.feedless.connectedApp.TelegramConnection
import org.migor.feedless.connectedApp.TelegramConnectionRepository
import org.migor.feedless.eq
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.message.MessageService
import org.migor.feedless.repository.InboxService
import org.migor.feedless.systemSettings.SystemSettings
import org.migor.feedless.systemSettings.SystemSettingsRepository
import org.migor.feedless.user.UserId
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
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import reactor.test.scheduler.VirtualTimeScheduler
import java.net.URI
import java.time.Duration
import java.time.LocalDateTime


@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TelegramBotServiceTest {

  lateinit var telegramConnectionRepository: TelegramConnectionRepository
  lateinit var restTemplate: RestTemplate
  lateinit var messageService: MessageService
  lateinit var systemSettingsRepository: SystemSettingsRepository
  lateinit var telegramBotService: TelegramBotService
  lateinit var inboxService: InboxService
  val botToken = "MY_SECRET_TOKEN"
  val appHost = ""

  @BeforeEach
  fun setUp() = runTest {
    telegramConnectionRepository = mock(TelegramConnectionRepository::class.java)
    restTemplate = mock(RestTemplate::class.java)
    messageService = mock(MessageService::class.java)
    systemSettingsRepository = mock(SystemSettingsRepository::class.java)
    inboxService = mock(InboxService::class.java)
    val properties = TelegramProperties(token = botToken)
    telegramBotService = TelegramBotService(
      properties,
      testPublicUrls(appHost = appHost),
      telegramConnectionRepository,
      mock(Environment::class.java),
      messageService,
      systemSettingsRepository,
      restTemplate,
      inboxService
    )

    `when`(telegramConnectionRepository.findAllAuthorized()).thenReturn(emptyList())
    `when`(systemSettingsRepository.save(any2())).thenAnswer { it.arguments[0] as SystemSettings }
    `when`(systemSettingsRepository.findByName(any2())).thenReturn(null)
  }

  @Test
  fun `by default lastUpdateId is Int MAX_VALUE`() {
    assertThat(telegramBotService.lastUpdateId).isEqualTo(Int.MAX_VALUE)
  }

  @Test
  fun `given lastUpdateId is not stored, when initialized it gets set and saved`() = runTest {
    `when`(systemSettingsRepository.findByName(any2())).thenReturn(null)

    telegramBotService.onInit()

    assertThat(telegramBotService.lastUpdateId).isEqualTo(Int.MAX_VALUE)
    verify(systemSettingsRepository, times(1)).save(any2())
  }

  @Test
  fun `given lastUpdateId is stored, when initialized it gets used`() = runTest {
    val storedSettings = SystemSettings(
      valueInt = 15243,
      name = "tg-last-updated-id"
    )
    `when`(systemSettingsRepository.findByName(any2())).thenReturn(storedSettings)

    telegramBotService.onInit()

    assertThat(telegramBotService.lastUpdateId).isEqualTo(15243)
    verify(systemSettingsRepository, times(0)).save(any2())
  }

  @Test
  fun `pollUpdates invokes telegram api using a token`() {
    telegramBotService.pollUpdates()
    verify(restTemplate).getForObject(
      argThat<URI> {
        it.toURL().toString() == "https://api.telegram.org/botMY_SECRET_TOKEN/getUpdates"
      },
      eq(String::class.java)
    )
  }

  @Test
  fun `pollUpdates will update lastUpdateId using the last message`() = runTest {
    `when`(
      restTemplate.getForObject(
        any(URI::class.java),
        eq(String::class.java)
      )
    )
      .thenReturn("""{"ok":true,"result":[{"update_id":874112}]}""")
    telegramBotService.onInit()
    reset(systemSettingsRepository)

    telegramBotService.pollUpdates()

    assertThat(telegramBotService.lastUpdateId).isEqualTo(874112)
    verify(systemSettingsRepository).save(argThat { it.valueInt == 874112 })
  }

  @Test
  fun `onInit will subscribe to all authorized chatIds`() = runTest {
    `when`(telegramConnectionRepository.findAllAuthorized())
      .thenReturn(
        listOf(
          newTelegramConnection(123),
          newTelegramConnection(567)
        )
      )

    `when`(messageService.subscribe(any2())).thenReturn(Flux.empty())

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
    jsonItem.repositoryId = randomRepositoryId()
    jsonItem.repositoryName = "foo-repository"

    assertThat(jsonItem.toTelegramMessage(isSaas)).isEqualTo(exptected)
  }

  private fun newTelegramConnection(
    chatId: Long,
    authorized: Boolean = true,
    userId: UserId? = null
  ): TelegramConnection {
    return TelegramConnection(
      chatId = chatId,
      authorized = authorized,
      userId = userId
    )
  }

  @Test
  fun `valid updates from telegram will be appended to the inbox`() = runTest {
    val chatId: Long = 8273
    // a plain text message (no command entity), first as update 1, then as update 2
    fun updates(updateId: Int) = """{"ok":true,"result":[{"update_id":$updateId,"message":{"message_id":1,
      "date":1700000000,"chat":{"id":$chatId,"type":"private"},"text":"hello"}}]}"""
    `when`(
      restTemplate.getForObject(
        any(URI::class.java),
        eq(String::class.java)
      )
    )
      .thenReturn(updates(1), updates(2))

    `when`(telegramConnectionRepository.findByChatId(eq(chatId)))
      .thenReturn(
        newTelegramConnection(chatId, true, UserId()),
      )
    telegramBotService.onInit()
    telegramBotService.pollUpdates()

    // when
    telegramBotService.pollUpdates()

    // then
    verify(inboxService).appendMessage(any2(), any2())
  }

}
