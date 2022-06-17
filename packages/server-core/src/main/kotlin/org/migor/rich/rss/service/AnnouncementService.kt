package org.migor.rich.rss.service

import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.util.FeedUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Service
@Profile("!database")
class AnnouncementService {

  private val log = LoggerFactory.getLogger(AnnouncementService::class.simpleName)

  @Autowired
  lateinit var authService: AuthService

  @Autowired
  lateinit var propertyService: PropertyService

  val announcements: Array<Announcement> = arrayOf(
    Announcement(
      channel = AnnouncementChannel.FEED,
      title = "Share the light",
      body = """Hey buddy, thanks for using feeds. Feel free to reach out to me for improvements or bugs you encounter https://github.com/damoeb/rss-proxy/issues or send me a message on twitter https://twitter.com/damoeb""",
      trigger = FeedAge(4, 10, DurationUnit.HOURS),
    ),
    Announcement(
      channel = AnnouncementChannel.WEB,
      title = "Feed Access will expire soon",
      body = "Update your feed url here that runs an eternity",
      trigger = FeedAge(1, 10, DurationUnit.HOURS),
    )
  )

  fun byToken(corrId: String, token: AuthToken, feedUrl: String): List<RichArticle> {
    val diff = Date().time - token.issuedAt.time

    return announcements.filter {
      run {
        val age = diff.toDuration(DurationUnit.MILLISECONDS).toLong(it.trigger.unit)
        isRelevantAnnouncement(token, it) && it.trigger.minAge <= age && it.trigger.maxAge > age
      }
    }.map { toArticle(it, feedUrl) }
  }

  private fun isRelevantAnnouncement(token: AuthToken, announcement: Announcement): Boolean {
    return (token.isWeb && announcement.channel == AnnouncementChannel.WEB)
      || ((token.isAnonymous) && announcement.channel == AnnouncementChannel.FEED)
  }

  private fun toArticle(announcement: Announcement, feedUrl: String): RichArticle {
    val announcementUrl = "${propertyService.publicUrl}/?fixFeedUrl=${URLEncoder.encode(
      feedUrl,
      StandardCharsets.UTF_8
    )
    }&reason="
    val articleId = FeedUtil.toURI("announcement", announcementUrl, Date())
    return RichArticle(
      id = articleId,
      title = announcement.title,
      tags = listOf("announcement"),
      contentText = announcement.body,
      url = announcementUrl,
      author = "system",
      publishedAt = Date()
    )
  }
}

data class Announcement(
  val channel: AnnouncementChannel,
  val title: String,
  val body: String,
  val trigger: FeedAge,
  val fields: Array<Field> = emptyArray()
)

data class FeedAge(
  val minAge: Long,
  val maxAge: Long,
  val unit: DurationUnit
)

data class Field(
  val name: String,
  val placeholder: String,
  val type: FieldType
)

enum class FieldType {
  TEXT, EMAIL, NUMBER
}

enum class AnnouncementChannel {
  WEB,
  FEED
}
