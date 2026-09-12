package org.migor.feedless.agent

import org.migor.feedless.generated.types.AgentEvent
import org.migor.feedless.generated.types.RegisterAgentInput
import org.migor.feedless.generated.types.ScrapeResponseInput
import org.reactivestreams.Publisher

// Lives here, not in domain: the agent protocol is made of generated GraphQL types.
interface AgentGateway {
  suspend fun registerAgent(data: RegisterAgentInput): Publisher<AgentEvent>
  suspend fun handleScrapeResponse(harvestJobId: String, scrapeResponse: ScrapeResponseInput)
}
