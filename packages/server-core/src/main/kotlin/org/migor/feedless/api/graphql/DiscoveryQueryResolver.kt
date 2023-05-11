package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.apache.commons.lang3.BooleanUtils
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.Throttled
import org.migor.feedless.feed.discovery.FeedDiscoveryService
import org.migor.feedless.generated.types.DiscoverFeedsInput
import org.migor.feedless.generated.types.FeedDiscoveryDocument
import org.migor.feedless.generated.types.FeedDiscoveryResponse
import org.migor.feedless.generated.types.FilteredRemoteNativeFeedItem
import org.migor.feedless.generated.types.GenericFeeds
import org.migor.feedless.generated.types.RemoteNativeFeed
import org.migor.feedless.generated.types.RemoteNativeFeedInput
import org.migor.feedless.generated.types.WebDocument
import org.migor.feedless.service.FeedParserService
import org.migor.feedless.service.FilterService
import org.migor.feedless.util.GenericFeedUtil
import org.migor.feedless.util.GenericFeedUtil.toDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.MimeType
import org.springframework.web.bind.annotation.RequestHeader
import java.util.*

@DgsComponent
class DiscoveryQueryResolver {

  private val log = LoggerFactory.getLogger(DiscoveryQueryResolver::class.simpleName)

  @Autowired
  lateinit var filterService: FilterService

  @Autowired
  lateinit var feedParserService: FeedParserService

  @Autowired
  lateinit var feedDiscovery: FeedDiscoveryService

  @Throttled
  @DgsQuery
  @PreAuthorize("hasAuthority('READ')")
  @Transactional(propagation = Propagation.NEVER)
  suspend fun remoteNativeFeed(
    @InputArgument data: RemoteNativeFeedInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): RemoteNativeFeed? = coroutineScope {
    log.info("[$corrId] remoteNativeFeed $data")
    val feed = feedParserService.parseFeedFromUrl(corrId, data.nativeFeedUrl)
    RemoteNativeFeed.newBuilder()
      .description(feed.description)
      .title(feed.title)
//      Author=feed.author,
      .feedUrl(feed.feedUrl)
      .websiteUrl(feed.websiteUrl)
      .language(feed.language)
      .publishedAt(feed.publishedAt.time)
      .expired(BooleanUtils.isTrue(feed.expired))
      .items(feed.items.map {
        FilteredRemoteNativeFeedItem.newBuilder()
          .omitted(data.applyFilter?.let { filter -> !filterService.matches(corrId, it, filter.filter) }
            ?: false)
          .item(WebDocument.newBuilder()
            .title(it.title)
            .description(it.contentText)
            .contentText(it.contentText)
            .contentRaw(it.contentRaw)
            .contentRawMime(it.contentRawMime)
            .publishedAt(it.publishedAt.time)
            .startingAt(it.startingAt?.time)
            .createdAt(Date().time)
            .url(it.url)
            .imageUrl(it.imageUrl)
            .build()
          )
          .build()
      })
      .build()
  }

  @Throttled
  @DgsQuery
  @PreAuthorize("hasAuthority('READ')")
  @Transactional(propagation = Propagation.NEVER)
  suspend fun discoverFeeds(
    @InputArgument data: DiscoverFeedsInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): FeedDiscoveryResponse = coroutineScope {
    log.info("[$corrId] discoverFeeds $data")
    val fetchOptions = GenericFeedUtil.fromDto(data.fetchOptions)
    val discovery = feedDiscovery.discoverFeeds(corrId, fetchOptions)
    val response = discovery.results

    val document = response.document
    FeedDiscoveryResponse.newBuilder()
      .failed(response.failed)
      .errorMessage(response.errorMessage)
      .document(FeedDiscoveryDocument.newBuilder()
          .mimeType(document.mimeType)
          .htmlBody(document.mimeType?.let {
            if (MimeType.valueOf(it).subtype == "html") {
              document.body
            } else {
              null
            }
          }
          )
          .title(document.title)
          .url(document.url)
          .language(document.language)
          .description(document.description)
          .imageUrl(document.imageUrl)
          .build())
      .websiteUrl(discovery.options.harvestUrl)
      .nativeFeeds(response.nativeFeeds.map {DtoResolver.toDto(it)
      })
      .fetchOptions(toDto(data.fetchOptions))
      .genericFeeds(
        GenericFeeds.newBuilder()
          .feeds(response.genericFeedRules.map {toDto(it) })
          .build()
      )
      .build()
  }
}
