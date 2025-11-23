package org.migor.feedless.session

import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecret
import org.migor.feedless.userSecret.UserSecretType
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime


@Service
@Transactional(propagation = Propagation.NEVER)
@ConditionalOnMissingBean(StatefulAuthService::class)
class StatelessAuthService : AuthService() {
    @Value("\${app.rootEmail}")
    private lateinit var rootEmail: String

    @Value("\${app.rootSecretKey}")
    private lateinit var rootSecretKey: String

    private lateinit var root: User
    private lateinit var key: UserSecret


    @PostConstruct
    fun init() {
        root = User(
            email = rootEmail,
            lastLogin = LocalDateTime.now(),
            hasAcceptedTerms = true,
        )
        key = UserSecret(
            value = rootSecretKey,
            validUntil = LocalDateTime.now().plusDays(1),
            type = UserSecretType.SecretKey,
            ownerId = root.id,
        )
    }

    override suspend fun verifyTokenSignature(token: String): Jwt? = null
    override suspend fun assertToken(request: HttpServletRequest) {

    }

    override fun isWhitelisted(request: HttpServletRequest): Boolean = true

    override suspend fun interceptToken(request: HttpServletRequest): Jwt {
        TODO("ignore")
    }

    override fun authenticateUser(email: String, secretKey: String): User {
        return if (email == rootEmail && secretKey == rootSecretKey) {
            root
        } else {
            throw AccessDeniedException("User does not exist or password invalid")
        }
    }

    override suspend fun findUserById(userId: UserId): User? {
        return if (root.id == userId.uuid) {
            root
        } else {
            null
        }
    }

    override suspend fun findBySecretKeyValue(secretKey: String, email: String): UserSecret? {
        return if (email == rootEmail && secretKey == rootSecretKey) {
            key
        } else {
            throw AccessDeniedException("User does not exist or password invalid")
        }
    }
}
