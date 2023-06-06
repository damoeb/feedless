package org.migor.feedless.trigger.plugins

import com.vladmihalcea.hibernate.util.StringUtils
import org.jsoup.Jsoup
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.models.FeatureState
import org.migor.feedless.data.jpa.models.MediaItem
import org.migor.feedless.data.jpa.models.MediaThumbnail
import org.migor.feedless.data.jpa.models.WebDocumentAttachments
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.data.jpa.repositories.WebDocumentDAO
import org.migor.feedless.harvest.HarvestAbortedException
import org.migor.feedless.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.util.ResourceUtils
import java.io.IOException
import java.net.URL
import java.util.*

@Service
@Profile(AppProfiles.database)
class DetectMediaPlugin : WebDocumentPlugin {

  private val log = LoggerFactory.getLogger(DetectMediaPlugin::class.simpleName)

  @Autowired
  lateinit var webDocumentDAO: WebDocumentDAO

  override fun id(): String = "detectMedia"
  override fun description(): String = "Look for attached media streams in websites and add them to a feed"
  override fun executionPhase(): PluginPhase = PluginPhase.harvest
  override fun state(): FeatureState = FeatureState.stable
  override fun enabled(): Boolean = true
  override fun configurableByUser(): Boolean = true
  override fun configurableInUserProfileOnly(): Boolean  = false

  override fun processWebDocument(corrId: String, webDocument: WebDocumentEntity) {
    val url = webDocument.url
    if (!ResourceUtils.isUrl(url)) {
      throw HarvestAbortedException("illegal url $url")
    }
    runCatching {
      val thumbnails = mutableListOf<MediaThumbnail>()
      val mediaItems = mutableListOf<MediaItem>()

      ytdl(corrId, url, thumbnails, mediaItems)

      webDocument.contentHtml()?.let {
        Jsoup.parse(it).select("iframe[src]").toList()
          .firstOrNull { it.attr("src").startsWith("https://www.youtube.com") }?.let {
            ytdl(corrId, it.attr("src"), thumbnails, mediaItems)
          }
      }

      log.info("[$corrId] detected media items ${mediaItems.isNotEmpty()}")

      webDocument.attachments?.let {
        mediaItems.addAll(it.media)
      }

      val attachments = WebDocumentAttachments(
        thumbnails = thumbnails.distinctBy { ignoreParams(it.url) },
        media = mediaItems.distinctBy { ignoreParams(it.url) },
      )

      webDocument.attachments = attachments
      webDocumentDAO.save(webDocument)

    }.onFailure {
      log.error("[$corrId] ${it.message}")
    }
  }

  private fun ytdl(corrId: String, url: String, thumbnails: MutableList<MediaThumbnail>, mediaItems: MutableList<MediaItem>) {
    log.info("[$corrId] yt-dlp $url")
    val stdout = execCmd("yt-dlp -J $url")
    val json = JsonUtil.gson.fromJson(stdout, YoutubeDlJson::class.java)
    json?.let {
      json.thumbnail?.let {
        thumbnails.add(MediaThumbnail(it))
      }
      json.thumbnails?.let {
        thumbnails.addAll(it.map { MediaThumbnail(it.url) })
      }

      json.url?.let {
        mediaItems.add(MediaItem(url = it, format = StringUtils.toLowercase(json.format), duration = json.duration))
      }
      json.formats?.let {
        mediaItems.addAll(it.map { MediaItem(url = it.url, format = StringUtils.toLowercase(it.format), duration = json.duration) })
      }
    }
  }

  private fun ignoreParams(url: String): String {
    val u = URL(url)
    return "${u.host}/${u.path}"
  }

  @Throws(IOException::class)
  fun execCmd(cmd: String): String {
    val s = Scanner(Runtime.getRuntime().exec(cmd).inputStream)
    val builder = StringBuilder()
    while (s.hasNextLine()) {
      builder.append(s.nextLine())
    }
    return builder.toString()
  }
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
