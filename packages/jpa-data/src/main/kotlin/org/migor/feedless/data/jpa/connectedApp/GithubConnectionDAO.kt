package org.migor.feedless.data.jpa.connectedApp

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile("${AppProfiles.user} & ${AppLayer.repository}")
interface GithubConnectionDAO : JpaRepository<GithubConnectionEntity, UUID> {

  fun existsByUserId(userId: UUID): Boolean
  fun existsByGithubId(githubId: String): Boolean
}
