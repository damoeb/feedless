package org.migor.feedless.jpa.user

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile("${AppProfiles.user} & ${AppLayer.repository}")
interface UserDAO : JpaRepository<UserEntity, UUID> {
  fun findByEmail(name: String): UserEntity?
  fun existsByEmail(email: String): Boolean

  fun findFirstByAdminIsTrue(): UserEntity?

  fun findByAnonymousIsTrue(): UserEntity

  @Query(
    """
    SELECT u from UserEntity u
    inner join GithubConnectionEntity c on c.userId=u.id
    where c.githubId = :githubId
  """
  )
  fun findByGithubId(@Param("githubId") githubId: String): UserEntity?
}
