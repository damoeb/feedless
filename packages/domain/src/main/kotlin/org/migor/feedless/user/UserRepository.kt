package org.migor.feedless.user

interface UserRepository {
    suspend fun findByEmail(name: String): User?
    suspend fun existsByEmail(email: String): Boolean

    suspend fun findFirstByAdminIsTrue(): User?

    suspend fun findByAnonymousIsTrue(): User

    suspend fun findByGithubId(githubId: String): User?
    suspend fun findById(id: UserId): User?
    suspend fun save(user: User): User
    fun deleteAll()
}