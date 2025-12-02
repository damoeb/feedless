package org.migor.feedless.connectedApp

import org.migor.feedless.user.UserId

interface GithubConnectionRepository {

  fun existsByUserId(userId: UserId): Boolean
  fun existsByGithubId(githubId: String): Boolean
  fun save(githubConnection: GithubConnection): GithubConnection
}
