package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import com.netflix.graphql.dgs.context.DgsContext
import com.netflix.graphql.dgs.internal.DgsWebMvcRequestData
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.Throttled
import org.migor.feedless.api.auth.CookieProvider
import org.migor.feedless.api.auth.CurrentUser
import org.migor.feedless.data.jpa.models.toDto
import org.migor.feedless.generated.types.*
import org.migor.feedless.service.AgentService
import org.migor.feedless.service.PlanService
import org.migor.feedless.service.PropertyService
import org.migor.feedless.service.SourceSubscriptionService
import org.migor.feedless.service.WebDocumentService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.context.request.ServletWebRequest
import java.util.*

@DgsComponent
@org.springframework.context.annotation.Profile(AppProfiles.database)
class QueryResolver {

  private val log = LoggerFactory.getLogger(QueryResolver::class.simpleName)
  private val pageSize = 20

  @Autowired
  lateinit var currentUser: CurrentUser

  @Autowired
  lateinit var agentService: AgentService

  @Autowired
  lateinit var environment: Environment

  @Autowired
  lateinit var cookieProvider: CookieProvider

  @Autowired
  lateinit var sourceSubscriptionService: SourceSubscriptionService

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var webDocumentService: WebDocumentService

  @Autowired
  lateinit var planService: PlanService

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
    sourceSubscriptionService.findAll(offset, pageSize)
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
    sourceSubscriptionService.findById(data.where.id).toDto()
  }

  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun profile(dfe: DataFetchingEnvironment): Profile = coroutineScope {
    unsetSessionCookie(dfe)
    val defaultProfile = Profile.newBuilder()
      .preferReader(true)
      .preferFulltext(true)
      .isLoggedIn(false)
      .isAnonymous(true)
      .dateFormat(propertyService.dateFormat)
      .timeFormat(propertyService.timeFormat)
      .minimalFeatureState(FeatureState.experimental)
      .build()

    if (currentUser.isUser()) {
      runCatching {
        val user = currentUser.user()
        Profile.newBuilder()
          .preferReader(true)
          .preferFulltext(true)
          .dateFormat(propertyService.dateFormat)
          .timeFormat(propertyService.timeFormat)
          .isLoggedIn(true)
          .isAnonymous(false)
          .userId(user.id.toString())
          .minimalFeatureState(FeatureState.experimental)
          .build()
      }.getOrDefault(defaultProfile)
    } else {
      defaultProfile

    }
  }

  private fun unsetSessionCookie(dfe: DataFetchingEnvironment) {
    val cookie = cookieProvider.createExpiredSessionCookie("JSESSION")
    ((DgsContext.getRequestData(dfe)!! as DgsWebMvcRequestData).webRequest!! as ServletWebRequest).response!!.addCookie(cookie)
  }

  @Throttled
  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun agents(
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): List<Agent> = coroutineScope {
    log.info("[$corrId] agents")
    agentService.findAll()
  }

  @Throttled
  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun webDocument(
    @InputArgument data: WebDocumentWhereInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): WebDocument = coroutineScope {
    log.info("[$corrId] webDocument $data")
    webDocumentService.findById(UUID.fromString(data.where.id))
      .orElseThrow { IllegalArgumentException("webDocument not found")}.toDto()
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
    webDocumentService.findBySubscriptionId(subscriptionId, data.cursor?.page).map { it.toDto() }
  }

  private fun handlePageNumber(page: Int?): Int =
    page ?: 0

  private fun handlePageSize(pageSize: Int?): Int =
    (pageSize ?: this.pageSize).coerceAtLeast(1).coerceAtMost(this.pageSize)

  @Throttled
  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun plans(@RequestHeader(ApiParams.corrId) corrId: String,): List<Plan> = coroutineScope {
    log.info("[$corrId] plans")
    planService.findAll().map { it.toDto() }
  }
}
