package org.migor.feedless.user

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.connectedApp.GithubConnection
import org.migor.feedless.connectedApp.GithubConnectionRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.user} & ${AppLayer.service}")
class GithubConnectionService(
  private var githubConnectionRepository: GithubConnectionRepository
) {

  @Transactional
  suspend fun save(githubLink: GithubConnection): GithubConnection {
    return githubConnectionRepository.save(githubLink)
  }

  @Transactional(readOnly = true)
  suspend fun existsByGithubId(githubId: String): Boolean {
    return githubConnectionRepository.existsByGithubId(githubId)
  }

  @Transactional(readOnly = true)
  suspend fun existsByUserId(userId: UserId): Boolean {
    return githubConnectionRepository.existsByUserId(userId)
  }

}
