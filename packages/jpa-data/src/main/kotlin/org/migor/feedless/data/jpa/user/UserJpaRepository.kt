package org.migor.feedless.data.jpa.user

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrNull

@Component
@Profile("${AppProfiles.user} & ${AppLayer.repository}")
class UserJpaRepository(private val userDAO: UserDAO) : UserRepository {
    override suspend fun findByEmail(name: String): User? {
        return withContext(Dispatchers.IO) {
            userDAO.findByEmail(name)?.toDomain()
        }
    }

    override suspend fun existsByEmail(email: String): Boolean {
        return withContext(Dispatchers.IO) {
            userDAO.existsByEmail(email)
        }
    }

    override suspend fun findFirstByAdminIsTrue(): User? {
        return withContext(Dispatchers.IO) {
            userDAO.findFirstByAdminIsTrue()?.toDomain()
        }
    }

    override suspend fun findByAnonymousIsTrue(): User {
        return withContext(Dispatchers.IO) {
            userDAO.findByAnonymousIsTrue().toDomain()
        }
    }

    override suspend fun findByGithubId(githubId: String): User? {
        return withContext(Dispatchers.IO) {
            userDAO.findByGithubId(githubId)?.toDomain()
        }
    }

    override suspend fun findById(id: UserId): User? {
        return withContext(Dispatchers.IO) {
            userDAO.findById(id.uuid).getOrNull()?.toDomain()
        }
    }

    override suspend fun save(user: User): User {
        return withContext(Dispatchers.IO) {
            userDAO.save(user.toEntity()).toDomain()
        }
    }

    override fun deleteAll() {
        userDAO.deleteAll()
    }
}
