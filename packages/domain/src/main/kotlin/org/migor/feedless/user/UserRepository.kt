package org.migor.feedless.user

interface UserRepository {
  fun findByEmail(name: String): User?
  fun existsByEmail(email: String): Boolean

  fun findFirstByAdminIsTrue(): User?

  fun findByAnonymousUser(): User

  fun findByGithubId(githubId: String): User?
  fun findById(id: UserId): User?
  fun save(user: User): User
  fun deleteAll()
}
