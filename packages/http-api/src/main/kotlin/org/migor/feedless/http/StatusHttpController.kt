package org.migor.feedless.http

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.http.api.StatusApi
import org.migor.feedless.http.api.model.ServerAgents
import org.migor.feedless.http.api.model.ServerBuild
import org.migor.feedless.http.api.model.ServerStatus
import org.migor.feedless.status.ServerStatusPort
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** The one public /api/v1 operation: a missing, expired or invalid token all answer the same 200. */
@RestController
@RequestMapping("/api/v1")
@Profile("${AppProfiles.properties} & ${AppLayer.api}")
class StatusHttpController(
  private val serverStatus: ServerStatusPort,
) : StatusApi {

  companion object {
    const val PUBLIC_STATUS_PATH = "/api/v1/status"
  }

  override suspend fun getStatus(): ResponseEntity<ServerStatus> {
    val status = serverStatus.status()
    return ResponseEntity.ok(
      ServerStatus(
        version = status.version,
        build = ServerBuild(commit = status.commit, date = status.buildDate),
        agents = ServerAgents(connected = status.connectedAgents),
      ),
    )
  }
}
