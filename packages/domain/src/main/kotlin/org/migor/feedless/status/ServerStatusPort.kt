package org.migor.feedless.status

/**
 * What the public `GET /api/v1/status` reports: the running version, its build, and how many
 * prerender agents are connected. Deliberately nothing that identifies an agent or a user.
 */
data class ServerStatus(
  val version: String,
  val commit: String,
  /** Build time in epoch milliseconds. */
  val buildDate: Long,
  val connectedAgents: Int,
)

interface ServerStatusPort {
  suspend fun status(): ServerStatus
}
