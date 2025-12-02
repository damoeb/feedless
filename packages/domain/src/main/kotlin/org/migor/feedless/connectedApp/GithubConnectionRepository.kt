package org.migor.feedless.connectedApp

import org.migor.feedless.user.UserId

interface GithubConnectionRepository {

  suspend fun existsByUserId(userId: UserId): Boolean
  suspend fun existsByGithubId(githubId: String): Boolean
  suspend fun save(githubConnection: GithubConnection): GithubConnection
}
