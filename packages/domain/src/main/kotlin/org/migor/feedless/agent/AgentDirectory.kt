package org.migor.feedless.agent

import org.migor.feedless.user.UserId

interface AgentDirectory {
  /** Number of connected agents, whoever owns them — the public `GET /api/v1/status` reports it. */
  suspend fun countConnected(): Int
  suspend fun findAllByOwnerIdOrOpenInstanceIsTrue(userId: UserId?): List<Agent>
}
