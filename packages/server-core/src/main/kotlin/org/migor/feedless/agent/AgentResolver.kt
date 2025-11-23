package org.migor.feedless.agent

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.DgsSubscription
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.RegisterAgentInput
import org.migor.feedless.generated.types.SubmitAgentDataInput
import org.migor.feedless.session.injectCurrentUser
import org.migor.feedless.user.userId
import org.migor.feedless.util.toMillis
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.migor.feedless.generated.types.Agent as AgentDto
import org.migor.feedless.generated.types.AgentEvent as AgentEventDto

@DgsComponent
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.agent} & ${AppLayer.api}")
class AgentResolver(
    private val agentService: AgentService
) {

    private val log = LoggerFactory.getLogger(AgentResolver::class.simpleName)

    @DgsSubscription
    fun registerAgent(@InputArgument data: RegisterAgentInput): Publisher<AgentEventDto> {
        log.info("registerAgent ${data.secretKey.email}")
        return runBlocking {
            coroutineScope {
                data.secretKey.let { agentService.registerAgent(data) }
            }
        }
    }

    @Throttled
    @DgsMutation(field = DgsConstants.MUTATION.SubmitAgentData)
    @PreAuthorize("@capabilityService.hasCapability('agent')")
    suspend fun submitAgentData(@InputArgument data: SubmitAgentDataInput): Boolean = coroutineScope {
        log.info("[${data.corrId}] submitAgentData")
        agentService.handleScrapeResponse(data.callbackId, data.scrapeResponse)
        true
    }

    @Throttled
    @DgsQuery
    suspend fun agents(
        dfe: DataFetchingEnvironment,
    ): List<AgentDto> {
        log.debug("agents")
        return withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
            agentService.findAllByUserId(coroutineContext.userId()).map { it.toDto() }
        }
    }
}

internal fun Agent.toDto(): AgentDto {
    return AgentDto(
        ownerId = ownerId.toString(),
        name = name,
        addedAt = createdAt.toMillis(),
        version = version,
        openInstance = openInstance,
        secretKeyId = secretKeyId.toString(),
    )
}
