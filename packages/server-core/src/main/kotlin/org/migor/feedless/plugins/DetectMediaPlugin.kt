package org.migor.feedless.plugins

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.models.AttachmentEntity
import org.migor.feedless.data.jpa.models.SourceSubscriptionEntity
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.data.jpa.repositories.AttachmentDAO
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.harvest.HarvestAbortedException
import org.migor.feedless.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.ResourceUtils
import java.io.IOException
import java.util.*

@JsonIgnoreProperties
data class MediaItem(
  var url: String,
  var format: String? = null,
//  var size: Long? = null,
  var duration: Long? = null
)

@Service
@Profile(AppProfiles.database)
class DetectMediaPlugin: MapEntityPlugin {

  private val log = LoggerFactory.getLogger(DetectMediaPlugin::class.simpleName)

  @Autowired
  lateinit var attachmentDAO: AttachmentDAO


  override fun id(): String = FeedlessPlugins.org_feedless_detect_media.name
  override fun listed(): Boolean = true

  override fun name(): String = "Detect Audio/Video"

  override fun description(): String = "Look for attached media streams in websites and add them to a feed"

  @Transactional(propagation = Propagation.NESTED)
  override fun mapEntity(
    corrId: String,
    webDocument: WebDocumentEntity,
    subscription: SourceSubscriptionEntity,
    params: PluginExecutionParamsInput?
  ) {
    val url = webDocument.url
    if (!ResourceUtils.isUrl(url)) {
      throw HarvestAbortedException(corrId, "illegal url $url")
    }
    runCatching {
      val mediaItems = mutableListOf<MediaItem>()

      ytdl(corrId, url, mediaItems)

      webDocument.contentHtml?.let {
        Jsoup.parse(it).select("iframe[src]").toList()
          .firstOrNull { it.attr("src").startsWith("https://www.youtube.com") }?.let {
            ytdl(corrId, it.attr("src"), mediaItems)
          }
      }

      log.info("[$corrId] detected media items ${mediaItems.isNotEmpty()}")

      webDocument.attachments = attachmentDAO.saveAll(mediaItems.map { it.toEntity(webDocument.id) })

    }.onFailure {
      log.error("[$corrId] ${it.message}")
    }
  }

  private fun ytdl(corrId: String, url: String, attachments: MutableList<MediaItem>) {
    log.info("[$corrId] yt-dlp $url")
    val stdout = execCmd("yt-dlp -J $url")
    val json = JsonUtil.gson.fromJson(stdout, YoutubeDlJson::class.java)
    json?.let {

      json.url?.let {
        attachments.add(MediaItem(url = it, format = StringUtils.lowerCase(json.format), duration = json.duration))
      }
      json.formats?.let {
        attachments.addAll(it.map { MediaItem(url = it.url, format = StringUtils.lowerCase(it.format), duration = json.duration) })
      }
    }
  }

  @Throws(IOException::class)
  fun execCmd(cmd: String): String {
    val scanner = Scanner(Runtime.getRuntime().exec(cmd).inputStream)
    val builder = StringBuilder()
    while (scanner.hasNextLine()) {
      builder.append(scanner.nextLine())
    }
    return builder.toString()
  }
}

private fun MediaItem.toEntity(id: UUID): AttachmentEntity {
  val a = AttachmentEntity()
  a.webDocumentId = id
  a.remoteData = true
  a.type = this.format!!
  a.url = this.url
  return a
}

data class YoutubeDlJson(
    val duration: Long?,
    val thumbnail: String?,
    val url: String?,
    val format: String?,
    val thumbnails: List<YoutubeDlThumbnailItem>?,
    val formats: List<YoutubeDlFormatItem>?
)

data class YoutubeDlFormatItem(val url: String, val format: String)
data class YoutubeDlThumbnailItem(val url: String)
