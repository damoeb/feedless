package org.migor.feedless.source

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.NotFoundException
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.data.jpa.models.toDto
import org.migor.feedless.data.jpa.repositories.WebDocumentDAO
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.Activity
import org.migor.feedless.generated.types.ActivityItem
import org.migor.feedless.generated.types.DeleteWebDocumentInput
import org.migor.feedless.generated.types.SourceSubscription
import org.migor.feedless.generated.types.WebDocument
import org.migor.feedless.generated.types.WebDocumentWhereInput
import org.migor.feedless.generated.types.WebDocumentsInput
import org.migor.feedless.service.FrequencyItem
import org.migor.feedless.service.WebDocumentService
import org.migor.feedless.session.SessionService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader
import java.util.*

@DgsComponent
@Profile(AppProfiles.database)
class WebDocumentResolver {

  private val log = LoggerFactory.getLogger(WebDocumentResolver::class.simpleName)

  private val pageSize = 20

  @Autowired
  lateinit var environment: Environment

  @Autowired
  lateinit var sourceSubscriptionService: SourceSubscriptionService

  @Autowired
  lateinit var sessionService: SessionService

  @Autowired
  lateinit var webDocumentService: WebDocumentService

  @Autowired
  lateinit var webDocumentDAO: WebDocumentDAO

  @Throttled
  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun webDocument(
    @InputArgument data: WebDocumentWhereInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): WebDocument = coroutineScope {
    log.info("[$corrId] webDocument $data")
    webDocumentService.findById(UUID.fromString(data.where.id))
      .orElseThrow { NotFoundException("webDocument not found") }.toDto()
  }

  @Throttled
  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun webDocuments(
    @InputArgument data: WebDocumentsInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): List<WebDocument> = coroutineScope {
    log.info("[$corrId] webDocuments $data")
    val subscriptionId = UUID.fromString(data.where.sourceSubscription.where.id)
    // authentication
    val subscription = sourceSubscriptionService.findById(corrId, subscriptionId)
    webDocumentService.findAllBySubscriptionId(subscription.id, data.cursor?.page).map { it.toDto() }
  }

  @DgsData(parentType = DgsConstants.SOURCESUBSCRIPTION.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun documentCount(dfe: DgsDataFetchingEnvironment): Long = coroutineScope {
    val source: SourceSubscription = dfe.getSource()
    webDocumentDAO.countBySubscriptionId(UUID.fromString(source.id))
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('USER')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun deleteWebDocument(
    @InputArgument data: DeleteWebDocumentInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Boolean = coroutineScope {
    webDocumentService.deleteWebDocumentById(corrId, sessionService.user(corrId), UUID.fromString(data.where.id))
    true
  }

  @DgsData(parentType = DgsConstants.SOURCESUBSCRIPTION.TYPE_NAME)
  suspend fun activity(
    dfe: DgsDataFetchingEnvironment,
  ): Activity = coroutineScope {
    val source: SourceSubscription = dfe.getSource()

    Activity.newBuilder()
      .items(webDocumentService.getWebDocumentFrequency(UUID.fromString(source.id)).map { it.toDto() })
      .build()
  }

}

private fun FrequencyItem.toDto(): ActivityItem {
  fun leftPad(num: Int): String {
    return StringUtils.leftPad("$num", 2, "0")
  }

  return ActivityItem.newBuilder()
    .index("${year}${leftPad(month)}${leftPad(day)}")
    .count(count)
    .build()
}
