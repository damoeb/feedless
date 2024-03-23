package org.migor.feedless.data.jpa.repositories

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.models.UserEntity
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile(AppProfiles.database)
interface UserDAO : JpaRepository<UserEntity, UUID> {
  fun findByEmail(name: String): UserEntity?
  fun existsByEmail(email: String): Boolean

  @Query(
    """
    select u from UserEntity u
    where u.root = true
  """
  )
  fun findRootUser(): UserEntity?

  fun findByAnonymousIsTrue(): UserEntity
  fun findByGithubId(githubId: String): UserEntity?
  fun existsByGithubId(githubId: String): Boolean
}
