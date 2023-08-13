package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.Throttled
import org.migor.feedless.feed.discovery.FeedDiscoveryService
import org.migor.feedless.generated.types.ScrapeRequestInput
import org.migor.feedless.generated.types.ScrapeResponse
import org.migor.feedless.util.GenericFeedUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader
import java.util.*

@DgsComponent
class ScrapeQueryResolver {

  private val log = LoggerFactory.getLogger(ScrapeQueryResolver::class.simpleName)

  @Autowired
  lateinit var feedDiscovery: FeedDiscoveryService

  @Throttled
  @DgsQuery
  @PreAuthorize("hasAuthority('READ')")
  @Transactional(propagation = Propagation.NEVER)
  suspend fun scrape(
    @InputArgument data: ScrapeRequestInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): ScrapeResponse = coroutineScope {
    log.info("[$corrId] scrape $data")
    val scrapeRequest = GenericFeedUtil.fromDto(data)
    feedDiscovery.discoverFeeds(corrId, scrapeRequest)
  }
}
