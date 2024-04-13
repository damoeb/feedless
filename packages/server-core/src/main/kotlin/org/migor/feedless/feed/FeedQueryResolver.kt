package org.migor.feedless.feed

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.apache.commons.lang3.BooleanUtils
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.dto.RichFeed
import org.migor.feedless.api.fromDto
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.generated.types.PreviewFeedInput
import org.migor.feedless.generated.types.RemoteNativeFeed
import org.migor.feedless.generated.types.RemoteNativeFeedInput
import org.migor.feedless.generated.types.WebDocument
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader
import java.util.*

@DgsComponent
class FeedQueryResolver {

  private val log = LoggerFactory.getLogger(FeedQueryResolver::class.simpleName)

  @Autowired
  lateinit var feedParserService: FeedParserService

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
    feedParserService.parseFeedFromRequest(corrId, data.requests.map { it.fromDto() }, data.filters)
  }
}

fun RichFeed.asRemoteNativeFeed(): RemoteNativeFeed {
  return RemoteNativeFeed.newBuilder()
    .description(description)
    .title(title)
    .feedUrl(feedUrl)
    .websiteUrl(websiteUrl)
    .language(language)
    .publishedAt(publishedAt.time)
    .expired(BooleanUtils.isTrue(expired))
    .items(items.map {
      WebDocument.newBuilder()
        .id(it.url)
        .contentTitle(it.title)
        .contentText(it.contentText)
        .contentRawBase64(it.contentRawBase64)
        .contentRawMime(it.contentRawMime)
        .publishedAt(it.publishedAt.time)
        .startingAt(it.startingAt?.time)
        .createdAt(Date().time)
        .url(it.url)
        .imageUrl(it.imageUrl)
        .build()
    })
    .build()

}
