package org.migor.feedless.feed

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.apache.commons.lang3.BooleanUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.fromDto
import org.migor.feedless.api.isHtml
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.feed.parser.json.JsonAttachment
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.generated.types.Enclosure
import org.migor.feedless.generated.types.GeoPoint
import org.migor.feedless.generated.types.PreviewFeedInput
import org.migor.feedless.generated.types.RemoteNativeFeed
import org.migor.feedless.generated.types.RemoteNativeFeedInput
import org.migor.feedless.generated.types.WebDocument
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader
import java.nio.charset.StandardCharsets
import java.util.*

@DgsComponent
@Profile("${AppProfiles.scrape} & ${AppProfiles.api}")
class FeedQueryResolver {

  private val log = LoggerFactory.getLogger(FeedQueryResolver::class.simpleName)

  @Autowired
  private lateinit var feedParserService: FeedParserService

  @Throttled
  @DgsQuery
  @PreAuthorize("hasAuthority('ANONYMOUS')")
  @Transactional(propagation = Propagation.NEVER)
  suspend fun remoteNativeFeed(
    @InputArgument data: RemoteNativeFeedInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): RemoteNativeFeed = coroutineScope {
    log.info("[$corrId] remoteNativeFeed $data")
    feedParserService.parseFeedFromUrl(corrId, data.nativeFeedUrl).asRemoteNativeFeed()
  }

  @Throttled
  @DgsQuery
  @PreAuthorize("hasAuthority('ANONYMOUS')")
  @Transactional(propagation = Propagation.NEVER)
  suspend fun previewFeed(
    @InputArgument data: PreviewFeedInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): RemoteNativeFeed = coroutineScope {
    log.info("[$corrId] previewFeed $data")
    feedParserService.parseFeedFromRequest(corrId, data.sources
      .filterIndexed { index, _ -> index < 5 }
      .map { it.fromDto() }, data.filters, data.tags
    )
  }
}

fun JsonFeed.asRemoteNativeFeed(): RemoteNativeFeed {

  return RemoteNativeFeed(
    description = description,
    title = title,
    feedUrl = feedUrl,
    websiteUrl = websiteUrl,
    language = language,
    publishedAt = publishedAt.time,
    tags = tags,
    nextPageUrls = links,
    expired = BooleanUtils.isTrue(expired),
    items = items.map {
      var contentHtml: String? = null
      var contentRawBase64: String? = null
      var contentRawMime: String? = null
      if (isHtml(it.contentRawMime)) {
        try {
          contentHtml = Base64.getDecoder().decode(it.contentRawBase64).toString(StandardCharsets.UTF_8)
        } catch (e: Exception) {
          contentHtml = it.contentRawBase64
        }
      } else {
        contentRawBase64 = it.contentRawBase64
        contentRawMime = it.contentRawMime
      }

      WebDocument(
        id = it.url,
        tags = it.tags,
        contentTitle = it.title,
        contentText = it.contentText,
        publishedAt = it.publishedAt.time,
        startingAt = it.startingAt?.time,
        localized = it.latLng?.let { GeoPoint(lat=it.x, lon=it.y) },
        createdAt = Date().time,
        url = it.url,
        enclosures = it.attachments.map { it.toEnclosure() },
        imageUrl = it.imageUrl,
        contentHtml = contentHtml,
        contentRawMime = contentRawMime,
        contentRawBase64 = contentRawBase64
      )
    }
  )

}

private fun JsonAttachment.toEnclosure(): Enclosure = Enclosure(
  url = url,
  type = type,
  size = length,
  duration = duration,
)
