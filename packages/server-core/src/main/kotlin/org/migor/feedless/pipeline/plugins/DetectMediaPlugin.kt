package org.migor.feedless.pipeline.plugins

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.migor.feedless.AppProfiles
import org.migor.feedless.HarvestAbortedException
import org.migor.feedless.attachment.AttachmentDAO
import org.migor.feedless.attachment.AttachmentEntity
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.pipeline.MapEntityPlugin
import org.migor.feedless.repository.RepositoryEntity
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
class DetectMediaPlugin : MapEntityPlugin {

  private val log = LoggerFactory.getLogger(DetectMediaPlugin::class.simpleName)

  @Autowired
  lateinit var attachmentDAO: AttachmentDAO


  override fun id(): String = FeedlessPlugins.org_feedless_detect_media.name
  override fun listed(): Boolean = true

  override fun name(): String = "Detect Audio/Video"

  @Transactional(propagation = Propagation.NESTED)
  override fun mapEntity(
    corrId: String,
    document: DocumentEntity,
    repository: RepositoryEntity,
    params: PluginExecutionParamsInput
  ): DocumentEntity {
    val url = document.url
    log.debug("[$corrId] mapEntity $url")
    if (!ResourceUtils.isUrl(url)) {
      throw HarvestAbortedException(corrId, "illegal url $url")
    }
    runCatching {
      val mediaItems = mutableListOf<MediaItem>()

      ytdl(corrId, url, mediaItems)

      document.contentHtml?.let {
        Jsoup.parse(it).select("iframe[src]").toList()
          .firstOrNull { it.attr("src").startsWith("https://www.youtube.com") }?.let {
            ytdl(corrId, it.attr("src"), mediaItems)
          }
      }

      log.info("[$corrId] detected media items ${mediaItems.isNotEmpty()}")

      document.attachments = attachmentDAO.saveAll(mediaItems.map { it.toEntity(document.id) })

    }.onFailure {
      log.error("[$corrId] ${it.message}")
    }
    return document
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
        attachments.addAll(it.map {
          MediaItem(
            url = it.url,
            format = StringUtils.lowerCase(it.format),
            duration = json.duration
          )
        })
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
  a.documentId = id
  a.hasData = true
  a.contentType = this.format!!
  a.remoteDataUrl = this.url
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
