package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.apache.commons.lang3.BooleanUtils
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.Throttled
import org.migor.feedless.generated.types.FilteredRemoteNativeFeedItem
import org.migor.feedless.generated.types.RemoteNativeFeed
import org.migor.feedless.generated.types.RemoteNativeFeedInput
import org.migor.feedless.generated.types.WebDocument
import org.migor.feedless.service.FeedParserService
import org.migor.feedless.service.FilterService
import org.migor.feedless.service.HttpService
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
  lateinit var filterService: FilterService

  @Autowired
  lateinit var feedParserService: FeedParserService

  @Autowired
  lateinit var httpService: HttpService

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
}
