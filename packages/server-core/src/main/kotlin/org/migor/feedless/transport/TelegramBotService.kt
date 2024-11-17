package org.migor.feedless.transport

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.attachment.AttachmentEntity
import org.migor.feedless.config.SystemSettingsDAO
import org.migor.feedless.config.SystemSettingsEntity
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.message.MessageService
import org.migor.feedless.repository.InboxService
import org.migor.feedless.user.TelegramConnectionDAO
import org.migor.feedless.user.TelegramConnectionEntity
import org.migor.feedless.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
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
@Profile("${AppProfiles.telegram} & ${AppLayer.service} & ${AppProfiles.prod}")
class TelegramBotService(
  @Value("\${app.telegram.bot.token}")
  private val botToken: String,
  @Value("\${app.appHost}")
  private val appHost: String,
  private val telegramConnectionDAO: TelegramConnectionDAO,
  private val environment: Environment,
  private val messageService: MessageService,
  private val systemSettingsDAO: SystemSettingsDAO,
  private val restTemplate: RestTemplate,
  private val inboxService: InboxService,
) {

  private val log = LoggerFactory.getLogger(TelegramBotService::class.simpleName)

  private val settingName = "telegram_last_update_id"
  private lateinit var lastUpdateSettings: SystemSettingsEntity

  companion object {
    fun toTopic(chatId: Long): String {
      return "telegram:${chatId}"
    }
  }

  var lastUpdateId: Int = Int.MAX_VALUE

  @PostConstruct
  fun onInit() {
    lastUpdateSettings = systemSettingsDAO.findByName(settingName) ?: run {
      lastUpdateSettings = SystemSettingsEntity()
      lastUpdateSettings.name = settingName
      lastUpdateSettings.valueInt = Int.MAX_VALUE
      systemSettingsDAO.save(lastUpdateSettings)
    }

    lastUpdateId = lastUpdateSettings.valueInt ?: Int.MAX_VALUE
    val isSaas = environment.acceptsProfiles(Profiles.of(AppProfiles.saas))

    val chats = telegramConnectionDAO.findAllByAuthorizedIsTrue()
    log.info("Using bot token ${StringUtils.abbreviate(botToken, "...", 4)}")
    log.info("Subscribing to ${chats.size} telegram chats")

    createTelegramPublisher(subscribeToChats(chats))
      .subscribe { (chatId, message) -> sendMessage(chatId, message.toTelegramMessage(isSaas)) }
  }

  fun createTelegramPublisher(messages: Flux<Pair<Long, JsonItem>>): Flux<Pair<Long, JsonItem>> {
    return messages.onBackpressureBuffer(100)
      // telegram policy: to multiple users, the API will not allow more than 30 messages per second
      .window(Duration.ofSeconds(1))
      .flatMap { window -> window.take(30) }
  }

  fun subscribeToChats(chats: List<TelegramConnectionEntity>): Flux<Pair<Long, JsonItem>> {
    return Flux.create { sink ->
      chats.forEach {
        // seed https://core.telegram.org/bots/faq#my-bot-is-hitting-limits-how-do-i-avoid-this
        messageService.subscribe(toTopic(it.chatId))
          // telegram policy: max 20 messages per minute to the same group
          .window(Duration.ofMinutes(1)) // 1-minute windows
          .flatMap { window -> window.take(20) }
          .map { message -> Pair(it.chatId, message) }
          .subscribe(sink::next, sink::error, sink::complete)
      }
    }
  }

//  @Scheduled(fixedDelay = 4000)
  fun pollUpdates() {
    try {
      val url = "https://api.telegram.org/bot$botToken/getUpdates"
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
          lastUpdateSettings.valueInt = it.updateId
          systemSettingsDAO.save(lastUpdateSettings)
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
        val connection = telegramConnectionDAO.findByChatId(chatId)
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

  private suspend fun storeMessage(connection: TelegramConnectionEntity, update: Update) {
    inboxService.appendMessage(connection.userId!!, toDocument(update))
  }

  private fun handleCommand(connection: TelegramConnectionEntity, update: Update, chatId: Long) {
    val command = update.message.text.lowercase().trim()
    when(command) {
      "/start" -> welcomeNewUser(chatId, connection)
      else -> log.warn("Cannot handle command $command")
    }
  }

  private fun welcomeNewUser(chatId: Long, connectionOptional: TelegramConnectionEntity?) {
    val link = connectionOptional ?: run {
      val t = TelegramConnectionEntity()
      t.chatId = chatId
      telegramConnectionDAO.save(t)
    }

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
    val url = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$message"
    restTemplate.getForObject(url, String::class.java)
  }

  private suspend fun toDocument(update: Update): DocumentEntity {
    println(JsonUtil.gson.toJson(update.message))
    val doc = DocumentEntity()
    doc.url = "https://telegram.com/${update.updateId}"
    doc.status = ReleaseStatus.released

    var supported = false
    if (update.message.hasDocument()) {
      doc.title = update.message.document.fileName
      doc.text = update.message.document.fileName
      val attachment = getTelegramFile(update.message.document.fileId, update.message.document.mimeType)
      attachment?.let {
        doc.attachments = mutableListOf(attachment)
      }
      supported = true
    }
    if (update.message.hasPhoto()) {
      doc.attachments = update.message.photo.mapNotNull { getTelegramFile(it.fileId, "image/png") }.toMutableList()
      supported = true
    }

    if (update.message.hasText()) {
      doc.title = StringUtils.abbreviate(update.message.text, "...", 50) ?: ""
      doc.text = StringUtils.trimToEmpty(update.message.text)
      supported = true
    }

    if (supported) {
      sendMessage(update.message.chatId!!, "Saved")
    } else {
      sendMessage(update.message.chatId!!, "I don't know how to store this message")
    }

    return doc
  }

  private suspend fun getTelegramFile(fileId: String, mimeType: String): AttachmentEntity? {
    val getFileUrl = "https://api.telegram.org/bot$botToken/getFile?file_id=${fileId}"
    val getFile = restTemplate.getForObject<TelegramGetFileResponse>(URI.create(getFileUrl))
    return if (getFile.ok) {
      val response =
        restTemplate.getForObject<ByteArray>(URI.create("https://api.telegram.org/file/bot$botToken/${getFile.result.file_path}"))
      val a = AttachmentEntity()
      a.size = getFile.result.file_size
      a.mimeType = mimeType
      a.data = response
      a
    } else {
      null
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
