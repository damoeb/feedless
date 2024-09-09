package org.migor.feedless.agent

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.DgsSubscription
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.Agent
import org.migor.feedless.generated.types.AgentEvent
import org.migor.feedless.generated.types.RegisterAgentInput
import org.migor.feedless.generated.types.SubmitAgentDataInput
import org.migor.feedless.session.SessionService
import org.migor.feedless.session.useRequestContext
import org.migor.feedless.util.CryptUtil
import org.migor.feedless.util.toMillis
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestHeader

@DgsComponent
@Profile("${AppProfiles.agent} & ${AppProfiles.api}")
class AgentResolver {

  private val log = LoggerFactory.getLogger(AgentResolver::class.simpleName)

  @Autowired
  private lateinit var sessionService: SessionService

  @Autowired
  private lateinit var agentService: AgentService

  @DgsSubscription
  fun registerAgent(@InputArgument data: RegisterAgentInput): Publisher<AgentEvent> {
    val corrId = CryptUtil.newCorrId()
    log.info("[$corrId] registerAgent ${data.secretKey.email}")
    return runBlocking {
      coroutineScope {
        data.secretKey.let { agentService.registerPrerenderAgent(corrId, data) }
      }
    }
  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.SubmitAgentData)
  @PreAuthorize("hasAuthority('PROVIDE_HTTP_RESPONSE')")
  suspend fun submitAgentData(@InputArgument data: SubmitAgentDataInput): Boolean = coroutineScope {
    log.debug("[${data.corrId}] submitAgentData")
    agentService.handleScrapeResponse(data.corrId, data.callbackId, data.scrapeResponse)
    true
  }

  @Throttled
  @DgsQuery
  suspend fun agents(@RequestHeader(ApiParams.corrId) corrId: String): List<Agent> {
    log.debug("[$corrId] agents")
    return withContext(useRequestContext(currentCoroutineContext())) {
      agentService.findAllByUserId(sessionService.userId()).map { it.toDto() }
    }
  }
}

private fun AgentEntity.toDto(): Agent {
  return Agent(
    ownerId = ownerId.toString(),
    name = name,
    addedAt = createdAt.toMillis(),
    version = version,
    openInstance = openInstance,
    secretKeyId = secretKeyId.toString(),
  )
}
