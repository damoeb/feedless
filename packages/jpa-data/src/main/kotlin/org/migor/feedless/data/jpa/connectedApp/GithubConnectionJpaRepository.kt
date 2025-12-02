package org.migor.feedless.data.jpa.connectedApp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
  override suspend fun existsByUserId(userId: UserId): Boolean {
    return withContext(Dispatchers.IO) {
      githubConnectionDAO.existsByUserId(userId.uuid)
    }
  }

  override suspend fun existsByGithubId(githubId: String): Boolean {
    return withContext(Dispatchers.IO) {
      githubConnectionDAO.existsByGithubId(githubId)
    }
  }

  override suspend fun save(githubConnection: GithubConnection): GithubConnection {
    return withContext(Dispatchers.IO) {
      githubConnectionDAO.save(githubConnection.toEntity()).toDomain()
    }
  }


}
