package org.migor.feedless.status

/** Public, so deliberately nothing that identifies an agent or a user. */
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
