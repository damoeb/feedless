package org.migor.feedless.agent

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.DgsSubscription
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.generated.types.Agent
import org.migor.feedless.generated.types.AgentEvent
import org.migor.feedless.generated.types.RegisterAgentInput
import org.migor.feedless.generated.types.SubmitAgentDataInput
import org.migor.feedless.session.SessionService
import org.migor.feedless.util.CryptUtil
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestHeader
import java.util.*

@DgsComponent
@org.springframework.context.annotation.Profile(AppProfiles.database)
class AgentResolver {

  private val log = LoggerFactory.getLogger(AgentResolver::class.simpleName)

  @Autowired
  lateinit var currentUser: SessionService

  @Autowired
  lateinit var agentService: AgentService

  @DgsSubscription
  fun registerAgent(@InputArgument data: RegisterAgentInput): Publisher<AgentEvent> {
    val corrId = CryptUtil.newCorrId()
    log.info("[$corrId] registerAgent ${data.secretKey?.email}")
    return data.secretKey?.let { agentService.registerPrerenderAgent(corrId, data) }
      ?: throw PermissionDeniedException("expected secretKey, found none ($corrId)")
  }

  @Throttled
  @DgsMutation
  @PreAuthorize("hasAuthority('PROVIDE_HTTP_RESPONSE')")
  suspend fun submitAgentData(@InputArgument data: SubmitAgentDataInput): Boolean = coroutineScope {
    log.info("[${data.corrId}] submitAgentData")
    agentService.handleScrapeResponse(data.corrId, data.jobId, data.scrapeResponse)
    true
  }

  @Throttled
  @DgsQuery
  @PreAuthorize("hasAuthority('USER')")
  suspend fun agents(
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): List<Agent> = coroutineScope {
    log.info("[$corrId] agents")
    agentService.findAll(currentUser.userId()!!).map { it.toDto() }
  }
}

private fun AgentEntity.toDto(): Agent {
  return Agent.newBuilder()
    .ownerId(ownerId.toString())
    .addedAt(createdAt.time)
    .version(version)
    .openInstance(openInstance)
    .secretKeyId(secretKeyId.toString())
    .build()
}
