package org.migor.feedless.feed

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.BooleanUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.fromDto
import org.migor.feedless.api.isHtml
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.feed.parser.json.JsonAttachment
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.generated.types.Attachment
import org.migor.feedless.generated.types.FeedPreview
import org.migor.feedless.generated.types.GeoPoint
import org.migor.feedless.generated.types.PreviewFeedInput
import org.migor.feedless.generated.types.RemoteNativeFeed
import org.migor.feedless.generated.types.RemoteNativeFeedInput
import org.migor.feedless.generated.types.Record
import org.migor.feedless.session.useRequestContext
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.*

@DgsComponent
@Profile("${AppProfiles.scrape} & ${AppLayer.api}")
class FeedQueryResolver {

  private val log = LoggerFactory.getLogger(FeedQueryResolver::class.simpleName)

  @Autowired
  private lateinit var feedParserService: FeedParserService

  @Throttled
  @DgsQuery
  @PreAuthorize("hasAuthority('ANONYMOUS')")
  @Transactional(propagation = Propagation.NEVER)
  suspend fun remoteNativeFeed(
    dfe: DataFetchingEnvironment,
    @InputArgument data: RemoteNativeFeedInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): RemoteNativeFeed = withContext(useRequestContext(currentCoroutineContext(), dfe)) {
    log.debug("[$corrId] remoteNativeFeed $data")
    feedParserService.parseFeedFromUrl(corrId, data.nativeFeedUrl).asRemoteNativeFeed()
  }

  @Throttled
  @DgsQuery
  @PreAuthorize("hasAuthority('ANONYMOUS')")
  @Transactional(propagation = Propagation.NEVER)
  @Deprecated("replace with scrape")
  suspend fun previewFeed(
    dfe: DataFetchingEnvironment,
    @InputArgument data: PreviewFeedInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): FeedPreview = withContext(useRequestContext(currentCoroutineContext(), dfe)) {
    log.debug("[$corrId] previewFeed $data")
    runCatching {
      feedParserService.parseFeedFromRequest(corrId, data.sources
        .filterIndexed { index, _ -> index < 5 }
        .map { it.fromDto() }, data.filters, data.tags
      )
    }.onFailure {
      log.warn("[$corrId]", it)
    }.getOrThrow()
  }
}

fun JsonFeed.asRemoteNativeFeed(): RemoteNativeFeed {

  return RemoteNativeFeed(
    description = description,
    title = title,
    feedUrl = feedUrl,
    websiteUrl = websiteUrl,
    language = language,
    publishedAt = publishedAt.toMillis(),
    tags = tags,
    nextPageUrls = links,
    expired = BooleanUtils.isTrue(expired),
    items = items.map {
      var html: String? = null
      var rawBase64: String? = null
      var rawMimeType: String? = null
      if (isHtml(it.rawMimeType)) {
        try {
          html = Base64.getDecoder().decode(it.rawBase64).toString(StandardCharsets.UTF_8)
        } catch (e: Exception) {
          html = it.rawBase64
        }
      } else {
        rawBase64 = it.rawBase64
        rawMimeType = it.rawMimeType
      }

      Record(
          id = it.url,
          tags = it.tags,
          title = it.title,
          text = it.text,
          publishedAt = it.publishedAt.toMillis(),
          startingAt = it.startingAt?.toMillis(),
          latLng = it.latLng?.let { GeoPoint(lat = it.x, lon = it.y) },
          createdAt = LocalDateTime.now().toMillis(),
          url = it.url,
          attachments = it.attachments.map { it.toDto() },
          imageUrl = it.imageUrl,
          html = html,
          rawMimeType = rawMimeType,
          rawBase64 = rawBase64,
      )
    }
  )

}

private fun JsonAttachment.toDto(): Attachment = Attachment(
  url = url,
  type = type,
  size = length,
  duration = duration,
)
