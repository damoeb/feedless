package org.migor.feedless.user

import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile(AppProfiles.database)
interface UserDAO : JpaRepository<UserEntity, UUID> {
  fun findByEmail(name: String): UserEntity?
  fun existsByEmail(email: String): Boolean

  fun findFirstByRootIsTrue(): UserEntity?

  fun findByAnonymousIsTrue(): UserEntity
  fun findByGithubId(githubId: String): UserEntity?
  fun existsByGithubId(githubId: String): Boolean
}
