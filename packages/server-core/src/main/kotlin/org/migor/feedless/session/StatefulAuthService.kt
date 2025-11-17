package org.migor.feedless.session

import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.NotFoundException
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.user.UserDAO
import org.migor.feedless.data.jpa.user.UserEntity
import org.migor.feedless.data.jpa.user.toDomain
import org.migor.feedless.data.jpa.userSecret.UserSecretDAO
import org.migor.feedless.data.jpa.userSecret.UserSecretEntity
import org.migor.feedless.data.jpa.userSecret.toDomain
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecret
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.net.InetAddress
import java.time.LocalDateTime
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.jvm.optionals.getOrNull
import kotlin.properties.Delegates
import kotlin.time.DurationUnit
import kotlin.time.toDuration


@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.session} & ${AppLayer.repository} & ${AppLayer.service}")
class StatefulAuthService : AuthService() {
  private lateinit var whitelistedIps: List<String>
  private val log = LoggerFactory.getLogger(StatefulAuthService::class.simpleName)

  @Autowired
  private lateinit var propertyService: PropertyService

  @Autowired
  private lateinit var userDAO: UserDAO

  @Autowired
  private lateinit var userSecretDAO: UserSecretDAO

  @Value("\${auth.token.anonymous.validForDays}")
  lateinit var tokenAnonymousValidForDays: String

  @Value("\${default.auth.token.anonymous.validForDays}")
  lateinit var defaultTokenAnonymousValidForDays: String

  @Value("\${app.whitelistedHosts}")
  lateinit var whitelistedHostsParam: String

  private var tokenAnonymousValidFor: Long by Delegates.notNull()

  private val attrAuthorities = "authorities"

  @PostConstruct
  fun postConstruct() {
    tokenAnonymousValidFor = parseDuration(tokenAnonymousValidForDays, defaultTokenAnonymousValidForDays)
    log.info("tokenAnonymousValidFor=${tokenAnonymousValidFor}")

    resolveWhitelistedHosts()
  }

  override suspend fun verifyTokenSignature(token: String): Jwt {
    return NimbusJwtDecoder
      .withSecretKey(getSecretKey())
      .build()
      .decode(token)
  }

  override suspend fun interceptToken(request: HttpServletRequest): Jwt {
    return verifyTokenSignature(interceptTokenRaw(request))
  }

  @Transactional(readOnly = true)
  override fun authenticateUser(email: String, secretKey: String): User {
    log.debug("authRoot")
    val root = userDAO.findByEmail(email)?.toDomain() ?: throw NotFoundException("user not found")
    if (!root.admin) {
      throw PermissionDeniedException("account is not root")
    }
    userSecretDAO.findBySecretKeyValue(secretKey, email)
      ?: throw IllegalArgumentException("secretKey does not match")
    return root
  }

  @Transactional(readOnly = true)
  override suspend fun findUserById(userId: UserId): User? {
    return withContext(Dispatchers.IO) {
      userDAO.findById(userId.value).map { it.toDomain() }.getOrNull()
    }
  }

  @Transactional(readOnly = true)
  override suspend fun findBySecretKeyValue(secretKey: String, email: String): UserSecret? {
    return withContext(Dispatchers.IO) {
      userSecretDAO.findBySecretKeyValue(secretKey, email)?.toDomain()
    }
  }

  @Transactional
  override suspend fun updateLastUsed(id: UUID, date: LocalDateTime) {
    withContext(Dispatchers.IO) {
      userSecretDAO.updateLastUsed(id, date)
    }
  }

  @Transactional(readOnly = true)
  override suspend fun assertToken(request: HttpServletRequest) {
    if (!isWhitelisted(request)) {
      interceptToken(request)
    }
  }

  override fun isWhitelisted(request: HttpServletRequest): Boolean {
//    val isWhitelisted = whitelistedIps.contains(request.remoteHost)
//    log.info("isWhitelisted? ${request.remoteHost} -> $isWhitelisted")
    return whitelistedIps.contains(request.remoteHost)
  }

  // --

  private fun resolveWhitelistedHosts() {
    this.whitelistedIps = whitelistedHostsParam
      .trim()
      .split(" ", ",").mapNotNull {
        try {
          InetAddress.getByName(it.trim()).hostAddress
        } catch (e: Exception) {
          log.warn("Cannot resolve DNS $it: ${e.message}")
          null
        }
      }
      .plus(
        listOf(
          InetAddress.getLocalHost().hostAddress,
          InetAddress.getLoopbackAddress().hostAddress,
          "127.0.0.1",
          "0:0:0:0:0:0:0:1"
        )
      )
      .distinct()
    log.info("whitelistedIps=${whitelistedIps}")
  }

  private fun parseDuration(actual: String, fallback: String) = runCatching {
    actual.toLong().toDuration(DurationUnit.DAYS).inWholeMinutes
  }.getOrElse { fallback.toLong() }

  private suspend fun getAuthorities(jwt: Jwt): List<String> {
    return jwt.getClaim(attrAuthorities) as List<String>
  }

  private fun getSecretKey(): SecretKey {
    return SecretKeySpec(propertyService.jwtSecret.encodeToByteArray(), "HmacSHA256")
  }
}
