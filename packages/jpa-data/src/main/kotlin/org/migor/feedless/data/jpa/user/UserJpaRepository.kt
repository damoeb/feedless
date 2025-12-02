package org.migor.feedless.data.jpa.user

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Component
@Transactional(propagation = Propagation.MANDATORY)
@Profile("${AppProfiles.user} & ${AppLayer.repository}")
class UserJpaRepository(private val userDAO: UserDAO) : UserRepository {
  override fun findByEmail(name: String): User? {
    return userDAO.findByEmail(name)?.toDomain()
  }

  override fun existsByEmail(email: String): Boolean {
    return userDAO.existsByEmail(email)
  }

  override fun findFirstByAdminIsTrue(): User? {
    return userDAO.findFirstByAdminIsTrue()?.toDomain()
  }

  override fun findByAnonymousUser(): User {
    return userDAO.findByAnonymousIsTrue().toDomain()
  }

  override fun findByGithubId(githubId: String): User? {
    return userDAO.findByGithubId(githubId)?.toDomain()
  }

  override fun findById(id: UserId): User? {
    return userDAO.findById(id.uuid).getOrNull()?.toDomain()
  }

  override fun save(user: User): User {
    return userDAO.save(user.toEntity()).toDomain()
  }

  override fun deleteAll() {
    userDAO.deleteAll()
  }
}
