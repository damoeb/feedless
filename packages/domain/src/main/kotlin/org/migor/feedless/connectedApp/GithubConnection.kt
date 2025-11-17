package org.migor.feedless.connectedApp

import org.migor.feedless.user.UserId
import java.time.LocalDateTime

data class GithubConnection(
  val id: ConnectedAppId,
  val authorized: Boolean,
  val authorizedAt: LocalDateTime?,
  val userId: UserId?,
  val githubId: String?,
  val createdAt: LocalDateTime
)

