package org.migor.feedless.status

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.agent.AgentDirectory
import org.migor.feedless.license.parseBuildTimestamp
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

/** The agent count is 0 without the agent profile, rather than the endpoint requiring it. */
@Service
@Profile("${AppProfiles.properties} & ${AppLayer.service}")
class ServerStatusService(
  @Value("\${app.version}") private val version: String,
  @Value("\${APP_GIT_COMMIT:unknown}") private val commit: String,
  @Value("\${APP_BUILD_TIMESTAMP:}") buildTimestamp: String,
  private val agentDirectory: ObjectProvider<AgentDirectory>,
) {

  private val log = LoggerFactory.getLogger(ServerStatusService::class.simpleName)

  // Parsed once: a bad APP_BUILD_TIMESTAMP reports 0 instead of turning a health check into a 500.
  private val buildDate: Long = try {
    parseBuildTimestamp(buildTimestamp)
  } catch (e: IllegalArgumentException) {
    log.warn("[boot] ${e.message}; GET /api/v1/status reports build.date 0")
    0
  }

  suspend fun status(): ServerStatus {
    return ServerStatus(
      version = version,
      commit = commit,
      buildDate = buildDate,
      connectedAgents = agentDirectory.ifAvailable?.countConnected() ?: 0,
    )
  }
}
