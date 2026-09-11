package org.migor.feedless.status

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.agent.AgentRegistry
import org.migor.feedless.license.parseBuildTimestamp
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

/**
 * Backs the public `GET /api/v1/status`. Version, commit and build date come from the same
 * properties GraphQL `serverSettings` reads (`app.version`, `APP_GIT_COMMIT`, `APP_BUILD_TIMESTAMP`).
 *
 * The agent registry only exists with the agent profile; without it no agent can connect, so the
 * count is 0 rather than the status endpoint requiring that profile.
 */
@Service
@Profile("${AppProfiles.properties} & ${AppLayer.service}")
class ServerStatusService(
  @Value("\${app.version}") private val version: String,
  @Value("\${APP_GIT_COMMIT:unknown}") private val commit: String,
  @Value("\${APP_BUILD_TIMESTAMP:}") private val buildTimestamp: String,
  private val agentRegistry: ObjectProvider<AgentRegistry>,
) : ServerStatusPort {

  override suspend fun status(): ServerStatus {
    return ServerStatus(
      version = version,
      commit = commit,
      buildDate = parseBuildTimestamp(buildTimestamp),
      connectedAgents = agentRegistry.ifAvailable?.countConnected() ?: 0,
    )
  }
}
