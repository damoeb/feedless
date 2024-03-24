package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsSubscription
import com.netflix.graphql.dgs.InputArgument
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.generated.types.AgentEvent
import org.migor.feedless.generated.types.RegisterAgentInput
import org.migor.feedless.service.AgentService
import org.migor.feedless.util.CryptUtil.newCorrId
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile


@DgsComponent
@Profile(AppProfiles.database)
class AgentResolver {

  private val log = LoggerFactory.getLogger(AgentResolver::class.simpleName)

  @Autowired
  lateinit var agentService: AgentService

  @DgsSubscription
  fun registerAgent(@InputArgument data: RegisterAgentInput): Publisher<AgentEvent> {
    val corrId = newCorrId()
    log.info("[$corrId] registerAgent ${data.secretKey?.email}")
    return data.secretKey?.let { agentService.registerPrerenderAgent(corrId, data) }
      ?: throw PermissionDeniedException("expected secretKey, found none ($corrId)")
  }
}
