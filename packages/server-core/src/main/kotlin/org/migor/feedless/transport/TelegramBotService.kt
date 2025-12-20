package org.migor.feedless.transport

import com.google.gson.Gson
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.attachment.Attachment
import org.migor.feedless.connectedApp.TelegramConnection
import org.migor.feedless.connectedApp.TelegramConnectionRepository
import org.migor.feedless.data.jpa.connectedApp.TelegramConnectionDAO
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentId
import org.migor.feedless.document.ReleaseStatus
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.message.MessageService
import org.migor.feedless.repository.InboxService
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.systemSettings.SystemSettings
import org.migor.feedless.systemSettings.SystemSettingsRepository
import org.migor.feedless.user.UserId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.telegram.telegrambots.meta.api.objects.Update
import reactor.core.publisher.Flux
import java.net.URI
import java.time.Duration

@Service
@Profile("${AppProfiles.telegram} & ${AppLayer.service}")
@ConditionalOnBean(TelegramConnectionDAO::class, TelegramProperties::class)
class TelegramBotService(
  private val telegramProperties: TelegramProperties,
  @Value("\${app.appHost}")
  private val appHost: String,
  private val telegramConnectionRepository: TelegramConnectionRepository,
  private val environment: Environment,
  private val messageService: MessageService,
  private val systemSettingsRepository: SystemSettingsRepository,
  private val restTemplate: RestTemplate,
  private val inboxService: InboxService,
) {

  private val log = LoggerFactory.getLogger(TelegramBotService::class.simpleName)

  private val settingName = "telegram_last_update_id"
  private lateinit var lastUpdateSettings: SystemSettings

  companion object {
    fun toTopic(chatId: Long): String {
      return "telegram:${chatId}"
    }
  }

  var lastUpdateId: Int = Int.MAX_VALUE

  @PostConstruct
  fun onInit() {
    runBlocking {
      lastUpdateSettings = systemSettingsRepository.findByName(settingName) ?: systemSettingsRepository.save(
        SystemSettings(
          name = settingName,
          valueInt = Int.MAX_VALUE
        )
      )
    }

    lastUpdateId = lastUpdateSettings.valueInt ?: Int.MAX_VALUE
    val isSaas = environment.acceptsProfiles(Profiles.of(AppProfiles.saas))

    runBlocking {
      val chats = telegramConnectionRepository.findAllAuthorized()
      log.info("Using bot token ${StringUtils.abbreviate(telegramProperties.token, "...", 4)}")
      log.info("Subscribing to ${chats.size} telegram chats")

      createTelegramPublisher(subscribeToChats(chats))
        .subscribe { (chatId, message) -> sendMessage(chatId, message.toTelegramMessage(isSaas)) }
    }
  }

  fun createTelegramPublisher(messages: Flux<Pair<Long, JsonItem>>): Flux<Pair<Long, JsonItem>> {
    return messages.onBackpressureBuffer(100)
      // telegram policy: to multiple users, the API will not allow more than 30 messages per second
      .window(Duration.ofSeconds(1))
      .flatMap { window -> window.take(30) }
  }

  fun subscribeToChats(chats: List<TelegramConnection>): Flux<Pair<Long, JsonItem>> {
    return Flux.create { sink ->
      chats.forEach {
        // seed https://core.telegram.org/bots/faq#my-bot-is-hitting-limits-how-do-i-avoid-this
        messageService.subscribe(toTopic(it.chatId!!))
          // telegram policy: max 20 messages per minute to the same group
          .window(Duration.ofMinutes(1)) // 1-minute windows
          .flatMap { window -> window.take(20) }
          .map { message -> Pair(it.chatId!!, message) }
          .subscribe(sink::next, sink::error, sink::complete)
      }
    }
  }

  @Scheduled(fixedDelay = 4000)
  fun pollUpdates() {
    try {
      val url = "https://api.telegram.org/bot${telegramProperties.token}/getUpdates"
      val uri = URI.create(url)

      val response = restTemplate.getForObject(uri, TelegramUpdatesResponse::class.java)

      response?.result?.let {
        runBlocking {
          coroutineScope {
            response.result.filter { it.updateId > lastUpdateId }
              .forEach { update ->
                handleUpdate(update)
              }
          }
        }

        response.result.lastOrNull()?.let {
          lastUpdateId = it.updateId
          runBlocking {
            lastUpdateSettings = systemSettingsRepository.save(lastUpdateSettings.copy(valueInt = it.updateId))
          }
        }
      }
    } catch (e: Exception) {
      log.warn("telegram ${e.message}")
    }
  }

  private suspend fun handleUpdate(update: Update) {
    val chatId = update.message?.chatId

    chatId?.let {
      try {
        val connection = telegramConnectionRepository.findByChatId(chatId)
        if (connection == null || !connection.authorized) {
          welcomeNewUser(chatId, connection)
        } else {
          if (update.hasMessage()) {
            if (update.message.isCommand) {
              handleCommand(connection, update, chatId)
            } else {
              storeMessage(connection, update)
            }
          }
        }
      } catch (e: Exception) {
        log.warn("Cannot handle telegram update: ${e.message}")
      }
    }

    val messageText = update.message?.text

    if (chatId != null || messageText != null) {
      log.warn("Received invalid message from chat $chatId: $messageText")
    }
  }

  private suspend fun storeMessage(connection: TelegramConnection, update: Update) {
    inboxService.appendMessage(connection.userId!!, toDocument(update))
  }

  private suspend fun handleCommand(connection: TelegramConnection, update: Update, chatId: Long) {
    val command = update.message.text.lowercase().trim()
    when (command) {
      "/start" -> welcomeNewUser(chatId, connection)
      else -> log.warn("Cannot handle command $command")
    }
  }

  private suspend fun welcomeNewUser(chatId: Long, connectionOptional: TelegramConnection?) {
    val link = connectionOptional ?: telegramConnectionRepository.save(
      TelegramConnection(
        chatId = chatId,
        userId = null
      )
    )

    log.info("welcome telegram user ${link.id}")
    sendMessage(chatId, "Hi, to proceed connect your feedless account here ${appHost}/connect-app/${link.id}")
  }

  fun showOptionsForKnownUser(chatId: Long) {
    sendMessage(
      chatId,
      """Perfect! From now on you will receive all updates, unless a repository is muted (default). Note that telegrams applies throttling, which may result in data loss. """.trimIndent()
    )
  }

  fun sendMessage(chatId: Long, message: String) {
    log.info(message)
    val url = "https://api.telegram.org/bot${telegramProperties.token}/sendMessage?chat_id=$chatId&text=$message"
    restTemplate.getForObject(url, String::class.java)
  }

  private suspend fun toDocument(update: Update): Document {
    println(Gson().toJson(update.message))
    var doc = Document(
      url = "https://telegram.com/${update.updateId}",
      status = ReleaseStatus.released,

      // satisfy non-null
      contentHash = "",
      text = "",
      repositoryId = RepositoryId()
    )
    var supported = false

    doc = if (update.message.hasDocument()) {
      supported = true
      val attachment = getTelegramFile(update.message.document.fileId, update.message.document.mimeType)
      attachment?.let {
        doc.copy(attachments = mutableListOf(attachment))
      } ?: doc
    } else {
      doc
    }

    doc = if (update.message.hasPhoto()) {
      supported = true
      doc.copy(
        attachments =
          update.message.photo.mapNotNull { getTelegramFile(it.fileId, "image/png") }.toMutableList()
      )
    } else {
      doc
    }

    doc = if (update.message.hasText()) {
      supported = true
      doc.copy(
        title = StringUtils.abbreviate(update.message.text, "...", 50) ?: "",
        text = StringUtils.trimToEmpty(update.message.text)
      )
    } else {
      doc
    }

    if (supported) {
      sendMessage(update.message.chatId!!, "Saved")
    } else {
      sendMessage(update.message.chatId!!, "I don't know how to store this message")
    }

    return doc
  }

  private suspend fun getTelegramFile(fileId: String, mimeType: String): Attachment? {
    val getFileUrl = "https://api.telegram.org/bot${telegramProperties.token}/getFile?file_id=${fileId}"
    val getFile = restTemplate.getForObject<TelegramGetFileResponse>(URI.create(getFileUrl))
    return if (getFile.ok) {
      val response =
        restTemplate.getForObject<ByteArray>(URI.create("https://api.telegram.org/file/bot${telegramProperties.token}/${getFile.result.file_path}"))
      Attachment(
        size = getFile.result.file_size,
        documentId = DocumentId(),
        mimeType = mimeType,
        data = response
      )
    } else {
      null
    }
  }

  suspend fun findByUserIdAndAuthorizedIsTrue(ownerId: UserId): TelegramConnection? {
    return withContext(Dispatchers.IO) {
      telegramConnectionRepository.findByUserIdAndAuthorized(ownerId)
    }
  }

}


fun JsonItem.toTelegramMessage(isSaas: Boolean): String {
  return if (isSaas) {
    """$text

via $repositoryName https://feedless.org/article/${id}""".trimIndent()
  } else {
    """$text

via $repositoryName $url""".trimIndent()
  }
}

data class TelegramUpdatesResponse(
  val ok: Boolean,
  val result: List<Update>
)

data class GetFileData(
  val file_id: String,
  val file_unique_id: String,
  val file_size: Long,
  val file_path: String,
)

data class TelegramGetFileResponse(
  val ok: Boolean,
  val result: GetFileData
)
