package org.migor.feedless.browserautomation

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.DgsSubscription
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.throttle.Throttled
import org.migor.feedless.capability.CapabilityService
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.AgentEvent
import org.migor.feedless.generated.types.RegisterAgentInput
import org.migor.feedless.generated.types.SubmitAgentDataInput
import org.migor.feedless.user.UserId
import org.migor.feedless.util.toMillis
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.migor.feedless.generated.types.Agent as AgentDto

@DgsComponent
@Profile("${AppProfiles.browserAutomation} & ${AppLayer.api}")
class BrowserAutomationResolver(
  private val browserAutomationGateway: BrowserAutomationGateway,
  private val browserAutomationDirectory: BrowserAutomationDirectory,
  private val capabilityService: CapabilityService
) {

  private val log = LoggerFactory.getLogger(BrowserAutomationResolver::class.simpleName)

  @DgsSubscription
  fun registerAgent(@InputArgument data: RegisterAgentInput): Publisher<AgentEvent> {
    log.info("registerAgent ${data.secretKey.email}")
    return runBlocking {
      coroutineScope {
        data.secretKey.let { browserAutomationGateway.registerAgent(data) }
      }
    }
  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.SubmitAgentData)
  @PreAuthorize("@capabilityService.hasCapability('agent')")
  suspend fun submitAgentData(@InputArgument data: SubmitAgentDataInput): Boolean = coroutineScope {
    log.info("[${data.corrId}] submitAgentData")
    browserAutomationGateway.handleScrapeResponse(data.callbackId, data.scrapeResponse)
    true
  }

  @Throttled
  @DgsQuery
  suspend fun agents(
    dfe: DataFetchingEnvironment,
  ): List<AgentDto> = coroutineScope {
    log.info("agents")
    withContext(Dispatchers.IO) {
      browserAutomationDirectory.findAllByOwnerIdOrOpenInstanceIsTrue(userId())
    }.map { it.toDto() }
  }

  private fun userId(): UserId? {
    return capabilityService.getCapability(UserCapability.ID)?.let { UserCapability.resolve(it) }
  }

}

internal fun BrowserAutomation.toDto(): AgentDto {
  return AgentDto(
    ownerId = ownerId.toString(),
    name = name,
    addedAt = createdAt.toMillis(),
    version = version,
    openInstance = openInstance,
    secretKeyId = secretKeyId.toString(),
  )
}
