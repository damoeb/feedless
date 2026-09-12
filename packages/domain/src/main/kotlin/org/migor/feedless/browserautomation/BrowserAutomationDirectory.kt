package org.migor.feedless.browserautomation

import org.migor.feedless.user.UserId

interface BrowserAutomationDirectory {
  /** Number of connected agents, whoever owns them — the public `GET /api/v1/status` reports it. */
  suspend fun countConnected(): Int
  suspend fun findAllByOwnerIdOrOpenInstanceIsTrue(userId: UserId?): List<BrowserAutomation>
}
