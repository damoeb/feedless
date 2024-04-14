package org.migor.feedless.source

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.models.toDto
import org.migor.feedless.data.jpa.repositories.ScrapeSourceDAO
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.ScrapeRequest
import org.migor.feedless.generated.types.SourceSubscription
import org.migor.feedless.generated.types.SourceSubscriptionUniqueWhereInput
import org.migor.feedless.generated.types.SourceSubscriptionUpdateInput
import org.migor.feedless.generated.types.SourceSubscriptionWhereInput
import org.migor.feedless.generated.types.SourceSubscriptionsCreateInput
import org.migor.feedless.generated.types.SourceSubscriptionsInput
import org.migor.feedless.harvest.toScrapeRequest
import org.migor.feedless.session.SessionService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader
import java.util.*

@DgsComponent
@Profile(AppProfiles.database)
class SourceResolver {

  private val log = LoggerFactory.getLogger(SourceResolver::class.simpleName)

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var sourceSubscriptionService: SourceSubscriptionService

  @Autowired
  lateinit var sessionService: SessionService

  @Autowired
  lateinit var scrapeRequestDAO: ScrapeSourceDAO


  @Throttled
  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun sourceSubscriptions(
    @InputArgument data: SourceSubscriptionsInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): List<SourceSubscription> = coroutineScope {
    log.info("[$corrId] sourceSubscriptions $data")
    val pageNumber = handlePageNumber(data.cursor.page)
    val pageSize = handlePageSize(data.cursor.pageSize)
    val offset = pageNumber * pageSize
    sourceSubscriptionService.findAll(offset, pageSize, sessionService.userId())
      .map { it.toDto() }
  }

  @Throttled
  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun sourceSubscription(
    @InputArgument data: SourceSubscriptionWhereInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): SourceSubscription = coroutineScope {
    log.info("[$corrId] sourceSubscription $data")
    sourceSubscriptionService.findById(corrId, UUID.fromString(data.where.id)).toDto()
  }

  @Throttled
  @DgsMutation
  @PreAuthorize("hasAuthority('ANONYMOUS')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun createSourceSubscriptions(
    @InputArgument("data") data: SourceSubscriptionsCreateInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): List<SourceSubscription> = coroutineScope {
    log.info("[$corrId] createSourceSubscriptions $data")
    sourceSubscriptionService.create(corrId, data)
  }

  @Throttled
  @DgsMutation
  @PreAuthorize("hasAuthority('USER')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun updateSourceSubscription(
    @InputArgument("data") data: SourceSubscriptionUpdateInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): SourceSubscription = coroutineScope {
    log.info("[$corrId] updateSourceSubscription $data")
    sourceSubscriptionService.update(corrId, UUID.fromString(data.where.id), data.data).toDto()
  }

  @Throttled
  @DgsMutation
  @PreAuthorize("hasAuthority('USER')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun deleteSourceSubscription(
    @InputArgument("data") data: SourceSubscriptionUniqueWhereInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Boolean = coroutineScope {
    log.info("[$corrId] deleteSourceSubscription $data")
    sourceSubscriptionService.delete(corrId, UUID.fromString(data.id))
    true
  }


  @DgsData(parentType = DgsConstants.SOURCESUBSCRIPTION.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun sources(dfe: DgsDataFetchingEnvironment): List<ScrapeRequest> = coroutineScope {
    val source: SourceSubscription = dfe.getSource()
    scrapeRequestDAO.findAllBySubscriptionId(UUID.fromString(source.id)).map { scrapeSource ->
      run {
        val scrapeRequest = scrapeSource.toScrapeRequest()
        scrapeRequest.corrId = null
        scrapeRequest
      }
    }
  }

}

private fun handlePageNumber(page: Int?): Int =
  page ?: 0

private fun handlePageSize(pageSize: Int?): Int =
  (pageSize ?: PropertyService.maxPageSize).coerceAtLeast(1).coerceAtMost(PropertyService.maxPageSize)
