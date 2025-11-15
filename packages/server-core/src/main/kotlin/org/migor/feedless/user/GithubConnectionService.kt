package org.migor.feedless.user

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.jpa.user.GithubConnectionDAO
import org.migor.feedless.jpa.user.GithubConnectionEntity
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.user} & ${AppLayer.service}")
class GithubConnectionService(
  private var githubConnectionDAO: GithubConnectionDAO
) {

  @Transactional
  suspend fun save(githubLink: GithubConnectionEntity) {
    return withContext(Dispatchers.IO) {
      githubConnectionDAO.save(githubLink)
    }
  }

  @Transactional(readOnly = true)
  suspend fun existsByGithubId(githubId: String): Boolean {
    return withContext(Dispatchers.IO) {
      githubConnectionDAO.existsByGithubId(githubId)
    }
  }

  @Transactional(readOnly = true)
  suspend fun existsByUserId(userId: UUID): Boolean {
    return withContext(Dispatchers.IO) {
      githubConnectionDAO.existsByUserId(userId)
    }
  }

}
