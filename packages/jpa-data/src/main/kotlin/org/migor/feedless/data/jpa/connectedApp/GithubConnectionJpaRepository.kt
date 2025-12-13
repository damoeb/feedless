package org.migor.feedless.data.jpa.connectedApp

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.connectedApp.GithubConnection
import org.migor.feedless.connectedApp.GithubConnectionRepository
import org.migor.feedless.user.UserId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("${AppProfiles.user} & ${AppLayer.repository}")
class GithubConnectionJpaRepository(private val githubConnectionDAO: GithubConnectionDAO) : GithubConnectionRepository {
  override fun existsByUserId(userId: UserId): Boolean {
    return githubConnectionDAO.existsByUserId(userId.uuid)
  }

  override fun existsByGithubId(githubId: String): Boolean {
    return githubConnectionDAO.existsByGithubId(githubId)
  }

  override fun save(githubConnection: GithubConnection): GithubConnection {
    return githubConnectionDAO.save(githubConnection.toEntity()).toDomain()
  }


}
