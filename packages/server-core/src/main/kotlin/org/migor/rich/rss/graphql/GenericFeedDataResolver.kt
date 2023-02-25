package org.migor.rich.rss.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.rich.rss.generated.types.GenericFeed
import org.migor.rich.rss.generated.types.NativeFeed
import org.migor.rich.rss.graphql.DtoResolver.toDTO
import org.migor.rich.rss.service.FeedService
import org.migor.rich.rss.transform.WebToFeedTransformer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@DgsComponent
class GenericFeedDataResolver {

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var webToFeedTransformer: WebToFeedTransformer

  @DgsData(parentType = "GenericFeed")
  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_UNCOMMITTED)
  suspend fun nativeFeed(dfe: DgsDataFetchingEnvironment): NativeFeed? = coroutineScope {
    val feed: GenericFeed = dfe.getSource()
    feedService.findNativeById(UUID.fromString(feed.id)).map { toDTO(it) }.orElseThrow()
  }

  @DgsData(parentType = "GenericFeed")
  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_UNCOMMITTED)
  suspend fun feedUrl(dfe: DgsDataFetchingEnvironment): String = coroutineScope {
    val feed: GenericFeed = dfe.getSource()
    webToFeedTransformer.createFeedUrl(feed)
  }

  @DgsData(parentType = "GenericFeed")
  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_UNCOMMITTED)
  suspend fun hash(dfe: DgsDataFetchingEnvironment): String = coroutineScope {
    val feed: GenericFeed = dfe.getSource()
    feedService.toHash(feed.specification.selectors)
  }

}
