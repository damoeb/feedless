package org.migor.rich.rss.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.rich.rss.generated.GenericFeedDto
import org.migor.rich.rss.generated.NativeFeedDto
import org.migor.rich.rss.graphql.DtoResolver.toDTO
import org.migor.rich.rss.service.GenericFeedService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@DgsComponent
class NativeFeedDataResolver {

  @Autowired
  lateinit var genericFeedService: GenericFeedService

  @DgsData(parentType = "NativeFeed")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun genericFeed(dfe: DgsDataFetchingEnvironment): GenericFeedDto? = coroutineScope {
    val feed: NativeFeedDto = dfe.getSource()
    genericFeedService.findByNativeFeedId(UUID.fromString(feed.id)).map { toDTO(it) }.orElse(null)
  }

}
